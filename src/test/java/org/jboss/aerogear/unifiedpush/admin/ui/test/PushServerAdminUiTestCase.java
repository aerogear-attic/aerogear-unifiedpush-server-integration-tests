/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.admin.ui.test;

import category.AdminUI;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.LoginPage;
import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.ReLoginPage;
import org.jboss.aerogear.unifiedpush.admin.ui.model.Installation;
import org.jboss.aerogear.unifiedpush.admin.ui.model.VariantType;
import org.jboss.aerogear.unifiedpush.admin.ui.page.*;
import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.*;
import org.jboss.aerogear.unifiedpush.admin.ui.utils.InstallationUtils;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jboss.aerogear.unifiedpush.admin.ui.utils.StringUtilities.isEmpty;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

@Category(AdminUI.class)
public class PushServerAdminUiTestCase extends AbstractPushServerAdminUiTest {

    @Override
    protected String getPagePath() {
        return "";
    }

    @FindBy(tagName = "aside")
    private Navigation navigation;

    @FindBy(tagName = "header")
    private Header header;

    @FindByJQuery(".modal-dialog")
    private ModalDialog modal;

    @Page
    private LoginPage loginPage;

    @Page
    private ReLoginPage reLoginPage;

    @Page
    private PasswordChangePage passwordChangePage;

    @Page
    private PushAppsPage pushAppsPage;

    @Page
    private PushAppEditPage pushAppEditPage;

    @Page
    private VariantsPage variantsPage;

    @Page
    private VariantRegistrationPage variantRegistrationPage;

    @Page
    private AndroidVariantEditPage androidVariantEditPage;

    @Page
    private iOSVariantEditPage iOSVariantEditPage;

    @Page
    private SimplePushVariantEditPage simplePushVariantEditPage;

    @Page
    private InstallationDetailsPage installationDetailsPage;

//    @Page
//    private ConfirmationBoxPage confirmationBoxPage;

    @Test//(expected = WebDriverException.class)
    @InSequence(1)
    public void testUnauthorizedAccess() {
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);

        // initialize page
        initializePageUrl();
        // navigate to push apps page
        navigateToURL(pushAppsPage.getPageURL());
        loginPage.waitForPage();
    }

    @Test//(expected = WebDriverException.class)
    @InSequence(2)
    public void login() {
        // initialize page
        initializePageUrl();
        // perform login
        loginPage.login(ADMIN_USERNAME, NEW_ADMIN_PASSWORD);
        // change password
//        passwordChangePage.changePassword(NEW_ADMIN_PASSWORD);
//        reLoginPage.login(ADMIN_USERNAME, NEW_ADMIN_PASSWORD);
    }

    @Test
    @InSequence(3)
    public void testPushAppRegistration() {
        navigation.goToApplications();
        // initially there shouldn't exist any push applications
        while (!pushAppsPage.getApplicationList().isEmpty()) {
            Application application = pushAppsPage.getApplicationList().get(0);
            application.remove();
            modal.waitForDialog();
            modal.confirmName(application.getName());
            modal.remove();
        }
        assertEquals("Initially there are 0 push apps", pushAppsPage.countApplications(), 0);
        // register a new push application
        pushAppsPage.waitForPage();
        pushAppsPage.pressCreateButton();
        modal.waitForDialog();
        // register a push application
        pushAppEditPage.registerNewPushApp(PUSH_APP_NAME, PUSH_APP_DESC);
        // navigate to push apps page
        final List<Application> pushAppsList = pushAppsPage.getApplicationList();
        // there should exist one push application
        assertEquals("There should exist 1 push app", pushAppsList.size(), 1);
        // The push app row should contain the right info name, desc, variants
        assertFalse(pushAppsList.isEmpty());
        assertEquals(PUSH_APP_NAME, pushAppsList.get(0).getName());
        assertEquals(PUSH_APP_DESC, pushAppsList.get(0).getDescription());
        assertEquals(0, pushAppsList.get(0).getVariantCount());
    }

    @Test
    @InSequence(4)
    public void testPushAppCancellation() {
        // there should exist one push application
        assertEquals("There should exist 1 push app", pushAppsPage.countApplications(), 1);
        pushAppsPage.pressCreateButton();
        modal.waitForDialog();
        // press cancel
        pushAppEditPage.cancel();
        // there should exist one push application
        assertEquals("There should still exist 1 push app", pushAppsPage.countApplications(), 1);
    }

    @Test
    @InSequence(5)
    public void testPushAppEdit() {
        // there should exist one push application
        assertEquals("There should still exist 1 push app", pushAppsPage.countApplications(), 1);
        // press the edit link
        pushAppsPage.getApplicationList().get(0).edit();
        // the push app details should be the expected ones
        modal.waitForDialog();
        assertEquals(PUSH_APP_NAME, pushAppEditPage.getName());
        assertEquals(PUSH_APP_DESC, pushAppEditPage.getDescription());
        // update the push application name
        pushAppEditPage.updatePushApp(UPDATED_PUSH_APP_NAME, UPDATED_PUSH_APP_DESC);
        final List<Application> pushAppsList = pushAppsPage.getApplicationList();
        // The push app row should contain the updated info name, desc
        assertFalse(pushAppsList.isEmpty());
        assertEquals(UPDATED_PUSH_APP_NAME, pushAppsList.get(0).getName());
        assertEquals(UPDATED_PUSH_APP_DESC, pushAppsList.get(0).getDescription());
        assertEquals(0, pushAppsList.get(0).getVariantCount());
    }

    @Test
    @InSequence(6)
    public void testAndroidVariantRegistration() {
        // there should exist one push application
        assertEquals("There should still exist 1 push app", pushAppsPage.countApplications(), 1);
        // press the variants link
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();
        // assert header title
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        // application id & master secret should exist
        assertTrue(!isEmpty(variantsPage.getApplicationId()) && !isEmpty(variantsPage.getMasterSecret()));
        // initially there are zero variants
        assertEquals("initially there are zero variants", variantsPage.countVariants(), 0);
        // add a new variant
        variantsPage.addVariant();
        // register new android variant
        modal.waitForDialog();
        variantRegistrationPage.registerAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC, ANDROID_VARIANT_PROJECT_NUMBER, ANDROID_VARIANT_GOOGLE_KEY);
        // one variant should exist
        assertEquals(variantsPage.getVariantList().size(), 1);
        Variant variant = variantsPage.findVariantRow(ANDROID_VARIANT_NAME);
        assertNotNull(variant);
        assertEquals(ANDROID_VARIANT_NAME, variant.getName());
