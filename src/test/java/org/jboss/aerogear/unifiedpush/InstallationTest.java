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
package org.jboss.aerogear.unifiedpush;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.api.android.AndroidVariantWorker;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.installation.InstallationBlueprint;
import org.jboss.aerogear.test.api.installation.InstallationContext;
import org.jboss.aerogear.test.api.installation.InstallationEditor;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationContext;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationWorker;
import org.jboss.aerogear.test.api.installation.ios.iOSInstallationWorker;
import org.jboss.aerogear.test.api.installation.simplepush.SimplePushInstallationWorker;
import org.jboss.aerogear.test.api.ios.iOSVariantWorker;
import org.jboss.aerogear.test.api.simplepush.SimplePushVariantWorker;
import org.jboss.aerogear.test.model.AbstractVariant;
import org.jboss.aerogear.test.model.AndroidVariant;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.SimplePushVariant;
import org.jboss.aerogear.test.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.UnifiedPushServer;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.Constants;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(ArquillianRules.class)
public class InstallationTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {

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
    public void testAndroidInstallations() {
        AndroidVariant variant = ups.with(AndroidVariantWorker.worker(), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(AndroidInstallationWorker.worker(), variant);
    }

    @Test
    public void testAndroidInstallationsUTF8() {
        AndroidVariant variant = ups.with(
                AndroidVariantWorker.worker().contentType(ContentTypes.jsonUTF8()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(AndroidInstallationWorker.worker().contentType(ContentTypes.jsonUTF8()), variant);
    }

    @Test
    public void testiOSInstallations() {
        iOSVariant variant = ups.with(
                iOSVariantWorker.worker()
                        .defaultCertificate(Constants.IOS_CERTIFICATE_PATH)
                        .defaultPassphrase(Constants.IOS_CERTIFICATE_PASSPHRASE), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(iOSInstallationWorker.worker(), variant);
    }

    @Test
    public void testiOSInstallationsUTF8() {
        iOSVariant variant = ups.with(
                iOSVariantWorker.worker()
                        .defaultCertificate(Constants.IOS_CERTIFICATE_PATH)
                        .defaultPassphrase(Constants.IOS_CERTIFICATE_PASSPHRASE)
                        .contentType(ContentTypes.jsonUTF8()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(iOSInstallationWorker.worker().contentType(ContentTypes.jsonUTF8()), variant);
    }

    @Test
    public void testSimplePushInstallations() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(SimplePushInstallationWorker.worker(), variant);
    }

    @Test
    public void testSimplePushInstallationsUTF8() {
        SimplePushVariant variant = ups.with(
                SimplePushVariantWorker.worker().contentType(ContentTypes.jsonUTF8()), getRegisteredApplication())
                .generate().persist()
                .detachEntity();

        performInstallationCRUD(SimplePushInstallationWorker.worker().contentType(ContentTypes.jsonUTF8()), variant);
    }

    public <BLUEPRINT extends InstallationBlueprint<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            EDITOR extends InstallationEditor<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            PARENT extends AbstractVariant,
            CONTEXT extends InstallationContext<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            WORKER extends InstallationWorker<BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>>

    void performInstallationCRUD(WORKER worker, PARENT parent) {
        // CREATE
        List<InstallationImpl> persistedInstallations = ups.with(worker, parent)
                .generate().alias("AwesomeAppěščřžýáíéňľ").persist()
                .generate().alias("AwesomeAppவான்வழிe").persist()
                .detachEntities();
        assertThat(persistedInstallations, is(notNullValue()));
        assertThat(persistedInstallations.size(), is(2));

        InstallationImpl persistedInstallation = persistedInstallations.get(0);
        InstallationImpl persistedInstallation1 = persistedInstallations.get(1);

        // READ
        CONTEXT context = ups.with(worker, parent).findAll();
        List<InstallationImpl> readInstallations = context.detachEntities();
        assertThat(readInstallations, is(notNullValue()));
        assertThat(readInstallations.size(), is(2));

        InstallationUtils.checkEquality(persistedInstallation, context.detachEntity(persistedInstallation.getId()));
        InstallationUtils.checkEquality(persistedInstallation1,
                context.detachEntity(persistedInstallation1.getId()));

        // UPDATE
        ups.with(worker, parent).edit(persistedInstallation.getId()).alias("newalias").merge();
        InstallationImpl readInstallation = ups.with(worker, parent)
                .find(persistedInstallation.getId())
                .detachEntity();

        assertThat(readInstallation.getAlias(), is("newalias"));

        // DELETE
        readInstallations = ups.with(worker, parent)
                .removeById(persistedInstallation.getId())
                .removeById(persistedInstallation1.getId())
                .findAll()
                .detachEntities();

        assertThat(readInstallations.size(), is(0));


    }

}
