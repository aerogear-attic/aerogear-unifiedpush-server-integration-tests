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
import org.jboss.aerogear.test.Helper;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.ModelAsserts;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.variant.simplepush.SimplePushVariantContext;
import org.jboss.aerogear.test.api.variant.simplepush.SimplePushVariantWorker;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.ContentTypes;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
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
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(ArquillianRules.class)
@Category(SimplePush.class)
public class SimplePushVariantTest {

    private static final String TOO_LONG_NAME = Helper.randomStringOfLength(256);
    private static final String TOO_LONG_DESCRIPTION = Helper.randomStringOfLength(256);

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
            List<SimplePushVariant> variants = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                    .findAll()
                    .detachEntities();

            assertThat(variants.size(), is(0));
        }
    };

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

    private PushApplication getRegisteredApplication() {
        return ups.with(PushApplicationWorker.worker()).findAll().detachEntity();
    }

    @Test
    public void registerWithoutAuthorization() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);

        Session invalidSession = Session.newSession(ups.getSession().getBaseUrl().toExternalForm());

        SimplePushVariantWorker.worker().createContext(invalidSession, getRegisteredApplication()).generate().persist();
    }

    @Test
    public void registerWithoutApplication() {
        PushApplication nonexistentApplication = ups.with(PushApplicationWorker.worker()).generate();

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(SimplePushVariantWorker.worker(), nonexistentApplication).generate().persist();
    }

    @Test
    public void registerWithTooLongName() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().name(TOO_LONG_NAME).persist();
    }

    @Test
    public void registerWithTooLongDescription() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().description(TOO_LONG_DESCRIPTION).persist();
    }

    @Test
    public void findVariantWithInvalidID() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).find(UUID.randomUUID().toString());
    }

    @Test
    public void updateVariantWithInvalidID() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).merge(variant);
    }

    @Test
    public void updateWithTooLongName() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().persist().detachEntity();

        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);

        try {
            ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                    .edit(variant.getVariantID()).name(TOO_LONG_NAME).merge();
        } finally {
            ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).remove(variant);
        }
    }

    @Test
    public void updateWithTooLongDescription() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().persist().detachEntity();

        exception.expectUnexpectedResponseException(HttpStatus.SC_BAD_REQUEST);

        try {
            ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                    .edit(variant.getVariantID()).description(TOO_LONG_DESCRIPTION).merge();
        } finally {
            ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).remove(variant);
        }
    }

    @Test
    public void removeVariantWithInvalidID() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).remove(variant);
    }

    @Test
    public void resetSecret() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().persist().detachEntity();

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).resetSecret(variant.getVariantID());

        SimplePushVariant changedVariant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .find(variant.getVariantID()).detachEntity();

        assertThat(changedVariant.getVariantID(), is(variant.getVariantID()));
        assertThat(changedVariant.getSecret(), is(not(variant.getSecret())));

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).remove(changedVariant);
    }

    @Test
    public void resetSecretWithInvalidID() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).generate();

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).resetSecret(variant.getVariantID());
    }

    @Test
    public void testCRUD() {
        performCRUD(SimplePushVariantWorker.worker());
    }

    @Test
    public void testCRUDUTF8() {
        performCRUD(SimplePushVariantWorker.worker().contentType(ContentTypes.jsonUTF8()));
    }

    private void performCRUD(SimplePushVariantWorker worker) {
        PushApplication application = getRegisteredApplication();

        // CREATE
        List<SimplePushVariant> persistedVariants = ups.with(worker, application)
                .generate().name("AwesomeAppěščřžýáíéňľ").persist()
                .generate().name("AwesomeAppவான்வழிe").persist()
                .detachEntities();

        assertThat(persistedVariants, is(notNullValue()));
        assertThat(persistedVariants.size(), is(2));

        SimplePushVariant persistedVariant = persistedVariants.get(0);
        SimplePushVariant persistedVariant1 = persistedVariants.get(1);

        // READ
        SimplePushVariantContext context = ups.with(worker, application).findAll();
        List<SimplePushVariant> readVariants = context.detachEntities();
        assertThat(readVariants, is(notNullValue()));
        assertThat(readVariants.size(), is(2));

        ModelAsserts.assertModelsEqual(persistedVariant,
                context.detachEntity(persistedVariant.getVariantID()));
        ModelAsserts.assertModelsEqual(persistedVariant1,
                context.detachEntity(persistedVariant1.getVariantID()));

        // UPDATE
        ups.with(worker, application)
                .edit(persistedVariant.getVariantID()).name("newname").description("newdescription").merge();
        Variant readVariant = ups.with(worker, application)
                .find(persistedVariant.getVariantID()).detachEntity();

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


}
