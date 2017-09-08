package org.giscience.measures.example;

import org.giscience.measures.rest.server.RestServer;
import org.heigit.bigspatialdata.oshdb.OSHDB;
import org.heigit.bigspatialdata.oshdb.OSHDB_H2;

public class Example {
    public static void main(String[] args) throws Exception {
        OSHDB oshdb = new OSHDB_H2("./karlsruhe-regbez").multithreading(true);
        RestServer restServer = new RestServer();
        restServer.register(new MeasureNumberOfElements(oshdb));
        restServer.run();
    }
}
