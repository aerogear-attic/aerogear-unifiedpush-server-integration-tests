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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.aerogear.unifiedpush.utils.SimplePushVariantUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

public class SimplePushRegistrationTest extends GenericUnifiedPushTest {

    @ArquillianResource
    private URL context;

    @Override
    protected String getContextRoot() {
        return context.toExternalForm();
    }

    private final static String UPDATED_SIMPLE_PUSH_DEVICE_TYPE = "upd_web";
    private final static String UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM = "UPD_MozillaOS";
    private final static String UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM_VERSION = "UPD_MozillaOS";
    private final static String[] UPDATED_SIMPLE_PUSH_CATEGORIES = { "12345" };
    private final static String UPDATED_SIMPLE_PUSH_ALIAS = "upd_qa_simple_push_1@aerogear";

    @Rule
    public CheckingExpectedException thrown = CheckingExpectedException.none();

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Test
    @InSequence(100)
    public void registerSimplePushVariantMissingAuthCookies() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        SimplePushVariantUtils.generateAndRegister(getRegisteredPushApplication(),
            Session.createInvalid(getContextRoot()));
    }

    @Test
    @InSequence(101)
    public void verifyRegistrations() {
        List<PushApplication> pushApplications = PushApplicationUtils.listAll(getSession());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());

        PushApplication pushApplication = pushApplications.iterator().next();
        PushApplicationUtils.checkEquality(getRegisteredPushApplication(), pushApplication);

        List<SimplePushVariant> simplePushVariants = SimplePushVariantUtils.listAll(pushApplication, getSession());
        assertNotNull(simplePushVariants);
        assertEquals(1, simplePushVariants.size());

        SimplePushVariant simplePushVariant = simplePushVariants.iterator().next();
        SimplePushVariantUtils.checkEquality(getRegisteredSimplePushVariant(), simplePushVariant);

        List<InstallationImpl> installations = InstallationUtils.listAll(simplePushVariant, getSession());
        assertNotNull(installations);
        assertEquals(getRegisteredSimplePushInstallations().size(), installations.size());

        for (InstallationImpl installation : installations) {
            boolean found = false;
            for (InstallationImpl registeredInstallation : getRegisteredSimplePushInstallations()) {
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
    @InSequence(102)
    public void updateSimplePushVariant() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.generate();

        getRegisteredSimplePushVariant().setName(simplePushVariant.getName());
        getRegisteredSimplePushVariant().setDescription(simplePushVariant.getDescription());

        SimplePushVariantUtils.update(getRegisteredSimplePushVariant(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(103)
    public void verifyUpdate() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.findById(getRegisteredSimplePushVariant().getVariantID(),
            getRegisteredPushApplication(), getSession());
        assertNotNull(simplePushVariant);
        SimplePushVariantUtils.checkEquality(getRegisteredSimplePushVariant(), simplePushVariant);
    }

    @Test
    @InSequence(104)
    public void updateSimplePushInstallation() {
        InstallationImpl installation = getRegisteredSimplePushInstallations().get(0);

        installation.setDeviceType(UPDATED_SIMPLE_PUSH_DEVICE_TYPE);
        installation.setOperatingSystem(UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM);
        installation.setOsVersion(UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM_VERSION);
        HashSet<String> categories = new HashSet<String>();
        for (String category : UPDATED_SIMPLE_PUSH_CATEGORIES) {
            categories.add(category);
        }
        installation.setCategories(categories);
        installation.setAlias(UPDATED_SIMPLE_PUSH_ALIAS);

        InstallationUtils.register(installation, getRegisteredSimplePushVariant(), getSession());
    }

    @Test
    @InSequence(105)
    public void verifySimplePushInstallationUpdate() {
        InstallationImpl registeredInstallation = getRegisteredSimplePushInstallations().get(0);

        InstallationImpl installation = InstallationUtils.findById(registeredInstallation.getId(),
            getRegisteredSimplePushVariant(), getSession());

        assertNotNull(installation);
        InstallationUtils.checkEquality(registeredInstallation, installation);
    }

    @Test
    @InSequence(106)
    public void registerSimplePushVariantWithWrongPushApplication() {
        PushApplication pushApplication = PushApplicationUtils.generate();

        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        SimplePushVariantUtils.generateAndRegister(pushApplication, getSession());
    }

    @Test
    @InSequence(107)
    public void listAllSimplePushVariants() {
        List<SimplePushVariant> simplePushVariants = SimplePushVariantUtils.listAll(getRegisteredPushApplication(),
            getSession());

        assertNotNull(simplePushVariants);
        assertEquals(1, simplePushVariants.size());
    }

    @Test
    @InSequence(108)
    public void findSimplePushVariant() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.findById(getRegisteredSimplePushVariant().getVariantID(),
            getRegisteredPushApplication(), getSession());

        assertNotNull(simplePushVariant);
        SimplePushVariantUtils.checkEquality(getRegisteredSimplePushVariant(), simplePushVariant);
    }

    @Test
    @InSequence(109)
    public void findSimplePushVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        SimplePushVariantUtils.findById(UUID.randomUUID().toString(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(110)
    public void updateSimplePushVariantWithInvalidId() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.generate();

        simplePushVariant.setVariantID(UUID.randomUUID().toString());

        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        SimplePushVariantUtils.update(simplePushVariant, getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(111)
    public void removeSimplePushVariantWithInvalidId() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.generate();

        simplePushVariant.setVariantID(UUID.randomUUID().toString());

        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        SimplePushVariantUtils.delete(simplePushVariant, getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(116)
    public void unregisterInstallation() {
        InstallationImpl registeredInstallation = getRegisteredSimplePushInstallations().get(0);

        InstallationUtils.unregister(registeredInstallation, getRegisteredSimplePushVariant(), getSession());
    }

    @Test
    @InSequence(117)
    public void verifyInstallationRemoval() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        InstallationUtils.findById(getRegisteredSimplePushInstallations().get(0).getId(), getRegisteredSimplePushVariant(),
            getSession());
    }

    @Test
    @InSequence(118)
    public void unauthorizedUnregisterInstallation() {
        SimplePushVariant generatedSimplePushVariant = SimplePushVariantUtils.generate();

        InstallationImpl registeredInstallation = getRegisteredSimplePushInstallations().get(0);

        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        InstallationUtils.unregister(registeredInstallation, generatedSimplePushVariant, getSession());
    }

    @Test
    @InSequence(1000)
    public void removeSimplePushVariant() {
        SimplePushVariantUtils.delete(getRegisteredSimplePushVariant(), getRegisteredPushApplication(), getSession());
    }
}
