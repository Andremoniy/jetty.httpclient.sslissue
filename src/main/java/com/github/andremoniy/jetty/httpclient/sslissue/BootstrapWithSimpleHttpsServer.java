package com.github.andremoniy.jetty.httpclient.sslissue;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class BootstrapWithSimpleHttpsServer {

    private final static Logger LOG = Log.getLogger(BootstrapWithSimpleHttpsServer.class);
    private static final int SERVER_PORT = 8080;
    private static final String BASE_URL = "https://localhost:" + SERVER_PORT + "/";

    public static void main(String[] args) throws Exception {

        final HttpsServer server = startServer();

        final HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
        httpClient.start();

        final CountDownLatch errorCountDownLatch = new CountDownLatch(1);

        final Runnable get = () -> {
            do {
                try {
                    httpClient.GET(BASE_URL + "get");
                } catch (Exception e) {
                    errorCountDownLatch.countDown();
                    LOG.warn(e.getMessage(), e);
                    throw new IllegalStateException(e);
                }
            } while (true);
        };
        final Runnable post = () -> {
            do {
                try {
                    httpClient.POST(BASE_URL + "post").send();
                } catch (Exception e) {
                    errorCountDownLatch.countDown();
                    LOG.warn(e.getMessage(), e);
                    throw new IllegalStateException(e);
                }
            } while (true);
        };

        final Set<Thread> threadSet = new HashSet<>();
        final int numberOfThreadsPairs = Runtime.getRuntime().availableProcessors() / 2;
        LOG.info("Running {} threads", numberOfThreadsPairs * 2);
        for (int i = 0; i < numberOfThreadsPairs; i++) {
            final Thread getThread = new Thread(get);
            getThread.start();
            threadSet.add(getThread);
            final Thread postThread = new Thread(post);
            postThread.start();
            threadSet.add(postThread);
        }

        errorCountDownLatch.await();
        threadSet.forEach(Thread::interrupt);
        server.stop(0);
    }

    private static HttpsServer startServer() throws Exception {

        final HttpsServer server = HttpsServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/get", (exchange -> {
            String respText = "Hello!";
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.createContext("/post", (exchange -> {
            String respText = "Hello!";
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor

        final SSLContext ssl = SSLContext.getInstance("TLS");

        final KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        final KeyStore store = KeyStore.getInstance("JKS");

        final char[] keystorePassword = "123456".toCharArray();
        store.load(BootstrapWithSimpleHttpsServer.class.getClassLoader().getResourceAsStream("keystore.jks"), keystorePassword);

        keyFactory.init(store, keystorePassword);

        final TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(store);

        ssl.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom());

        server.setHttpsConfigurator(new HttpsConfigurator(ssl));
        server.start();

        return server;
    }

}
