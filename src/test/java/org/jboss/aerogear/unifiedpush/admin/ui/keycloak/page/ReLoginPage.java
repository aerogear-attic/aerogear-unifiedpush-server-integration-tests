package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

public class ReLoginPage extends LoginPage {

    private final static String TITLE = "Please Re-Login";

    @Override
    public String getExpectedTitle() {
        return TITLE;
    }
}
