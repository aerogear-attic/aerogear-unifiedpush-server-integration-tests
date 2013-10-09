package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class InstallationManagementEndpointTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private static String installationId = null;

    private static final String UPDATED_DEVICE_TOKEN = "MyDeviceToken_123";
    private static final String UPDATED_DEVICE_TYPE = "SmartPhone";
    private static final String UPDATED_OS = "AndroidOS";
    private static final String UPDATED_OS_VERSION = "10";
    private static final String UPDATED_ALIAS = "rh@qa.example.com";

    public static String getInstallationId() {
        return installationId;
    }

    public static void setInstallationId(String installationId) {
        InstallationManagementEndpointTest.installationId = installationId;
    }

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                InstallationManagementEndpointTest.class);
    }

    @RunAsClient
    @Test
    @InSequence(12)
    public void findInstallations() {
        Response androidInstallationsResponse = InstallationUtils.findInstallations(getAndroidVariantId(), getAuthCookies(),
                getContextRoot());
        assertNotNull(androidInstallationsResponse);
        assertEquals(androidInstallationsResponse.statusCode(), Status.OK.getStatusCode());

        List<Map<String, String>> androidInstallations = androidInstallationsResponse.getBody().jsonPath().getList("");
        assertNotNull(androidInstallations);
        assertEquals(androidInstallations.size(), 3);

        String installationId = androidInstallations.get(0).get("id");
        assertNotNull(installationId);
        setInstallationId(installationId);
    }

    @RunAsClient
    @Test
    @InSequence(13)
    public void findInstallation() {
        Response androidInstallationResponse = InstallationUtils.findInstallation(getAndroidVariantId(), installationId,
                getAuthCookies(), getContextRoot());
        assertNotNull(androidInstallationResponse);
        assertEquals(androidInstallationResponse.statusCode(), Status.OK.getStatusCode());

        Map<String, String> androidInstallation = androidInstallationResponse.getBody().jsonPath().get();
        assertNotNull(androidInstallation);
        assertEquals(androidInstallation.get("id"), installationId);
    }

    @RunAsClient
    @Test
    @InSequence(14)
    public void updateInstallation() {

        InstallationImpl updatedInstallation = InstallationUtils.createInstallation(UPDATED_DEVICE_TOKEN, UPDATED_DEVICE_TYPE,
                UPDATED_OS, UPDATED_OS_VERSION, UPDATED_ALIAS, null, null);

        Response updateResponse = InstallationUtils.updateInstallation(getAndroidVariantId(), installationId,
                updatedInstallation, getAuthCookies(), getContextRoot());
        assertNotNull(updateResponse);
        assertEquals(updateResponse.statusCode(), Status.NO_CONTENT.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(15)
    public void verifyUpdatedInstallation() {

        Response androidInstallationResponse = InstallationUtils.findInstallation(getAndroidVariantId(), installationId,
                getAuthCookies(), getContextRoot());
        assertNotNull(androidInstallationResponse);
        assertEquals(androidInstallationResponse.statusCode(), Status.OK.getStatusCode());

        Map<String, String> androidInstallation = androidInstallationResponse.getBody().jsonPath().get();
        assertNotNull(androidInstallation);
        assertEquals(installationId, androidInstallation.get("id"));
        assertEquals(UPDATED_DEVICE_TOKEN, androidInstallation.get("deviceToken"));
        assertEquals(UPDATED_DEVICE_TYPE, androidInstallation.get("deviceType"));
        assertEquals(UPDATED_OS, androidInstallation.get("operatingSystem"));
        assertEquals(UPDATED_OS_VERSION, androidInstallation.get("osVersion"));
        assertEquals(UPDATED_ALIAS, androidInstallation.get("alias"));
    }

    @RunAsClient
    @Test
    @InSequence(16)
    public void removeInstallation() {
        Response removeResponse = InstallationUtils.removeInstallation(getAndroidVariantId(), installationId, getAuthCookies(),
                getContextRoot());
        assertNotNull(removeResponse);
        assertEquals(removeResponse.statusCode(), Status.NO_CONTENT.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(17)
    public void verifyInstallationRemoval() {
        Response androidInstallationResponse = InstallationUtils.findInstallation(getAndroidVariantId(), installationId,
                getAuthCookies(), getContextRoot());
        assertNotNull(androidInstallationResponse);
        assertEquals(Status.NOT_FOUND.getStatusCode(), androidInstallationResponse.statusCode());
    }
}
