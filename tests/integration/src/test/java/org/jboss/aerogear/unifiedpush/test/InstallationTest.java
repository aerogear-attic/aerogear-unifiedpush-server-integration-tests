/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.test;

import category.SimplePush;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.Utilities;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.extension.CleanupRequest;
import org.jboss.aerogear.test.api.installation.InstallationBlueprint;
import org.jboss.aerogear.test.api.installation.InstallationContext;
import org.jboss.aerogear.test.api.installation.InstallationEditor;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationWorker;
import org.jboss.aerogear.test.api.installation.ios.iOSInstallationBlueprint;
import org.jboss.aerogear.test.api.installation.ios.iOSInstallationWorker;
import org.jboss.aerogear.test.api.installation.simplepush.SimplePushInstallationWorker;
import org.jboss.aerogear.test.api.variant.android.AndroidVariantWorker;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantWorker;
import org.jboss.aerogear.test.api.variant.simplepush.SimplePushVariantWorker;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.test.util.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.test.util.Deployments;
import org.jboss.aerogear.unifiedpush.test.util.ModelAsserts;
import org.jboss.aerogear.unifiedpush.test.util.TestUtils;
import org.jboss.aerogear.unifiedpush.test.util.UnifiedPushServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

// TODO all tests except 'testRegisterSimplePushInstallationWithoutEndpoint' are failing because of AGPUSH-680
@RunWith(ArquillianRules.class)
public class InstallationTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {
            with(CleanupRequest.request()).cleanApplications();

            PushApplication application = with(PushApplicationWorker.worker())
                    .generate().persist()
                    .detachEntity();

