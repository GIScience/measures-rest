package org.giscience.measures.rest.server;

import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class RequestParameter {
    UriInfo _context;
    List<String> _ignoreKeys = Arrays.asList("resolution", "bbox", "latLng", "date", "dateFrom");

    public class RequestValue {
        String _value;

        public RequestValue(String value) {
            this._value = value;
        }

        @Override
        public String toString() {
            return this._value;
        }

        public Integer toInteger() {
            return Integer.parseInt(this._value);
        }

        public Double toDouble() {
            return Double.parseDouble(this._value);
        }
    }

    public RequestParameter(UriInfo context) {
        this._context = context;
    }

    public Boolean setDefault(String key, String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, -1);
        if (this._context.getQueryParameters().getFirst(key) != null) return false;
        this._context.getQueryParameters().putSingle(key, value);
        return true;
    }

    public RequestValue get(String key) throws RequestParameterException {
        if (this._context.getQueryParameters().getFirst(key) == null) throw new RequestParameterException(key);
        return new RequestValue(this._context.getQueryParameters().getFirst(key));
    }

    public String getID() {
        return this._context
                .getQueryParameters()
                .keySet()
                .stream()
                .filter(key -> !this._ignoreKeys.contains(key))
                .sorted()
                .map(key -> key + "=" + this._context.getQueryParameters(false).getFirst(key))
                .collect(Collectors.joining("&"));
    }
}
