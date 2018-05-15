package org.giscience.measures.example;

import org.giscience.measures.rest.measure.Measure;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.measures.rest.server.RequestParameterException;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.cells.GridCell;

import javax.ws.rs.Path;
import java.time.ZonedDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

@Path("api/" + MeasureEmpty.name)
public class MeasureEmpty extends Measure<Double> {
    public static final String name = "measure-empty";

    @Override
    protected SortedMap<GridCell, Double> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, RequestParameter p) throws Exception, RequestParameterException {

        p.get("test");

        return new TreeMap<>();
    }
}
