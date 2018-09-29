package com.cloudbees.plugins.credentials.api.resource;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface APIExportable<T extends APIResource> {

    @CheckForNull
    default T getDataAPI() {
        return null;
    }
}
