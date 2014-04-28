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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.ModelAsserts;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantContext;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantWorker;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.ContentTypes;
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
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(ArquillianRules.class)
public class iOSVariantTest {

    // TODO begin of code that is same for each variant
    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {

            PushApplication application = with(PushApplicationWorker.worker()).generate().persist().detachEntity();

            return this;
        }
    };

    @Rule
    public CheckingExpectedException exception = new CheckingExpectedException() {
        @Override
        protected void afterExceptionAssert() {
            List<iOSVariant> variants = ups.with(defaultWorker(), getRegisteredApplication())
                    .findAll()
                    .detachEntities();

            assertThat(variants.size(), is(0));
        }
    };

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

    private iOSVariantWorker defaultWorker() {
        return iOSVariantWorker.worker()
                .defaultCertificate(Constants.IOS_CERTIFICATE_PATH)
                .defaultPassphrase(Constants.IOS_CERTIFICATE_PASSPHRASE);
    }
    // TODO end of code that is same for each variant

    @Test
    public void registerWithoutAuthorization() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);

        Session invalidSession = Session.newSession(ups.getSession().getBaseUrl().toExternalForm());
        defaultWorker().createContext(invalidSession, getRegisteredApplication()).generate().persist();
    }

    @Test
    public void registerWithMissingCertificate() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);

        ups.with(defaultWorker(), getRegisteredApplication())
                .generate().certificate(new byte[0]).passphrase("").persist();
    }

    @Test
    public void registerWithoutApplication() {
        PushApplication nonexistentApplication = ups.with(PushApplicationWorker.worker()).generate();

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(defaultWorker(), nonexistentApplication).generate().persist();
    }


    @Test
    public void findVariantWithInvalidID() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(defaultWorker(), getRegisteredApplication()).find(UUID.randomUUID().toString());
    }

    @Test
    public void updateVariantWithInvalidID() {
        iOSVariant variant = ups.with(defaultWorker(), getRegisteredApplication())
                .generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(defaultWorker(), getRegisteredApplication()).merge(variant);
    }

    @Test
    public void updateWithMissingCertificate() {
        iOSVariant variant = ups.with(defaultWorker(), getRegisteredApplication())
                .generate().persist().detachEntity();
        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);
        try {
            ups.with(defaultWorker(), getRegisteredApplication())
                    .edit(variant.getVariantID()).certificate(new byte[0]).passphrase("").merge();
        } finally {
            ups.with(defaultWorker(), getRegisteredApplication()).remove(variant);
        }
    }

    @Test
    public void updateWithInvalidIDPatch() {
        iOSVariant variant = ups.with(defaultWorker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(defaultWorker(), getRegisteredApplication()).mergePatch(variant);
    }

    @Test
    public void removeVariantWithInvalidID() {
        iOSVariant variant = ups.with(defaultWorker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ups.with(defaultWorker(), getRegisteredApplication()).remove(variant);
    }

    @Test
    public void resetSecret() {
        iOSVariant variant = ups.with(iOSVariantWorker.worker(), getRegisteredApplication())
                .generate().persist().detachEntity();

        ups.with(iOSVariantWorker.worker(), getRegisteredApplication()).resetSecret(variant.getVariantID());

        iOSVariant changedVariant = ups.with(iOSVariantWorker.worker(), getRegisteredApplication())
                .find(variant.getVariantID()).detachEntity();

        assertThat(changedVariant.getVariantID(), is(variant.getVariantID()));
        assertThat(changedVariant.getSecret(), is(not(variant.getSecret())));

        ups.with(iOSVariantWorker.worker(), getRegisteredApplication()).remove(changedVariant);
    }

    @Test
    public void resetSecretWithInvalidID() {
        iOSVariant variant = ups.with(iOSVariantWorker.worker(), getRegisteredApplication()).generate();

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ups.with(iOSVariantWorker.worker(), getRegisteredApplication()).resetSecret(variant.getVariantID());
    }

    @Test
    public void testCRUD() {
        performCRUD(defaultWorker());
    }

    @Test
    public void testCRUDUTF8() {
        performCRUD(defaultWorker().contentType(ContentTypes.jsonUTF8()));
    }

    private void performCRUD(iOSVariantWorker worker) {
        PushApplication application = getRegisteredApplication();

        // CREATE
        List<iOSVariant> persistedVariants = ups.with(worker, application)
                .generate().name("AwesomeAppěščřžýáíéňľ").persist()
                .generate().name("AwesomeAppவான்வழிe").persist()
                .detachEntities();

        assertThat(persistedVariants, is(notNullValue()));
        assertThat(persistedVariants.size(), is(2));
        
        iOSVariant persistedVariant = persistedVariants.get(0);
        iOSVariant persistedVariant1 = persistedVariants.get(1);
        
        // READ
        iOSVariantContext context = ups.with(worker, application).findAll();
        List<iOSVariant> readVariants = context.detachEntities();
        assertThat(readVariants, is(notNullValue()));
        assertThat(readVariants.size(), is(2));

        ModelAsserts.assertModelsEqual(persistedVariant, context.detachEntity(persistedVariant.getVariantID()));
        ModelAsserts.assertModelsEqual(persistedVariant1, context.detachEntity(persistedVariant1.getVariantID()));

        // UPDATE, method: PUT
        ups.with(worker, application)
                .edit(persistedVariant.getVariantID())
                    .name("newname")
                    .description("newdescription")
                    .certificate(Constants.IOS_CERTIFICATE_PATH)
                    .passphrase(Constants.IOS_CERTIFICATE_PASSPHRASE)
                    .merge();
        iOSVariant readVariant = ups.with(worker, application).find(persistedVariant.getVariantID()).detachEntity();
        assertThat(readVariant.getName(), is("newname"));
        assertThat(readVariant.getDescription(), is("newdescription"));

        // UPDATE, method: PATCH
        ups.with(worker, application)
                .edit(persistedVariant1.getVariantID()).name("newname1").description("newdescription1").mergePatch();
        iOSVariant readVariant1 = ups.with(worker, application).find(persistedVariant1.getVariantID()).detachEntity();
        assertThat(readVariant1.getName(), is("newname1"));
        assertThat(readVariant1.getDescription(), is("newdescription1"));

        // DELETE
        readVariants = ups.with(worker, application)
                .removeById(persistedVariant.getVariantID())
                .removeById(persistedVariant1.getVariantID())
                .findAll()
                .detachEntities();
        assertThat(readVariants.size(), is(0));


    }

}
