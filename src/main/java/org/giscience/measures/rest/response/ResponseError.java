package org.giscience.measures.rest.response;

import javax.ws.rs.core.Response;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class ResponseError {
    private final String _message;

    public ResponseError(String message) {
        this._message = message;
    }

    public static Response create(String message) {
        return Response.status(200).entity(new ResponseError(message)).build();
    }

    public boolean getError() {
        return true;
    }

    public String getMessage() {
        return this._message;
    }
}
