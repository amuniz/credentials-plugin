package com.cloudbees.plugins.credentials.api.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Collections;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type", include = JsonTypeInfo.As.PROPERTY)
@JsonTypeIdResolver(SymbolResolver.class)
public class APIResource {

    // TODO: once hudson.model.Api is fixed to be able to serialize the target object without a StaplerRequest in the context
    // @JsonRawValue
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String missingModel;

    public APIResource() {
    }

    /**
     * To be used only when the model does not provides a getDataAPI() implementation, so the JSON contains info about it.
     */
    public APIResource(Object model) {
        // TODO: modify the Api/Stapler impl of doJson to decouple it from the StaplerRequest
        // Api oldApi = new Api(model);
        // missingModel = oldApi.doJson();

        missingModel = model.getClass().getName();
    }

    public Object toModel() {
        return null;
    }

    public String getMissingModel() {
        return missingModel;
    }

    /**
     * To be overridden by subclasses to perform data validations.
     */
    public List<String> validate() {
       return Collections.emptyList();
    }
}
