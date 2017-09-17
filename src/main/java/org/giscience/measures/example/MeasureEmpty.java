package org.giscience.measures.example;

import org.giscience.measures.rest.measure.Measure;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.geometry.GridCell;

import javax.ws.rs.Path;
import java.time.ZonedDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
@Path("api/" + MeasureEmpty.name)
public class MeasureEmpty extends Measure<Double> {
    public static final String name = "measure-empty";

    @Override
    protected SortedMap<GridCell, Double> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom) throws Exception {
        return new TreeMap<>();
    }
}
