package org.giscience.measures.rest.utils;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class BoundingBox {
    public final double minLat;
    public final double maxLat;
    public final double minLon;
    public final double maxLon;

    public BoundingBox(double minLat, double maxLat, double minLon, double maxLon) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    @Override
    public String toString() {
        return String.format("(%f, %f) (%f, %f)", this.minLat, this.maxLat, this.minLon, this.maxLon);
    }
}
