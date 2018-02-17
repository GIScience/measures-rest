package org.giscience.measures.rest.cache;

import org.apache.commons.lang3.tuple.Pair;
import org.giscience.measures.rest.measure.Measure;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.cells.GridCell;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public abstract class Cache {
    protected abstract <R> boolean isCacheEmpty(Measure<R> m);

    protected abstract <R> Pair<SortedMap<GridCell, R>, List<GridCell>> readFromCache(Measure<R> m, ZonedDateTime date, ZonedDateTime dateFrom, Collection<GridCell> gridCells);

    protected abstract <R> void saveToCache(Measure<R> m, ZonedDateTime date, ZonedDateTime dateFrom, SortedMap<GridCell, R> data);

    public <R> SortedMap<GridCell, R> getData(Measure<R> m, BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom, Collection<GridCell> gridCells, Function<BoundingBox, SortedMap<GridCell, R>> compute) {
        SortedMap<GridCell, R> result;
        if (this.isCacheEmpty(m)) {
            // make computation and save
            result = compute.apply(bbox);
            for (GridCell gc : gridCells) result.putIfAbsent(gc, null);
            this.saveToCache(m, date, dateFrom, result);
        } else {
            // read from cache
            Pair<SortedMap<GridCell, R>, List<GridCell>> p = this.readFromCache(m, date, dateFrom, gridCells);
            result = p.getLeft();
            List<GridCell> todo = p.getRight();
            // detect if computation is needed
            if (!todo.isEmpty()) {
                // make computation and save
                List<Double> lats = todo.stream().map(GridCell::getLat).collect(Collectors.toList());
                List<Double> lons = todo.stream().map(GridCell::getLon).collect(Collectors.toList());
                result.putAll(compute.apply(new BoundingBox(Collections.min(lats), Collections.max(lats), Collections.min(lons), Collections.max(lons))));
                for (GridCell gc : gridCells) result.putIfAbsent(gc, null);
                this.saveToCache(m, date, dateFrom, result);
            }
        }
        return result;
    }

    protected static String _zonedDateTimeToString(ZonedDateTime zdt) {
        return (zdt != null) ? zdt.toString() : "null";
    }
}
