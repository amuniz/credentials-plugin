package com.cloudbees.plugins.credentials.api.resource;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface APIExportable {

    @CheckForNull
    default APIResource getDataAPI() {
        return null;
    }
}
