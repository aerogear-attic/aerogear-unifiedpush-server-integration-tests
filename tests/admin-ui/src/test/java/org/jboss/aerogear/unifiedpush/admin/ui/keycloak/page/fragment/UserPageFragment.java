package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment;

import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.model.User;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

public class UserPageFragment {

    @FindByJQuery("td:eq(0) a")
    private WebElement usernameCell;

    @FindByJQuery("td:eq(1)")
    private WebElement lastNameCell;

    @FindByJQuery("td:eq(2)")
    private WebElement firstNameCell;

    @FindByJQuery("td:eq(3)")
    private WebElement emailCell;

    public User getUser() {
        User user = new User();
        user.setUsername(usernameCell.getText());
        user.setLastName(lastNameCell.getText());
        user.setFirstName(firstNameCell.getText());
        user.setEmail(emailCell.getText());

        return user;
    }

    public void edit() {
        usernameCell.click();
    }

    public String getUsername() {
        return usernameCell.getText();
    }
}
