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
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class Bootstrap {

    public static void main(String[] args) throws Exception {
        final int port = 55556;

        final Server server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server, getConnectionFactories());
        serverConnector.setPort(port);
        server.addConnector(serverConnector);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        final ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", SimpleService.class.getCanonicalName());

        server.setHandler(context);

        server.start();

        final HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
        httpClient.start();

        final String baseUrl = "https://localhost:" + port + "/";

        final Runnable get = () -> {
            do {
                try {
                    httpClient.GET(baseUrl + "get");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (true);
        };
        final Runnable post = () -> {
            do {
                try {
                    httpClient.POST(baseUrl + "post").send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (true);
        };

        for (int i = 0; i < 2; i++) {
            new Thread(get).start();
            new Thread(post).start();
        }

        server.join();

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
