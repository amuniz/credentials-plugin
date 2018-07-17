package com.cloudbees.plugins.credentials.api.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import org.jenkinsci.plugins.structs.SymbolLookup;

import java.io.IOException;
import java.util.Set;

/**
 * Uses SymbolLookup.find(Class type, String symbol) to resolve classes
 */
public class SymbolResolver extends TypeIdResolverBase {

    private JavaType javaType;

    @Override
    public void init(JavaType bt) {
        javaType = bt;
    }

    @Override
    public String idFromValue(Object o) {
        return idFromValueAndType(o, o.getClass());
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        // TODO: could this use javaType instead of APIResource?
        APIResource resource = SymbolLookup.get().find(APIResource.class, id);
        if (resource != null) {
            return context.constructSpecializedType(javaType, resource.getClass());
        } else {
            try {
                return context.constructType(Class.forName(id));
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public String idFromValueAndType(Object o, Class<?> aClass) {
        Set<String> symbols = SymbolLookup.getSymbolValue(aClass);
        if (symbols.isEmpty()) {
            return o.getClass().getName();
        } else {
            return symbols.iterator().next();
        }
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
