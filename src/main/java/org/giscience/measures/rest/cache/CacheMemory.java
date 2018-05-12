package org.giscience.measures.rest.cache;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.giscience.measures.rest.measure.Measure;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.utils.geogrid.cells.GridCell;

import java.time.ZonedDateTime;
import java.util.*;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class CacheMemory extends Cache {
    private SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<String, SortedMap<GridCell, Object>>>>>> _cache = new TreeMap<>();

    @Override
    protected <R> boolean isCacheEmpty(Measure<R> m) {
        return (this._cache.get(m.getId()) == null);
    }

    @Override
    protected <R> Pair<SortedMap<GridCell, R>, List<GridCell>> readFromCache(Measure<R> m, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, RequestParameter parameter, Collection<GridCell> gridCells) {
        SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<String, SortedMap<GridCell, Object>>>>> cacheMeasure = this._cache.getOrDefault(m.getId(), new TreeMap<>());
        SortedMap<String, SortedMap<Integer, SortedMap<String, SortedMap<GridCell, Object>>>> cacheDate = cacheMeasure.getOrDefault(CacheMemory._zonedDateTimeToString(date), new TreeMap<>());
        SortedMap<Integer, SortedMap<String, SortedMap<GridCell, Object>>> cacheDateFrom = cacheDate.getOrDefault(CacheMemory._zonedDateTimeToString(dateFrom), new TreeMap<>());
        SortedMap<String, SortedMap<GridCell, Object>> cacheIntervalInDays = cacheDateFrom.getOrDefault(intervalInDays, new TreeMap<>());
        SortedMap<GridCell, Object> cache = cacheIntervalInDays.getOrDefault(parameter.getID(), new TreeMap<>());
        SortedMap<GridCell, R> result = new TreeMap<>();
        List<GridCell> todo = new ArrayList<>();
        for (GridCell gc : gridCells) {
            if (cache.containsKey(gc)) result.put(gc, (R) cache.get(gc));
            else todo.add(gc);
        }
        return ImmutablePair.of(result, todo);
    }

    @Override
    protected <R> void saveToCache(Measure<R> m, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, RequestParameter parameter, SortedMap<GridCell, R> data) {
        SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<String, SortedMap<GridCell, Object>>>>> cacheMeasure = this._cache.getOrDefault(m.getId(), new TreeMap<>());
        SortedMap<String, SortedMap<Integer, SortedMap<String, SortedMap<GridCell, Object>>>> cacheDate = cacheMeasure.getOrDefault(CacheMemory._zonedDateTimeToString(date), new TreeMap<>());
        SortedMap<Integer, SortedMap<String, SortedMap<GridCell, Object>>> cacheDateFrom = cacheDate.getOrDefault(CacheMemory._zonedDateTimeToString(dateFrom), new TreeMap<>());
        SortedMap<String, SortedMap<GridCell, Object>> cacheIntervalInDays = cacheDateFrom.getOrDefault(intervalInDays, new TreeMap<>());
        SortedMap<GridCell, Object> cache = cacheIntervalInDays.getOrDefault(parameter.getID(), new TreeMap<>());
        cache.putAll(data);
        cacheIntervalInDays.put(parameter.getID(), cache);
        cacheDateFrom.put(intervalInDays, cacheIntervalInDays);
        cacheDate.put(CacheMemory._zonedDateTimeToString(dateFrom), cacheDateFrom);
        cacheMeasure.put(CacheMemory._zonedDateTimeToString(date), cacheDate);
        this._cache.put(m.getId(), cacheMeasure);
    }
}
