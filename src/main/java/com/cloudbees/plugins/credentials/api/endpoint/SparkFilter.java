package com.cloudbees.plugins.credentials.api.endpoint;


import hudson.ExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import jenkins.model.Jenkins;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import spark.servlet.SparkApplication;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class SparkFilter extends spark.servlet.SparkFilter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(new SparkFilterConfig());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            String path = req.getRequestURI().substring(req.getContextPath().length());

            if (path.startsWith("/data")) {
                super.doFilter(request, response, chain);
                return;
            }
        }
        chain.doFilter(request, response);

    }


    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public void initializeJenkinsFilter() throws ServletException {
        SparkFilter filter = new SparkFilter();
        PluginServletFilter.addFilter(filter);
    }

    private static class SparkFilterConfig implements FilterConfig {

        private Map<String, String> initParams;

        public SparkFilterConfig() {
            initParams = new HashMap<>();
            initParams.put("applicationClass", SparkApp.class.getName());
        }

        @Override
        public String getFilterName() {
            return "SparkFilter";
        }

        @Override
        public ServletContext getServletContext() {
            return Jenkins.getInstance().servletContext;
        }

        @Override
        public String getInitParameter(String name) {
            return initParams.get(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return new IteratorEnumeration(initParams.keySet().iterator());
        }
    }

    public static class SparkApp implements SparkApplication {

        @Override
        public void init() {
            ExtensionList<APIEndpoint> endpoints = ExtensionList.lookup(APIEndpoint.class);
            for (APIEndpoint endpoint : endpoints) {
                endpoint.init();
            }
        }
    }
}
