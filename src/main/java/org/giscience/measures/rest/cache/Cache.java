package org.giscience.measures.rest.cache;

import org.apache.commons.lang3.tuple.Pair;
import org.giscience.measures.rest.measure.Measure;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.geometry.GridCell;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Cache {
    protected abstract boolean isCacheEmpty(Measure m);

    protected abstract <R> Pair<SortedMap<GridCell, R>, List<GridCell>> readFromCache(Measure m, Collection<GridCell> gridCells);

    protected abstract <R> void saveToCache(Measure m, SortedMap<GridCell, R> data);

    public <R> SortedMap<GridCell, R> getData(Measure m, BoundingBox bbox, Collection<GridCell> gridCells, Function<BoundingBox, SortedMap<GridCell, R>> compute) {
        SortedMap<GridCell, R> result;
        if (this.isCacheEmpty(m)) {
            // make computation and save
            result = compute.apply(bbox);
            for (GridCell gc : gridCells) result.putIfAbsent(gc, null);
            this.saveToCache(m, result);
        } else {
            // read from cache
            Pair<SortedMap<GridCell, R>, List<GridCell>> p = this.readFromCache(m, gridCells);
            result = p.getLeft();
            List<GridCell> todo = p.getRight();
            // detect if computation is needed
            if (!todo.isEmpty()) {
                // make computation and save
                List<Double> lats = todo.stream().map(GridCell::getLat).collect(Collectors.toList());
                List<Double> lons = todo.stream().map(GridCell::getLon).collect(Collectors.toList());
                result.putAll(compute.apply(new BoundingBox(Collections.min(lons), Collections.max(lons), Collections.min(lats), Collections.max(lats))));
                for (GridCell gc : gridCells) result.putIfAbsent(gc, null);
                this.saveToCache(m, result);
            }
        }
        return result;
    }
}
