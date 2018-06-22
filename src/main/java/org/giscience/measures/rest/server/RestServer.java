package org.giscience.measures.rest.server;

import org.giscience.measures.rest.cache.Cache;
import org.giscience.measures.rest.cache.CacheMemory;
import org.giscience.measures.rest.measure.Measure;
import org.giscience.utils.geogrid.cells.GridCellIDType;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class RestServer {
    private final URI _baseUrl;
    private final Measures _measures = new Measures();
    private final ResourceConfig _resourceConfig = new ResourceConfig();
    private final Cache _cache;
    private GridCellIDType _gridCellIDType = GridCellIDType.ADAPTIVE_1_PERCENT;
    private boolean _compression;

    public RestServer() {
        this(8080);
    }

    public RestServer(int port) {
        this(UriBuilder.fromPath("/").scheme("http").host("localhost").port(port).build());
    }

    public RestServer(URI baseUrl) {
        this._baseUrl = baseUrl;
        this._cache = new CacheMemory();
    }

    public RestServer(Cache cache) {
        this(UriBuilder.fromPath("/").scheme("http").host("localhost").port(8080).build(), cache);
    }

    public RestServer(int port, Cache cache) {
        this(UriBuilder.fromPath("/").scheme("http").host("localhost").port(port).build(), cache);
    }

    public RestServer(URI baseUrl, Cache cache) {
        this._baseUrl = baseUrl;
        this._cache = cache;
    }

    public void setGridCellIDType(GridCellIDType gridCellIDType) {
        this._gridCellIDType = gridCellIDType;
    }

    public void disableCompression() {
        this._compression = false;
    }

    public RestServer register(Measure m) {
        m.setCache(this._cache);
        m.setGridCellIDType(this._gridCellIDType);
        this._measures.addMeasure(m);
        return this;
    }

    @Path("/")
    protected class Measures {
        private final HashMap<String, Measure> _measures = new HashMap<>();

        public void addMeasure(Measure measure) {
            String id = measure.getId().replaceAll("(?<!^)(?=[A-Z])", "-").toLowerCase();
            this._measures.put(id, measure);
        }

        @Path("/api/{measure}")
        public Measure get(@PathParam("measure") String measure) {
            return this._measures.getOrDefault(measure, null);
        }
    }

    public void run() throws IOException, InterruptedException {
        this._resourceConfig.register(this._measures);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(this._baseUrl, this._resourceConfig, false);
        if (this._compression) {
            CompressionConfig config = server.getListener("grizzly").getCompressionConfig();
            config.setCompressionMode(CompressionConfig.CompressionMode.FORCE);
            config.setCompressionMinSize(1);
            config.setCompressableMimeTypes("application/json");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        server.start();
        System.out.println(this._baseUrl);
        Thread.currentThread().join();
    }
}
