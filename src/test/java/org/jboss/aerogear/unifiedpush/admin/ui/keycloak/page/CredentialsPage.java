package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CredentialsPage {

    @FindBy(id = "password")
    private WebElement newPassword;

    @FindBy(id = "confirmPassword")
    private WebElement confirmPassword;

    @FindByNg(action = "resetPassword(true)")
    private WebElement save;


    public void setPassword(String password) {
        newPassword.sendKeys(password);
        confirmPassword.sendKeys(password);
        save.click();
    }
}
