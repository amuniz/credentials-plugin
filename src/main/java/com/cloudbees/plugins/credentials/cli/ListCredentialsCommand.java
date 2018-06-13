/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc..
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cloudbees.plugins.credentials.cli;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import hudson.Extension;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.json.XML;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.stapler.export.DataWriter;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;
import org.kohsuke.stapler.export.TreePruner;

/**
 * Lists the credentials in a specific credentials store
 *
 * @since 2.1.1
 */
@Extension
public class ListCredentialsCommand extends BaseCredentialsCLICommand {
    /**
     * The store to list credentials in.
     */
    @Argument(metaVar = "STORE", usage = "Store ID", required = true)
    public CredentialsStore store;

    @Option(name = "--json")
    public boolean asJson;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortDescription() {
        return Messages.ListCredentialsCommand_ShortDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int run() throws Exception {
        store.checkPermission(CredentialsProvider.VIEW);
        if (asJson) {
            // Does not work because the ObjectMapper is not aware of the @Exported annotation so it generates shit
            /*
            ObjectMapper mapper = new ObjectMapper();
            SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
                    .serializeAllExcept("parent");
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("parentFilter", theFilter);
            mapper.writer(filters).writeValue(stdout, store.getStoreAction().getDomains());
            */

            // This is a hack, and does not work neither, I suspect it is a bug in stapler
            /*
            Map<String, CredentialsStoreAction.DomainWrapper> domains = store.getStoreAction().getDomains();
            DataWriter dataWriter = Flavor.JSON.createDataWriter(domains.get("_"), new PrintWriter(stdout));
            TreePruner pruner = new TreePruner.ByDepth(1);
            Model p = new ModelBuilder().get(domains.get("_").getClass());
            p.writeTo(domains.get("_"), pruner, dataWriter);
            */

            // convert the XML into JSON
            List<Domain> domains = store.getDomains();
            List<Credentials> all = new ArrayList<>();
            for (Domain domain : domains) {
                List<Credentials> credentials = store.getCredentials(domain);
                all.addAll(credentials);
            }
            String xml = CredentialsStoreAction.SECRETS_REDACTED.toXML(all);

            // hacky, org.json is the only library supporting XML collections to be mapped to JSON arrays
            org.json.JSONObject jObject = XML.toJSONObject(xml);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            Object json = mapper.readValue(jObject.toString(), Object.class);
            mapper.writeValue(stdout, json);
            return 0;
        }
        List<Domain> domains = store.getDomains();
        for (Domain domain : domains) {
            List<Credentials> credentials = store.getCredentials(domain);
            Map<String, String> nameById = new LinkedHashMap<String, String>(credentials.size());
            int maxIdLen = "# of Credentials".length(), maxNameLen = 0;
            int index = 0;
            for (Credentials c : credentials) {
                String id;
                if (c instanceof IdCredentials) {
                    id = ((IdCredentials) c).getId();
                } else {
                    while (nameById.containsKey("index-" + index)) {
                        index++;
                    }
                    id = "index-" + index;
                    index++;
                }
                String name = CredentialsNameProvider.name(c);
                nameById.put(id, name);
                maxIdLen = Math.max(maxIdLen, id.length());
                maxNameLen = Math.max(maxNameLen, name.length());
            }
            stdout.println(StringUtils.repeat("=", maxIdLen + maxNameLen + 1));
            stdout.println(StringUtils.rightPad("Domain", maxIdLen) + " " + (domain.isGlobal()
                    ? "(global)"
                    : domain.getName()));
            stdout.println(StringUtils.rightPad("Description", maxIdLen) + " " + StringUtils
                    .defaultString(domain.getDescription()));
            stdout.println(StringUtils.rightPad("# of Credentials", maxIdLen) + " " + credentials.size());
            stdout.println(StringUtils.repeat("=", maxIdLen + maxNameLen + 1));
            stdout.println(StringUtils.rightPad("Id", maxIdLen) + " Name");
            stdout.println(StringUtils.repeat("=", maxIdLen) + " " + StringUtils.repeat("=", maxNameLen));
            for (Map.Entry<String, String> entry : nameById.entrySet()) {
                stdout.println(StringUtils.rightPad(entry.getKey(), maxIdLen) + " " + entry.getValue());
            }
            stdout.println(StringUtils.repeat("=", maxIdLen + maxNameLen + 1));
            stdout.println();
        }
        return 0;
    }
}
