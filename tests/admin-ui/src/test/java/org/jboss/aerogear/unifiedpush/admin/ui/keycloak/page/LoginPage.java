package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.aerogear.unifiedpush.admin.ui.utils.WebElementUtils.clearNfill;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class LoginPage {
    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "kc-login")
    private WebElement submitButton;

    @FindBy(id = "kc-header")
    private WebElement headerTitle;

    @FindByJQuery(".kc-feedback-text")
    private WebElement message;

    @Drone
    private WebDriver driver;

    private final static String TITLE = "LOG IN TO UNIFIEDPUSH SERVER";

    private final static String PAGE_URL = "#/login";

    public final static String WRONG_PASSWORD_MESSAGE = "Invalid username or password.";

    public String getHeaderTitle() {
        return headerTitle.getText().trim();
    }

    public String getExpectedTitle() {
        return TITLE;
    }

    public String getMessage() {
        return message.getText();
    }

    public void login(String username, String password) {
        waitModel().until().element(usernameField).is().present();
        clearNfill(usernameField, username);
        clearNfill(passwordField, password);
        submitButton.click();
    }

    public void waitForPage() {
        waitModel().until().element(submitButton).is().present();
    }

    public String getPageURL() {
        return PAGE_URL;
    }
}
