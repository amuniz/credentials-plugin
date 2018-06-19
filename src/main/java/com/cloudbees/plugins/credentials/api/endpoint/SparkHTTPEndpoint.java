package com.cloudbees.plugins.credentials.api.endpoint;

import com.cloudbees.plugins.credentials.api.resource.JSONDataMapper;
import com.cloudbees.plugins.credentials.api.resource.VersionedResource;
import com.cloudbees.plugins.credentials.domains.DomainCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import spark.Spark;

import java.util.Arrays;

@Extension
public class SparkHTTPEndpoint extends APIEndpoint {

    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    @Override
    public void init() {
        Spark.port(8081);

        // TODO: to be called by plugins using the API
        Spark.get("/api/credentials/:id", (request, response) -> {

            String id = request.params(":id");

            ObjectMapper mapper = JSONDataMapper.getJSONMapper();
            DomainCredentials domainCredentials = new DomainCredentials(null, null);
            return mapper.writeValueAsString(new VersionedResource(Arrays.asList(domainCredentials.getDataAPI())));
        });
    }
}