            return this;
        }
    };

    @Rule
    public CheckingExpectedException exception = CheckingExpectedException.none();

    @BeforeClass
    public static void setup() {
        TestUtils.setupRestAssured();
    }

    @AfterClass
    public static void cleanup() {
        TestUtils.teardownRestAssured();
    }

    @Deployment(name = Deployments.AUTH_SERVER, testable = false, order = 1)
    @TargetsContainer("main-server-group")
    public static WebArchive createAuthServerDeployment() {
        return Deployments.authServer();
    }

    @Deployment(name = Deployments.AG_PUSH, testable = false, order = 2)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }

    @Deployment(name = Deployments.TEST_EXTENSION, testable = false, order = 4)
    @TargetsContainer("main-server-group")
    public static WebArchive createTestExtensionDeployment() {
        return Deployments.testExtension();
    }

    private PushApplication getRegisteredApplication() {
        return ups.with(PushApplicationWorker.worker()).findAll().detachEntity();
    }

    @Test
    public void testAndroidInstallations() {
        AndroidVariant variant = ups.with(AndroidVariantWorker.worker(), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(AndroidInstallationWorker.worker(), variant);
    }

    @Test
    public void testAndroidInstallationsUTF8() {
        AndroidVariant variant = ups.with(
                AndroidVariantWorker.worker().contentType(Utilities.ContentTypes.jsonUTF8()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(AndroidInstallationWorker.worker().contentType(Utilities.ContentTypes.jsonUTF8()), variant);
    }

    @Test
    public void testiOSInstallations() {
        iOSVariant variant = ups.with(
                iOSVariantWorker.worker()
                        .defaultCertificate(TestUtils.getDefaultApnsCertificate())
                        .defaultPassphrase(TestUtils.getDefaultApnsCertificatePassword()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(iOSInstallationWorker.worker(), variant);
    }

    @Test
    public void testiOSInstallationsUTF8() {
        iOSVariant variant = ups.with(
                iOSVariantWorker.worker()
                        .defaultCertificate(TestUtils.getDefaultApnsCertificate())
                        .defaultPassphrase(TestUtils.getDefaultApnsCertificatePassword())
                        .contentType(Utilities.ContentTypes.jsonUTF8()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(iOSInstallationWorker.worker().contentType(Utilities.ContentTypes.jsonUTF8()), variant);
    }

    @Test
    public void testiOSInstallationWithUpperCaseToken() {
        iOSVariant variant = ups.with(
                iOSVariantWorker.worker()
                        .defaultCertificate(TestUtils.getDefaultApnsCertificate())
                        .defaultPassphrase(TestUtils.getDefaultApnsCertificatePassword())
                        .contentType(Utilities.ContentTypes.jsonUTF8()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        iOSInstallationBlueprint blueprint = ups.with(iOSInstallationWorker.worker(), variant).generate();

        blueprint.deviceToken(blueprint.getDeviceToken().toUpperCase()).persist();

        List<Installation> installations = ups.with(iOSInstallationWorker.worker(), variant).findAll().detachEntities();
        assertThat(installations.size(), is(1));
        assertThat(installations.get(0).getDeviceToken(), is(blueprint.getDeviceToken().toLowerCase()));
    }

    @Category(SimplePush.class)
    @Test
    public void testSimplePushInstallations() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(SimplePushInstallationWorker.worker(), variant);
    }

    @Category(SimplePush.class)
    @Test
    public void testSimplePushInstallationsUTF8() {
        SimplePushVariant variant = ups.with(
                SimplePushVariantWorker.worker().contentType(Utilities.ContentTypes.jsonUTF8()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(SimplePushInstallationWorker.worker().contentType(Utilities.ContentTypes.jsonUTF8()), variant);
    }

    @Category(SimplePush.class)
    @Test
    public void testRegisterSimplePushInstallationWithoutEndpoint() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);
        ups.with(SimplePushInstallationWorker.worker(), variant).generate().deviceToken(null).persist();
    }

    public <BLUEPRINT extends InstallationBlueprint<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            EDITOR extends InstallationEditor<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            PARENT extends Variant,
            CONTEXT extends InstallationContext<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            WORKER extends InstallationWorker<BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>>

    void performInstallationCRUD(WORKER worker, PARENT parent) {
        // CREATE
        List<Installation> persistedInstallations = ups.with(worker, parent)
                .generate().alias("AwesomeAppěščřžýáíéňľ").persist()
                .generate().alias("AwesomeAppவான்வழிe").persist()
                .detachEntities();
        assertThat(persistedInstallations, is(notNullValue()));
        assertThat(persistedInstallations.size(), is(2));

        Installation persistedInstallation = persistedInstallations.get(0);
        Installation persistedInstallation1 = persistedInstallations.get(1);

        // CREATE without deviceToken
        try {
            ups.with(worker, parent).generate().deviceToken(null).persist();
            fail("Registration with null device token should have failed!");
        } catch (UnexpectedResponseException e) {
            assertThat(e.getActualStatusCode(), is(HttpStatus.SC_BAD_REQUEST));
        }

        // READ
        CONTEXT context = ups.with(worker, parent).findAll();
        List<Installation> readInstallations = context.detachEntities();
        assertThat(readInstallations, is(notNullValue()));
        assertThat(readInstallations.size(), is(2));

        ModelAsserts.assertModelsEqual(persistedInstallation, context.detachEntity(persistedInstallation.getId()));
        ModelAsserts.assertModelsEqual(persistedInstallation1,
                context.detachEntity(persistedInstallation1.getId()));

        // READ with wrong id
        try {
            ups.with(worker, parent).find(UUID.randomUUID().toString());
            fail("Find should fail with SC_NOT_FOUND!");
        } catch (UnexpectedResponseException e) {
            assertThat(e.getActualStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        }

        // UPDATE by CREATE
        Installation updatedInstallation = ups.with(worker, parent)
                .generate().deviceToken(persistedInstallation.getDeviceToken()).alias("newalias").persist()
                .detachEntity();
        Installation readInstallation = ups.with(worker, parent)
                .find(persistedInstallation.getId())
                .detachEntity();
        assertThat(updatedInstallation.getAlias(), is(not(persistedInstallation.getAlias())));
        assertThat(readInstallation.getAlias(), is(updatedInstallation.getAlias()));

        // UPDATE
        ups.with(worker, parent).edit(persistedInstallation1.getId()).alias("newalias").merge();
        Installation readInstallation1 = ups.with(worker, parent)
                .find(persistedInstallation1.getId())
                .detachEntity();

        assertThat(readInstallation1.getAlias(), is("newalias"));

        // UPDATE with wrong id
        try {
            BLUEPRINT blueprint = ups.with(worker, parent).generate();
            ups.with(worker, parent).merge(blueprint);
            fail("Update should fail with SC_NOT_FOUND!");
        } catch (UnexpectedResponseException e) {
            assertThat(e.getActualStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        }

        // DELETE
        readInstallations = ups.with(worker, parent)
                .removeById(persistedInstallation.getId())
                .findAll()
                .detachEntities();

        assertThat(readInstallations.size(), is(1));

        // DELETE with wrong id
        try {
            BLUEPRINT blueprint = ups.with(worker, parent).generate();
            ups.with(worker, parent).remove(blueprint);
            fail("Delete should fail with SC_NOT_FOUND!");
        } catch (UnexpectedResponseException e) {
            assertThat(e.getActualStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        }

        // UNREGISTER
        readInstallations = ups.with(worker, parent)
                .unregisterById(persistedInstallation1.getId())
                .findAll()
                .detachEntities();

        assertThat(readInstallations.size(), is(0));

    }

}
