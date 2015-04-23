package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

public class AuthPage {

    @FindByJQuery("a:contains('Administration Console')")
    private WebElement consoleLink;

    public void openConsole() {
        consoleLink.click();
    }
}
