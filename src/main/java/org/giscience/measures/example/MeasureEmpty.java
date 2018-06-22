package org.giscience.measures.example;

import org.giscience.measures.rest.measure.Measure;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.cells.GridCell;

import java.time.ZonedDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

public class MeasureEmpty extends Measure<Double> {
    @Override
    protected SortedMap<GridCell, Double> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, RequestParameter p) throws Exception {
        return new TreeMap<>();
    }
}
