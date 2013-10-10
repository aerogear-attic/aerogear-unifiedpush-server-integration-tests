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
package org.jboss.aerogear.unifiedpush.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.jayway.restassured.path.json.JsonPath;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.service.AndroidVariantService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AndroidVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class AndroidRegistrationTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String UPDATED_ANDROID_VARIANT_GOOGLE_KEY = "UPD_IDDASDASDSAQ__1";

    private final static String UPDATED_ANDROID_VARIANT_NAME = "UPD_AndroidVariant__1";

    private final static String UPDATED_ANDROID_VARIANT_DESC = "UPD_awesome variant__1";

    private final static String UPDATED_ANDROID_DEVICE_OS = "AndroidOS";

    private final static String UPDATED_ANDROID_DEVICE_TYPE = "AndroidPhone";

    private final static String UPDATED_ANDROID_DEVICE_OS_VERSION = "4.1.2";

    private final static String UPDATED_ANDROID_CLIENT_ALIAS = "upd_qa_android_1@aerogear";

    @Inject
    private AndroidVariantService androidVariantService;

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    private ClientInstallationService clientInstallationService;

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class, AndroidRegistrationTest.class);
    }

    @RunAsClient
    @Test
    @InSequence(100)
    public void registerAndroidVariantMissingGooglekey() {
        assertNotNull(getPushApplicationId());
        assertNotNull(getAuthCookies());
        AndroidVariant variant = AndroidVariantUtils.createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC, null,
                null, null, null);
        Response response = AndroidVariantUtils.registerAndroidVariant(getPushApplicationId(), variant, getAuthCookies(),
                getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.BAD_REQUEST.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(101)
    public void registerAndroidVariantMissingAuthCookies() {
        assertNotNull(getPushApplicationId());
        AndroidVariant variant = AndroidVariantUtils.createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC, null,
                null, null, ANDROID_VARIANT_GOOGLE_KEY);

        Response response = AndroidVariantUtils.registerAndroidVariant(getPushApplicationId(), variant,
                new HashMap<String, String>(), getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @InSequence(102)
    public void verifyRegistrations() {

        assertNotNull(pushAppService);
        assertNotNull(androidVariantService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());

        List<AndroidVariant> androidVariants = androidVariantService.findAllAndroidVariants();

        AndroidVariant androidVariant = androidVariants != null ? androidVariants.get(0) : null;

        assertNotNull(androidVariant);

        List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                androidVariant.getVariantID(), null, null, null);

        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));
        assertTrue(androidVariants != null && androidVariants.size() == 1 && androidVariant != null);
        assertEquals(ANDROID_VARIANT_GOOGLE_KEY, androidVariant.getGoogleKey());
        assertNotNull(deviceTokens);
        assertTrue(deviceTokens.contains(ANDROID_DEVICE_TOKEN) && deviceTokens.contains(ANDROID_DEVICE_TOKEN_2)
                && deviceTokens.contains(ANDROID_DEVICE_TOKEN_3));
    }

    @RunAsClient
    @Test
    @InSequence(103)
    public void updateAndroidVariant() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getAndroidVariantId());

        AndroidVariant variant = AndroidVariantUtils.createAndroidVariant(UPDATED_ANDROID_VARIANT_NAME,
                UPDATED_ANDROID_VARIANT_DESC, null, null, null, UPDATED_ANDROID_VARIANT_GOOGLE_KEY);

        Response response = AndroidVariantUtils.updateAndroidVariant(getPushApplicationId(), variant, getAuthCookies(),
                getAndroidVariantId(), getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @InSequence(104)
    public void verifyUpdate() {
        assertNotNull(androidVariantService);

        List<AndroidVariant> androidVariants = androidVariantService.findAllAndroidVariants();
        AndroidVariant androidVariant = androidVariants != null ? androidVariants.get(0) : null;

        assertNotNull(androidVariant);
        assertTrue(androidVariants != null && androidVariants.size() == 1 && androidVariant != null);
        assertEquals(UPDATED_ANDROID_VARIANT_NAME, androidVariant.getName());
        assertEquals(UPDATED_ANDROID_VARIANT_DESC, androidVariant.getDescription());
        assertEquals(UPDATED_ANDROID_VARIANT_GOOGLE_KEY, androidVariant.getGoogleKey());
    }

    @RunAsClient
    @Test
    @InSequence(105)
    public void updateAndroidInstallation() {
        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN,
                UPDATED_ANDROID_DEVICE_TYPE, UPDATED_ANDROID_DEVICE_OS, UPDATED_ANDROID_DEVICE_OS_VERSION,
                UPDATED_ANDROID_CLIENT_ALIAS, null, null);

        Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                androidInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(106)
    public void verifyAndroidInstallationUpdate() {

        assertNotNull(pushAppService);
        assertNotNull(androidVariantService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());

        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));

        List<AndroidVariant> androidVariants = androidVariantService.findAllAndroidVariants();
        assertTrue(androidVariants != null && androidVariants.size() == 1);

        AndroidVariant androidVariant = androidVariants != null ? androidVariants.get(0) : null;

        assertNotNull(androidVariant);

        InstallationImpl installation = clientInstallationService.findInstallationForVariantByDeviceToken(
                androidVariant.getVariantID(), ANDROID_DEVICE_TOKEN);

        assertNotNull(installation);
        assertEquals(UPDATED_ANDROID_DEVICE_TYPE, installation.getDeviceType());
        assertEquals(UPDATED_ANDROID_DEVICE_OS, installation.getOperatingSystem());
        assertEquals(UPDATED_ANDROID_DEVICE_OS_VERSION, installation.getOsVersion());
        assertEquals(UPDATED_ANDROID_CLIENT_ALIAS, installation.getAlias());
    }

    @RunAsClient
    @Test
    @InSequence(107)
    public void registerAndroidVariantWithWrongPushApplication() {
        assertNotNull(getAuthCookies());

        AndroidVariant variant = AndroidVariantUtils.createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC, null,
                null, null, ANDROID_VARIANT_GOOGLE_KEY);

        String nonExistentPushAppId = "this-will-never-exist";

        Response response = AndroidVariantUtils.registerAndroidVariant(nonExistentPushAppId, variant, getAuthCookies(),
                getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(108)
    public void listAllAndroidVariants() {
        assertNotNull(getAuthCookies());

        Response response = AndroidVariantUtils.listAllAndroidVariants(getPushApplicationId(), getAuthCookies(),
                getContextRoot());

        JsonPath body = response.getBody().jsonPath();
        List<Object> variants = body.getList("");

        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, variants.size());
    }

    @RunAsClient
    @Test
    @InSequence(109)
    public void findAndroidVariant() {
        assertNotNull(getAuthCookies());

        Response response = AndroidVariantUtils.findAndroidVariantById(getPushApplicationId(), getAndroidVariantId(),
                getAuthCookies(), getContextRoot());

        JsonPath body = response.getBody().jsonPath();

        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(getAndroidVariantId(), body.getString("variantID"));
        assertEquals(UPDATED_ANDROID_VARIANT_NAME, body.getString("name")); // TODO in iOS test this is non-updated value
    }

    @RunAsClient
    @Test
    @InSequence(110)
    public void findAndroidVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        Response response = AndroidVariantUtils.findAndroidVariantById(getPushApplicationId(), getAndroidVariantId()
                + "-invalidation", getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(111)
    public void updateAndroidVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        AndroidVariant variant = AndroidVariantUtils.createAndroidVariant(ANDROID_VARIANT_NAME + "-invalidation",
                ANDROID_VARIANT_DESC, null, null, null, null);

        Response response = AndroidVariantUtils.updateAndroidVariant(getPushApplicationId(), variant, getAuthCookies(),
                getAndroidVariantId() + "-invalidation", getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(112)
    public void removeAndroidVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        Response response = AndroidVariantUtils.deleteAndroidVariant(getPushApplicationId(), getAndroidVariantId()
                + "-invalidation", getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(113)
    public void unregisterInstallation() {
        Response response = InstallationUtils.unregisterInstallation(getAndroidVariantId(), getAndroidSecret(),
                ANDROID_DEVICE_TOKEN, getContextRoot());
        assertNotNull(response);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
    }

    @Test
    @InSequence(114)
    public void verifyInstallationRemoval() {
        InstallationImpl installation = clientInstallationService.findInstallationForVariantByDeviceToken(
                getAndroidVariantId(), ANDROID_DEVICE_TOKEN);
        assertNull(installation);
    }

    @RunAsClient
    @Test
    @InSequence(115)
    public void unauthorizedUnregisterInstallation() {
        Response response = InstallationUtils.unregisterInstallation("", getAndroidSecret(), ANDROID_DEVICE_TOKEN,
                getContextRoot());
        assertNotNull(response);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(1000)
    public void removeAndroidVariant() {
        assertNotNull(getAuthCookies());

        Response response = AndroidVariantUtils.deleteAndroidVariant(getPushApplicationId(), getAndroidVariantId(),
                getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.statusCode());
    }

}
