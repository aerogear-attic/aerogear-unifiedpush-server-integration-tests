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

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.ExpectedException;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.iOSVariantUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

public class iOSRegistrationTest extends GenericUnifiedPushTest {

    @ArquillianResource
    private URL context;

    @Override
    protected String getContextRoot() {
        return context.toExternalForm();
    }

    private static final String UPDATED_IOS_DEVICE_TYPE = "IPhone";
    private static final String UPDATED_IOS_OPERATING_SYSTEM = "IOS6";
    private static final String UPDATED_IOS_OPERATING_SYSTEM_VERSION = "5";
    private static final String UPDATED_IOS_ALIAS = "upd_qa_iOS_1@aerogear";

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @InSequence(100)
    public void updateIOSVariantPatchCase() {
        iOSVariant generatedVariant = iOSVariantUtils.generate(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE,
            false);

        getRegisteredIOSVariant().setName(generatedVariant.getName());
        getRegisteredIOSVariant().setDescription(generatedVariant.getDescription());

        iOSVariantUtils.updatePatch(getRegisteredIOSVariant(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(101)
    public void verifyUpdatePatch() {
        iOSVariant iOSVariant = iOSVariantUtils.findById(getRegisteredIOSVariant().getVariantID(),
            getRegisteredPushApplication(), getSession());

        assertNotNull(iOSVariant);
        iOSVariantUtils.checkEquality(getRegisteredIOSVariant(), iOSVariant);
    }

    @Test
    @InSequence(102)
    public void updateiOSVariant() {
        iOSVariant generatedVariant = iOSVariantUtils.generate(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE,
            false);

        getRegisteredIOSVariant().setName(generatedVariant.getName());
        getRegisteredIOSVariant().setDescription(generatedVariant.getDescription());

        iOSVariantUtils.update(getRegisteredIOSVariant(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(103)
    public void verifyiOSVariantUpdate() {
        iOSVariant iOSVariant = iOSVariantUtils.findById(getRegisteredIOSVariant().getVariantID(),
            getRegisteredPushApplication(), getSession());

        assertNotNull(iOSVariant);
        iOSVariantUtils.checkEquality(getRegisteredIOSVariant(), iOSVariant);
    }

    @Test
    @InSequence(104)
    public void verifyRegistrations() {
        List<PushApplication> pushApplications = PushApplicationUtils.listAll(getSession());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());

        PushApplication pushApplication = pushApplications.iterator().next();
        PushApplicationUtils.checkEquality(getRegisteredPushApplication(), pushApplication);

        List<iOSVariant> iOSVariants = iOSVariantUtils.listAll(pushApplication, getSession());
        assertNotNull(iOSVariants);
        assertEquals(1, iOSVariants.size());

        iOSVariant iOSVariant = iOSVariants.iterator().next();
        iOSVariantUtils.checkEquality(getRegisteredIOSVariant(), iOSVariant);

        List<InstallationImpl> installations = InstallationUtils.listAll(iOSVariant, getSession());
        assertNotNull(installations);
        assertEquals(getRegisteredAndroidInstallations().size(), installations.size());

        for (InstallationImpl installation : installations) {
            boolean found = false;
            for (InstallationImpl registeredInstallation : getRegisteredIOSInstallations()) {
                if (!installation.getId().equals(registeredInstallation.getId())) {
                    continue;
                }
                found = true;
                InstallationUtils.checkEquality(registeredInstallation, installation);
            }
            assertTrue("Unknown installation!", found);
        }
    }

    @RunAsClient
    @Test
    @InSequence(105)
    public void updateiOSInstallation() {
        InstallationImpl installation = getRegisteredIOSInstallations().get(0);

        installation.setDeviceType(UPDATED_IOS_DEVICE_TYPE);
        installation.setAlias(UPDATED_IOS_ALIAS);
        installation.setOperatingSystem(UPDATED_IOS_OPERATING_SYSTEM);
        installation.setOsVersion(UPDATED_IOS_OPERATING_SYSTEM_VERSION);

        InstallationUtils.register(installation, getRegisteredIOSVariant(), getSession());
    }

    @Test
    @InSequence(106)
    public void verifyiOSInstallationUpdate() {
        InstallationImpl registeredInstallation = getRegisteredIOSInstallations().get(0);

        InstallationImpl installation = InstallationUtils.findById(registeredInstallation.getId(),
            getRegisteredIOSVariant(), getSession());

        assertNotNull(installation);
        InstallationUtils.checkEquality(registeredInstallation, installation);
    }

    @Test
    @InSequence(107)
    public void registeriOSVariantWithWrongPushApplication() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        iOSVariantUtils.generateAndRegister(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE, false,
            PushApplicationUtils.generate(), getSession());
    }

    @Test
    @InSequence(108)
    public void registeriOSVariantWithWrongCertificate() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);
        iOSVariantUtils.generateAndRegister(IOS_CERTIFICATE_PATH, UUID.randomUUID().toString(), false,
            getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(109)
    public void listAlliOSVariants() {
        List<iOSVariant> iOSVariants = iOSVariantUtils.listAll(getRegisteredPushApplication(), getSession());

        assertNotNull(iOSVariants);
        assertEquals(1, iOSVariants.size());
    }

    @Test
    @InSequence(110)
    public void findiOSVariant() {
        iOSVariant iOSVariant = iOSVariantUtils.findById(getRegisteredIOSVariant().getVariantID(),
            getRegisteredPushApplication(), getSession());
        iOSVariantUtils.checkEquality(getRegisteredIOSVariant(), iOSVariant);
    }

    @Test
    @InSequence(111)
    public void findiOSVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        iOSVariantUtils.findById(UUID.randomUUID().toString(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(112)
    public void updateiOSVariantPatchWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        iOSVariantUtils.updatePatch(iOSVariantUtils.generate(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE,
            false), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(113)
    public void updateiOSVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        iOSVariantUtils.update(iOSVariantUtils.generate(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE, false),
            getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(114)
    public void updateiOSVariantWithWrongCertificate() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);
        iOSVariant iOSVariant = iOSVariantUtils.generate(IOS_CERTIFICATE_PATH, UUID.randomUUID().toString(), false);

        iOSVariant.setVariantID(getRegisteredIOSVariant().getVariantID());
        iOSVariant.setSecret(getRegisteredIOSVariant().getSecret());

        iOSVariantUtils.update(iOSVariant, getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(115)
    public void removeiOSVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        iOSVariantUtils.delete(iOSVariantUtils.generate(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE, false),
            getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(116)
    public void unregisterInstallation() {
        InstallationUtils.unregister(getRegisteredIOSInstallations().get(0), getRegisteredIOSVariant(),
            getSession());
    }

    @Test
    @InSequence(117)
    public void verifyInstallationRemoval() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        InstallationUtils.findById(getRegisteredIOSInstallations().get(0).getId(), getRegisteredIOSVariant(),
            getSession());
    }

    @Test
    @InSequence(118)
    public void unauthorizedUnregisterInstallation() {
        iOSVariant variant = iOSVariantUtils.generate(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE, false);
        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        InstallationUtils.unregister(getRegisteredIOSInstallations().get(1), variant, getSession());
    }

    @Test
    // TODO this has to be the end of this test class, change it to be independent!!!
        @InSequence(1000)
        public
        void removeiOSVariant() {
        iOSVariantUtils.delete(getRegisteredIOSVariant(), getRegisteredPushApplication(), getSession());
    }

}
