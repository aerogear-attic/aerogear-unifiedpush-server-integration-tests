package org.jboss.aerogear.unifiedpush.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Installation;

public class DataGeneratorContext {

    private final DataGeneratorConfig config;
    private final List<PushApplication> applications = new ArrayList<PushApplication>();
    private final List<Installation> installations = new ArrayList<Installation>();
    private final List<Category> categories = new ArrayList<Category>();
    private final Map<String, Object> response = new LinkedHashMap<String, Object>();

    public DataGeneratorContext(DataGeneratorConfig config) {
        this.config = config;
    }

    public DataGeneratorConfig getConfig() {
        return config;
    }

    public List<PushApplication> getApplications() {
        return applications;
    }

    public List<Installation> getInstallations() {
        return installations;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Map<String, Object> getResponse() {
        return response;
    }

}