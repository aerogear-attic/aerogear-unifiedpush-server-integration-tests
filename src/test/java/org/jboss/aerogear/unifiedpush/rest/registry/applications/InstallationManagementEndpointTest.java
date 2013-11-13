package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import com.jayway.restassured.response.Response;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.ExpectedException;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public static String getInstallationId() {
        return installationId;
    }

    public static void setInstallationId(String installationId) {
        InstallationManagementEndpointTest.installationId = installationId;
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                InstallationManagementEndpointTest.class);
    }

    @Test
    @InSequence(12)
    public void findInstallations() {
        List<InstallationImpl> installations = InstallationUtils.listAll(getRegisteredAndroidVariant(), getSession());
        assertNotNull(installations);
        assertEquals(3, installations.size());
    }

    @Test
    @InSequence(13)
    public void findInstallation() {
        InstallationImpl registeredInstallation = getRegisteredAndroidInstallations().get(0);

        InstallationImpl installation = InstallationUtils.findById(registeredInstallation.getId(),
                getRegisteredAndroidVariant(), getSession());

        assertNotNull(installation);
        InstallationUtils.checkEquality(registeredInstallation, installation);

    }

    @Test
    @InSequence(14)
    public void updateInstallation() {
        // Let's generate token and alias
        InstallationImpl generatedInstallation = InstallationUtils.generateAndroid();

        InstallationImpl installation = getRegisteredAndroidInstallations().get(0);

        installation.setDeviceToken(generatedInstallation.getDeviceToken());
        installation.setAlias(generatedInstallation.getAlias());

        InstallationUtils.updateInstallation(installation, getRegisteredAndroidVariant(), getSession());
    }

    @Test
    @InSequence(15)
    public void verifyUpdatedInstallation() {
        InstallationImpl registeredInstallation = getRegisteredAndroidInstallations().get(0);

        InstallationImpl installation = InstallationUtils.findById(registeredInstallation.getId(), getRegisteredAndroidVariant(), getSession());

        assertNotNull(installation);
        InstallationUtils.checkEquality(registeredInstallation, installation);
    }

    @Test
    @InSequence(16)
    public void removeInstallation() {
        InstallationImpl registeredInstallation = getRegisteredAndroidInstallations().get(0);

        InstallationUtils.delete(registeredInstallation, getRegisteredAndroidVariant(), getSession());
    }

    @Test
    @InSequence(17)
    public void verifyInstallationRemoval() {
        InstallationImpl registeredInstallation = getRegisteredAndroidInstallations().get(0);
        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        InstallationUtils.findById(registeredInstallation.getId(), getRegisteredAndroidVariant(), getSession());
    }
}
