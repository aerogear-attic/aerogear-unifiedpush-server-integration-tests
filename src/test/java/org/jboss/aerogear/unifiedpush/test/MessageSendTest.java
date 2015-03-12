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

import category.APNS;
import category.ChromePackagedApp;
import category.GCM;
import category.NotIPv6Ready;
import category.SimplePush;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.notnoop.apns.EnhancedApnsNotification;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.extension.CleanupRequest;
import org.jboss.aerogear.test.api.extension.SenderStatisticsRequest;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationWorker;
import org.jboss.aerogear.test.api.installation.chromepackagedapp.ChromePackagedAppInstallationWorker;
import org.jboss.aerogear.test.api.installation.ios.iOSInstallationWorker;
import org.jboss.aerogear.test.api.installation.simplepush.SimplePushInstallationWorker;
import org.jboss.aerogear.test.api.sender.SenderRequest;
import org.jboss.aerogear.test.api.variant.android.AndroidVariantWorker;
import org.jboss.aerogear.test.api.variant.chromepackagedapp.ChromePackagedAppVariantWorker;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantWorker;
import org.jboss.aerogear.test.api.variant.simplepush.SimplePushVariantWorker;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.jboss.aerogear.unifiedpush.utils.TestUtils.apnsTestsEnabled;
import static org.jboss.aerogear.unifiedpush.utils.TestUtils.chromePackagedAppTestsEnabled;
import static org.jboss.aerogear.unifiedpush.utils.TestUtils.gcmTestsEnabled;
import static org.jboss.aerogear.unifiedpush.utils.TestUtils.prepareSenderRequest;
import static org.jboss.aerogear.unifiedpush.utils.TestUtils.simplePushTestsEnabled;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(ArquillianRules.class)
public class MessageSendTest {

