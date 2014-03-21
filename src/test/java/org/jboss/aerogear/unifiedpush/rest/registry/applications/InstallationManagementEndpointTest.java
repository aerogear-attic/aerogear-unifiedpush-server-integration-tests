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
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.List;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

public class InstallationManagementEndpointTest extends GenericUnifiedPushTest {

    @ArquillianResource
    private URL context;

    @Override
    protected String getContextRoot() {
        return context.toExternalForm();
    }

    private static String installationId = null;

    @Rule
    public CheckingExpectedException thrown = CheckingExpectedException.none();

    public static String getInstallationId() {
        return installationId;
    }

    public static void setInstallationId(String installationId) {
        InstallationManagementEndpointTest.installationId = installationId;
    }

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
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

        InstallationImpl installation = InstallationUtils.findById(registeredInstallation.getId(),
            getRegisteredAndroidVariant(),
            getSession());

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
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        InstallationUtils.findById(registeredInstallation.getId(), getRegisteredAndroidVariant(), getSession());
    }
}
