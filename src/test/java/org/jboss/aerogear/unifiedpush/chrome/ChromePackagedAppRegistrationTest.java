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
package org.jboss.aerogear.unifiedpush.chrome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.ChromePackagedAppVariant;
import org.jboss.aerogear.test.model.ChromePackagedAppVariant;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.ChromePackagedAppVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.ChromePackagedAppVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.ExpectedException;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

public class ChromePackagedAppRegistrationTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String UPDATED_ANDROID_OPERATING_SYSTEM = "AndroidOS";
    private final static String UPDATED_ANDROID_DEVICE_TYPE = "AndroidPhone";
    private final static String UPDATED_ANDROID_OPERATING_SYSTEM_VERSION = "4.1.2";
    private final static String UPDATED_ANDROID_ALIAS = "upd_qa_android_1@aerogear";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Test
    @InSequence(100)
    public void registerChromePackagedAppVariantMissingGooglekey() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);
        ChromePackagedAppVariantUtils.createAndRegister(UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            null,
            null,
            null,
            getRegisteredPushApplication(),
            getSession());
    }

    @Test
    @InSequence(101)
    public void registerChromePackagedAppVariantMissingAuthCookies() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        ChromePackagedAppVariantUtils.generateAndRegister(getRegisteredPushApplication(),
            Session.createInvalid(getContextRoot()));
    }

    @Test
    @InSequence(102)
    public void verifyRegistrations() {

        List<PushApplication> pushApplications = PushApplicationUtils.listAll(getSession());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());

        PushApplication pushApplication = pushApplications.iterator().next();
        PushApplicationUtils.checkEquality(getRegisteredPushApplication(), pushApplication);

        List<ChromePackagedAppVariant> chromePackagedAppVariants = ChromePackagedAppVariantUtils.listAll(pushApplication,
            getSession());
        assertNotNull(chromePackagedAppVariants);
        assertEquals(1, chromePackagedAppVariants.size());

        ChromePackagedAppVariant chromePackagedAppVariant = chromePackagedAppVariants.iterator().next();

        ChromePackagedAppVariantUtils.checkEquality(getRegisteredChromePackagedAppVariant(), chromePackagedAppVariant);

        List<InstallationImpl> installations = InstallationUtils.listAll(chromePackagedAppVariant, getSession());
        assertNotNull(installations);
        assertEquals(getRegisteredAndroidInstallations().size(), installations.size());

        for (InstallationImpl installation : installations) {
            boolean found = false;
            for (InstallationImpl registeredInstallation : getRegisteredChromeInstallations()) {
                if (!installation.getId().equals(registeredInstallation.getId())) {
                    continue;
                }
                found = true;
                InstallationUtils.checkEquality(registeredInstallation, installation);
            }
            assertTrue("Unknown installation!", found);
        }
    }

    @Test
    @InSequence(103)
    public void updateChromePackagedAppVariant() {
        ChromePackagedAppVariant androidVariant = ChromePackagedAppVariantUtils.generate();

        getRegisteredChromePackagedAppVariant().setName(androidVariant.getName());
        getRegisteredChromePackagedAppVariant().setDescription(androidVariant.getDescription());

        ChromePackagedAppVariantUtils.update(getRegisteredChromePackagedAppVariant(),
            getRegisteredPushApplication(),
            getSession());
    }

    @Test
    @InSequence(104)
    public void verifyUpdate() {
        ChromePackagedAppVariant androidVariant = ChromePackagedAppVariantUtils.findById(getRegisteredChromePackagedAppVariant().getVariantID(),
            getRegisteredPushApplication(),
            getSession());
        assertNotNull(androidVariant);
        ChromePackagedAppVariantUtils.checkEquality(getRegisteredChromePackagedAppVariant(), androidVariant);
    }

    @Test
    @InSequence(105)
    public void updateAndroidInstallation() {
        InstallationImpl installation = getRegisteredAndroidInstallations().get(0);

        installation.setDeviceType(UPDATED_ANDROID_DEVICE_TYPE);
        installation.setAlias(UPDATED_ANDROID_ALIAS);
        installation.setOperatingSystem(UPDATED_ANDROID_OPERATING_SYSTEM);
        installation.setOsVersion(UPDATED_ANDROID_OPERATING_SYSTEM_VERSION);

        InstallationUtils.register(installation, getRegisteredChromePackagedAppVariant(), getSession());
    }

    @Test
    @InSequence(106)
    public void verifyAndroidInstallationUpdate() {

        InstallationImpl registeredInstallation = getRegisteredAndroidInstallations().get(0);

        InstallationImpl installation = InstallationUtils.findById(registeredInstallation.getId(),
            getRegisteredChromePackagedAppVariant(), getSession());

        assertNotNull(installation);
        InstallationUtils.checkEquality(registeredInstallation, installation);
    }

    @Test
    @InSequence(107)
    public void registerChromePackagedAppVariantWithWrongPushApplication() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ChromePackagedAppVariantUtils.generateAndRegister(PushApplicationUtils.generate(), getSession());
    }

    @Test
    @InSequence(108)
    public void listAllChromePackagedAppVariants() {
        List<ChromePackagedAppVariant> androidVariants = ChromePackagedAppVariantUtils.listAll(getRegisteredPushApplication(),
            getSession());

        assertNotNull(androidVariants);
        assertEquals(1, androidVariants.size());
    }

    @Test
    @InSequence(109)
    public void findChromePackagedAppVariant() {
        ChromePackagedAppVariant androidVariant = ChromePackagedAppVariantUtils.findById(getRegisteredChromePackagedAppVariant().getVariantID(),
            getRegisteredPushApplication(),
            getSession());
        ChromePackagedAppVariantUtils.checkEquality(getRegisteredChromePackagedAppVariant(), androidVariant);
    }

    @Test
    @InSequence(110)
    public void findChromePackagedAppVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ChromePackagedAppVariantUtils.findById(UUID.randomUUID().toString(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(111)
    public void updateChromePackagedAppVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ChromePackagedAppVariantUtils.update(ChromePackagedAppVariantUtils.generate(),
            getRegisteredPushApplication(),
            getSession());
    }

    @Test
    @InSequence(112)
    public void removeChromePackagedAppVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ChromePackagedAppVariantUtils.delete(ChromePackagedAppVariantUtils.generate(),
            getRegisteredPushApplication(),
            getSession());
    }

    @Test
    @InSequence(113)
    public void unregisterInstallation() {
        InstallationUtils.unregister(getRegisteredAndroidInstallations().get(0), getRegisteredChromePackagedAppVariant(),
            getSession());
    }

    @Test
    @InSequence(114)
    public void verifyInstallationRemoval() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        InstallationUtils.findById(getRegisteredAndroidInstallations().get(0).getId(), getRegisteredChromePackagedAppVariant(),
            getSession());
    }

    @Test
    @InSequence(115)
    public void unauthorizedUnregisterInstallation() {
        ChromePackagedAppVariant variant = ChromePackagedAppVariantUtils.generate();
        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        InstallationUtils.unregister(getRegisteredAndroidInstallations().get(1), variant, getSession());
    }

    @Test
    @InSequence(1000)
    public void removeChromePackagedAppVariant() {
        ChromePackagedAppVariantUtils.delete(getRegisteredChromePackagedAppVariant(),
            getRegisteredPushApplication(),
            getSession());
    }

}
