package org.jboss.aerogear.unifiedpush.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycloakConfigurationResult {

    private List<String> foundRealms = new ArrayList<String>();
    private List<String> foundUsers = new ArrayList<String>();
    private Map<String, String> removedRequiredActions = new HashMap<String, String>();
    private List<String> roles = new ArrayList<String>();
    private Map<String, Object> extra = new HashMap<String, Object>();

    public List<String> getFoundRealms() {
        return foundRealms;
    }

    public void setFoundRealms(List<String> foundRealms) {
        this.foundRealms = foundRealms;
    }

    public List<String> getFoundUsers() {
        return foundUsers;
    }

    public void setFoundUsers(List<String> foundUsers) {
        this.foundUsers = foundUsers;
    }

    public Map<String, String> getRemovedRequiredActions() {
        return removedRequiredActions;
    }

    public void setRemovedRequiredActions(Map<String, String> removedRequiredActions) {
        this.removedRequiredActions = removedRequiredActions;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
