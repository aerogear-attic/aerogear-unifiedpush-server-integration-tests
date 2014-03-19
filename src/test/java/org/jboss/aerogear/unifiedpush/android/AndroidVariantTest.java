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
package org.jboss.aerogear.unifiedpush.android;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.android.AndroidVariantContext;
import org.jboss.aerogear.test.api.android.AndroidVariantWorker;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationContext;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationWorker;
import org.jboss.aerogear.test.model.AndroidVariant;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.UnifiedPushServer;
import org.jboss.aerogear.unifiedpush.utils.AndroidVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.ContentTypes;
import org.jboss.aerogear.unifiedpush.utils.ExpectedException;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.ArquillianRule;
import org.jboss.arquillian.junit.ArquillianRules;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(ArquillianRules.class)
public class AndroidVariantTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {

            PushApplication application = with(PushApplicationWorker.worker()).generate().persist().detachEntity();

            // TODO register installations

            return this;
        }
    };

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"));

        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }

    @AfterClass
    public static void cleanup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"));
    }

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }

    private PushApplication getRegisteredApplication() {
        return ups.with(PushApplicationWorker.worker()).findAll().detachEntity();
    }

    @Test
    public void registerWithoutAuthorization() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);

        Session invalidSession = Session.newSession(ups.getSession().getBaseUrl().toExternalForm());

        try {
            AndroidVariantWorker.worker().createContext(invalidSession, getRegisteredApplication()).generate()
                    .persist();
        } finally {
            assertThat(ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).findAll().detachEntities()
                    .size(), is(0));
        }
    }

    @Test
    public void registerWithMissingGoogleKey() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);

        try {
            ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).generate().googleKey(null).persist();
        } finally {
            assertThat(ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).findAll().detachEntities()
                    .size(), is(0));
        }
    }

    @Test
    public void findVariantWithInvalidID() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).find(UUID.randomUUID().toString());
    }

    @Test
    public void updateVariantWithInvalidID() {
        AndroidVariant variant = ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).merge(variant);
    }

    @Test
    public void removeVariantWithInvalidID() {
        AndroidVariant variant = ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ups.with(AndroidVariantWorker.worker(), getRegisteredApplication()).remove(variant);
    }

    @Test
    public void testCRUD() {
        performCRUD(AndroidVariantWorker.worker());
    }

    @Test
    public void testCRUDUTF8() {
        performCRUD(AndroidVariantWorker.worker().contentType(ContentTypes.jsonUTF8()));
    }

    @Test
    public void testInstallationCRUD() {
        performInstallationCRUD(AndroidInstallationWorker.worker());
    }

    @Test
    public void testInstallationCRUDUTF8() {
        performInstallationCRUD(AndroidInstallationWorker.worker().contentType(ContentTypes.jsonUTF8()));
    }

    private void performCRUD(AndroidVariantWorker worker) {
        PushApplication application = getRegisteredApplication();

        // CREATE
        List<AndroidVariant> persistedVariants = ups.with(worker, application)
                .generate().name("AwesomeAppěščřžýáíéňľ").persist()
                .generate().name("AwesomeAppவான்வழிe").persist()
                .detachEntities();

        assertThat(persistedVariants, is(notNullValue()));
        assertThat(persistedVariants.size(), is(2));

        AndroidVariant persistedVariant = persistedVariants.get(0);
        AndroidVariant persistedVariant1 = persistedVariants.get(1);

        // READ
        AndroidVariantContext context = ups.with(worker, application).findAll();
        List<AndroidVariant> readVariants = context.detachEntities();
        assertThat(readVariants, is(notNullValue()));
        assertThat(readVariants.size(), is(2));

        AndroidVariantUtils.checkEquality(persistedVariant,
                context.detachEntity(persistedVariant.getVariantID()));
        AndroidVariantUtils.checkEquality(persistedVariant1,
                context.detachEntity(persistedVariant1.getVariantID()));

        // UPDATE
        ups.with(worker, application)
                .edit(persistedVariant.getVariantID()).name("newname").description("newdescription").merge();
        AndroidVariant readVariant = ups.with(worker, application).find(persistedVariant.getVariantID()).detachEntity();

        assertThat(readVariant.getName(), is("newname"));
        assertThat(readVariant.getDescription(), is("newdescription"));

        // DELETE
        readVariants = ups.with(worker, application)
                .removeById(persistedVariant.getVariantID())
                .removeById(persistedVariant1.getVariantID())
                .findAll()
                .detachEntities();
        assertThat(readVariants.size(), is(0));
    }

    public void performInstallationCRUD(AndroidInstallationWorker worker) {
        PushApplication application = getRegisteredApplication();
        AndroidVariant variant = ups.with(AndroidVariantWorker.worker(), application)
                .generate().persist()
                .detachEntity();

        try {
            // CREATE
            List<InstallationImpl> persistedInstallations = ups.with(worker, variant)
                    .generate().alias("AwesomeAppěščřžýáíéňľ").persist()
                    .generate().alias("AwesomeAppவான்வழிe").persist()
                    .detachEntities();
            assertThat(persistedInstallations, is(notNullValue()));
            assertThat(persistedInstallations.size(), is(2));

            InstallationImpl persistedInstallation = persistedInstallations.get(0);
            InstallationImpl persistedInstallation1 = persistedInstallations.get(1);

            // READ
            AndroidInstallationContext context = ups.with(worker, variant).findAll();
            List<InstallationImpl> readInstallations = context.detachEntities();
            assertThat(readInstallations, is(notNullValue()));
            assertThat(readInstallations.size(), is(2));

            InstallationUtils.checkEquality(persistedInstallation, context.detachEntity(persistedInstallation.getId()));
            InstallationUtils.checkEquality(persistedInstallation1,
                    context.detachEntity(persistedInstallation1.getId()));

            // UPDATE
            ups.with(worker, variant).edit(persistedInstallation.getId()).alias("newalias").merge();
            InstallationImpl readInstallation = ups.with(worker, variant)
                    .find(persistedInstallation.getId())
                    .detachEntity();

            assertThat(readInstallation.getAlias(), is("newalias"));

            // DELETE
            readInstallations = ups.with(worker, variant)
                    .removeById(persistedInstallation.getId())
                    .removeById(persistedInstallation1.getId())
                    .findAll()
                    .detachEntities();

            assertThat(readInstallations.size(), is(0));

        } finally {
            // FIXME cleanup should be done another way!
            ups.with(AndroidVariantWorker.worker(), application).remove(variant);
        }

    }

}
