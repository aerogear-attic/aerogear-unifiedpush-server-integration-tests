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

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class AndroidRegistrationTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String UPDATED_ANDROID_VARIANT_GOOGLE_KEY = "UPD_IDDASDASDSAQ__1";

    private final static String UPDATED_ANDROID_VARIANT_NAME = "UPD_AndroidVariant__1";

    private final static String UPDATED_ANDROID_VARIANT_DESC = "UPD_awesome variant__1";


    private final static String UPDATED_ANDROID_OPERATING_SYSTEM = "AndroidOS";
    private final static String UPDATED_ANDROID_DEVICE_TYPE = "AndroidPhone";
    private final static String UPDATED_ANDROID_OPERATING_SYSTEM_VERSION = "4.1.2";
    private final static String UPDATED_ANDROID_ALIAS = "upd_qa_android_1@aerogear";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                AndroidRegistrationTest.class);
    }

    @Test
    @InSequence(100)
    public void registerAndroidVariantMissingGooglekey() {
        thrown.expectUnexpectedResponseException(Status.BAD_REQUEST);
        AndroidVariantUtils.createAndRegister(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null,
                getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(101)
    public void registerAndroidVariantMissingAuthCookies() {
        thrown.expectUnexpectedResponseException(Status.UNAUTHORIZED);
        AndroidVariantUtils.generateAndRegister(getRegisteredPushApplication(),
                AuthenticationUtils.Session.createInvalid(getContextRoot()));
    }

    @Test
    @InSequence(102)
    public void verifyRegistrations() {

        List<PushApplication> pushApplications = PushApplicationUtils.listAll(getSession());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());

        PushApplication pushApplication = pushApplications.iterator().next();
        PushApplicationUtils.checkEquality(getRegisteredPushApplication(), pushApplication);

        List<AndroidVariant> androidVariants = AndroidVariantUtils.listAll(pushApplication, getSession());
        assertNotNull(androidVariants);
        assertEquals(1, androidVariants.size());

        AndroidVariant androidVariant = androidVariants.iterator().next();

        AndroidVariantUtils.checkEquality(getRegisteredAndroidVariant(), androidVariant);

        List<InstallationImpl> installations = InstallationUtils.listAll(androidVariant, getSession());
        assertNotNull(installations);
        assertEquals(getRegisteredAndroidInstallations().size(), installations.size());

        for (InstallationImpl installation : installations) {
            boolean found = false;
            for (InstallationImpl registeredInstallation : getRegisteredAndroidInstallations()) {
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
    public void updateAndroidVariant() {
        AndroidVariant androidVariant = AndroidVariantUtils.generate();

        getRegisteredAndroidVariant().setName(androidVariant.getName());
        getRegisteredAndroidVariant().setDescription(androidVariant.getDescription());
        getRegisteredAndroidVariant().setGoogleKey(androidVariant.getGoogleKey());

        AndroidVariantUtils.update(getRegisteredAndroidVariant(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(104)
    public void verifyUpdate() {
        AndroidVariant androidVariant = AndroidVariantUtils.findById(getRegisteredAndroidVariant().getVariantID(),
                getRegisteredPushApplication(), getSession());
        assertNotNull(androidVariant);
        AndroidVariantUtils.checkEquality(getRegisteredAndroidVariant(), androidVariant);
    }

    @Test
    @InSequence(105)
    public void updateAndroidInstallation() {
        InstallationImpl installation = getRegisteredAndroidInstallations().get(0);

        installation.setDeviceType(UPDATED_ANDROID_DEVICE_TYPE);
        installation.setAlias(UPDATED_ANDROID_ALIAS);
        installation.setOperatingSystem(UPDATED_ANDROID_OPERATING_SYSTEM);
        installation.setOsVersion(UPDATED_ANDROID_OPERATING_SYSTEM_VERSION);

        InstallationUtils.register(installation, getRegisteredAndroidVariant(), getContextRoot());
    }

    @Test
    @InSequence(106)
    public void verifyAndroidInstallationUpdate() {

        InstallationImpl registeredInstallation = getRegisteredAndroidInstallations().get(0);

        InstallationImpl installation = InstallationUtils.findById(registeredInstallation.getId(),
                getRegisteredAndroidVariant(), getSession());

        assertNotNull(installation);
        InstallationUtils.checkEquality(registeredInstallation, installation);
    }

    @Test
    @InSequence(107)
    public void registerAndroidVariantWithWrongPushApplication() {
        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        AndroidVariantUtils.generateAndRegister(PushApplicationUtils.generate(), getSession());
    }

    @Test
    @InSequence(108)
    public void listAllAndroidVariants() {
        List<AndroidVariant> androidVariants = AndroidVariantUtils.listAll(getRegisteredPushApplication(),
                getSession());

        assertNotNull(androidVariants);
        assertEquals(1, androidVariants.size());
    }

    @Test
    @InSequence(109)
    public void findAndroidVariant() {
        AndroidVariant androidVariant = AndroidVariantUtils.findById(getRegisteredAndroidVariant().getVariantID(),
                getRegisteredPushApplication(), getSession());
        AndroidVariantUtils.checkEquality(getRegisteredAndroidVariant(), androidVariant);
    }

    @Test
    @InSequence(110)
    public void findAndroidVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        AndroidVariantUtils.findById(UUID.randomUUID().toString(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(111)
    public void updateAndroidVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        AndroidVariantUtils.update(AndroidVariantUtils.generate(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(112)
    public void removeAndroidVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        AndroidVariantUtils.delete(AndroidVariantUtils.generate(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(113)
    public void unregisterInstallation() {
        InstallationUtils.unregister(getRegisteredAndroidInstallations().get(0), getRegisteredAndroidVariant(),
                getContextRoot());
    }

    @Test
    @InSequence(114)
    public void verifyInstallationRemoval() {
        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        InstallationUtils.findById(getRegisteredAndroidInstallations().get(0).getId(), getRegisteredAndroidVariant(),
                getSession());
    }

    @Test
    @InSequence(115)
    public void unauthorizedUnregisterInstallation() {
        AndroidVariant variant = AndroidVariantUtils.generate();
        thrown.expectUnexpectedResponseException(Status.UNAUTHORIZED);
        InstallationUtils.unregister(getRegisteredAndroidInstallations().get(1), variant, getContextRoot());
    }

    @Test
    @InSequence(1000)
    public void removeAndroidVariant() {
        AndroidVariantUtils.delete(getRegisteredAndroidVariant(), getRegisteredPushApplication(), getSession());
    }

}
