package org.giscience.measures.rest.measure;

import org.giscience.measures.rest.cache.Cache;
import org.giscience.measures.rest.response.ResponseData;
import org.giscience.measures.rest.response.ResponseError;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.measures.rest.server.RequestParameterException;
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

    public Boolean refersToTimeSpan() {
        return false;
    }

    public ZonedDateTime defaultDate() {
        return ZonedDateTime.now(UTC).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS);
    }

    public ZonedDateTime defaultDateFrom() {
        return null;
    }

    public Integer defaultDaysBefore() {
        return 3 * 12 * 60;
    }

    public Integer defaultIntervalInDays() {
        return 30;
    }

    @GET
    @Path("/grid")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(@QueryParam("resolution") Integer resolution, @QueryParam("bbox") String bboxString, @DefaultValue("false") @QueryParam("latLng") boolean latLng, @DefaultValue("") @QueryParam("date") String dateString, @DefaultValue("") @QueryParam("dateFrom") String dateFromString, @DefaultValue("") @QueryParam("daysBefore") String daysBeforeString, @DefaultValue("") @QueryParam("intervalInDays") Integer intervalInDaysInteger, @Context UriInfo context) throws Exception {
        // separate lat and lon values
        List<String> bboxStrings = Arrays.asList(bboxString.split(","));

        // parse bbox parameter
        if (bboxStrings.size() != 4) return ResponseError.create("Parameter \"bbox\" not in correct format. (Not enough values)");
        List<Double> bboxDoubles;
        try {
            bboxDoubles = bboxStrings.stream().map(Double::parseDouble).collect(Collectors.toList());
        } catch (Exception e) {
            return ResponseError.create("Parameter \"bbox\" not in correct format. (Invalid number)");
        }

        // parse date parameter
        ZonedDateTime date;
        try {
            date = (dateString.isEmpty()) ? this.defaultDate() : ZonedDateTime.of(LocalDate.parse(dateString, ISO_LOCAL_DATE), LocalTime.MIDNIGHT, UTC);
        } catch (DateTimeParseException e) {
            return ResponseError.create("Parameter \"date\" not in correct format. (Invalid  date)");
        }

        // parse dateFrom and daysBefore parameter
        ZonedDateTime dateFrom;
        if (!dateFromString.isEmpty() && !daysBeforeString.isEmpty()) return ResponseError.create("The parameters \"dateFrom\" and \"daysBefore\" are mutually exclusive. Please provide only one of them. (Invalid  parameter combination)");
        try {
        } catch (DateTimeParseException e) {
            return ResponseError.create("Parameter \"dateFrom\" not in correct format. (Invalid  date)");
        }
        if (!dateFromString.isEmpty()) dateFrom = ZonedDateTime.of(LocalDate.parse(dateFromString, ISO_LOCAL_DATE), LocalTime.MIDNIGHT, UTC);
        else {
            try {
                if (!daysBeforeString.isEmpty()) dateFrom = date.minusDays(Long.parseLong(daysBeforeString));
                else dateFrom = null;
            } catch (DateTimeParseException e) {
                return ResponseError.create("Parameter \"daysBefore\" not in correct format. (Invalid  number)");
            }
        }
        if (dateFrom != null && !this.refersToTimeSpan()) return ResponseError.create("The measure refers to only one point in time. The parameters \"dateFrom\" and \"daysBefore\" are not allowed. (Invalid  parameter combination)");
        if (dateFrom == null && this.refersToTimeSpan()) dateFrom = this.defaultDateFrom();
        if (dateFrom == null && this.defaultDaysBefore() != null) dateFrom = date.minusDays(this.defaultDaysBefore());
        if (dateFrom == null) return ResponseError.create("The measure refers to a time span. One of the parameters \"dateFrom\" and \"daysBefore\" has to be provided. (Invalid  parameter combination)");
        ZonedDateTime dateFromFinal = dateFrom;

        Integer intervalInDays;
        intervalInDays = (intervalInDaysInteger == null) ? this.defaultIntervalInDays() : intervalInDaysInteger;

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
            SortedMap<GridCell, R> result = this._cache.getData(this, bbox2, date, dateFromFinal, intervalInDays, parameter, gridCells, bbox3 -> {
                try {
                    return this.compute(bbox3, date, dateFromFinal, intervalInDays, parameter);
                } catch (RequestParameterException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Response response = ResponseData.create("grid", resolution, date, dateFromFinal, intervalInDays, result, this._gridCellIDType, latLng);
            response.getHeaders().add("Access-Control-Allow-Origin", "*");
            return response;
        } catch (RequestParameterException e) {
            e.printStackTrace();
            return ResponseError.create(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseError.create("Unknown computation error");
        }
    }

    public String getId() {
        return this.getClass().getSimpleName();
    }

    public String getIdLong() {
        return this.getClass().getCanonicalName();
    }

    protected abstract SortedMap<GridCell, R> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, RequestParameter p) throws Exception;
}
