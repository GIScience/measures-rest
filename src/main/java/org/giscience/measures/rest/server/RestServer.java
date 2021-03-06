package org.giscience.measures.rest.server;

import org.giscience.measures.rest.cache.Cache;
import org.giscience.measures.rest.cache.CacheMemory;
import org.giscience.measures.rest.measure.Measure;
import org.giscience.utils.geogrid.cells.GridCellIDType;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class RestServer {
    private final URI _baseUrl;
    private final Measures _measures = Measures.getInstance();
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

    public void run() throws IOException, InterruptedException {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(MeasuresResource.class);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(this._baseUrl, resourceConfig, false);
        if (this._compression) {
            CompressionConfig config = server.getListener("grizzly").getCompressionConfig();
            config.setCompressionMode(CompressionConfig.CompressionMode.FORCE);
            config.setCompressionMinSize(1);
            config.setCompressibleMimeTypes("application/json");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        server.start();
        System.out.println(this._baseUrl);
        Thread.currentThread().join();
    }
}
