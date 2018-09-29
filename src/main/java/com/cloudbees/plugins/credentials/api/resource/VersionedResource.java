package com.cloudbees.plugins.credentials.api.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Wrapper for in/out messages.
 * It adds the API version and the effective content of the request/response.
 */
public class VersionedResource {

    public static final String DEFAULT_VERSION = "1";

    private String version = DEFAULT_VERSION;

    private List<APIResource> data;

    /**
     * Creates a versioned resource using {@code data} and the {@link #DEFAULT_VERSION}.
     * @param data the data object
     */
    public VersionedResource(List<APIResource> data) {
        this.data = data;
    }

    /**
     * Creates a versioned resource using {@code data} and the given {@code version}.
     *
     * @param version a specific API version
     * @param data the data object
     */
    public VersionedResource(String version, List<APIResource> data) {
        if (version == null) {
            throw new IllegalArgumentException("version has to be an integer, found null");
        }
        try {
            Integer.parseInt(version);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("version has to be an integer, found " + version);
        }
        this.version = version;
        this.data = data;
    }

    public List<APIResource> getData() {
        return data;
    }

    public void setData(List<APIResource> data) {
        this.data = data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}