package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class KCNavigation {

    @FindByJQuery(".bs-sidebar a:contains('Settings')")
    private WebElement settingsLink;

    @FindByJQuery(".bs-sidebar a:contains('Users')")
    private WebElement usersLink;

    @FindByJQuery(".bs-sidebar a:contains('Roles')")
    private WebElement rolesLink;

    @FindByJQuery(".bs-sidebar a:contains('Applications')")
    private WebElement applicationsLink;

    @FindByJQuery(".bs-sidebar a:contains('OAuth')")
    private WebElement oauthLink;

    @FindByJQuery(".bs-sidebar a:contains('Sessions')")
    private WebElement sessionsLink;

    @FindByJQuery(".bs-sidebar a:contains('Security')")
    private WebElement securityLink;

    @FindByJQuery(".bs-sidebar a:contains('Events')")
    private WebElement eventssLink;

    @FindByJQuery("#content h2:visible")
    private WebElement currentHeader;

    public void settings() {
        openPage(settingsLink, "General Settings");
    }

    public void users() {
        openPage(usersLink, "Users");
    }

    public void roles() {
        openPage(rolesLink, "Realm-Level Roles");
    }

    public void applications() {
        openPage(applicationsLink, "Applications");
    }

    public void oauth() {
        openPage(oauthLink, "OAuth Clients");
    }

    public void sessions() {
        openPage(sessionsLink, "Total Active Sessions");
    }

    public void security() {
        openPage(securityLink, "Browser Security Headers");
    }

    public void events() {
        openPage(eventssLink, "Events");
    }

    private void openPage(WebElement page, String headerText) {
        page.click();
        waitModel().until().element(currentHeader).text().contains(headerText);
    }

    public void waitForPage() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitModel().until().element(currentHeader).is().present();
    }
}
