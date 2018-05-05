package org.giscience.measures.rest.measure;

import org.giscience.measures.rest.cache.Cache;
import org.giscience.measures.rest.response.ResponseData;
import org.giscience.measures.rest.response.ResponseError;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.cells.GridCell;
import org.giscience.utils.geogrid.cells.GridCellIDType;
import org.giscience.utils.geogrid.grids.ISEA3H;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public abstract class Measure<R> {
    private Cache _cache;
    private GridCellIDType _gridCellIDType;
    protected ISEA3H _grid;
    SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public void setCache(Cache cache) {
        this._cache = cache;
    }

    public void setGridCellIDType(GridCellIDType gridCellIDType) {
        this._gridCellIDType = gridCellIDType;
    }

    @GET
    @Path("/grid")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(@QueryParam("resolution") Integer resolution, @QueryParam("bbox") String bboxString, @DefaultValue("false") @QueryParam("latLng") boolean latLng, @DefaultValue("") @QueryParam("date") String dateString, @DefaultValue("") @QueryParam("dateFrom") String dateFromString, @Context UriInfo context) throws Exception {
        // separate lat and lon values
        List<String> bboxStrings = Arrays.asList(bboxString.split(","));

        // parse bbox parameter
        if (bboxStrings.size() != 4) return ResponseError.create("Parameter \"bbox\" not in correct format. (Not enough values)");
        List<Double> bboxDoubles;
        try {
            bboxDoubles = bboxStrings.stream().map(Double::parseDouble).collect(Collectors.toList());
        } catch (Exception e) {
            return ResponseError.create("Parameter \"bbox\" not in correct format. (No valid number)");
        }

        // parse date parameter
        ZonedDateTime date;
        try {
            date = (dateString.isEmpty()) ? ZonedDateTime.now(UTC).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS) : ZonedDateTime.of(LocalDate.parse(dateString, ISO_LOCAL_DATE), LocalTime.MIDNIGHT, UTC);
        } catch (DateTimeParseException e) {
            return ResponseError.create("Parameter \"date\" not in correct format. (No valid date)");
        }

        // parse dateFrom parameter
        ZonedDateTime dateFrom;
        try {
            dateFrom = (dateFromString.isEmpty()) ? null : ZonedDateTime.of(LocalDate.parse(dateFromString, ISO_LOCAL_DATE), LocalTime.MIDNIGHT, UTC);
        } catch (DateTimeParseException e) {
            return ResponseError.create("Parameter \"dateFrom\" not in correct format. (No valid date)");
        }

        // parameters
        RequestParameter parameter = new RequestParameter(context);

        // create grid
        this._grid = new ISEA3H(resolution);

        // create response
        BoundingBox bbox = new BoundingBox(bboxDoubles.get(1), bboxDoubles.get(3), bboxDoubles.get(0), bboxDoubles.get(2));
        double buffer = this._grid.bufferEstimator(bbox.minLat, bbox.maxLat, bbox.minLon, bbox.maxLon);
        BoundingBox bbox2 = new BoundingBox(Math.max(bbox.minLat - buffer, -90), Math.min(bbox.maxLat + buffer, 90), bbox.minLon - buffer, bbox.maxLon + buffer);
        try {
            Collection<GridCell> gridCells = this._grid.cellsForBound(bbox2.minLat, bbox2.maxLat, bbox2.minLon, bbox2.maxLon);
            SortedMap<GridCell, R> result = this._cache.getData(this, bbox2, date, dateFrom, parameter, gridCells, bbox3 -> {
                try {
                    return this.compute(bbox3, date, dateFrom, parameter);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
            Response response = ResponseData.create("grid", resolution, date, dateFrom, result, this._gridCellIDType, latLng);
            response.getHeaders().add("Access-Control-Allow-Origin", "*");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseError.create("Unknown computation error");
        }
    }

    public String getId() {
        return this.getClass().getCanonicalName();
    }

    protected abstract SortedMap<GridCell, R> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom, RequestParameter p) throws Exception;
}
