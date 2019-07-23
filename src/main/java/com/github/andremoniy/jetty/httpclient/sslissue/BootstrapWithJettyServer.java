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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BootstrapWithJettyServer {

    private final static Logger LOG = Log.getLogger(BootstrapWithJettyServer.class);
    private static final int SERVER_PORT = 50734;
    private static final String BASE_URL = "https://localhost:" + SERVER_PORT + "/";
    private static final String KEYROOL_PRWD = "123456";

    public static void main(String[] args) throws Exception {

        startServer();

        final HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
        httpClient.start();

        final int numberOfThreadsPairs = Runtime.getRuntime().availableProcessors() / 2;
        final int numberOfThreads = numberOfThreadsPairs * 2;
        LOG.info("Running {} threads", numberOfThreads);

        final AtomicReference<CountDownLatch> syncStart = new AtomicReference<>(new CountDownLatch(1));
        final AtomicReference<CountDownLatch> syncEnd = new AtomicReference<>(new CountDownLatch(1));
        final AtomicReference<CountDownLatch> syncProceed = new AtomicReference<>(new CountDownLatch(numberOfThreads));
        final CountDownLatch errorCountDownLatch = new CountDownLatch(1);

        final AtomicInteger iterationCounter = new AtomicInteger();

        final Runnable get = () -> {
            do {
                try {
                    syncStart.get().await();
                    httpClient.GET(BASE_URL + "get");
                    syncProceed.get().countDown();
                    syncEnd.get().await();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ie);
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
                    syncStart.get().await();
                    httpClient.POST(BASE_URL + "post").send();
                    syncProceed.get().countDown();
                    syncEnd.get().await();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ie);
                } catch (Exception e) {
                    errorCountDownLatch.countDown();
                    LOG.warn(e.getMessage(), e);
                    throw new IllegalStateException(e);
                }
            } while (true);
        };

        for (int i = 0; i < numberOfThreadsPairs; i++) {
            new Thread(get).start();
            new Thread(post).start();
        }

        new Thread(() -> {
            do {
                try {
                    LOG.info("Starting {} iteration", iterationCounter.incrementAndGet());

                    syncStart.get().countDown();

                    syncProceed.get().await();

                    syncProceed.set(new CountDownLatch(numberOfThreads));
                    syncStart.set(new CountDownLatch(1));
                    final CountDownLatch oldSyncEnd = syncEnd.get();

                    syncEnd.set(new CountDownLatch(1));

                    oldSyncEnd.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            } while (true);
        }).start();

        errorCountDownLatch.await();
    }

    private static Server startServer() throws Exception {
        final Server server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server, getConnectionFactories());
        serverConnector.setPort(BootstrapWithJettyServer.SERVER_PORT);
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
        try (InputStream inputStream = BootstrapWithJettyServer.class.getClassLoader().getResourceAsStream("keystore.jks");
             FileOutputStream fileOutputStream = new FileOutputStream(tempKeystore)) {
            fileOutputStream.write(inputStream.readAllBytes());
        }

        sslContextFactory.setKeyStore(KeyStore.getInstance(tempKeystore, KEYROOL_PRWD.toCharArray()));
        sslContextFactory.setKeyManagerPassword(KEYROOL_PRWD);
        // https://webtide.com/openjdk-11-and-tls-1-3-issues/
        sslContextFactory.setExcludeProtocols("TLSv1.3");
        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");

        return new ConnectionFactory[]{
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig)
        };
    }


}
