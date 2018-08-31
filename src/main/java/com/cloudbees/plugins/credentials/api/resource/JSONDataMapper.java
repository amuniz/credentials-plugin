package com.cloudbees.plugins.credentials.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JSON handling utility.
 * Decoupled from the actual I/O mechanism (could be CLI, HTTP API, etc).
 *
 * This should become pluggable to support more data formats (ie. YAML).
 */
public class JSONDataMapper {

    public static ObjectMapper getJSONMapper() {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = TypeFactory.defaultInstance()
                .withClassLoader(Jenkins.getInstance().getPluginManager().uberClassLoader);
        mapper.setTypeFactory(tf);
        return mapper;
    }

    public static VersionedResource getVersionedResource(ObjectMapper mapper, JsonNode json) throws JsonProcessingException {
        JsonNode versionNode = json.at("/version");
        JsonNode dataNode = json.at("/data");
        if (versionNode.isMissingNode() || dataNode.isMissingNode()) {
            throw new IllegalArgumentException("Input format not supported. Either `version` and/or `data` nodes don't exist");
        }
        String version = versionNode.textValue();
        JsonNode data = dataNode;
        List<String> validations = new ArrayList<>();
        if (data.isArray()) {
            List<APIResource> theList = new ArrayList<>();
            for (JsonNode node : data) {
                APIResource resource = mapper.treeToValue(node, APIResource.class);
                validations.addAll(resource.validate());
                theList.add(resource);
            }
            if (!validations.isEmpty()) {
                // TODO: throw ValidationException with collected info
            }
            return new VersionedResource(version, theList);
        } else {
            APIResource resource = mapper.treeToValue(data, APIResource.class);
            validations.addAll(resource.validate());
            if (!validations.isEmpty()) {
                // TODO: throw ValidationException with collected info
            }
            return new VersionedResource(version, Arrays.asList(resource));
        }
    }
}
