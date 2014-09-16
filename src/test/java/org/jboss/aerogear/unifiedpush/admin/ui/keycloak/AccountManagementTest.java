package org.jboss.aerogear.unifiedpush.admin.ui.keycloak;

import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.model.Account;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.AccountPage;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.LoginPage;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.PasswordPage;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.ReLoginPage;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment.FlashMessage;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment.Navigation;
import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.Header;
import org.jboss.aerogear.unifiedpush.admin.ui.test.AbstractPushServerAdminUiTest;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.support.FindBy;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountManagementTest extends AbstractPushServerAdminUiTest {

    @FindByJQuery(".alert")
    private FlashMessage flashMessage;

    @FindByJQuery("body")
    private Navigation navigation;

    @FindBy(tagName = "header")
    private Header upsAdminUIHeader;

    @Page
    private AccountPage accountPage;

    @Page
    private PasswordPage passwordPage;

    @Page
    private LoginPage loginPage;

    @Page
    private ReLoginPage reLoginPage;
    
    private static String USERNAME = "admin";
    private static String PASSWORD = "123";
    private static String NEW_PASSWORD = "newpassword";
    private static String WRONG_PASSWORD = "wrongpassword";


    @Test
    @InSequence(0)
    public void loginPageTest() {
        driver.get(contextRoot.toString());

        loginPage.login("", "");
        assertEquals(loginPage.getMessage(), LoginPage.WRONG_PASSWORD_MESSAGE);

        loginPage.login(USERNAME, WRONG_PASSWORD);
        assertEquals(loginPage.getMessage(), LoginPage.WRONG_PASSWORD_MESSAGE);

        loginPage.login(USERNAME, PASSWORD);
        upsAdminUIHeader.accountManagement();
    }

    @Test
    @InSequence(1)
    public void passwordPageValidationTest() {
        navigation.password();
        passwordPage.save();
        flashMessage.waitUntilPresent();
        assertTrue(flashMessage.getText(), flashMessage.isError());

        passwordPage.setPassword(WRONG_PASSWORD, NEW_PASSWORD);
        passwordPage.save();
        flashMessage.waitUntilPresent();
        assertTrue(flashMessage.getText(), flashMessage.isError());

        passwordPage.setOldPasswordField(PASSWORD);
        passwordPage.setNewPasswordField("something");
        passwordPage.setConfirmField("something else");
        passwordPage.save();
        flashMessage.waitUntilPresent();
        assertTrue(flashMessage.getText(), flashMessage.isError());
    }

    @Test
    @InSequence(2)
    public void changePasswordTest() {
        navigation.password();
        passwordPage.setPassword(PASSWORD, NEW_PASSWORD);
        passwordPage.save();
        flashMessage.waitUntilPresent();
        assertTrue(flashMessage.getText(), flashMessage.isSuccess());
        navigation.signOut();
        loginPage.login(USERNAME, NEW_PASSWORD);
        navigation.password();
        passwordPage.setPassword(NEW_PASSWORD, PASSWORD);
        passwordPage.save();
        flashMessage.waitUntilPresent();
        assertTrue(flashMessage.getText(), flashMessage.isSuccess());
    }

    @Test
    @InSequence(3)
    public void accountPageTest() {
        navigation.account();
        Account adminAccount = accountPage.getAccount();
        assertEquals(adminAccount.getUsername(), USERNAME);
        adminAccount.setEmail("a@b");
        adminAccount.setFirstName("John");
        adminAccount.setLastName("Smith");
        accountPage.setAccount(adminAccount);
        accountPage.save();
        flashMessage.waitUntilPresent();
        assertTrue(flashMessage.getText(), flashMessage.isSuccess());

        navigation.signOut();
        loginPage.login(USERNAME, PASSWORD);

        navigation.account();
        assertEquals(adminAccount, accountPage.getAccount());
    }



    @Override
    protected String getPagePath() {
        return "";
    }
}
