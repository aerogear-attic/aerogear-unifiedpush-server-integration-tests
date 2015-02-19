package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.model.User;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment.FlashMessage;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment.Switch;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment.UserPageFragment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class UsersPage {

    @FindByJQuery("#content .btn")
    private WebElement addUserButton;

    @FindByJQuery("#content a:contains('View all users')")
    private WebElement allUsersLink;

    @FindByNg(repeat = "user in users")
    private List<UserPageFragment> users;

    @Drone
    private WebDriver driver;

    @FindBy(id = "username")
    private WebElement usernameInput;

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "firstName")
    private WebElement firstNameInput;

    @FindBy(id = "lastName")
    private WebElement lastNameInput;

    @FindByNg(model = "user.emailVerified")
    private Switch emailVerifiedSwitchToggle;

    @FindByNg(model = "user.enabled")
    private Switch userEnabledSwitchToggle;

    @FindBy(css = "input[class*='select2-input']")
    private WebElement requiredUserActionsInput;

    @FindBy(css = "input[class*='search']")
    private WebElement searchInput;

    @FindBy(css = "table[class*='table']")
    private WebElement dataTable;

    @FindBy(css = "button.kc-icon-search.ng-scope")
    private WebElement searchButton;

    @FindByJQuery("button[class='ng-binding btn btn-danger']")
    private WebElement deleteConfirmationButton;

    @FindByJQuery("button[kc-cancel] ")
    private WebElement cancel;

    @FindByJQuery(".alert")
    private FlashMessage flashMessage;

    @FindByJQuery("[name='userForm'] .btn-primary:visible")
    private WebElement saveUser;


    public void addUser(User user) {
        addUserButton.click();
        usernameInput.sendKeys(user.getUsername());
        emailInput.sendKeys(user.getEmail());
        if (user.isEnabled()) {
            userEnabledSwitchToggle.turnOn();
        } else {
            userEnabledSwitchToggle.turnOff();
        }
        if (user.isEmailVerified()) {
            emailVerifiedSwitchToggle.turnOn();
        } else {
            emailVerifiedSwitchToggle.turnOff();
        }
        requiredUserActionsInput.sendKeys("Update Password");
        driver.findElement(ByJQuery.selector(".select2-highlighted")).click();
        saveUser.click();
        flashMessage.waitUntilPresent();
    }

    public UserPageFragment findUser(String username) {
        searchInput.sendKeys(username);
        searchButton.click();
        return users.get(0);
    }

    public void activateUser(String username) {
        findUser(username).edit();
        userEnabledSwitchToggle.turnOff();
        userEnabledSwitchToggle.turnOn();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        saveUser.click();
        flashMessage.waitUntilPresent();
    }

//    public void editUser(User user) {
//        driver.findElement(linkText(user.getUserName())).click();
//        waitAjaxForElement(usernameInput);
//        usernameInput.sendKeys(user.getUserName());
//        emailInput.sendKeys(user.getEmail());
//        if (!user.isUserEnabled()) {
//            userEnabledSwitchToggle.click();
//        }
//        if (user.isEmailVerified()) {
//            emailVerifiedSwitchToggle.click();
//        }
//        requiredUserActionsInput.sendKeys(user.getRequiredUserActions());
//        driver.findElement(buttonPrimary).click();
//        flashMessage.waitUntilPresent();
//        assertTrue(flashMessage.getText(), flashMessage.isSuccess());
//    }
//
//    public void deleteUser(String username) {
//        searchInput.sendKeys(username);
//        searchButton.click();
//        driver.findElement(linkText(username)).click();
//        waitAjaxForElement(buttonDanger);
//        driver.findElement(buttonDanger).click();
//        waitAjaxForElement(deleteConfirmationButton);
//        deleteConfirmationButton.click();
//        flashMessage.waitUntilPresent();
//        assertTrue(flashMessage.getText(), flashMessage.isSuccess());
//    }


    public void cancel() {
        cancel.click();
    }


}
