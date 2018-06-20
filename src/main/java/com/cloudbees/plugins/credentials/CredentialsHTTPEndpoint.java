package com.cloudbees.plugins.credentials;

import com.cloudbees.plugins.credentials.api.endpoint.APIEndpoint;
import com.cloudbees.plugins.credentials.api.resource.APIResource;
import com.cloudbees.plugins.credentials.api.resource.JSONDataMapper;
import com.cloudbees.plugins.credentials.api.resource.VersionedResource;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import jenkins.model.Jenkins;
import spark.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Extension
public class CredentialsHTTPEndpoint extends APIEndpoint {

    @Override
    public void init() {

        path("/credentials", () -> {
            before("/*", (req, resp) -> System.out.println("API call received")); // API global pre-run actions here
            get("", (req, resp) -> getCredentials());
            path("/domain", () -> {
                get("/:name", (req, resp) -> getCredentialsByDomainName(req));
                post("/create", (req, resp) -> true);
                put("/update", (req, resp) -> true);
            });
        });
    }

    private String getCredentialsByDomainName(Request request) throws JsonProcessingException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

        String domain = request.params(":name");
        if ("global".equals(domain)) {
            domain = null;
        }

        List<APIResource> found = new ArrayList<>();
        Map<Domain, List<Credentials>> domainCredentialsMap = SystemCredentialsProvider.getInstance().getDomainCredentialsMap();
        for (Map.Entry<Domain, List<Credentials>> entry : domainCredentialsMap.entrySet()) {
            if ((entry.getKey().isGlobal() && domain == null) || (!entry.getKey().isGlobal() && entry.getKey().getName().equals(domain))) {
                found = entry.getValue().stream().map(cred -> cred.getDataAPI()).collect(Collectors.toList());
                break;
            }
        }

        ObjectMapper mapper = JSONDataMapper.getJSONMapper();
        return mapper.writeValueAsString(new VersionedResource(found));
    }

    private Object getCredentials() throws JsonProcessingException {
        List<Credentials> credentials = SystemCredentialsProvider.getInstance().getCredentials();

        return JSONDataMapper.getJSONMapper().
                writeValueAsString(new VersionedResource(
                        credentials.stream()
                                .map(cred -> cred.getDataAPI())
                                .collect(Collectors.toList())));
    }


}
