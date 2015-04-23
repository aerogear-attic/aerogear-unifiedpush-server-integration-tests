package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

public class UserPageNavBar {

    @FindByJQuery("a:contains('Credentials')")
    private WebElement credentials;

    @FindByJQuery("a:contains('Role Mappings')")
    private WebElement roleMappings;

    public void credentials() {
        credentials.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void roleMappings() {
        roleMappings.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
