package com.cloudbees.plugins.credentials;

import com.cloudbees.plugins.credentials.api.endpoint.APIEndpoint;
import com.cloudbees.plugins.credentials.api.resource.APIResource;
import com.cloudbees.plugins.credentials.api.resource.JSONDataMapper;
import com.cloudbees.plugins.credentials.api.resource.VersionedResource;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;
import spark.Spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Extension
public class CredentialsHTTPEndpoint extends APIEndpoint {

    @Override
    public void init() {

        //
        // Get all credentials from the system store
        //
        get("/credentials", (request, response) -> {
            List<Credentials> credentials = SystemCredentialsProvider.getInstance().getCredentials();
            ObjectMapper mapper = JSONDataMapper.getJSONMapper();

            return JSONDataMapper.getJSONMapper().
                    writeValueAsString(new VersionedResource(
                            credentials.stream()
                                    .map(cred -> cred.getDataAPI())
                                    .collect(Collectors.toList())));
        });

        //
        // Get all credentials in a domain
        //
        get("/credentials/:domain", (request, response) -> {

            // TODO: this is requiring administer as en example of permissions check, it sould be store.checkPermission(CredentialsProvider.UPDATE);
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            String domain = request.params(":domain");
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
            DomainCredentials domainCredentials = new DomainCredentials(null, null);
            return mapper.writeValueAsString(new VersionedResource(found));
        });
    }

}
