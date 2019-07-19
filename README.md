# jetty.httpclient.sslissue

```
java.io.EOFException: HttpConnectionOverHTTP@6f56146f::DecryptedEndPoint@71b6e0a0{localhost/127.0.0.1:55556<->/127.0.0.1:51343,CLOSED,fill=-,flush=F,to=41030/0}
```

This project contains the mimimal code needed to reproduce the problem with Jetty Http client. Though it is hard to have it reproduced always. To do that one should compile and run this application (Bootstrap.class). After several minutes it should raise and print an exception similar to this:

```
2019-07-19 11:31:30.477:INFO::main: Logging initialized @1062ms to org.eclipse.jetty.util.log.StdErrLog
2019-07-19 11:31:30.954:INFO:oejs.Server:main: jetty-9.4.19.v20190610; built: 2019-06-10T16:30:51.723Z; git: afcf563148970e98786327af5e07c261fda175d3; jvm 11.0.4+11
2019-07-19 11:31:31.004:INFO:oejs.session:main: DefaultSessionIdManager workerName=node0
2019-07-19 11:31:31.005:INFO:oejs.session:main: No SessionScavenger set, using defaults
2019-07-19 11:31:31.014:INFO:oejs.session:main: node0 Scavenging every 600000ms
2019-07-19 11:31:31.970:INFO:oejsh.ContextHandler:main: Started o.e.j.s.ServletContextHandler@73d6d0c{/,null,AVAILABLE}
2019-07-19 11:31:31.998:INFO:oejus.SslContextFactory:main: x509=X509@607b2792(sitename,h=[],w=[]) for Server@7f9e1534[provider=null,keyStore=null,trustStore=null]
2019-07-19 11:31:32.287:INFO:oejs.AbstractConnector:main: Started ServerConnector@7ce026d3{SSL,[ssl, http/1.1]}{0.0.0.0:8080}
2019-07-19 11:31:32.288:INFO:oejs.Server:main: Started @2874ms
2019-07-19 11:31:32.330:WARN:oejusS.config:main: Trusting all certificates configured for Client@1869f114[provider=null,keyStore=null,trustStore=null]
2019-07-19 11:31:32.331:WARN:oejusS.config:main: No Client EndPointIdentificationAlgorithm configured for Client@1869f114[provider=null,keyStore=null,trustStore=null]
2019-07-19 11:31:32.337:INFO:cgajhs.Bootstrap:main: Running 8 threads
2019-07-19 11:32:43.292:WARN:cgajhs.Bootstrap:Thread-19: java.io.EOFException: HttpConnectionOverHTTP@43f11127::DecryptedEndPoint@58c1c452{localhost/127.0.0.1:8080<->/127.0.0.1:50734,OPEN,fill=-,flush=P,to=1/0}
java.util.concurrent.ExecutionException: java.io.EOFException: HttpConnectionOverHTTP@43f11127::DecryptedEndPoint@58c1c452{localhost/127.0.0.1:8080<->/127.0.0.1:50734,OPEN,fill=-,flush=P,to=1/0}
	at org.eclipse.jetty.client.util.FutureResponseListener.getResult(FutureResponseListener.java:118)
	at org.eclipse.jetty.client.util.FutureResponseListener.get(FutureResponseListener.java:101)
	at org.eclipse.jetty.client.HttpRequest.send(HttpRequest.java:683)
	at com.github.andremoniy.jetty.httpclient.sslissue.Bootstrap.lambda$main$1(Bootstrap.java:60)
	at java.base/java.lang.Thread.run(Thread.java:834)
Caused by: 
java.io.EOFException: HttpConnectionOverHTTP@43f11127::DecryptedEndPoint@58c1c452{localhost/127.0.0.1:8080<->/127.0.0.1:50734,OPEN,fill=-,flush=P,to=1/0}
	at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.earlyEOF(HttpReceiverOverHTTP.java:338)
	at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:1552)
	at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.shutdown(HttpReceiverOverHTTP.java:209)
	at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.process(HttpReceiverOverHTTP.java:147)
	at org.eclipse.jetty.client.http.HttpReceiverOverHTTP.receive(HttpReceiverOverHTTP.java:73)
	at org.eclipse.jetty.client.http.HttpChannelOverHTTP.receive(HttpChannelOverHTTP.java:133)
	at org.eclipse.jetty.client.http.HttpConnectionOverHTTP.onFillable(HttpConnectionOverHTTP.java:155)
	at org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:305)
	at org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:103)
	at org.eclipse.jetty.io.ssl.SslConnection$DecryptedEndPoint$IncompleteWriteCallback.succeeded(SslConnection.java:1309)
	at org.eclipse.jetty.io.WriteFlusher.write(WriteFlusher.java:293)
	at org.eclipse.jetty.io.AbstractEndPoint.write(AbstractEndPoint.java:380)
	at org.eclipse.jetty.io.ssl.SslConnection$DecryptedEndPoint.onIncompleteFlush(SslConnection.java:1068)
	at org.eclipse.jetty.io.AbstractEndPoint$2.onIncompleteFlush(AbstractEndPoint.java:54)
	at org.eclipse.jetty.io.WriteFlusher.write(WriteFlusher.java:285)
	at org.eclipse.jetty.io.AbstractEndPoint.write(AbstractEndPoint.java:380)
	at org.eclipse.jetty.client.http.HttpSenderOverHTTP$HeadersCallback.process(HttpSenderOverHTTP.java:268)
	at org.eclipse.jetty.util.IteratingCallback.processing(IteratingCallback.java:241)
	at org.eclipse.jetty.util.IteratingCallback.iterate(IteratingCallback.java:224)
	at org.eclipse.jetty.client.http.HttpSenderOverHTTP.sendHeaders(HttpSenderOverHTTP.java:62)
	at org.eclipse.jetty.client.HttpSender.send(HttpSender.java:214)
	at org.eclipse.jetty.client.http.HttpChannelOverHTTP.send(HttpChannelOverHTTP.java:85)
	at org.eclipse.jetty.client.HttpChannel.send(HttpChannel.java:128)
	at org.eclipse.jetty.client.HttpConnection.send(HttpConnection.java:201)
	at org.eclipse.jetty.client.http.HttpConnectionOverHTTP$Delegate.send(HttpConnectionOverHTTP.java:255)
	at org.eclipse.jetty.client.http.HttpConnectionOverHTTP.send(HttpConnectionOverHTTP.java:122)
	at org.eclipse.jetty.client.http.HttpDestinationOverHTTP.send(HttpDestinationOverHTTP.java:38)
	at org.eclipse.jetty.client.HttpDestination.process(HttpDestination.java:346)
	at org.eclipse.jetty.client.HttpDestination.process(HttpDestination.java:304)
	at org.eclipse.jetty.client.HttpDestination.send(HttpDestination.java:294)
	at org.eclipse.jetty.client.HttpDestination.send(HttpDestination.java:269)
	at org.eclipse.jetty.client.HttpDestination.send(HttpDestination.java:246)
	at org.eclipse.jetty.client.HttpClient.send(HttpClient.java:576)
	at org.eclipse.jetty.client.HttpRequest.send(HttpRequest.java:726)
	at org.eclipse.jetty.client.HttpRequest.send(HttpRequest.java:679)
	at com.github.andremoniy.jetty.httpclient.sslissue.Bootstrap.lambda$main$1(Bootstrap.java:60)
	at java.base/java.lang.Thread.run(Thread.java:834)

```


Be aware, that one may need to wait long enough. In the most cases it is reproducible within a time frame of 5-10 minutes. If it doesn't happen, try to restart again.

# How to run
There are 3 options how you can run this project

1. Using `Docker`: `docker build .` from the root directory of the project
2. Using `maven` only: `mvn clean test` from the root directory of the project
3. Using your favourite IDE: run the `Bootstrap#main` class directly from your IDE
