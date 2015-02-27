package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class RoleMappingsPage {

    @FindBy(id = "available")
    private WebElement available;

    @FindBy(id = "assigned")
    private WebElement assigned;

    @FindByNg(action = "addRealmRole()")
    private WebElement add;

    @FindByNg(action = "deleteRealmRole()")
    private WebElement remove;

    public void assignRole(String role) {
        waitModel().until().element(available).text().contains(role);
        new Select(available).selectByVisibleText(role);
        add.click();
    }

    public void removeRole(String role) {
        waitModel().until().element(assigned).text().contains(role);
        new Select(assigned).selectByVisibleText(role);
        remove.click();
    }
}
