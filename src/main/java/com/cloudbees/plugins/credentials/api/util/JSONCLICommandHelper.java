package com.cloudbees.plugins.credentials.api.util;

import com.cloudbees.plugins.credentials.api.resource.JSONDataMapper;
import com.cloudbees.plugins.credentials.api.resource.VersionedResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JSONCLICommandHelper {

    /**
     * Writes {@code res} to the CLI output.
     *
     * @param res the resource object to write.
     * @throws IOException if the JSON serialization fails.
     */
    public static void writeOutput(VersionedResource res, OutputStream output) throws IOException {
        ObjectMapper mapper = JSONDataMapper.getJSONMapper();
        mapper.writeValue(output, res);
    }

    /**
     * Reads the input and map it to a VersionedResource.
     * Type mapping relies on the <code>clazz</code> attributes.
     */
    public static VersionedResource readInput(InputStream input) throws IOException, ClassNotFoundException {
        ObjectMapper mapper = JSONDataMapper.getJSONMapper();
        try {
            JsonNode json = mapper.readTree(input);
            return JSONDataMapper.getVersionedResource(mapper, json);
        }
        catch (JsonProcessingException e) {
            throw new IOException("Error parsing input CLI JSON command: " + e.getMessage(), e);
        }
    }

}
