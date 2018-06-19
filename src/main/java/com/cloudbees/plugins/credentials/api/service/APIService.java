package com.cloudbees.plugins.credentials.api.service;

import com.cloudbees.plugins.credentials.api.resource.APIExportable;

import java.util.List;

public abstract class APIService<T extends APIExportable, PK> {

    public List<T> list() { return null; }

    public void create(PK id, T data) {}

    public T read(PK id) { return null; }

    public void update(PK id, T data) {}

    public void delete(PK id) {}

}
