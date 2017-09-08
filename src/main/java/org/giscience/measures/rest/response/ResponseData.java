package org.giscience.measures.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.giscience.utils.geogrid.geometry.GridCell;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class ResponseData<T> {
    private final String _type;
    private final int _resolution;
    private final List<GridCellValuePair<T>> _data;

    public ResponseData(String type, int resolution, List<GridCellValuePair<T>> data) {
        this._type = type;
        this._resolution = resolution;
        this._data = data;
    }

    public static Response create(String type, int resolution, List<GridCellValuePair> data) {
        return Response.status(200).entity(new ResponseData(type, resolution, data)).build();
    }

    public static Response create(String type, int resolution, SortedMap<GridCell, ?> data, boolean latLng) {
        return ResponseData.create(type, resolution, data.entrySet().stream().map(e -> (latLng) ? new GridCellValuePairWithLatLng(e.getKey().getId(), e.getKey().getLat(), e.getKey().getLon(), e.getValue()) : new GridCellValuePair(e.getKey().getId(), e.getKey().getLat(), e.getKey().getLon(), e.getValue())).collect(Collectors.toList()));
    }

    public String getType() {
        return this._type;
    }

    public int getResolution() {
        return this._resolution;
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
