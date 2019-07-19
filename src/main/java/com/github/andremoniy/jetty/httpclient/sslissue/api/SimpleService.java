package com.github.andremoniy.jetty.httpclient.sslissue.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class SimpleService {
    @GET
    @Path("/get")
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        return "OK";
    }

    @POST
    @Path("/post")
    @Produces(MediaType.TEXT_HTML)
    public String post() {
        return "OK";
    }
}
