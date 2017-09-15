# Measures REST

The framework `Measures REST` provides a REST server for measures.  In the context of this software, a measure is regarded as being a distribution of values on Earth, aggregated by some Discrete Global Grid System (DGGS).  Measures refer to properties of the data, or to how a dataset has been created.  Typical examples of measures are data quality measures or measures of the fitness for purpose.

## Overview

The framework provided by this repository is a framework only: if measures are provided, the resulting data can be cached and then be accessed by a REST interface.  The measures themselves rely on external data sources, which are independent of this framework.  Measures have, accordingly, to be written on an individual basis, referring to external data sources.  Whenever someone requests a measure by the REST interface, the REST server evaluates the corresponding measure and caches the data.

![Overview](https://github.com/giscience/measures-rest/blob/master/docs/images/overview.svg)

## Related Software

There exist different extensions that aid in implementing a measure for specific data sources:

* [**Measure Rest OSHDB.**](https://github.com/giscience/measure-rest-oshdb) This extension aids in implementing measures that consume data from the [HeiGIT OSHDB](???).

In addition, the REST interface is compatible with the JavaScript library [**geogrid.js**](https://github.com/giscience/geogrid.js), which visualizes measures as a layer to [Leaflet](http://leafletjs.com).  This framework makes extensive use of the library [**geogrid**](https://github.com/giscience/geogrid) that computes and handles Discrete Global Grid Systems (DGGS).

## Implementing a Measure

Measures always extend the class `Measure<R>`, where `R` is a generic parameter that refers to the result of the measure.  As an example, a measure returning a `Double` value always extends `Measure<Double>`.  A typical implementation of a measure looks like follows:

```java
@Path("api/" + MeasureExample.name)
public class MeasureExample extends Measure<R> {
    public static final String name = "measure-example";

    @Override
    protected SortedMap<GridCell, R> compute(BoundingBox bbox) throws Exception {
        // implement the measure here
    }
}
```

Observe that the measure contains a name (‘measure-empty’ in that case), which is also provided to the decorator `Path`.  The name is, among others, used to identify the particular measure in the REST interface.  The above code needs usually just to be copied and can, apart from an adaption of the class name and the variable `name`, stay unmodified.

The method `compute` needs to be overwritten by an implementation of the actual measure.  The bounding box for which data should be aggregated is provided as a parameter.  The result is a `SortedMap` with `GridCell` as keys and `R` as values.  Here, `GridCell` refers to the corresponding class of the library [geogrid](https://github.com/giscience/geogrid), which represents a grid cell.  The aggregation has to be implemented manually though it can use the functions provided by geogrid.  In particular, the `GridCell` for a tuple of coordinates can be computed as
```java
this._grid.cellForLocation(lat, lon);
```

## Running the REST Server

The REST server can easily be instantiated as

```java
RestServer restServer = new RestServer();
```

In this case, the server provides a REST interface on port 8080 for the URL `http://localhost`.  In case another port resp. URL should be used, corresponding parameters can be provided:

```java
RestServer restServer = new RestServer(8080);
RestServer restServer = new RestServer("http://localhost:8080");
```

In addition, the cache strategy (see also below), can be provided, for example:

```java
RestServer restServer = new RestServer("http://localhost:8080", new CacheMemory());
```

After having instantiated the server, the measures can be registered – this step needs to be repeated for every measure:

```java
restServer.register(new MeasureExample());
```

Finally, the server can be started:

```java
restServer.run();
```

## Accessing a Measure by the REST Interface

When having started the server, the registered measures can be accessed by the REST interface, which runs on `http://localhost:8080` by default.  A measure named `measure-example` can accordingly be evaluated by referring to `http://localhost:8080/api/measure-example`.  As parameters, the resolution of the grid and the bounding box need to be provided.  The complete URL is, for example, as follows:

[`http://localhost:8080/api/measure-example/grid?bbox=7.86,48.16,9.53,50.63&resolution=14`](http://localhost:8080/api/measure-example/grid?bbox=7.86,48.16,9.53,50.63&resolution=14)

Here, the bounding box is provided as minimum and maximum of the longitude and the latitude respectively.  As a result, a JSON file is returned which consists of the identifiers (IDs) of the grid cells (see [geogrid](https://github.com/giscience/geogrid)), as well as of the corresponding value of the measure:

```json
{
    "type":"grid",
    "resolution":14,
    "data":[
        {"value":.345, "id":"1309502766029885663"},
        {"value":.784, "id":"1309502815023667023"},
        {"value":.352, "id":"1309502838018652306"},
        {"value":null, "id":"1309502851015240491"},
        {"value":null, "id":"1309502862032073320"},
        {"value":.546, "id":"1309502875029138747"},
        {"value":null, "id":"1309502880017193079"},
        ...
    ]
}
```

If the file should in addition contain the coordinates, the parameter `latLng` needs to be set to `true`:

[`http://localhost:8080/api/measure-example/grid?bbox=7.86,48.16,9.53,50.63&resolution=14&latLng=true`](http://localhost:8080/api/measure-example/grid?bbox=7.86,48.16,9.53,50.63&resolution=14&latLng=true)

Accordingly, the result contains the corresponding coordinates of the centroid of the corresponding cell:

```json
{{
    "type":"grid",
    "resolution":14,
    "data":[
        {"value":.345, "id":"1309502766029885663", "lat":9.502766309434305, "lon":29.8856629972524},
        {"value":.784, "id":"1309502815023667023", "lat":9.502814964223331, "lon":23.667023156128714},
        {"value":.352, "id":"1309502838018652306", "lat":9.502838251618373, "lon":18.652305606256853},
        {"value":null, "id":"1309502851015240491", "lat":9.502851238108507, "lon":15.240490824377252},
        {"value":null, "id":"1309502862032073320", "lat":9.502862302688978, "lon":32.073320392682334},
        {"value":.546, "id":"1309502875029138747", "lat":9.502874619605427, "lon":29.138747369884587},
        {"value":null, "id":"1309502880017193079", "lat":9.502879767399307, "lon":17.19307948719968},
        ...
    ]
}
```

## Cache

The computation of a measure for a given bounding box at a given resolution can be very time consuming.  When results are needed within a fraction of a second, it might make sense to cache the results.  There exist different strategies to cache the data, each of which has individual advantages and disadvantages:

* **`CacheMemory`.**  This approach caches the data in the memory.
* more caching strategies are to be implemented

As a default, `CacheMemory` is being used.  A particular instance of a caching strategy can be used as follows during the instantiation of the REST server:

```java
RestServer restServer = new RestServer(new CacheMemory());
RestServer restServer = new RestServer(8080, new CacheMemory());
RestServer restServer = new RestServer("http://localhost:8080", new CacheMemory());
```

## Visualization

The REST interface of this framework is compatible with the JavaScript library [geogrid](https://github.com/giscience/geogrid), which visualizes measures as a layer to [Leaflet](http://leafletjs.com):

![Overview](https://github.com/giscience/geogrid.js/blob/master/docs/images/screenshot.jpg)

## Author

This software is written and maintained by Franz-Benjamin Mocnik, <mocnik@uni-heidelberg.de>, GIScience Research Group, Institute of Geography, Heidelberg University.

The development has been supported by the DFG project *A framework for measuring the fitness for purpose of OpenStreetMap data based on intrinsic quality indicators* (FA 1189/3-1).

(c) by Heidelberg University, 2017.
