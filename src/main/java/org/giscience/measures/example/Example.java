package org.giscience.measures.example;

import org.giscience.measures.rest.server.RestServer;

public class Example {
    public static void main(String[] args) throws Exception {
        RestServer restServer = new RestServer();
        restServer.register(new MeasureEmpty());
        restServer.run();
    }
}
