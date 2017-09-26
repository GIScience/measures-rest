package org.giscience.measures.rest.server;

import org.giscience.measures.rest.measure.Measure;
import org.giscience.measures.rest.cache.Cache;
import org.giscience.measures.rest.cache.CacheMemory;
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
    private final ResourceConfig _resourceConfig = new ResourceConfig();
    private final Cache _cache;
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

    public void disableCompression() {
        this._compression = false;
    }

    public RestServer register(Measure m) {
        m.setCache(this._cache);
        this._resourceConfig.register(m);
        return this;
    }

    public void run() throws IOException, InterruptedException {
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
