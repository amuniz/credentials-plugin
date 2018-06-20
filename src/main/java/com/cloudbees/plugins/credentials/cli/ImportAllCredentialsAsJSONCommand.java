package com.cloudbees.plugins.credentials.cli;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.api.resource.APIResource;
import com.cloudbees.plugins.credentials.api.util.JSONCLICommandHelper;
import com.cloudbees.plugins.credentials.api.resource.VersionedResource;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainCredentials;
import hudson.Extension;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.Collection;

@Extension
public class ImportAllCredentialsAsJSONCommand extends BaseCredentialsCLICommand {

    @Argument(metaVar = "STORE", usage = "Store Id", required = true)
    public CredentialsStore store;

    @Option(name = "--json")
    private boolean asJson;

    @Override
    public String getShortDescription() {
        return "Import credentials as JSON (produced by \"list-credentials STORE --json\"";
    }

    @Override
    protected int run() throws Exception {
        if (asJson) {
            VersionedResource input = JSONCLICommandHelper.readInput(stdin);
            for (Object res : (Collection) input.getData()) {
                DomainCredentials domainCredentials = (DomainCredentials) ((APIResource) res).toModel();
                Domain domain = domainCredentials.getDomain();
                if (domainCredentials.getDomain().getName() == null) {
                    domain = Domain.global();
                }
                for (Credentials creds : domainCredentials.getCredentials()) {
                    store.addCredentials(domain, creds);
                }
            }
        }
        return 0;
    }
}
