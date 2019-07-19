package com.github.andremoniy.jetty.httpclient.sslissue;

import com.github.andremoniy.jetty.httpclient.sslissue.api.SimpleService;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class Bootstrap {

    private final static Logger LOG = Log.getLogger(Bootstrap.class);
    private static final int SERVER_PORT = 8080;
    private static final String BASE_URL = "https://localhost:" + SERVER_PORT + "/";

    public static void main(String[] args) throws Exception {

        final Server server = startServer();

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
        server.stop();
    }

    private static Server startServer() throws Exception {
        final Server server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server, getConnectionFactories());
        serverConnector.setPort(Bootstrap.SERVER_PORT);
        server.addConnector(serverConnector);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        final ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", SimpleService.class.getCanonicalName());

        server.setHandler(context);

        server.start();
        return server;
    }

    private static ConnectionFactory[] getConnectionFactories() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        final HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        final SslContextFactory sslContextFactory = new SslContextFactory.Server();

        final java.io.File tempKeystore = java.io.File.createTempFile("keystore", "jks");
        try (InputStream inputStream = Bootstrap.class.getClassLoader().getResourceAsStream("keystore.jks");
             FileOutputStream fileOutputStream = new FileOutputStream(tempKeystore)) {
            fileOutputStream.write(inputStream.readAllBytes());
        }

        sslContextFactory.setKeyStore(KeyStore.getInstance(tempKeystore, "123456".toCharArray()));
        sslContextFactory.setKeyManagerPassword("123456");

        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");

        return new ConnectionFactory[]{
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig)
        };
    }


}
