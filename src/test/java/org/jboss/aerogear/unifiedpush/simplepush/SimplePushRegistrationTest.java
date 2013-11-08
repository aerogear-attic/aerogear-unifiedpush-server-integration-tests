/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.simplepush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.SimplePushVariantUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class SimplePushRegistrationTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String UPDATED_SIMPLE_PUSH_VARIANT_NAME = "UPD_SimplePushVariant__1";

    private final static String UPDATED_SIMPLE_PUSH_VARIANT_DESC = "UPD_awesome variant__1";

    private final static String UPDATED_SIMPLE_PUSH_DEVICE_TYPE = "upd_web";

    private final static String UPDATED_SIMPLE_PUSH_DEVICE_OS = "UPD_MozillaOS";

    private final static String UPDATED_SIMPLE_PUSH_CATEGORY = "12345";

    private final static String UPDATED_SIMPLE_PUSH_CLIENT_ALIAS = "upd_qa_simple_push_1@aerogear";

    private final static String UPDATED_SIMPLE_PUSH_NETWORK_URL = "http://localhost:8081/endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN;

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class, SimplePushRegistrationTest.class);
    }

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    private ClientInstallationService clientInstallationService;

    @RunAsClient
    @Test
    @InSequence(100)
    public void registerSimplePushVariantMissingAuthCookies() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getAuthCookies());

        SimplePushVariant variant = SimplePushVariantUtils.createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME,
                SIMPLE_PUSH_VARIANT_DESC, null, null, null);
        Response response = SimplePushVariantUtils.registerSimplePushVariant(getPushApplicationId(), variant,
                new HashMap<String, String>(), getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @InSequence(101)
    public void verifyRegistrations() {

        assertNotNull(pushAppService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());

        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));
        
        PushApplication pushApp = pushApps.iterator().next();
        
        Set<SimplePushVariant> simplePushVariants = pushApp.getSimplePushVariants();
        SimplePushVariant simplePushVariant = simplePushVariants != null ? simplePushVariants.iterator().next() : null;

        List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                simplePushVariant.getVariantID(), null, null, null);

        

        assertTrue(simplePushVariants != null && simplePushVariants.size() == 1 && simplePushVariant != null);

        assertEquals(SIMPLE_PUSH_VARIANT_NAME, simplePushVariant.getName());

        assertNotNull(deviceTokens);

        assertTrue(deviceTokens.contains(SIMPLE_PUSH_DEVICE_TOKEN));
    }

    @RunAsClient
    @Test
    @InSequence(102)
    public void updateSimplePushVariant() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getSimplePushVariantId());

        SimplePushVariant variant = SimplePushVariantUtils.createSimplePushVariant(UPDATED_SIMPLE_PUSH_VARIANT_NAME,
                UPDATED_SIMPLE_PUSH_VARIANT_DESC, null, null, null);

        Response response = SimplePushVariantUtils.updateSimplePushVariant(getPushApplicationId(), variant, getAuthCookies(),
                getSimplePushVariantId(), getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @InSequence(103)
    public void verifyUpdate() {
        
        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils.getAdminLoginName());
        
        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));
        
        PushApplication pushApp = pushApps.iterator().next();
        
        Set<SimplePushVariant> simplePushVariants = pushApp.getSimplePushVariants();
        assertTrue(simplePushVariants != null && simplePushVariants.size() == 1);

        SimplePushVariant simplePushVariant = simplePushVariants != null ? simplePushVariants.iterator().next() : null;
        assertNotNull(simplePushVariant);
        assertEquals(UPDATED_SIMPLE_PUSH_VARIANT_NAME, simplePushVariant.getName());
        assertEquals(UPDATED_SIMPLE_PUSH_VARIANT_DESC, simplePushVariant.getDescription());
    }

    @RunAsClient
    @Test
    @InSequence(104)
    public void updateSimplePushInstallation() {
        assertNotNull(getSimplePushVariantId());
        assertNotNull(getSimplePushSecret());
        InstallationImpl simplePushInstallation = InstallationUtils.createInstallation(SIMPLE_PUSH_DEVICE_TOKEN,
                UPDATED_SIMPLE_PUSH_DEVICE_TYPE, UPDATED_SIMPLE_PUSH_DEVICE_OS, "", UPDATED_SIMPLE_PUSH_CLIENT_ALIAS,
                UPDATED_SIMPLE_PUSH_CATEGORY, UPDATED_SIMPLE_PUSH_NETWORK_URL);

        Response response = InstallationUtils.registerInstallation(getSimplePushVariantId(), getSimplePushSecret(),
                simplePushInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(105)
    public void verifySimplePushInstallationUpdate() {

        assertNotNull(pushAppService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());
        assertTrue(pushApps != null && pushApps.size() == 1);
        assertTrue(PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));

        PushApplication pushApp = pushApps.iterator().next();
        
        Set<SimplePushVariant> simplePushVariants = pushApp.getSimplePushVariants();
        assertTrue(simplePushVariants != null && simplePushVariants.size() == 1);

        SimplePushVariant simplePushVariant = simplePushVariants != null ? simplePushVariants.iterator().next() : null;
        assertNotNull(simplePushVariant);

        InstallationImpl installation = clientInstallationService.findInstallationForVariantByDeviceToken(
                simplePushVariant.getVariantID(), SIMPLE_PUSH_DEVICE_TOKEN);

        assertNotNull(installation);
        assertEquals(UPDATED_SIMPLE_PUSH_DEVICE_TYPE, installation.getDeviceType());
        assertEquals(UPDATED_SIMPLE_PUSH_DEVICE_OS, installation.getOperatingSystem());

        assertEquals(UPDATED_SIMPLE_PUSH_CLIENT_ALIAS, installation.getAlias());
        assertEquals(UPDATED_SIMPLE_PUSH_NETWORK_URL, installation.getSimplePushEndpoint());

        assertFalse("Device categories are not empty", installation.getCategories().isEmpty());
        assertEquals("Device categories contains updated category", UPDATED_SIMPLE_PUSH_CATEGORY, installation.getCategories().iterator().next());
    }

    @RunAsClient
    @Test
    @InSequence(106)
    public void registerSimplePushVariantWithWrongPushApplication() {
        assertNotNull(getAuthCookies());

        SimplePushVariant variant = SimplePushVariantUtils.createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME,
                SIMPLE_PUSH_VARIANT_DESC, null, null, null);

        String nonExistentPushAppId = "this-will-never-exist";

        Response response = SimplePushVariantUtils.registerSimplePushVariant(nonExistentPushAppId, variant, getAuthCookies(),
                getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(107)
    public void listAllSimplePushVariants() {
        assertNotNull(getAuthCookies());

        Response response = SimplePushVariantUtils.listAllSimplePushVariants(getPushApplicationId(), getAuthCookies(),
                getContextRoot());
        JsonPath body = response.getBody().jsonPath();
        List<Object> variants = body.getList("");

        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, variants.size());
    }

    @RunAsClient
    @Test
    @InSequence(108)
    public void findSimplePushVariant() {
        assertNotNull(getAuthCookies());

        Response response = SimplePushVariantUtils.findSimplePushVariantById(getPushApplicationId(), getSimplePushVariantId(),
                getAuthCookies(), getContextRoot());
        JsonPath body = response.getBody().jsonPath();

        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(getSimplePushVariantId(), body.getString("variantID"));
        assertEquals(UPDATED_SIMPLE_PUSH_VARIANT_NAME, body.getString("name"));
    }

    @RunAsClient
    @Test
    @InSequence(109)
    public void findSimplePushVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        Response response = SimplePushVariantUtils.findSimplePushVariantById(getPushApplicationId(), getSimplePushVariantId()
                + "-invalidation", getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(110)
    public void updateSimplePushVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        SimplePushVariant variant = SimplePushVariantUtils.createSimplePushVariant(UPDATED_SIMPLE_PUSH_VARIANT_NAME
                + "-invalidation", UPDATED_SIMPLE_PUSH_VARIANT_DESC, null, null, null);

        Response response = SimplePushVariantUtils.updateSimplePushVariant(getPushApplicationId(), variant, getAuthCookies(),
                getSimplePushVariantId() + "invalidation", getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(111)
    public void removeSimplePushVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        Response response = SimplePushVariantUtils.deleteSimplePushVariant(getPushApplicationId(), getSimplePushVariantId()
                + "-invalidation", getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(116)
    public void unregisterInstallation() {
        Response response = InstallationUtils.unregisterInstallation(getSimplePushVariantId(), getSimplePushSecret(),
                SIMPLE_PUSH_DEVICE_TOKEN, getContextRoot());
        assertNotNull(response);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
    }

    @Test
    @InSequence(117)
    public void verifyInstallationRemoval() {
        InstallationImpl installation = clientInstallationService.findInstallationForVariantByDeviceToken(
                getSimplePushVariantId(), SIMPLE_PUSH_DEVICE_TOKEN);
        assertNull(installation);
    }

    @RunAsClient
    @Test
    @InSequence(118)
    public void unauthorizedUnregisterInstallation() {
        Response response = InstallationUtils.unregisterInstallation("", getSimplePushSecret(), SIMPLE_PUSH_DEVICE_TOKEN,
                getContextRoot());
        assertNotNull(response);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(1000)
    public void removeSimplePushVariant() {
        assertNotNull(getAuthCookies());

        Response response = SimplePushVariantUtils.deleteSimplePushVariant(getPushApplicationId(), getSimplePushVariantId(),
                getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.statusCode());
    }
}
