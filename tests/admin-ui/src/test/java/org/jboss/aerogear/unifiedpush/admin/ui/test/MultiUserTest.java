package org.jboss.aerogear.unifiedpush.admin.ui.test;

import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.model.User;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.*;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment.KCNavigation;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment.UserPageNavBar;
import org.jboss.aerogear.unifiedpush.admin.ui.page.DashboardPage;
import org.jboss.aerogear.unifiedpush.admin.ui.page.PushAppEditPage;
import org.jboss.aerogear.unifiedpush.admin.ui.page.PushAppsPage;
import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.*;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MultiUserTest extends AbstractPushServerAdminUiTest {

    public static final String CONSOLE_PATH = "admin/aerogear/console/index.html";
    @Page
    private UsersPage usersPage;

    @Page
    private KCNavigation keyCloakNavigation;

    @Page
    private Header header;

    @FindBy(tagName = "aside")
    private Navigation navigation;

    @Page
    private LoginPage loginPage;

    @Page
    private PasswordChangePage passwordChangePage;

    @Page
    private DashboardPage dashboardPage;

    @Page
    private PushAppsPage pushAppsPage;

    @Page
    private PushAppEditPage pushAppEditPage;

    @Page
    private AuthPage authPage;

    @Page
    private RoleMappingsPage roleMappingsPage;

    @Page
    private CredentialsPage credentialsPage;

    @FindByJQuery(".dropdown")
    private Dropdown dropdown;

    @FindByJQuery(".modal-dialog")
    private ModalDialog modal;

    @FindByJQuery("#content-area>ul.nav")
    private UserPageNavBar userPageNavBar;

    @Drone
    private WebDriver driver;

    private static String ADMIN_USERNAME = "admin";
    private static String PASSWORD = "123";
    private static String DEVELOPER_USERNAME = "developer";
    private static String DEVELOPER_PASSWORD = "developer";
    private static String NEW_USER_USERNAME = "newuser";
    private static String NEW_USER_PASSWORD = "newuser";
    public static final String ADMIN_APPLICATION = "admin's application";
    public static final String DEVELOPER_APPLICATION = "developer's application";
    public static final String NEW_USER_APPLICATION = "new user's application";


    @Test
    @InSequence(0)
    public void activateDeveloperTest() throws URISyntaxException {
        String ble = contextRoot.toExternalForm();
        driver.manage().timeouts().setScriptTimeout(15, TimeUnit.SECONDS);

        driver.get(authContextRoot.toURI().resolve(CONSOLE_PATH).toString());
        loginPage.waitForPage();
        loginPage.login(ADMIN_USERNAME, PASSWORD);
        if (passwordChangePage.isPagePresent()) {
            passwordChangePage.changePassword(PASSWORD);
        }
        keyCloakNavigation.waitForPage();
        keyCloakNavigation.users();
        usersPage.activateUser(DEVELOPER_USERNAME);

        dropdown.click("Sign Out");
        loginPage.waitForPage();
    }

    @Test
    @InSequence(1)
    public void createNewUserTest() throws URISyntaxException {
        driver.get(authContextRoot.toURI().resolve(CONSOLE_PATH).toString());
        loginPage.waitForPage();
        loginPage.login(ADMIN_USERNAME, PASSWORD);
        if (passwordChangePage.isPagePresent()) {
            passwordChangePage.changePassword(PASSWORD);
        }
        keyCloakNavigation.waitForPage();
        keyCloakNavigation.users();

        User newUser = new User();
        newUser.setUsername(NEW_USER_USERNAME);
        newUser.setEnabled(true);

        usersPage.addUser(newUser);

        userPageNavBar.roleMappings();
        roleMappingsPage.assignRole("developer");
        userPageNavBar.credentials();
        credentialsPage.setPassword(NEW_USER_PASSWORD);
        modal.waitForDialog();
        modal.remove();
        dropdown.click("Sign Out");
        loginPage.waitForPage();
    }

    @Test
    @InSequence(2)
    public void adminCreateApplicationTest() {
        driver.get(contextRoot.toExternalForm());
        loginPage.waitForPage();
        loginPage.login(ADMIN_USERNAME, PASSWORD);
        if (passwordChangePage.isPagePresent()) {
            passwordChangePage.changePassword(PASSWORD);
        }
        navigation.goToApplications();
        pushAppsPage.waitForPage();

        navigation.goToApplications();
        // initially there shouldn't exist any push applications
        while (!pushAppsPage.getApplicationList().isEmpty()) {
            Application application = pushAppsPage.getApplicationList().get(0);
            application.remove();
            modal.waitForDialog();
            modal.confirmName(application.getName());
            modal.remove();
        }

        pushAppsPage.pressCreateButton();
        pushAppEditPage.registerNewPushApp(ADMIN_APPLICATION, "");

        header.logout();
    }

    @Test
    @InSequence(3)
    public void newUserCreateApplicationTest() {
        driver.get(contextRoot.toExternalForm());
        loginPage.waitForPage();
        loginPage.login(NEW_USER_USERNAME, NEW_USER_PASSWORD);
        if (passwordChangePage.isPagePresent()) {
            passwordChangePage.changePassword(NEW_USER_PASSWORD);
        }
        navigation.goToApplications();
        pushAppsPage.waitForPage();

        navigation.goToApplications();


        pushAppsPage.pressCreateButton();
        pushAppEditPage.registerNewPushApp(NEW_USER_APPLICATION, "");

        header.logout();
    }

    @Test
    @InSequence(4)
    public void developerApplicationListTest() {
        driver.get(contextRoot.toExternalForm());
        loginPage.login(DEVELOPER_USERNAME, DEVELOPER_PASSWORD);
        if (passwordChangePage.isPagePresent()) {
            passwordChangePage.changePassword(DEVELOPER_PASSWORD);
        }

        navigation.goToApplications();
        pushAppsPage.waitForPage();

        assertEquals(0, pushAppsPage.countApplications());
        assertFalse(pushAppsPage.applicationExists(ADMIN_APPLICATION, ""));
    }

    @Test
    @InSequence(5)
    public void developerCreateAppTest() {
        pushAppsPage.pressCreateButton();
        pushAppEditPage.registerNewPushApp(DEVELOPER_APPLICATION, "");

        pushAppsPage.waitForPage();

        assertEquals(1, pushAppsPage.countApplications());

        header.logout();
    }

    @Test
    @InSequence(6)
    public void adminApplicationListTest() {
        driver.get(contextRoot.toExternalForm());
        loginPage.login(ADMIN_USERNAME, PASSWORD);
        if (passwordChangePage.isPagePresent()) {
            passwordChangePage.changePassword(PASSWORD);
        }

        navigation.goToApplications();
        pushAppsPage.waitForPage();

        assertEquals(3, pushAppsPage.countApplications());
        assertTrue(pushAppsPage.applicationExists(ADMIN_APPLICATION, ""));
        assertTrue(pushAppsPage.applicationExists(DEVELOPER_APPLICATION, ""));
        assertTrue(pushAppsPage.applicationExists(NEW_USER_APPLICATION, ""));
        header.logout();
        loginPage.waitForPage();
    }

    @Test
    @InSequence(7)
    public void newUserApplicationListTest() {
        driver.get(contextRoot.toExternalForm());
        loginPage.waitForPage();
        loginPage.login(NEW_USER_USERNAME, NEW_USER_PASSWORD);
        if (passwordChangePage.isPagePresent()) {
            passwordChangePage.changePassword(NEW_USER_PASSWORD);
        }

        navigation.goToApplications();
        pushAppsPage.waitForPage();

        assertEquals(1, pushAppsPage.countApplications());
        assertTrue(pushAppsPage.applicationExists(NEW_USER_APPLICATION, ""));

    }


    @Override
    protected String getPagePath() {
        return null;
    }
}
