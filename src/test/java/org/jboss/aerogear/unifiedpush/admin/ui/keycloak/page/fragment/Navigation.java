package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Navigation {

    @FindByJQuery(".nav li:eq(0) a")
    private WebElement upsConsoleLink;

    @FindByJQuery(".nav li:eq(1) a")
    private WebElement signOutLink;

    @FindByJQuery(".bs-sidebar ul li:eq(0) a")
    private WebElement accountLink;

    @FindByJQuery(".bs-sidebar ul li:eq(1) a")
    private WebElement passwordLink;

    @FindByJQuery(".bs-sidebar ul li:eq(2) a")
    private WebElement authenticatorLink;

    @FindByJQuery(".bs-sidebar ul li:eq(3) a")
    private WebElement sessionsLink;

    public void upsConsole() {
        upsConsoleLink.click();
    }

    public void signOut() {
        signOutLink.click();
    }

    public void account() {
        accountLink.click();
    }

    public void password() {
        passwordLink.click();
    }

    public void authenticator() {
        authenticatorLink.click();
    }

    public void sessions() {
        sessionsLink.click();
    }


}
