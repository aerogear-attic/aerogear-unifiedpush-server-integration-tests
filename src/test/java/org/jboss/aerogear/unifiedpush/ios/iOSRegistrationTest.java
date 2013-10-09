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
package org.jboss.aerogear.unifiedpush.ios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.jayway.restassured.path.json.JsonPath;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.iOSVariantService;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.iOSVariantUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class iOSRegistrationTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private static final String UPDATED_IOS_VARIANT_NAME = "IOS_Variant__2";

    private static final String UPDATED_IOS_VARIANT_DESC = "awesome variant__2";

    private static final String UPDATED_IOS_DEVICE_OS = "IOS6";

    private static final String UPDATED_IOS_DEVICE_TYPE = "IPhone";

    private static final String UPDATED_IOS_DEVICE_OS_VERSION = "5";

    private static final String UPDATED_IOS_CLIENT_ALIAS = "upd_qa_iOS_1@aerogear";

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class, iOSRegistrationTest.class);
    }

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private iOSVariantService iosVariantService;

    @RunAsClient
    @Test
    @InSequence(100)
    public void updateIOSVariantPatchCase() {
        assertNotNull(getPushApplicationId());
        assertNotNull(getiOSVariantId());
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.TRUE, null, null,
                UPDATED_IOS_VARIANT_NAME, UPDATED_IOS_VARIANT_DESC);
        Response response = iOSVariantUtils.updateIOsVariantPatch(getPushApplicationId(), (iOSApplicationUploadForm) form,
                getAuthCookies(), getiOSVariantId(), getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @InSequence(101)
    public void verifyUpdatePatch() {
        assertNotNull(iosVariantService);

        List<iOSVariant> iOSVariants = iosVariantService.findAlliOSVariants();
        iOSVariant iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null;

        assertNotNull(iOSVariant);
        assertTrue(iOSVariants != null && iOSVariants.size() == 1 && iOSVariant != null);

        assertEquals(UPDATED_IOS_VARIANT_NAME, iOSVariant.getName());
        assertEquals(UPDATED_IOS_VARIANT_DESC, iOSVariant.getDescription());
        assertTrue(!iOSVariant.isProduction());
    }

    @RunAsClient
    @Test
    @InSequence(102)
    public void updateiOSVariant() {
        assertNotNull(getPushApplicationId());
        assertNotNull(getiOSVariantId());
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.TRUE,
                IOS_CERTIFICATE_PASS_PHRASE, null, IOS_VARIANT_NAME, IOS_VARIANT_DESC);
        Response response = iOSVariantUtils.updateIOsVariant(getPushApplicationId(), (iOSApplicationUploadForm) form,
                getAuthCookies(), IOS_CERTIFICATE_PATH, getiOSVariantId(), getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @InSequence(103)
    public void verifyiOSVariantUpdate() {
        assertNotNull(iosVariantService);

        List<iOSVariant> iOSVariants = iosVariantService.findAlliOSVariants();
        iOSVariant iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null;

        assertNotNull(iOSVariant);
        assertTrue(iOSVariants != null && iOSVariants.size() == 1 && iOSVariant != null);

        assertEquals(IOS_VARIANT_NAME, iOSVariant.getName());
        assertEquals(IOS_VARIANT_DESC, iOSVariant.getDescription());
        assertTrue(iOSVariant.isProduction());
    }

    @Test
    @InSequence(104)
    public void verifyRegistrations() {

        assertNotNull(pushAppService);
        assertNotNull(iosVariantService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());

        List<iOSVariant> iOSVariants = iosVariantService.findAlliOSVariants();

        iOSVariant iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null;

        assertNotNull(iOSVariant);

        List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                iOSVariant.getVariantID(), null, null, null);

        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));
        assertTrue(iOSVariants != null && iOSVariants.size() == 1 && iOSVariant != null);
        assertEquals(IOS_VARIANT_NAME, iOSVariant.getName());
        assertNotNull(deviceTokens);
        assertTrue(deviceTokens.contains(IOS_DEVICE_TOKEN) && deviceTokens.contains(IOS_DEVICE_TOKEN_2));
    }

    @RunAsClient
    @Test
    @InSequence(105)
    public void updateiOSInstallation() {
        assertNotNull(getiOSVariantId());
        assertNotNull(getiOSPushSecret());

        InstallationImpl iOSInstallation = InstallationUtils.createInstallation(IOS_DEVICE_TOKEN, UPDATED_IOS_DEVICE_TYPE,
                UPDATED_IOS_DEVICE_OS, UPDATED_IOS_DEVICE_OS_VERSION, UPDATED_IOS_CLIENT_ALIAS, null, null);

        Response response = InstallationUtils.registerInstallation(getiOSVariantId(), getiOSPushSecret(), iOSInstallation,
                getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(106)
    public void verifyiOSInstallationUpdate() {

        assertNotNull(pushAppService);
        assertNotNull(iosVariantService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());

        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));

        List<iOSVariant> iOSVariants = iosVariantService.findAlliOSVariants();
        assertTrue(iOSVariants != null && iOSVariants.size() == 1);

        iOSVariant iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null;

        assertNotNull(iOSVariant);

        InstallationImpl installation = clientInstallationService.findInstallationForVariantByDeviceToken(
                iOSVariant.getVariantID(), IOS_DEVICE_TOKEN);

        assertNotNull(installation);
        assertEquals(UPDATED_IOS_DEVICE_TYPE, installation.getDeviceType());
        assertEquals(UPDATED_IOS_DEVICE_OS, installation.getOperatingSystem());
        assertEquals(UPDATED_IOS_DEVICE_OS_VERSION, installation.getOsVersion());
        assertEquals(UPDATED_IOS_CLIENT_ALIAS, installation.getAlias());
    }

    @RunAsClient
    @Test
    @InSequence(107)
    public void registeriOSVariantWithWrongPushApplication() {
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.FALSE,
                IOS_CERTIFICATE_PASS_PHRASE, null, IOS_VARIANT_NAME, IOS_VARIANT_DESC);

        String nonExistentPushAppId = "this-will-never-exist";

        Response response = iOSVariantUtils.registerIOsVariant(nonExistentPushAppId, form, getAuthCookies(),
                IOS_CERTIFICATE_PATH, getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(108)
    public void registeriOSVariantWithWrongCertificate() {
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.FALSE,
                IOS_CERTIFICATE_PASS_PHRASE + "-invalidation", null, IOS_VARIANT_NAME, IOS_VARIANT_DESC);

        Response response = iOSVariantUtils.registerIOsVariant(getPushApplicationId(), form, getAuthCookies(),
                IOS_CERTIFICATE_PATH, getContextRoot());

        assertNotNull(response);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(109)
    public void listAlliOSVariants() {
        assertNotNull(getAuthCookies());

        Response response = iOSVariantUtils.listAlliOSVariants(getPushApplicationId(), getAuthCookies(), getContextRoot());

        JsonPath body = response.getBody().jsonPath();
        List variants = body.getList("");

        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, variants.size());
    }

    @RunAsClient
    @Test
    @InSequence(110)
    public void findiOSVariant() {
        assertNotNull(getAuthCookies());

        Response response = iOSVariantUtils.findiOSVariantById(getPushApplicationId(), getiOSVariantId(),
                getAuthCookies(), getContextRoot());

        JsonPath body = response.getBody().jsonPath();

        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(getiOSVariantId(), body.getString("variantID"));
        assertEquals(IOS_VARIANT_NAME, body.getString("name"));
    }

    @RunAsClient
    @Test
    @InSequence(111)
    public void findiOSVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        Response response = iOSVariantUtils.findiOSVariantById(getPushApplicationId(), getiOSVariantId() + "-invalidation",
                getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(112)
    public void updateiOSVariantPatchWithInvalidId() {
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.FALSE, null, null,
                UPDATED_IOS_VARIANT_NAME + "-invalidation", UPDATED_IOS_VARIANT_DESC);
        Response response = iOSVariantUtils.updateIOsVariantPatch(getPushApplicationId(), form, getAuthCookies(),
                getiOSVariantId() + "-invalidation", getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(113)
    public void updateiOSVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.FALSE,
                IOS_CERTIFICATE_PASS_PHRASE, null, IOS_VARIANT_NAME + "-invalidation", IOS_VARIANT_DESC);
        Response response = iOSVariantUtils.updateIOsVariant(getPushApplicationId(), form, getAuthCookies(),
                IOS_CERTIFICATE_PATH, getiOSVariantId() + "-invalidation", getContextRoot());

        assertNotNull(response);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(114)
    public void updateiOSVariantWithWrongCertificate() {
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.FALSE,
                IOS_CERTIFICATE_PASS_PHRASE + "-invalidation", null, IOS_VARIANT_NAME + "-invalidation", IOS_VARIANT_DESC);
        Response response = iOSVariantUtils.updateIOsVariant(getPushApplicationId(), form, getAuthCookies(),
                IOS_CERTIFICATE_PATH, getiOSVariantId(), getContextRoot());

        assertNotNull(response);

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode());
    }

    @RunAsClient
    @Test
    @InSequence(115)
    public void removeiOSVariantWithInvalidId() {
        assertNotNull(getAuthCookies());

        Response response = iOSVariantUtils.deleteiOSVariant(getPushApplicationId(), getiOSVariantId() + "-invalidation",
                getAuthCookies(), getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode());
    }

    // TODO this has to be the end of this test class, change it to be independent!!!
    @RunAsClient
    @Test
    @InSequence(116)
    public void removeiOSVariant() {
        assertNotNull(getAuthCookies());

        Response response = iOSVariantUtils.deleteiOSVariant(getPushApplicationId(), getiOSVariantId(), getAuthCookies(),
                getContextRoot());

        assertNotNull(response);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.statusCode());
    }

}
