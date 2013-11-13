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

import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class SimplePushRegistrationTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String UPDATED_SIMPLE_PUSH_VARIANT_NAME = "UPD_SimplePushVariant__1";
    private final static String UPDATED_SIMPLE_PUSH_VARIANT_DESC = "UPD_awesome variant__1";

    private final static String UPDATED_SIMPLE_PUSH_DEVICE_TYPE = "upd_web";
    private final static String UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM = "UPD_MozillaOS";
    private final static String UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM_VERSION = "UPD_MozillaOS";
    private final static String[] UPDATED_SIMPLE_PUSH_CATEGORIES = { "12345" };
    private final static String UPDATED_SIMPLE_PUSH_ALIAS = "upd_qa_simple_push_1@aerogear";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                SimplePushRegistrationTest.class);
    }

    @Inject
    private PushApplicationService pushApplicationService;

    @Inject
    private ClientInstallationService clientInstallationService;

    @RunAsClient
    @Test
    @InSequence(100)
    public void registerSimplePushVariantMissingAuthCookies() {
        thrown.expectUnexpectedResponseException(Status.UNAUTHORIZED);
        SimplePushVariantUtils.generateAndRegister(getRegisteredPushApplication(),
                AuthenticationUtils.Session.createInvalid(getContextRoot()));
    }

    @Test
    @InSequence(101)
    public void verifyRegistrations() {

        assertNotNull(pushApplicationService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApplications = pushApplicationService.findAllPushApplicationsForDeveloper(
                getSession().getLoginName());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());
        assertTrue(PushApplicationUtils.nameExistsInList(getRegisteredPushApplication().getName(), pushApplications));

        PushApplication pushApplication = pushApplications.iterator().next();

        Set<SimplePushVariant> simplePushVariants = pushApplication.getSimplePushVariants();
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
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.generate();

        getRegisteredSimplePushVariant().setName(simplePushVariant.getName());
        getRegisteredSimplePushVariant().setDescription(simplePushVariant.getDescription());

        SimplePushVariantUtils.update(getRegisteredSimplePushVariant(), getRegisteredPushApplication(), getSession());
    }

    @Test
    @InSequence(103)
    public void verifyUpdate() {
        List<PushApplication> pushApplications = pushApplicationService.findAllPushApplicationsForDeveloper(
                getSession().getLoginName());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());
        assertTrue(PushApplicationUtils.nameExistsInList(getRegisteredPushApplication().getName(), pushApplications));

        PushApplication pushApplication = pushApplications.iterator().next();

        Set<SimplePushVariant> simplePushVariants = pushApplication.getSimplePushVariants();
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

        InstallationUtils.register(installation, getRegisteredSimplePushVariant(), getContextRoot());
    }

    @Test
    @InSequence(105)
    public void verifySimplePushInstallationUpdate() {

        assertNotNull(pushApplicationService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApplications = pushApplicationService.findAllPushApplicationsForDeveloper(
                getSession().getLoginName());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());
        assertTrue(PushApplicationUtils.nameExistsInList(getRegisteredPushApplication().getName(), pushApplications));

        PushApplication pushApplication = pushApplications.iterator().next();

        Set<SimplePushVariant> simplePushVariants = pushApplication.getSimplePushVariants();
        SimplePushVariant simplePushVariant = simplePushVariants != null ? simplePushVariants.iterator().next() : null;
        assertNotNull(simplePushVariant);

        InstallationImpl installation = clientInstallationService.findInstallationForVariantByDeviceToken(
                simplePushVariant.getVariantID(), SIMPLE_PUSH_DEVICE_TOKEN);

        assertNotNull(installation);
        assertEquals(UPDATED_SIMPLE_PUSH_DEVICE_TYPE, installation.getDeviceType());
        assertEquals(UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM, installation.getOperatingSystem());
        assertEquals(UPDATED_SIMPLE_PUSH_OPERATING_SYSTEM_VERSION, installation.getOsVersion());
        assertEquals(UPDATED_SIMPLE_PUSH_ALIAS, installation.getAlias());
    }

    @RunAsClient
    @Test
    @InSequence(106)
    public void registerSimplePushVariantWithWrongPushApplication() {
        PushApplication pushApplication = PushApplicationUtils.generate();

        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        SimplePushVariantUtils.generateAndRegister(pushApplication, getSession());
    }

    @RunAsClient
    @Test
    @InSequence(107)
    public void listAllSimplePushVariants() {
        List<SimplePushVariant> simplePushVariants = SimplePushVariantUtils.listAll(getRegisteredPushApplication(),
                getSession());

        assertNotNull(simplePushVariants);
        assertEquals(3, simplePushVariants.size());
    }

    @RunAsClient
    @Test
    @InSequence(108)
    public void findSimplePushVariant() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.findById(getRegisteredSimplePushVariant()
                .getVariantID(), getRegisteredPushApplication(), getSession());

        assertNotNull(simplePushVariant);
        SimplePushVariantUtils.checkEquality(getRegisteredSimplePushVariant(), simplePushVariant);
    }

    @RunAsClient
    @Test
    @InSequence(109)
    public void findSimplePushVariantWithInvalidId() {
        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        SimplePushVariantUtils.findById(getRegisteredSimplePushVariant().getVariantID(),
                getRegisteredPushApplication(), getSession());
    }

    @RunAsClient
    @Test
    @InSequence(110)
    public void updateSimplePushVariantWithInvalidId() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.generate();

        simplePushVariant.setVariantID(UUID.randomUUID().toString());

        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        SimplePushVariantUtils.update(simplePushVariant, getRegisteredPushApplication(), getSession());
    }

    @RunAsClient
    @Test
    @InSequence(111)
    public void removeSimplePushVariantWithInvalidId() {
        SimplePushVariant simplePushVariant = SimplePushVariantUtils.generate();

        simplePushVariant.setVariantID(UUID.randomUUID().toString());

        thrown.expectUnexpectedResponseException(Status.NOT_FOUND);
        SimplePushVariantUtils.delete(simplePushVariant, getRegisteredPushApplication(), getSession());
    }

    @RunAsClient
    @Test
    @InSequence(116)
    public void unregisterInstallation() {
        InstallationImpl registeredInstallation = getRegisteredSimplePushInstallations().get(0);

        InstallationUtils.unregister(registeredInstallation, getRegisteredSimplePushVariant(), getContextRoot());
    }

    @Test
    @InSequence(117)
    public void verifyInstallationRemoval() {
        InstallationImpl registeredInstallation = getRegisteredSimplePushInstallations().get(0);

        InstallationImpl installation = clientInstallationService.findInstallationForVariantByDeviceToken(
                getRegisteredSimplePushVariant().getVariantID(), registeredInstallation.getDeviceToken());
        assertNull(installation);
    }

    @RunAsClient
    @Test
    @InSequence(118)
    public void unauthorizedUnregisterInstallation() {
        SimplePushVariant generatedSimplePushVariant = SimplePushVariantUtils.generate();

        InstallationImpl registeredInstallation = getRegisteredSimplePushInstallations().get(0);

        thrown.expectUnexpectedResponseException(Status.UNAUTHORIZED);
        InstallationUtils.unregister(registeredInstallation, generatedSimplePushVariant, getContextRoot());
    }

    @RunAsClient
    @Test
    @InSequence(1000)
    public void removeSimplePushVariant() {
        SimplePushVariantUtils.delete(getRegisteredSimplePushVariant(), getRegisteredPushApplication(), getSession());
    }
}
