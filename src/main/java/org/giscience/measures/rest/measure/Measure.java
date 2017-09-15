package org.giscience.measures.rest.measure;

import org.giscience.measures.rest.cache.Cache;
import org.giscience.measures.rest.response.ResponseData;
import org.giscience.measures.rest.response.ResponseError;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.geometry.GridCell;
import org.giscience.utils.geogrid.grids.ISEA3H;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public abstract class Measure<R> {
    private Cache _cache;
    protected ISEA3H _grid;

    public void setCache(Cache cache) {
        this._cache = cache;
    }

    @GET
    @Path("/grid")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(@QueryParam("bbox") String bboxString, @DefaultValue("13") @QueryParam("resolution") Integer resolution, @DefaultValue("false") @QueryParam("latLng") boolean latLng) throws Exception {
        // separate lat and lon values
        List<String> bboxStrings = Arrays.asList(bboxString.split(","));

        // parse bbox parameter
        if (bboxStrings.size() != 4) return ResponseError.create("Parameter bbox not in correct format. (Not enough values)");
        List<Double> bboxDoubles;
        try {
            bboxDoubles = bboxStrings.stream().map(Double::parseDouble).collect(Collectors.toList());
        } catch (Exception e) {
            return ResponseError.create("Parameter bbox not in correct format. (No number)");
        }

        // create grid
        this._grid = new ISEA3H(resolution);

        // create response
        BoundingBox bbox = new BoundingBox(bboxDoubles.get(1), bboxDoubles.get(3), bboxDoubles.get(0), bboxDoubles.get(2));
        double buffer = this._grid.bufferEstimator(bbox.minLat, bbox.maxLat, bbox.minLon, bbox.maxLon);
        BoundingBox bbox2 = new BoundingBox(Math.max(bbox.minLat - buffer, -90), Math.min(bbox.maxLat + buffer, 90), bbox.minLon - buffer, bbox.maxLon + buffer);
        try {
            Collection<GridCell> gridCells = this._grid.cellsForBound(bbox2.minLat, bbox2.maxLat, bbox2.minLon, bbox2.maxLon);
            SortedMap<GridCell, R> result = this._cache.getData(this, bbox2, gridCells, bbox3 -> {
                try {
                    return this.compute(bbox3);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
            return ResponseData.create("grid", resolution, result, latLng);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseError.create("Unknown computation error");
        }
    }

    public String getId() {
        return this.getClass().getCanonicalName();
    }

    protected abstract SortedMap<GridCell, R> compute(BoundingBox bbox) throws Exception;
}
