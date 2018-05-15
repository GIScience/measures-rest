package org.giscience.measures.rest.server;

public class RequestParameterException extends RuntimeException {
    public RequestParameterException(String key) {
        super(String.format("The parameter \"%s\" is missing.", key));
    }
}
