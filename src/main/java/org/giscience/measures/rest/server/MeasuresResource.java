package org.giscience.measures.rest.server;

import org.giscience.measures.rest.measure.Measure;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.HashMap;

@Path("/")
public class MeasuresResource {
    @Path("/api/{measure}")
    public Measure get(@PathParam("measure") String measure) {
        return Measures.getInstance().get(measure);
    }
}

class Measures {
    private static Measures instance = null;
    private final HashMap<String, Measure> _measures = new HashMap<>();

    private Measures() {}

    public static Measures getInstance() {
        if (Measures.instance == null) Measures.instance = new Measures();
        return Measures.instance;
    }

    public void addMeasure(Measure measure) {
        String id = measure.getId().replaceAll("(?<!^)(?=[A-Z])", "-").toLowerCase();
        if (id.startsWith("measure-")) id = id.substring(8);
        this._measures.put(id, measure);
    }

    public Measure get(String measure) {
        return this._measures.getOrDefault(measure, null);
    }
}
