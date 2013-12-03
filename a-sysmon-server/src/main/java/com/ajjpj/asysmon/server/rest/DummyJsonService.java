package com.ajjpj.asysmon.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author arno
 */
@Path("/dummy")
public class DummyJsonService {
    @GET
    @Produces("text/plain")
    public String hi() {
        return "Hi all!";
    }
}
