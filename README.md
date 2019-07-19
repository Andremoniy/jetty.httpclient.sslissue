# jetty.httpclient.sslissue

```
java.io.EOFException: HttpConnectionOverHTTP@6f56146f::DecryptedEndPoint@71b6e0a0{localhost/127.0.0.1:55556<->/127.0.0.1:51343,CLOSED,fill=-,flush=F,to=41030/0}
```

This project contains the mimimal code needed to reproduce the problem with Jetty Http client. Though it is hard to have it reproduced always. To do that one should compile and run this application (Bootstrap.class). After several minutes it should raise and print an exception similar to this:

```
2019-07-19 12:27:58.506:INFO::main: Logging initialized @390ms to org.eclipse.jetty.util.log.StdErrLog
2019-07-19 12:27:58.888:INFO:oejs.Server:main: jetty-9.4.19.v20190610; built: 2019-06-10T16:30:51.723Z; git: afcf563148970e98786327af5e07c261fda175d3; jvm 11+28
2019-07-19 12:27:58.943:INFO:oejs.session:main: DefaultSessionIdManager workerName=node0
2019-07-19 12:27:58.943:INFO:oejs.session:main: No SessionScavenger set, using defaults
2019-07-19 12:27:58.948:INFO:oejs.session:main: node0 Scavenging every 660000ms
2019-07-19 12:27:59.655:INFO:oejsh.ContextHandler:main: Started o.e.j.s.ServletContextHandler@4a8a60bc{/,null,AVAILABLE}
2019-07-19 12:27:59.670:INFO:oejus.SslContextFactory:main: x509=X509@4a8ab068(sitename,h=[],w=[]) for Server@1922e6d[provider=null,keyStore=null,trustStore=null]
2019-07-19 12:27:59.815:INFO:oejs.AbstractConnector:main: Started ServerConnector@45dd4eda{SSL,[ssl, http/1.1]}{0.0.0.0:55556}
2019-07-19 12:27:59.816:INFO:oejs.Server:main: Started @1730ms
2019-07-19 12:27:59.839:WARN:oejusS.config:main: Trusting all certificates configured for Client@31c269fd[provider=null,keyStore=null,trustStore=null]
2019-07-19 12:27:59.839:WARN:oejusS.config:main: No Client EndPointIdentificationAlgorithm configured for Client@31c269fd[provider=null,keyStore=null,trustStore=null]
java.util.concurrent.ExecutionException: java.io.EOFException: HttpConnectionOverHTTP@6f56146f::DecryptedEndPoint@71b6e0a0{localhost/127.0.0.1:55556<->/127.0.0.1:51343,CLOSED,fill=-,flush=F,to=41030/0}
    at org.eclipse.jetty.client.util.FutureResponseListener.getResult(FutureResponseListener.java:118)
    at org.eclipse.jetty.client.util.FutureResponseListener.get(FutureResponseListener.java:101)
    at org.eclipse.jetty.client.HttpRequest.send(HttpRequest.java:683)
    at org.eclipse.jetty.client.HttpClient.GET(HttpClient.java:362)
    at org.eclipse.jetty.client.HttpClient.GET(HttpClient.java:347)
    at com.github.andremoniy.jetty.httpclient.sslissue.Bootstrap.lambda$main$0(Bootstrap.java:55)
    at java.base/java.lang.Thread.run(Thread.java:834)
Caused by: java.io.EOFException: HttpConnectionOverHTTP@6f56146f::DecryptedEndPoint@71b6e0a0{localhost/127.0.0.1:55556<->/127.0.0.1:51343,CLOSED,fill=-,flush=F,to=41030/0}
    at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.earlyEOF(HttpReceiverOverHTTP.java:338)
    at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:1552)
    at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.shutdown(HttpReceiverOverHTTP.java:209)
    at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.process(HttpReceiverOverHTTP.java:147)
    at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.receive(HttpReceiverOverHTTP.java:73)
    at org.eclipse.jetty.client.http.HttpChannelOverHTTP.receive(HttpChannelOverHTTP.java:133)
    at org.eclipse.jetty.client.http.HttpConnectionOverHTTP.onFillable(HttpConnectionOverHTTP.java:155)
    at org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:305)
    at org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:103)
    at org.eclipse.jetty.io.ssl.SslConnection$DecryptedEndPoint.onFillable(SslConnection.java:427)
    at org.eclipse.jetty.io.ssl.SslConnection.onFillable(SslConnection.java:321)
    at org.eclipse.jetty.io.ssl.SslConnection$2.succeeded(SslConnection.java:159)
    at org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:103)
    at org.eclipse.jetty.io.ChannelEndPoint$2.run(ChannelEndPoint.java:117)
    at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.runTask(EatWhatYouKill.java:333)
    at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.doProduce(EatWhatYouKill.java:310)
    at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.tryProduce(EatWhatYouKill.java:168)
    at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.run(EatWhatYouKill.java:126)
    at org.eclipse.jetty.util.thread.ReservedThreadExecutor$ReservedThread.run(ReservedThreadExecutor.java:366)
    at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:781)
    at org.eclipse.jetty.util.thread.QueuedThreadPool$Runner.run(QueuedThreadPool.java:917)
    ... 1 more
```