    private final static String ALERT_MESSAGE = "Hello AeroGear!";
    private final static String SIMPLEPUSH_VERSION = "1";
    private final static String SIMPLEPUSH_VERSION_BODY = "version=" + SIMPLEPUSH_VERSION;

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {
            with(CleanupRequest.request()).cleanApplications();

            PushApplication application = with(PushApplicationWorker.worker())
                    .generate().persist()
                    .detachEntity();

            if (gcmTestsEnabled()) {
                AndroidVariant androidVariant = with(AndroidVariantWorker.worker(), application)
                        .generate().persist()
                        .detachEntity();

                with(AndroidInstallationWorker.worker(), androidVariant)
                        .generate(3).persist();
            }

            if (apnsTestsEnabled()) {
                iOSVariant iosVariant = with(
                        iOSVariantWorker.worker()
                                .defaultCertificate(Constants.IOS_CERTIFICATE_PATH)
                                .defaultPassphrase(Constants.IOS_CERTIFICATE_PASSPHRASE), application
                )
                        .generate().persist()
                        .detachEntity();

                with(iOSInstallationWorker.worker(), iosVariant)
                        .generate(3).persist();
            }

            if (simplePushTestsEnabled()) {
                SimplePushVariant simplePushVariant = with(SimplePushVariantWorker.worker(), application)
                        .generate().persist()
                        .detachEntity();

                with(SimplePushInstallationWorker.worker(), simplePushVariant)
                        .generate(3).persist();
            }

            if (chromePackagedAppTestsEnabled()) {
                ChromePackagedAppVariant chromePackagedAppVariant =
                        with(ChromePackagedAppVariantWorker.worker(), application)
                                .generate().persist()
                                .detachEntity();

                with(ChromePackagedAppInstallationWorker.worker(), chromePackagedAppVariant)
                        .generate(3).persist();

            }
            return this;
        }
    };

    @Rule
    public CheckingExpectedException exception = new CheckingExpectedException() {
        @Override
        protected void afterExceptionAssert() {
            SenderStatistics statistics = ups.with(SenderStatisticsRequest.request()).getAndClear();

            //assertThat(statistics.deviceTokens.size(), is(0));
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

    @Deployment(name = Deployments.TEST_EXTENSION, testable = false, order = 4)
    @TargetsContainer("main-server-group")
    public static WebArchive createTestExtensionDeployment() {
        return Deployments.testExtension();
    }

    private PushApplication getPushApplication() {
        return ups.with(PushApplicationWorker.worker()).findAll().detachEntity();
    }

    private AndroidVariant getAndroidVariant() {
        return ups.with(AndroidVariantWorker.worker(), getPushApplication()).findAll().detachEntity();
    }

    private iOSVariant getIOSVariant() {
        return ups.with(iOSVariantWorker.worker(), getPushApplication()).findAll().detachEntity();
    }

    private SimplePushVariant getSimplePushVariant() {
        return ups.with(SimplePushVariantWorker.worker(), getPushApplication()).findAll().detachEntity();
    }

    private ChromePackagedAppVariant getChromePackagedAppVariant() {
        return ups.with(ChromePackagedAppVariantWorker.worker(), getPushApplication()).findAll().detachEntity();
    }

    @Category({ GCM.class, NotIPv6Ready.class })
    @Test
    public void androidSelectiveSendByAliases() {
        SenderStatistics statistics;
        List<Installation> installations = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                .findAll().detachEntities();
        installations = installations.subList(0, installations.size() - 1);

        statistics = selectiveSendByAliases(installations);
        assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));

        statistics = selectiveSendByAliasesWithTtl(installations, 5000);
        assertThat(statistics.gcmMessage.getTimeToLive(), is(5000));
        assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));

        statistics = selectiveSendByAliasesWithContentAvailable(installations);
        assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("hello", "aerogear");
        attributes.put("aerogear", "rulez!");

        statistics = selectiveSendByAliasesWithAttributes(installations, attributes);
        assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
        assertThat(statistics.gcmMessage.getData().get("hello"), is("aerogear"));
        assertThat(statistics.gcmMessage.getData().get("aerogear"), is("rulez!"));
    }

    @Category({ APNS.class, NotIPv6Ready.class })
    @Test
    public void iosSelectiveSendByAliases() {
        SenderStatistics statistics;
        List<Installation> installations = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                .findAll().detachEntities();
        installations = installations.subList(0, installations.size() - 1);

        statistics = selectiveSendByAliases(installations);
        assertAlertPresent(statistics, ALERT_MESSAGE);

        // FIXME how to test this? It's changing over time (now() + MAXIUMUM_DATE)
//        assertThat(statistics.apnsExpiry, is(EnhancedApnsNotification.MAXIMUM_DATE.getTime()));

        int timestamp = (int) (System.currentTimeMillis() / 1000);
        statistics = selectiveSendByAliasesWithTtl(installations, 5000);
        assertAlertPresent(statistics, ALERT_MESSAGE);
        // FIXME is this safe or can the server have different time?
        assertThat(statistics.apnsExpiry, is(greaterThan(timestamp)));
        assertThat(statistics.apnsExpiry, is(not((int) EnhancedApnsNotification.MAXIMUM_DATE.getTime())));

        // FIXME add a way to verify content available!
        statistics = selectiveSendByAliasesWithContentAvailable(installations);
        assertAlertPresent(statistics, ALERT_MESSAGE);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("hello", "aerogear");
        attributes.put("aerogear", "rulez!");

        statistics = selectiveSendByAliasesWithAttributes(installations, attributes);
        assertAlertPresent(statistics, ALERT_MESSAGE);
        assertCustomFieldsPresent(statistics, attributes);
    }

    //  This tests
    @Category(SimplePush.class)
    @Test
    public void simplePushSelectiveSendByAliases() throws Exception {
        List<Installation> installations = ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant())
                .findAll().detachEntities();

        installations = installations.subList(0, installations.size() - 1);
        SimplePushServerSimulator simulator = SimplePushServerSimulator.create().start();

        ups.with(prepareSenderRequest())
                .message()
                .pushApplication(getPushApplication())
                .aliasesOf(installations)
                .alert(ALERT_MESSAGE)
                .simplePush(SIMPLEPUSH_VERSION)
                .send();

        List<HttpServerExchange> exchanges = simulator.awaitAndStop(installations.size()).getExchanges();

        for (Installation installation : installations) {
            boolean found = false;
            for (HttpServerExchange exchange : exchanges) {
                if (installation.getDeviceToken().endsWith(exchange.getRequestPath())) {
                    // assertThat(exchange.getRequestChannel().read);
                    found = true;
                }
            }
            if (!found) {
                fail("Message not sent for installation " + installation);
            }
        }
    }

    @Category(ChromePackagedApp.class)
    @Test
    public void chromePackagedAppSelectiveSendByAliases() {
        List<Installation> installations = ups.with(ChromePackagedAppInstallationWorker.worker(),
                getChromePackagedAppVariant()).findAll().detachEntities();
        SenderStatistics statistics = selectiveSendByAliases(installations.subList(0, installations.size() - 1));
        assertThat(statistics.gcmForChromeAlert, is(ALERT_MESSAGE));
    }

    @Category(NotIPv6Ready.class)
    @Test
    public void selectiveSendByCommonAlias() {
        String alias = UUID.randomUUID().toString();

        List<Installation> commonAliasInstallations = new ArrayList<Installation>();

        Installation androidInstallation = null;
        if (gcmTestsEnabled()) {
            androidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                    .generate().alias(alias).persist()
                    .detachEntity();
            commonAliasInstallations.add(androidInstallation);
        }

        Installation iosInstallation = null;
        if (apnsTestsEnabled()) {
            iosInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                    .generate().alias(alias).persist()
                    .detachEntity();
            commonAliasInstallations.add(iosInstallation);
        }

        Installation gcmForChromeInstallation = null;
        if (chromePackagedAppTestsEnabled()) {
            gcmForChromeInstallation = ups.with(ChromePackagedAppInstallationWorker.worker(),
                    getChromePackagedAppVariant())
                    .generate().alias(alias).persist()
                    .detachEntity();
            commonAliasInstallations.add(gcmForChromeInstallation);
        }

        List<Installation> simplePushInstallations = new ArrayList<Installation>();
        Installation simplePushInstallation = null;
        if (simplePushTestsEnabled()) {
            simplePushInstallation =
                    ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant())
                            .generate().alias(alias).persist()
                            .detachEntity();
            simplePushInstallations.add(simplePushInstallation);
        }

        SimplePushServerSimulator simulator = SimplePushServerSimulator.create().start();

        ups.with(prepareSenderRequest())
                .message()
                .pushApplication(getPushApplication())
                .aliases(alias)
                .alert(ALERT_MESSAGE)
                .simplePush(SIMPLEPUSH_VERSION)
                .send();

        List<HttpServerExchange> exchanges = simulator.awaitAndStop(simplePushInstallations.size()).getExchanges();

        assertThat(exchanges, is(notNullValue()));
        assertThat(exchanges.size(), is(simplePushInstallations.size()));

        for (Installation pushInstallation : simplePushInstallations) {
            boolean found = false;
            for (HttpServerExchange exchange : exchanges) {
                if (pushInstallation.getDeviceToken().endsWith(exchange.getRequestPath())) {
                    found = true;
                }
            }
            if (!found) {
                fail("Message not sent for SimplePush installation " + pushInstallation);
            }
        }

        SenderStatistics statistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(commonAliasInstallations.size(), Duration.FIVE_SECONDS);

        for (Installation commonAliasInstallation : commonAliasInstallations) {
            assertThat(statistics.deviceTokens, hasItem(commonAliasInstallation.getDeviceToken()));
        }

        if (gcmTestsEnabled()) {
            assertThat(statistics.gcmMessage, is(Matchers.notNullValue()));
            assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
        }
        if (apnsTestsEnabled()) {
            assertAlertPresent(statistics, ALERT_MESSAGE);
        }
        if (chromePackagedAppTestsEnabled()) {
            assertThat(statistics.gcmForChromeAlert, is(ALERT_MESSAGE));
        }

        if (gcmTestsEnabled()) {
            ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).unregister(androidInstallation);
        }
        if (apnsTestsEnabled()) {
            ups.with(iOSInstallationWorker.worker(), getIOSVariant()).unregister(iosInstallation);
        }
        if (chromePackagedAppTestsEnabled()) {
            ups.with(ChromePackagedAppInstallationWorker.worker(), getChromePackagedAppVariant()).unregister
                    (gcmForChromeInstallation);
        }
        if (simplePushTestsEnabled()) {
            ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant()).unregister(simplePushInstallation);
        }
    }

    @Category(NotIPv6Ready.class)
    @Test
    public void selectiveSendWithInvalidTokens() {

        List<Variant> variants = new ArrayList<Variant>();
        List<Installation> validInstallations = new ArrayList<Installation>();
        List<Installation> invalidInstallations = new ArrayList<Installation>();

        Installation validAndroidInstallation = null;
        Installation validIOSInstallation = null;

        if (gcmTestsEnabled()) {
            validInstallations.addAll(
                    ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).findAll().detachEntities());

            validAndroidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                    .generate().persist()
                    .detachEntity();
            validInstallations.add(validAndroidInstallation);

            Installation invalidAndroidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                    .generate().invalidateToken().persist()
                    .detachEntity();
            invalidInstallations.add(invalidAndroidInstallation);

            variants.add(getAndroidVariant());
        }
        if (apnsTestsEnabled()) {
            validInstallations.addAll(ups.with(iOSInstallationWorker.worker(), getIOSVariant()).findAll()
                    .detachEntities());

            validIOSInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                    .generate().persist()
                    .detachEntity();
            validInstallations.add(validIOSInstallation);

            Installation invalidIOSInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                    .generate().invalidateToken().persist()
                    .detachEntity();
            invalidInstallations.add(invalidIOSInstallation);

            variants.add(getIOSVariant());
        }

        // FIXME how about GCM for chrome and SimplePush? they do not report client invalidity
        ups.with(prepareSenderRequest())
                .message()
                .pushApplication(getPushApplication())
                .variants(variants)
                .alert(ALERT_MESSAGE)
                .send();

        SenderStatistics statistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(validInstallations.size() + invalidInstallations.size(), Duration.FIVE_SECONDS);

        for (Installation validInstallation : validInstallations) {
            assertThat(statistics.deviceTokens, hasItem(validInstallation.getDeviceToken()));
        }

        for (Installation invalidInstallation : invalidInstallations) {
            assertThat(statistics.deviceTokens, hasItem(invalidInstallation.getDeviceToken()));
        }

        List<Installation> afterSendInstallations = new ArrayList<Installation>();
        if (gcmTestsEnabled()) {
            afterSendInstallations.addAll(
                    ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).findAll().detachEntities());
        }
        if (apnsTestsEnabled()) {
            afterSendInstallations.addAll(
                    ups.with(iOSInstallationWorker.worker(), getIOSVariant()).findAll().detachEntities());
        }
        assertThat(afterSendInstallations.size(), is(validInstallations.size()));

        List<String> afterSendInstallationsTokens = new ArrayList<String>();
        for (Installation afterSendInstallation : afterSendInstallations) {
            afterSendInstallationsTokens.add(afterSendInstallation.getDeviceToken());
        }

        for (Installation validInstallation : validInstallations) {
            assertThat(afterSendInstallationsTokens, hasItem(validInstallation.getDeviceToken()));
        }

        if (gcmTestsEnabled()) {
            assertThat(statistics.gcmMessage, is(notNullValue()));
            assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
            ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).unregister(validAndroidInstallation);
        }
        if (apnsTestsEnabled()) {
            assertAlertPresent(statistics, ALERT_MESSAGE);
            ups.with(iOSInstallationWorker.worker(), getIOSVariant()).unregister(validIOSInstallation);
        }

    }

    @Category(NotIPv6Ready.class)
    @Test
    public void selectiveSendByCommonCategory() {
        String category = UUID.randomUUID().toString();

        List<Installation> commonAliasInstallations = new ArrayList<Installation>();

        Installation androidInstallation = null;
        if (gcmTestsEnabled()) {
            androidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                    .generate().categories(category).persist()
                    .detachEntity();
            commonAliasInstallations.add(androidInstallation);
        }

        Installation iosInstallation = null;
        if (apnsTestsEnabled()) {
            iosInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                    .generate().categories(category).persist()
                    .detachEntity();
            commonAliasInstallations.add(iosInstallation);
        }

        List<Installation> simplePushInstallations = new ArrayList<Installation>();
        Installation simplePushInstallation = null;
        if (simplePushTestsEnabled()) {
            simplePushInstallation = ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant())
                    .generate().categories(category).persist()
                    .detachEntity();
            simplePushInstallations.add(simplePushInstallation);
        }

        SimplePushServerSimulator simulator = SimplePushServerSimulator.create().start();

        ups.with(prepareSenderRequest())
                .message()
                .pushApplication(getPushApplication())
                .categories(category)
                .alert(ALERT_MESSAGE)
                .simplePush(SIMPLEPUSH_VERSION)
                .send();

        List<HttpServerExchange> exchanges = simulator.awaitAndStop(simplePushInstallations.size()).getExchanges();

        assertThat(exchanges, is(notNullValue()));
        assertThat(exchanges.size(), is(simplePushInstallations.size()));

        for (Installation pushInstallation : simplePushInstallations) {
            boolean found = false;
            for (HttpServerExchange exchange : exchanges) {
                if (pushInstallation.getDeviceToken().endsWith(exchange.getRequestPath())) {
                    found = true;
                }
            }
            if (!found) {
                fail("Message not sent for SimplePush installation " + pushInstallation);
            }
        }

        SenderStatistics statistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(commonAliasInstallations.size(), Duration.FIVE_SECONDS);

        for (Installation commonAliasInstallation : commonAliasInstallations) {
            assertThat(statistics.deviceTokens, hasItem(commonAliasInstallation.getDeviceToken()));
        }

        if (gcmTestsEnabled()) {
            assertThat(statistics.gcmMessage, is(Matchers.notNullValue()));
            assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
            ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).unregister(androidInstallation);
        }
        if (apnsTestsEnabled()) {
            assertAlertPresent(statistics, ALERT_MESSAGE);
            ups.with(iOSInstallationWorker.worker(), getIOSVariant()).unregister(iosInstallation);
        }
        if (simplePushTestsEnabled()) {
            ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant()).unregister(simplePushInstallation);
        }
    }

    @Category(NotIPv6Ready.class)
    @Test
    public void selectiveSendByVariants() {
        Map<Variant, InstallationWorker> variants = new HashMap<Variant, InstallationWorker>();

        if (gcmTestsEnabled()) {
            variants.put(getAndroidVariant(), AndroidInstallationWorker.worker());
        }
        if (apnsTestsEnabled()) {
            variants.put(getIOSVariant(), iOSInstallationWorker.worker());
        }

        selectiveSendByVariants(variants);
    }

    @Category(NotIPv6Ready.class)
    @Test
    public void selectiveSendEmptyPushApplicationId() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);

        ups.with(prepareSenderRequest())
                .message()
                .pushApplication(getPushApplication())
                .pushApplicationId("")
                .alert(ALERT_MESSAGE)
                .send();
    }

    @Category(NotIPv6Ready.class)
    @Test
    public void selectiveSendWrongPushApplicationId() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        ups.with(prepareSenderRequest())
                .message()
                .pushApplication(getPushApplication())
                .pushApplicationId(UUID.randomUUID().toString())
                .alert(ALERT_MESSAGE)
                .send();
    }

    @Category(NotIPv6Ready.class)
    @Test
    public void selectiveSendWrongMasterSecret() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        ups.with(prepareSenderRequest())
                .message()
                .pushApplication(getPushApplication())
                .masterSecret(UUID.randomUUID().toString())
                .alert(ALERT_MESSAGE)
                .send();
    }

    private SenderStatistics selectiveSendByAliases(List<Installation> installations) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(prepareSenderRequest())
                .message()
                .alert(ALERT_MESSAGE);

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliasesWithTtl(List<Installation> installations, int timeToLive) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(prepareSenderRequest())
                .message()
                .alert(ALERT_MESSAGE)
                .timeToLive(timeToLive);

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliasesWithContentAvailable(List<Installation> installations) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(prepareSenderRequest())
                .message()
                .alert(ALERT_MESSAGE)
                .contentAvailable();

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliasesWithAttributes(List<Installation> installations,
                                                                  Map<String, String> attributes) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(prepareSenderRequest())
                .message()
                .alert(ALERT_MESSAGE);
        for (String key : attributes.keySet()) {
            blueprint.attribute(key, attributes.get(key));
        }

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliases(List<Installation> installations,
                                                    SenderRequest.UnifiedMessageBlueprint messageBlueprint) {

        messageBlueprint.pushApplication(getPushApplication()).aliasesOf(installations).send();

        SenderStatistics senderStatistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(installations.size(), Duration.FIVE_SECONDS);

        assertThat(senderStatistics.deviceTokens.size(), is(installations.size()));
        for (Installation installation : installations) {
            assertThat(senderStatistics.deviceTokens, hasItem(installation.getDeviceToken()));
        }
        return senderStatistics;
    }

    private SenderStatistics selectiveSendByVariants(Map<Variant, InstallationWorker> variants) {
        ups.with(prepareSenderRequest())
                .message()
                .alert(ALERT_MESSAGE)
                .pushApplication(getPushApplication())
                .variants(new ArrayList<Variant>(variants.keySet()))
                .send();

        List<Installation> installations = new ArrayList<Installation>();
        for (Variant variant : variants.keySet()) {
            InstallationWorker worker = variants.get(variant);

            installations.addAll(ups.with(worker, variant).findAll().detachEntities());
        }

        SenderStatistics senderStatistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(installations.size(), Duration.FIVE_SECONDS);

        assertThat(installations.size(), is(senderStatistics.deviceTokens.size()));
        for (Installation installation : installations) {
            assertThat(senderStatistics.deviceTokens, hasItem(installation.getDeviceToken()));
        }

        if (gcmTestsEnabled()) {
            assertThat(senderStatistics.gcmMessage, is(notNullValue()));
            assertThat(senderStatistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
        }
        if (apnsTestsEnabled()) {
            assertAlertPresent(senderStatistics, ALERT_MESSAGE);
        }

        return senderStatistics;
    }

    private void assertAlertPresent(SenderStatistics statistics, String alertMessage) {
        assertThat(statistics.apnsPayload, is(notNullValue()));
        Logger.getLogger(MessageSendTest.class.getCanonicalName()).warning(statistics.apnsPayload);
        JSONObject jsonPayload = new JSONObject(statistics.apnsPayload);
        JSONObject aps = jsonPayload.optJSONObject("aps");
        assertThat(aps, is(notNullValue()));
        assertThat(aps.optString("alert"), is(alertMessage));
    }

    private void assertCustomFieldsPresent(SenderStatistics statistics, Map<String, String> attributes) {
        assertThat(statistics.apnsPayload, is(notNullValue()));
        Logger.getLogger(MessageSendTest.class.getCanonicalName()).warning(statistics.apnsPayload);
        JSONObject jsonPayload = new JSONObject(statistics.apnsPayload);
        for (String key : attributes.keySet()) {
            assertThat(jsonPayload.optString(key), is(attributes.get(key)));
        }
    }

    public static class SimplePushServerSimulator {
        private final Undertow server;
        private final List<HttpServerExchange> exchanges;

        private SimplePushServerSimulator() {
            server = Undertow.builder()
                    .addHttpListener(Constants.SOCKET_SERVER_PORT, "localhost")
                    .setHandler(new HttpHandler() {
                        @Override
                        public void handleRequest(HttpServerExchange exchange) throws Exception {
                            // FIXME aren't we leaking resources?
                            exchanges.add(exchange);
                            exchange.getResponseSender().send("OK");
                        }
                    })
                    .build();
            exchanges = new ArrayList<HttpServerExchange>();
        }

        public SimplePushServerSimulator start() {
            server.start();
            return this;
        }

        public SimplePushServerSimulator awaitAndStop(final int count) {
            try {
                Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return exchanges.size() == count;
                    }
                });
            } finally {
                stop();
            }
            return this;
        }

        public SimplePushServerSimulator stop() {
            server.stop();
            return this;
        }

        public List<HttpServerExchange> getExchanges() {
            return exchanges;
        }

        public static SimplePushServerSimulator create() {
            return new SimplePushServerSimulator();
        }

    }
}
