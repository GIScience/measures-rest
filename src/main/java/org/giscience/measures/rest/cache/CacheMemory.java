package org.giscience.measures.rest.cache;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.giscience.measures.rest.measure.Measure;
import org.giscience.utils.geogrid.geometry.GridCell;

import java.util.*;

public class CacheMemory extends Cache {
    private SortedMap<String, SortedMap<GridCell, Object>> _cache = new TreeMap<>();

    @Override
    protected boolean isCacheEmpty(Measure m) {
        return (this._cache.get(m.getId()) == null);
    }

    @Override
    protected <R> Pair<SortedMap<GridCell, R>, List<GridCell>> readFromCache(Measure m, Collection<GridCell> gridCells) {
        SortedMap<GridCell, Object> cache = this._cache.getOrDefault(m.getId(), new TreeMap<>());
        SortedMap<GridCell, R> result = new TreeMap<>();
        List<GridCell> todo = new ArrayList<>();
        for (GridCell gc : gridCells) {
            if (cache.containsKey(gc)) result.put(gc, (R) cache.get(gc));
            else todo.add(gc);
        }
        return ImmutablePair.of(result, todo);
    }

    @Override
    protected <R> void saveToCache(Measure m, SortedMap<GridCell, R> data) {
        SortedMap<GridCell, Object> cache = this._cache.getOrDefault(m.getId(), new TreeMap<>());
        cache.putAll(data);
        this._cache.put(m.getId(), cache);
    }
}
