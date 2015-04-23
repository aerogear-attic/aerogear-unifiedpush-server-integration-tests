package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class RoleMappingsPage {

    @FindBy(id = "available")
    private Select available;

    @FindBy(id = "assigned")
    private Select assigned;

    @FindByNg(action = "addRealmRole()")
    private WebElement add;

    @FindByNg(action = "deleteRealmRole()")
    private WebElement remove;

    public void assignRole(String role) {
        available.selectByVisibleText(role);
        add.click();
    }

    public void removeRole(String role) {
        assigned.selectByVisibleText(role);
        remove.click();
    }
}
