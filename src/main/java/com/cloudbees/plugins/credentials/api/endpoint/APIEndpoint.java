package com.cloudbees.plugins.credentials.api.endpoint;

import spark.Filter;
import spark.Route;
import spark.RouteGroup;
import spark.Spark;

public abstract class APIEndpoint {

    public abstract void init();

    public void get(String path, Route route) {
        Spark.get("/data" + path, route);
    }

    public void post(String path, Route route) {
        Spark.post("/data" + path, route);
    }

    public void put(String path, Route route) {
        Spark.put("/data" + path, route);
    }

    public void delete(String path, Route route) {
        Spark.delete("/data" + path, route);
    }

    public void path(String path, RouteGroup route) {
        Spark.path("/data" + path, route);
    }

    public void before(String path, Filter filter) {
        Spark.before("/data" + path, filter);
    }
}
