package com.cloudbees.plugins.credentials.api.endpoint;

import spark.Route;
import spark.Spark;

public abstract class APIEndpoint {

    public abstract void init();

    public void get(String path, Route route) {
        Spark.get("/data" + path, route);
    }
}
