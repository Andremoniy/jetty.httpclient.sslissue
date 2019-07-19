package com.github.andremoniy.jetty.httpclient.sslissue;

import org.junit.jupiter.api.Test;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class BootstrapWithSimpleHttpsServerTest {

    @Test
    void testBootstrap() {
        assertTimeout(ofMinutes(10), () -> BootstrapWithSimpleHttpsServer.main(new String[0]));
    }
}