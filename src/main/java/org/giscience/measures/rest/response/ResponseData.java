package org.giscience.measures.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.giscience.utils.geogrid.cells.GridCell;
import org.giscience.utils.geogrid.cells.GridCellIDType;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class ResponseData<T> {
    private final String _type;
    private final int _resolution;
    private final ZonedDateTime _date;
    private final ZonedDateTime _dateFrom;
    private final Integer _intervalInDays;
    private final List<GridCellValuePair<T>> _data;

    public ResponseData(String type, int resolution, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, List<GridCellValuePair<T>> data) {
        this._type = type;
        this._resolution = resolution;
        this._date = date;
        this._dateFrom = dateFrom;
        this._intervalInDays = intervalInDays;
        this._data = data;
    }

    public static Response create(String type, int resolution, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, List<GridCellValuePair> data) {
        return Response.status(200).entity(new ResponseData(type, resolution, date, dateFrom, intervalInDays, data)).build();
    }

    public static Response create(String type, int resolution, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, SortedMap<GridCell, ?> data, GridCellIDType gridCellIDType, boolean latLng) {
        return ResponseData.create(type, resolution, date, dateFrom, intervalInDays, data.entrySet().stream().map(e -> (latLng) ? new GridCellValuePairWithLatLng(e.getKey().getID(gridCellIDType), e.getKey().getLat(), e.getKey().getLon(), e.getValue()) : new GridCellValuePair(e.getKey().getID(gridCellIDType), e.getKey().getLat(), e.getKey().getLon(), e.getValue())).collect(Collectors.toList()));
    }

    public String getType() {
        return this._type;
    }

    public int getResolution() {
        return this._resolution;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDate() {
        return (this._date != null) ? this._date.toString() : null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDateFrom() {
        return (this._dateFrom != null) ? this._dateFrom.toString() : null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getIntervalInDays() {
        return this._intervalInDays;
    }

    public List<GridCellValuePair<T>> getData() {
        return this._data;
    }

    @JsonIgnoreProperties(value = {"lat", "lon"})
    public static class GridCellValuePair<T> {
        private final long _id;
        protected final double _lat;
        protected final double _lon;
        private final T _value;

        public GridCellValuePair(long id, double lat, double lon, T value) {
            this._id = id;
            this._lat = lat;
            this._lon = lon;
            this._value = value;
        }

        public String getId() {
            return String.valueOf(this._id);
        }

        public double getLat() {
            return this._lat;
        }

        public double getLon() {
            return this._lon;
        }

        public T getValue() {
            return this._value;
        }
    }

    @JsonIgnoreProperties(value = {})
    public static class GridCellValuePairWithLatLng<T> extends GridCellValuePair<T> {
        public GridCellValuePairWithLatLng(long id, double lat, double lon, T value) {
            super(id, lat, lon, value);
        }
    }
}