//        assertEquals(ANDROID_VARIANT_PROJECT_NUMBER, variant.getProjectNumber());
        assertEquals(ANDROID_VARIANT_DESC, variant.getDescription());
        assertEquals(VariantType.ANDROID.getTypeName(), variant.getType());
        assertEquals(0, variant.getInstallationCount());
        // go to push apps page
        variantsPage.navigateToPushAppsPage();
        pushAppsPage.waitForPage();
        final List<Application> pushAppsList = pushAppsPage.getApplicationList();
        // The variant counter should be updated to 1
        assertFalse(pushAppsList.isEmpty());
        assertEquals(UPDATED_PUSH_APP_NAME, pushAppsList.get(0).getName());
        assertEquals(UPDATED_PUSH_APP_DESC, pushAppsList.get(0).getDescription());
        assertEquals(1, pushAppsList.get(0).getVariantCount());
    }

    @Test
    @InSequence(7)
    public void testAndroidVariantDetailsPage() {
        // press the variants link
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();
        Variant variant = variantsPage.findVariantRow(ANDROID_VARIANT_NAME);
        assertNotNull(variant);
        // click on a variant
        variant.showDetails();
        // variant id and secre should exist
        assertTrue(!isEmpty(variant.getSecret()) && !isEmpty(variant.getId()));
    }

    @Test
    @InSequence(8)
    public void testAndroidVariantEdit() {
        // there should exist one variant
        assertEquals("There should still exist 1 variant", variantsPage.countVariants(), 1);
        // press the variants edit link
        variantsPage.getVariantList().get(0).edit();
        // the variant details should be the expected ones
        modal.waitForDialog();
        assertEquals(ANDROID_VARIANT_NAME, androidVariantEditPage.getName());
        assertEquals(ANDROID_VARIANT_DESC, androidVariantEditPage.getDescription());
//        assertEquals(ANDROID_VARIANT_GOOGLE_KEY, androidVariantEditPage.getGoogleApiKey());
        // register new android variant
        androidVariantEditPage.updateVariant(UPDATED_ANDROID_VARIANT_NAME, UPDATED_ANDROID_VARIANT_DESC,
            UPDATED_ANDROID_VARIANT_GOOGLE_KEY);
        // one variant should exist
        Variant variant = variantsPage.getVariantList().get(0);
        assertNotNull(variant);
        assertEquals(UPDATED_ANDROID_VARIANT_NAME, variant.getName());
        assertEquals(UPDATED_ANDROID_VARIANT_DESC, variant.getDescription());
        assertEquals(VariantType.ANDROID.getTypeName(), variant.getType());
        assertEquals(0, variant.getInstallationCount());
        variant.edit();
        modal.waitForDialog();
        // the variant details should be the expected ones
        assertEquals(UPDATED_ANDROID_VARIANT_NAME, androidVariantEditPage.getName());
        assertEquals(UPDATED_ANDROID_VARIANT_DESC, androidVariantEditPage.getDescription());
        assertEquals(UPDATED_ANDROID_VARIANT_GOOGLE_KEY, androidVariantEditPage.getGoogleApiKey());
    }

    @Test
    @InSequence(9)
    public void testAndroidVariantCancellation() {
        // register a push application
        modal.cancel();
        // there should exist one variant
        assertEquals("There should still exist 1 variant", variantsPage.countVariants(), 1);
    }

    @Test
    @InSequence(10)
    public void testiOSVariantRegistration() {

        // go to push apps page
        variantsPage.navigateToPushAppsPage();
        pushAppsPage.waitForPage();
        // there should exist one push application
        assertEquals("There should still exist 1 push app", pushAppsPage.countApplications(), 1);
        // press the variants link
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();
        // assert header title
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        // application id & master secret should exist
        assertTrue(!isEmpty(variantsPage.getApplicationId()) && !isEmpty(variantsPage.getMasterSecret()));
        // initially there is one variant
        assertEquals("There should exist one variant", variantsPage.countVariants(), 1);
        // add a new variant
        variantsPage.addVariant();
        modal.waitForDialog();
        // register ios variant
        variantRegistrationPage.registeriOSVariant(IOS_VARIANT_NAME, IOS_VARIANT_DESC, IOS_CERT_PATH, IOS_CERT_PASSPHRASE,
            false);
        assertEquals("There should exist two variants", variantsPage.countVariants(), 2);
    }

    @Test
    @InSequence(11)
    public void testiOSVariantDetailsPage() {
        variantsPage.waitForPage();
        Variant variant = variantsPage.findVariantRow(IOS_VARIANT_NAME);
        assertNotNull(variant);
        // click on a variant
        variant.showDetails();
        // variant id and secret should exist
        assertTrue(!isEmpty(variant.getSecret()) && !isEmpty(variant.getId()));
    }

    @Test
    @InSequence(12)
    public void testiOSVariantEditPatch() {
        Variant variant = variantsPage.findVariantRow(IOS_VARIANT_NAME);
        assertNotNull(variant);
        // go to ios variant edit page
        variant.edit();
        // edit variant
        modal.waitForDialog();
        iOSVariantEditPage.updateVariant(UPDATED_IOS_VARIANT_NAME_PATCH, UPDATED_IOS_VARIANT_DESC, null, null);
        List<Variant> variantList = variantsPage.getVariantList();
        assertEquals(variantList.size(), 2);
        assertEquals(variantList.get(1).getName(), UPDATED_IOS_VARIANT_NAME_PATCH);
        assertEquals(variantList.get(1).getDescription(), UPDATED_IOS_VARIANT_DESC);
    }

    @Test
    @InSequence(13)
    public void testiOSVariantEdit() {
        Variant variant = variantsPage.findVariantRow(UPDATED_IOS_VARIANT_NAME_PATCH);
        assertNotNull(variant);
        // go to ios variant edit page
        variant.edit();
        modal.waitForDialog();
        // edit variant
        iOSVariantEditPage
            .updateVariant(UPDATED_IOS_VARIANT_NAME, UPDATED_IOS_VARIANT_DESC, IOS_CERT_PATH, IOS_CERT_PASSPHRASE);
        List<Variant> variantList = variantsPage.getVariantList();
        assertEquals(variantList.size(), 2);
        assertEquals(variantList.get(1).getName(), UPDATED_IOS_VARIANT_NAME);
        assertEquals(variantList.get(1).getDescription(), UPDATED_IOS_VARIANT_DESC);
    }

    @Test
    @InSequence(14)
    public void testiOSVariantCancellation() {
        Variant variant = variantsPage.findVariantRow(UPDATED_IOS_VARIANT_NAME);
        assertNotNull(variant);
        variant.edit();
        modal.waitForDialog();
        modal.cancel();
        // there should exist one variant
        assertEquals("There should still exist 2 variants", variantsPage.countVariants(), 2);
    }

    @Test
    @InSequence(15)
    @Ignore("Simple Push is not available")
    public void testSimplePushVariantRegistration() {
        // go to push apps page
        // assert header title
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        // application id & master secret should exist
        assertTrue(!isEmpty(variantsPage.getApplicationId()) && !isEmpty(variantsPage.getMasterSecret()));
        // initially there are two variants
        assertEquals("There should exist two variants", variantsPage.countVariants(), 2);
        // add a new variant
        variantsPage.addVariant();
        // register ios variant
        modal.waitForDialog();
        variantRegistrationPage.registerSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC);
        assertEquals("There should exist three variants", variantsPage.countVariants(), 3);
    }

    @Test
    @InSequence(16)
    @Ignore("Simple Push is not available")
    public void testSimplePushVariantDetailsPage() {
        Variant variant = variantsPage.findVariantRow(SIMPLE_PUSH_VARIANT_NAME);
        assertNotNull(variant);
        // click on a variant
        variant.showDetails();
        // variant id and secret should exist
        assertTrue(!isEmpty(variant.getSecret()) && !isEmpty(variant.getId()));
    }

    @Test
    @InSequence(17)
    @Ignore("Simple Push is not available")
    public void testSimplePushVariantEdit() {
        Variant variant = variantsPage.findVariantRow(SIMPLE_PUSH_VARIANT_NAME);
        assertNotNull(variant);
        // go to simple push variant edit page
        variant.edit();
        // edit variant
        modal.waitForDialog();
        simplePushVariantEditPage.updateVariant(UPDATED_SIMPLE_PUSH_VARIANT_NAME, UPDATED_SIMPLE_PUSH_VARIANT_DESC);
        variant = variantsPage.findVariantRow(SIMPLE_PUSH_VARIANT_NAME);
        assertNotNull(variant);
        assertEquals(variantsPage.getVariantList().size(), 3);
        assertEquals(variant.getName(), UPDATED_SIMPLE_PUSH_VARIANT_NAME);
        assertEquals(variant.getDescription(), UPDATED_SIMPLE_PUSH_VARIANT_DESC);
    }

    @Test
    @InSequence(18)
    public void testiOSVariantProductionRegistration() {

        // assert header title
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        // application id & master secret should exist
        assertTrue(!isEmpty(variantsPage.getApplicationId()) && !isEmpty(variantsPage.getMasterSecret()));
        // initially there is one variant
        assertEquals("There should exist one variant", variantsPage.countVariants(), 2);
        // add a new variant
        variantsPage.addVariant();
        // register ios variant
        modal.waitForDialog();
        variantRegistrationPage.registeriOSVariant(IOS_VARIANT_NAME_2, IOS_VARIANT_DESC, IOS_CERT_PATH, IOS_CERT_PASSPHRASE,
            true);
        assertEquals("There should exist four variants", variantsPage.countVariants(), 3);
        Variant variant = variantsPage.findVariantRow(IOS_VARIANT_NAME_2);
        assertNotNull(variant);
        // edit the last iOS variant
        variant.edit();
        modal.waitForDialog();
        // the variant details should be the expected ones
        assertEquals(IOS_VARIANT_NAME_2, iOSVariantEditPage.getName());
        assertEquals(IOS_VARIANT_DESC, iOSVariantEditPage.getDescription());
//        assertEquals(true, iOSVariantEditPage.isProd());
        modal.cancel();
        variantsPage.navigateToPushAppsPage();
        pushAppsPage.waitForPage();
    }

    @Test
    @InSequence(19)
    public void registerAndroidInstallations() {
        navigation.goToApplications();
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();
        // assert header title
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        // application id & master secret should exist
        final String pushAppId = variantsPage.getApplicationId();
        final String masterSecret = variantsPage.getMasterSecret();
        assertTrue(!isEmpty(pushAppId) && !isEmpty(masterSecret));
        Variant variant = variantsPage.findVariantRow(UPDATED_ANDROID_VARIANT_NAME);
        assertNotNull(variant);
        // click on a variant
        variant.showDetails();
        // variant id and secret should exist
        final String variantId = variant.getId();
        final String secret = variant.getSecret();
        assertTrue(!isEmpty(variantId) && !isEmpty(secret));
        // register installation
        Installation androidInstallation = new Installation(ANDROID_INSTALLATION_TOKEN_ID, ANDROID_INSTALLATION_DEVICE_TYPE,
            ANDROID_INSTALLATION_OS, ANDROID_INSTALLATION_ALIAS, null, null, null, null);
        InstallationUtils.registerInstallation(contextRoot.toExternalForm(), variantId, secret, androidInstallation);
        // register second installation
        Installation secondAndroidInstallation = new Installation(ANDROID_INSTALLATION_TOKEN_ID_2,
            ANDROID_INSTALLATION_DEVICE_TYPE, ANDROID_INSTALLATION_OS, ANDROID_INSTALLATION_ALIAS, null, null, null, null);
        InstallationUtils.registerInstallation(contextRoot.toExternalForm(), variantId, secret, secondAndroidInstallation);

        // select the push app
        navigation.goToApplications();
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();
//        // fins the android variant
//        variant = variantsPage.findVariantRow(UPDATED_ANDROID_VARIANT_NAME);
//        assertNotNull(variant);
//        assertEquals(variant.getInstallationCount(), 2);
//        // click on a variant
//        variant.installations();
//        // two installations should exist
//        List<Installation> installationList = installationsPage.getInstallationList();
//        assertTrue(installationList != null && installationList.size() == 2);
//        // the installations should have the right token ids
//        assertTrue(installationsPage.tokenIdExistsInList(ANDROID_INSTALLATION_TOKEN_ID, installationList)
//            && installationsPage.tokenIdExistsInList(ANDROID_INSTALLATION_TOKEN_ID_2, installationList));
//        // platform should be Android
//        assertEquals(ANDROID_PLATFORM, installationList.get(0).getPlatform());
//        assertEquals(ANDROID_PLATFORM, installationList.get(1).getPlatform());
//        // status should be enabled
//        assertEquals(INSTALLATION_STATUS_ENABLED, installationList.get(0).getStatus());
//        assertEquals(INSTALLATION_STATUS_ENABLED, installationList.get(1).getStatus());
//        int rowNum = installationsPage.findInstallationRow(ANDROID_INSTALLATION_TOKEN_ID);
//        assertNotEquals(rowNum, -1);
//        // check installation details
//        installationsPage.pressInstallationLink(rowNum);
//        Installation installationDetails = installationDetailsPage.getInstallationDetails();
//        assertNotNull(installationDetails);
//        assertEquals(installationDetails.getDeviceToken(), ANDROID_INSTALLATION_TOKEN_ID);
//        assertEquals(installationDetails.getDeviceType(), ANDROID_INSTALLATION_DEVICE_TYPE);
//        assertEquals(installationDetails.getPlatform(), ANDROID_INSTALLATION_OS);
//        assertEquals(installationDetails.getAlias(), ANDROID_INSTALLATION_ALIAS);
//        installationDetailsPage.pressToggleLink();
//        installationDetailsPage.navigateToVariantPage();
//        // status should have been changed
//        installationList = installationsPage.getInstallationList();
//        assertEquals(2, installationList.size());
//        rowNum = installationsPage.findInstallationRow(ANDROID_INSTALLATION_TOKEN_ID);
//        assertNotEquals(rowNum, -1);
//        assertEquals(INSTALLATION_STATUS_DISABLED, installationList.get(rowNum).getStatus());
//        installationsPage.navigateToVariantsPage();
    }

    @Test
    @InSequence(20)
    public void registeriOSInstallations() {
        // assert header title
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        // application id & master secret should exist
        final String pushAppId = variantsPage.getApplicationId();
        final String masterSecret = variantsPage.getMasterSecret();
        assertTrue(!isEmpty(pushAppId) && !isEmpty(masterSecret));

        Variant variant = variantsPage.findVariantRow(UPDATED_IOS_VARIANT_NAME);
        assertNotNull(variant);
        // click on a variant
        variant.showDetails();
        // variant id and secret should exist
        final String variantId = variant.getId();
        final String secret = variant.getSecret();
        assertTrue(!isEmpty(variantId) && !isEmpty(secret));
        // register installation
        Installation iosInstallation = new Installation(IOS_INSTALLATION_TOKEN_ID, IOS_INSTALLATION_DEVICE_TYPE,
            IOS_INSTALLATION_OS, IOS_INSTALLATION_ALIAS, null, null, null, null);
        InstallationUtils.registerInstallation(contextRoot.toExternalForm(), variantId, secret, iosInstallation);
        // register second installation
        Installation secondiOSInstallation = new Installation(IOS_INSTALLATION_TOKEN_ID_2, IOS_INSTALLATION_DEVICE_TYPE,
            IOS_INSTALLATION_OS, IOS_INSTALLATION_ALIAS, null, null, null, null);
        InstallationUtils.registerInstallation(contextRoot.toExternalForm(), variantId, secret, secondiOSInstallation);
        // go back to push app page
//        installationsPage.navigateToVariantsPage();
//        // fins the android variant
//        variant = variantsPage.findVariantRow(UPDATED_IOS_VARIANT_NAME);
//        assertNotNull(variant);
//        assertEquals(variant.getInstallationCount(), 2);
//        // click on a variant
//        variant.showDetails();
//        // two installations should exist
//        List<Installation> installationList = installationsPage.getInstallationList();
//        assertTrue(installationList != null && installationList.size() == 2);
//        // the installations should have the right token ids
//        assertTrue(installationsPage.tokenIdExistsInList(IOS_INSTALLATION_TOKEN_ID, installationList)
//            && installationsPage.tokenIdExistsInList(IOS_INSTALLATION_TOKEN_ID_2, installationList));
//        // platform should be IOS
//        assertEquals(IOS_PLATFORM, installationList.get(0).getPlatform());
//        assertEquals(IOS_PLATFORM, installationList.get(1).getPlatform());
//        // status should be enabled
//        assertEquals(INSTALLATION_STATUS_ENABLED, installationList.get(0).getStatus());
//        assertEquals(INSTALLATION_STATUS_ENABLED, installationList.get(1).getStatus());
//        int rowNum = installationsPage.findInstallationRow(IOS_INSTALLATION_TOKEN_ID);
//        assertNotEquals(rowNum, -1);
//        // check installation details
//        installationsPage.pressInstallationLink(rowNum);
//        Installation installationDetails = installationDetailsPage.getInstallationDetails();
//        assertNotNull(installationDetails);
//        assertEquals(installationDetails.getDeviceToken(), IOS_INSTALLATION_TOKEN_ID);
//        assertEquals(installationDetails.getDeviceType(), IOS_INSTALLATION_DEVICE_TYPE);
//        assertEquals(installationDetails.getPlatform(), IOS_INSTALLATION_OS);
//        assertEquals(installationDetails.getAlias(), IOS_INSTALLATION_ALIAS);
//        installationDetailsPage.pressToggleLink();
//        installationDetailsPage.navigateToVariantPage();
//        // status should have been changed
//        installationList = installationsPage.getInstallationList();
//        rowNum = installationsPage.findInstallationRow(IOS_INSTALLATION_TOKEN_ID);
//        assertNotEquals(rowNum, -1);
//        assertEquals(INSTALLATION_STATUS_DISABLED, installationList.get(rowNum).getStatus());
//        installationsPage.navigateToVariantsPage();
    }

    @Test
    @InSequence(21)
    @Ignore("Simple Push is not available")
    public void registerSimplePushInstallations() {
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        // application id & master secret should exist
        final String pushAppId = variantsPage.getApplicationId();
        final String masterSecret = variantsPage.getMasterSecret();
        assertTrue(!isEmpty(pushAppId) && !isEmpty(masterSecret));

        Variant variant = variantsPage.findVariantRow(UPDATED_SIMPLE_PUSH_VARIANT_NAME);
        assertNotNull(variant);
        // click on a variant
        variant.showDetails();
        // variant id and secret should exist
        final String variantId = variant.getId();
        final String secret = variant.getSecret();
        assertTrue(!isEmpty(variantId) && !isEmpty(secret));
        // register installation
        Installation spInstallation = new Installation(SIMPLE_PUSH_INSTALLATION_TOKEN_ID, SIMPLE_PUSH_INSTALLATION_DEVICE_TYPE,
            SIMPLE_PUSH_INSTALLATION_OS, SIMPLE_PUSH_INSTALLATION_ALIAS, null, null, SIMPLE_PUSH_ENDPOINT_URL_1,
            SIMPLE_PUSH_CATEGORY);
        InstallationUtils.registerInstallation(contextRoot.toExternalForm(), variantId, secret, spInstallation);
        // register second installation
        Installation secondSpInstallation = new Installation(SIMPLE_PUSH_INSTALLATION_TOKEN_ID_2,
            SIMPLE_PUSH_INSTALLATION_DEVICE_TYPE, SIMPLE_PUSH_INSTALLATION_OS, SIMPLE_PUSH_INSTALLATION_ALIAS, null, null,
            SIMPLE_PUSH_ENDPOINT_URL_2, SIMPLE_PUSH_CATEGORY);
        InstallationUtils.registerInstallation(contextRoot.toExternalForm(), variantId, secret, secondSpInstallation);
        // go back to variants page
//        installationsPage.navigateToVariantsPage();
//        // finds the android variant
//        variant = variantsPage.findVariantRow(UPDATED_SIMPLE_PUSH_VARIANT_NAME);
//        assertNotNull(variant);
//        assertEquals(variant.getInstallationCount(), 2);
//        // click on a variant
//        variant.showDetails();
//        // two installations should exist
//        List<Installation> installationList = installationsPage.getInstallationList();
//        assertTrue(installationList != null && installationList.size() == 2);
//        // the installations should have the right token ids
//        assertTrue(installationsPage.tokenIdExistsInList(SIMPLE_PUSH_INSTALLATION_TOKEN_ID, installationList)
//            && installationsPage.tokenIdExistsInList(SIMPLE_PUSH_INSTALLATION_TOKEN_ID_2, installationList));
//        // platform should be SimplePush
//        assertEquals(SIMPLE_PUSH_PLATFORM, installationList.get(0).getPlatform());
//        assertEquals(SIMPLE_PUSH_PLATFORM, installationList.get(1).getPlatform());
//        // status should be enabled
//        assertEquals(INSTALLATION_STATUS_ENABLED, installationList.get(0).getStatus());
//        assertEquals(INSTALLATION_STATUS_ENABLED, installationList.get(1).getStatus());
//        int rowNum = installationsPage.findInstallationRow(SIMPLE_PUSH_INSTALLATION_TOKEN_ID);
//        assertNotEquals(rowNum, -1);
//        // check installation details
//        installationsPage.pressInstallationLink(rowNum);
//        Installation installationDetails = installationDetailsPage.getInstallationDetails();
//        assertNotNull(installationDetails);
//        assertEquals(installationDetails.getDeviceToken(), SIMPLE_PUSH_INSTALLATION_TOKEN_ID);
//        assertEquals(installationDetails.getDeviceType(), SIMPLE_PUSH_INSTALLATION_DEVICE_TYPE);
//        assertEquals(installationDetails.getPlatform(), SIMPLE_PUSH_INSTALLATION_OS);
//        assertEquals(installationDetails.getAlias(), SIMPLE_PUSH_INSTALLATION_ALIAS);
//        installationDetailsPage.pressToggleLink();
//        installationDetailsPage.navigateToVariantPage();
//        // status should have been changed
//        installationList = installationsPage.getInstallationList();
//        rowNum = installationsPage.findInstallationRow(SIMPLE_PUSH_INSTALLATION_TOKEN_ID);
//        assertNotEquals(rowNum, -1);
//        assertEquals(INSTALLATION_STATUS_DISABLED, installationList.get(rowNum).getStatus());
//        installationsPage.navigateToPushAppsPage();
    }

    @Test
    @InSequence(22)
    public void testSecondPushAppRegistration() {

        navigation.goToApplications();
        // initially there shouldn't exist any push applications
        assertEquals("Initially there is 1 push app", pushAppsPage.countApplications(), 1);
        // register a new push application
        pushAppsPage.pressCreateButton();
        modal.waitForDialog();
        // register a push application
        pushAppEditPage.registerNewPushApp(SECOND_PUSH_APP_NAME, PUSH_APP_DESC);

        final List<Application> pushAppsList = pushAppsPage.getApplicationList();
        assertTrue(pushAppsPage.applicationExists(SECOND_PUSH_APP_NAME, PUSH_APP_DESC));
        // there should exist two push applications
        assertEquals("There should exist 2 push apps", pushAppsList.size(), 2);
    }

    @Test
    @InSequence(23)
    public void testPushAppRemoval() {
        pushAppsPage.findApplication(SECOND_PUSH_APP_NAME).remove();
        modal.waitForDialog();
        modal.confirmName(SECOND_PUSH_APP_NAME);
        modal.remove();
        // the deleted push app should not exist
        assertFalse(pushAppsPage.applicationExists(SECOND_PUSH_APP_NAME, PUSH_APP_DESC));
    }

    @Test
    @InSequence(24)
    public void testSecondAndroidVariantRegistration() {
        assertEquals("There should still exist 1 push app", pushAppsPage.countApplications(), 1);
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        assertTrue(!isEmpty(variantsPage.getApplicationId()) && !isEmpty(variantsPage.getMasterSecret()));
        assertEquals("there has to be already four variants registered", variantsPage.countVariants(), 3);
        // add the second Android variant
        variantsPage.addVariant();
        modal.waitForDialog();
        // register new (second) Android variant
        variantRegistrationPage.registerAndroidVariant(ANDROID_VARIANT_NAME_2, ANDROID_VARIANT_DESC_2, ANDROID_VARIANT_PROJECT_NUMBER,
            ANDROID_VARIANT_GOOGLE_KEY_2);
        // five variants should exist
        assertEquals(variantsPage.getVariantList().size(), 4);
        Variant variant = variantsPage.findVariantRow(ANDROID_VARIANT_NAME_2);
        assertNotNull(variant);
        assertEquals(ANDROID_VARIANT_NAME_2, variant.getName());
        assertEquals(ANDROID_VARIANT_DESC_2, variant.getDescription());
//        assertEquals(ANDROID_VARIANT_PROJECT_NUMBER, variant.getProjectNumber());
        assertEquals(VariantType.ANDROID.getTypeName(), variant.getType());
        assertEquals(0, variant.getInstallationCount());
        // go to push apps page
        variantsPage.navigateToPushAppsPage();
        pushAppsPage.waitForPage();
        final List<Application> pushAppsList = pushAppsPage.getApplicationList();
        // The variant counter should be updated to 5
        assertFalse(pushAppsList.isEmpty());
        assertEquals(4, pushAppsList.get(0).getVariantCount());
    }

    @Test
    @InSequence(25)
    @Ignore("Simple Push is not available")
    public void testSecondSimplePushVariantRegistration() {
        assertEquals("There should still exist 1 push app", pushAppsPage.countApplications(), 1);
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();
        assertTrue(variantsPage.getHeaderTitle().contains(UPDATED_PUSH_APP_NAME));
        assertTrue(!isEmpty(variantsPage.getApplicationId()) && !isEmpty(variantsPage.getMasterSecret()));
        assertEquals("there has to be already five variants registered", variantsPage.countVariants(), 5);
        // add the second SimplePush variant
        variantsPage.addVariant();
        modal.waitForDialog();
        // register it
        variantRegistrationPage.registerSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME_2, SIMPLE_PUSH_VARIANT_DESC_2);

        assertEquals(variantsPage.getVariantList().size(), 6);
        Variant variant = variantsPage.findVariantRow(SIMPLE_PUSH_VARIANT_NAME_2);
        assertNotNull(variant);
        assertEquals(SIMPLE_PUSH_VARIANT_NAME_2, variant.getName());
        assertEquals(SIMPLE_PUSH_VARIANT_DESC_2, variant.getDescription());

        variantsPage.navigateToPushAppsPage();
        // The variant counter should be updated to 6
        pushAppsPage.waitForPage();
        assertEquals(6, pushAppsPage.getApplicationList().get(0).getVariantCount());
    }

    @Test
    @InSequence(26)
    public void testVariantRemoval() {
        navigation.goToApplications();
        pushAppsPage.getApplicationList().get(0).goToVariants();
        variantsPage.waitForPage();

        Variant variant = variantsPage.findVariantRow(UPDATED_ANDROID_VARIANT_NAME);
        assertNotNull(variant);

        variant.remove();
        modal.waitForDialog();
        modal.confirmName(variant.getName());
        modal.remove();

        assertNull(variantsPage.findVariantRow(UPDATED_ANDROID_VARIANT_NAME));
    }

    @Test
    @InSequence(27)
    public void testLogout() {
        header.logout();
        loginPage.waitForPage();
    }

    /* -- Testing data section -- */

    private static final String ADMIN_USERNAME = "admin";

    private static final String DEFAULT_ADMIN_PASSWORD = "123";

    private static final String NEW_ADMIN_PASSWORD = "123";

    private static final String PUSH_APP_NAME = "MyApp";

    // awesome application in Japanese
    private static final String SECOND_PUSH_APP_NAME = "素晴らしいアプリケーション";

    private static final String PUSH_APP_DESC = "Awesome app!";

    private static final String UPDATED_PUSH_APP_NAME = "MyNewApp";

    private static final String UPDATED_PUSH_APP_DESC = "My new awesome app!";

    private static final String ANDROID_VARIANT_NAME = "MyAndroidVariant";

    private static final String ANDROID_VARIANT_DESC = "My awesome variant!";

    private static final String ANDROID_VARIANT_PROJECT_NUMBER = "123";

    private static final String ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ";

    private static final String ANDROID_VARIANT_NAME_2 = "MyAndroidVariant2";

    private static final String ANDROID_VARIANT_DESC_2 = "My awesome second variant!";

    private static final String ANDROID_VARIANT_GOOGLE_KEY_2 = "IDDASDASDSAQ2";

    private static final String UPDATED_ANDROID_VARIANT_NAME = "MyNewAndroidVariant";

    private static final String UPDATED_ANDROID_VARIANT_DESC = "My new awesome variant!";

    private static final String UPDATED_ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ__1";

    private static final String IOS_VARIANT_NAME = "MyIOSVariant";

    private static final String IOS_VARIANT_NAME_2 = "MyIOSVariant";

    private static final String IOS_VARIANT_DESC = "My awesome IOS variant!";

    private static final String IOS_CERT_PASSPHRASE = "aerogear";

    private static final String IOS_CERT_PATH = "src/test/resources/certs/qaAerogear.p12";

    private static final String UPDATED_IOS_VARIANT_NAME_PATCH = "MyNewIOSVariantPatch";

    private static final String UPDATED_IOS_VARIANT_NAME = "MyNewIOSVariant";

    private static final String UPDATED_IOS_VARIANT_DESC = "My new awesome IOS variant!";

    private static final String SIMPLE_PUSH_VARIANT_NAME = "MySimplePushVariant";

    private static final String SIMPLE_PUSH_VARIANT_DESC = "My awesome SimplePush variant!";

    private static final String SIMPLE_PUSH_VARIANT_NAME_2 = "MySimplePushVariant2";

    private static final String SIMPLE_PUSH_VARIANT_DESC_2 = "My awesome second SimplePush variant!";

    private static final String UPDATED_SIMPLE_PUSH_VARIANT_NAME = "MySimplePushVariant";

    private static final String UPDATED_SIMPLE_PUSH_VARIANT_DESC = "My awesome SimplePush variant!";

    private static final String ANDROID_INSTALLATION_TOKEN_ID = "20c10ebd22e15a55c0c1c12695c535d37435dcfd20c10ebd22e15a55c0c1c12695c535d37435dcfd20c10ebd22e15a55c0c1";

    private static final String ANDROID_INSTALLATION_TOKEN_ID_2 = "20c10ebd22e15a55c0c1c12695c535d37435dcfd20c10ebd22e15a55c0c1c12695c535d37435dcfd20c10ebd22e15a55c0c2";

    private static final String ANDROID_INSTALLATION_DEVICE_TYPE = "Phone";

    private static final String ANDROID_INSTALLATION_OS = "ANDROID";

    private static final String ANDROID_INSTALLATION_ALIAS = "qa@example.com";

    private static final String IOS_INSTALLATION_TOKEN_ID = "abcd123456abcd123456abcd123456abcd123456abcd123456abcd123456abcd";

    private static final String IOS_INSTALLATION_TOKEN_ID_2 = "abcd123456abcd123456abcd123456abcd123456abcd123456abcd123456abce";

    private static final String IOS_INSTALLATION_DEVICE_TYPE = "Phone";

    private static final String IOS_INSTALLATION_OS = "IOS";

    private static final String IOS_INSTALLATION_ALIAS = "qa@example.com";

    private static final String SIMPLE_PUSH_INSTALLATION_TOKEN_ID = "abcd123654";

    private static final String SIMPLE_PUSH_ENDPOINT_URL_1 = "http://localhost:7777/" + SIMPLE_PUSH_INSTALLATION_TOKEN_ID;

    private static final String SIMPLE_PUSH_INSTALLATION_TOKEN_ID_2 = "abcd654321";

    private static final String SIMPLE_PUSH_ENDPOINT_URL_2 = "http://localhost:7777/" + SIMPLE_PUSH_INSTALLATION_TOKEN_ID_2;

    private static final String SIMPLE_PUSH_INSTALLATION_DEVICE_TYPE = "WebPhone";

    private static final String SIMPLE_PUSH_INSTALLATION_OS = "MozillaOS";

    private static final String SIMPLE_PUSH_INSTALLATION_ALIAS = "qa@example.com";

    private static final String SIMPLE_PUSH_CATEGORY = "web";

    private static final String IOS_PLATFORM = "IOS";

    private static final String ANDROID_PLATFORM = "ANDROID";

    private static final String SIMPLE_PUSH_PLATFORM = "MozillaOS";

    private static final String INSTALLATION_STATUS_ENABLED = "Enabled";

    private static final String INSTALLATION_STATUS_DISABLED = "Disabled";

    /* -- Testing data section -- */
}
