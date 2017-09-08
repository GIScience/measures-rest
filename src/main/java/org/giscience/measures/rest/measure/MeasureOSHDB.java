package org.giscience.measures.rest.measure;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.giscience.utils.geogrid.geometry.GridCell;
import org.heigit.bigspatialdata.oshdb.OSHDB;
import org.heigit.bigspatialdata.oshdb.api.mapper.Mapper;
import org.heigit.bigspatialdata.oshdb.api.mapper.MapperFactory;
import org.heigit.bigspatialdata.oshdb.api.objects.Timestamps;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;

import java.lang.reflect.ParameterizedType;
import java.util.SortedMap;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public abstract class MeasureOSHDB<R, M extends MapperFactory, O> extends Measure<R> {
    private OSHDB _oshdb;
    private Class<M> _mapperClass;

    public MeasureOSHDB(OSHDB oshdb) {
        super();
        this._oshdb = oshdb;
        ParameterizedType parametrizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this._mapperClass = (Class) parametrizedType.getActualTypeArguments()[1];
    }

    @Override
    protected SortedMap<GridCell, R> compute(BoundingBox bbox) throws Exception {
        Mapper mapper = ((Mapper) this._mapperClass.getMethod("using", OSHDB.class).invoke(null, this._oshdb))
                .boundingBox(bbox)
                .timestamps(new Timestamps(2008, 2017, 1, 12));
        return this.compute(mapper);
    }

    public Pair<GridCell, R> handleGrid(Geometry g, R r) {
        try {
            return ImmutablePair.of(this._grid.cellForCentroid(g), r);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public abstract SortedMap<GridCell, R> compute(Mapper<O> mapper) throws Exception;
}
