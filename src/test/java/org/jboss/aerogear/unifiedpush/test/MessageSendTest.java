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

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.jboss.aerogear.test.api.variant.android.AndroidVariantWorker;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.variant.chromepackagedapp.ChromePackagedAppVariantWorker;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationWorker;
import org.jboss.aerogear.test.api.installation.chromepackagedapp.ChromePackagedAppInstallationWorker;
import org.jboss.aerogear.test.api.installation.ios.iOSInstallationWorker;
import org.jboss.aerogear.test.api.installation.simplepush.SimplePushInstallationWorker;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantWorker;
import org.jboss.aerogear.test.api.sender.SenderRequest;
import org.jboss.aerogear.test.api.sender.SenderStatistics;
import org.jboss.aerogear.test.api.sender.SenderStatisticsRequest;
import org.jboss.aerogear.test.api.variant.simplepush.SimplePushVariantWorker;
import org.jboss.aerogear.test.model.AbstractVariant;
import org.jboss.aerogear.test.model.AndroidVariant;
import org.jboss.aerogear.test.model.ChromePackagedAppVariant;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.SimplePushVariant;
import org.jboss.aerogear.test.model.Variant;
import org.jboss.aerogear.test.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.Constants;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
            PushApplication application = with(PushApplicationWorker.worker())
                    .generate().persist()
                    .detachEntity();

            AndroidVariant androidVariant = with(AndroidVariantWorker.worker(), application)
                    .generate().persist()
                    .detachEntity();

            iOSVariant iosVariant = with(
                    iOSVariantWorker.worker()
                            .defaultCertificate(Constants.IOS_CERTIFICATE_PATH)
                            .defaultPassphrase(Constants.IOS_CERTIFICATE_PASSPHRASE), application
            )
                    .generate().persist()
                    .detachEntity();

            SimplePushVariant simplePushVariant = with(SimplePushVariantWorker.worker(), application)
                    .generate().persist()
                    .detachEntity();

            ChromePackagedAppVariant chromePackagedAppVariant =
                    with(ChromePackagedAppVariantWorker.worker(), application)
                            .generate().persist()
                            .detachEntity();

            with(AndroidInstallationWorker.worker(), androidVariant)
                    .generate(3).persist();

            with(iOSInstallationWorker.worker(), iosVariant)
                    .generate(3).persist();

            with(SimplePushInstallationWorker.worker(), simplePushVariant)
                    .generate(3).persist();

            with(ChromePackagedAppInstallationWorker.worker(), chromePackagedAppVariant)
                    .generate(3).persist();

            return this;
        }
    };

    @Rule
    public CheckingExpectedException exception = new CheckingExpectedException() {
        @Override
        protected void afterExceptionAssert() {
            SenderStatistics statistics = ups.with(SenderStatisticsRequest.request()).getAndClear();

            assertThat(statistics.deviceTokens.size(), is(0));
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
        return Deployments.unifiedPushServerWithCustomSenders();
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

    @Test
    public void androidSelectiveSendByAliases() {
        SenderStatistics statistics;
        List<InstallationImpl> installations = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
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

    @Test
    public void iosSelectiveSendByAliases() {
        SenderStatistics statistics;
        List<InstallationImpl> installations = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                .findAll().detachEntities();
        installations = installations.subList(0, installations.size() - 1);

        statistics = selectiveSendByAliases(installations);
        assertThat(statistics.apnsAlert, is(ALERT_MESSAGE));

        statistics = selectiveSendByAliasesWithTtl(installations, 5000);
        assertThat(statistics.apnsAlert, is(ALERT_MESSAGE));
        assertThat(statistics.apnsCustomFields, containsString("ttl=5000"));

        // FIXME add a way to verify content available!
        statistics = selectiveSendByAliasesWithContentAvailable(installations);
        assertThat(statistics.apnsAlert, is(ALERT_MESSAGE));

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("hello", "aerogear");
        attributes.put("aerogear", "rulez!");

        statistics = selectiveSendByAliasesWithAttributes(installations, attributes);
        assertThat(statistics.apnsAlert, is(ALERT_MESSAGE));

        for (String key : attributes.keySet()) {
            assertThat(statistics.apnsCustomFields, containsString(key + "=" + attributes.get(key)));
        }

    }

    @Test
    public void simplePushSelectiveSendByAliases() throws Exception {
        List<InstallationImpl> installations = ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant())
                .findAll().detachEntities();

        installations = installations.subList(0, installations.size() - 1);
        SimplePushServerSimulator simulator = SimplePushServerSimulator.create().start();

        ups.with(SenderRequest.request())
                .message()
                .pushApplication(getPushApplication())
                .aliasesOf(installations)
                .alert(ALERT_MESSAGE)
                .simplePush(SIMPLEPUSH_VERSION)
                .send();

        List<HttpServerExchange> exchanges = simulator.awaitAndStop(installations.size()).getExchanges();

        for (InstallationImpl installation : installations) {
            boolean found = false;
            for (HttpServerExchange exchange : exchanges) {
                if (exchange.getRequestPath().endsWith(installation.getDeviceToken())) {
                    // assertThat(exchange.getRequestChannel().read);
                    found = true;
                }
            }
            if (!found) {
                fail("Message not sent for installation " + installation);
            }
        }
    }

    @Test
    public void chromePackagedAppSelectiveSendByAliases() {
        List<InstallationImpl> installations = ups.with(ChromePackagedAppInstallationWorker.worker(),
                getChromePackagedAppVariant()).findAll().detachEntities();
        SenderStatistics statistics = selectiveSendByAliases(installations.subList(0, installations.size() - 1));
        assertThat(statistics.gcmForChromeAlert, is(ALERT_MESSAGE));
    }

    @Test
    public void selectiveSendByCommonAlias() {
        String alias = UUID.randomUUID().toString();

        List<InstallationImpl> commonAliasInstallations = new ArrayList<InstallationImpl>();
        InstallationImpl androidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                .generate().alias(alias).persist()
                .detachEntity();
        commonAliasInstallations.add(androidInstallation);

        InstallationImpl iosInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                .generate().alias(alias).persist()
                .detachEntity();
        commonAliasInstallations.add(iosInstallation);

        InstallationImpl gcmForChromeInstallation = ups.with(ChromePackagedAppInstallationWorker.worker(),
                getChromePackagedAppVariant())
                .generate().alias(alias).persist()
                .detachEntity();
        commonAliasInstallations.add(gcmForChromeInstallation);

        List<InstallationImpl> simplePushInstallations = new ArrayList<InstallationImpl>();
        InstallationImpl simplePushInstallation =
                ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant())
                        .generate().alias(alias).persist()
                        .detachEntity();
        simplePushInstallations.add(simplePushInstallation);

        SimplePushServerSimulator simulator = SimplePushServerSimulator.create().start();

        ups.with(SenderRequest.request())
                .message()
                .pushApplication(getPushApplication())
                .aliases(alias)
                .alert(ALERT_MESSAGE)
                .simplePush(SIMPLEPUSH_VERSION)
                .send();

        List<HttpServerExchange> exchanges = simulator.awaitAndStop(simplePushInstallations.size()).getExchanges();

        assertThat(exchanges, is(notNullValue()));
        assertThat(exchanges.size(), is(simplePushInstallations.size()));

        for (InstallationImpl pushInstallation : simplePushInstallations) {
            boolean found = false;
            for (HttpServerExchange exchange : exchanges) {
                if (exchange.getRequestPath().endsWith(pushInstallation.getDeviceToken())) {
                    found = true;
                }
            }
            if (!found) {
                fail("Message not sent for SimplePush installation " + pushInstallation);
            }
        }

        SenderStatistics statistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(commonAliasInstallations.size(), Duration.FIVE_SECONDS);

        for (InstallationImpl commonAliasInstallation : commonAliasInstallations) {
            assertThat(statistics.deviceTokens.contains(commonAliasInstallation.getDeviceToken()), is(true));
        }

        assertThat(statistics.gcmMessage, is(Matchers.notNullValue()));
        assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
        assertThat(statistics.apnsAlert, is(ALERT_MESSAGE));
        assertThat(statistics.gcmForChromeAlert, is(ALERT_MESSAGE));

        ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).unregister(androidInstallation);
        ups.with(iOSInstallationWorker.worker(), getIOSVariant()).unregister(iosInstallation);
        ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant()).unregister(simplePushInstallation);
    }

    @Test
    public void selectiveSendWithInvalidTokens() {

        List<InstallationImpl> validInstallations = new ArrayList<InstallationImpl>();
        List<InstallationImpl> invalidInstallations = new ArrayList<InstallationImpl>();

        validInstallations.addAll(
                ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).findAll().detachEntities());
        validInstallations.addAll(ups.with(iOSInstallationWorker.worker(), getIOSVariant()).findAll().detachEntities());

        InstallationImpl validAndroidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                .generate().persist()
                .detachEntity();
        validInstallations.add(validAndroidInstallation);

        InstallationImpl invalidAndroidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                .generate().invalidateToken().persist()
                .detachEntity();
        invalidInstallations.add(invalidAndroidInstallation);

        InstallationImpl validIOSInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                .generate().persist()
                .detachEntity();
        validInstallations.add(validIOSInstallation);

        InstallationImpl invalidIOSInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                .generate().invalidateToken().persist()
                .detachEntity();
        invalidInstallations.add(invalidIOSInstallation);

        // FIXME how about GCM for chrome and SimplePush? they do not report client invalidity

        ups.with(SenderRequest.request())
                .message()
                .pushApplication(getPushApplication())
                .variants(getAndroidVariant(), getIOSVariant())
                .alert(ALERT_MESSAGE)
                .send();

        SenderStatistics statistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(validInstallations.size() + invalidInstallations.size(), Duration.FIVE_SECONDS);

        for (InstallationImpl validInstallation : validInstallations) {
            assertThat(statistics.deviceTokens.contains(validInstallation.getDeviceToken()), is(true));
        }

        for (InstallationImpl invalidInstallation : invalidInstallations) {
            assertThat(statistics.deviceTokens.contains(invalidInstallation.getDeviceToken()), is(true));
        }

        List<InstallationImpl> afterSendInstallations = new ArrayList<InstallationImpl>();
        afterSendInstallations.addAll(
                ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).findAll().detachEntities());
        afterSendInstallations.addAll(
                ups.with(iOSInstallationWorker.worker(), getIOSVariant()).findAll().detachEntities());

        assertThat(afterSendInstallations.size(), is(validInstallations.size()));

        List<String> afterSendInstallationsTokens = new ArrayList<String>();
        for (InstallationImpl afterSendInstallation : afterSendInstallations) {
            afterSendInstallationsTokens.add(afterSendInstallation.getDeviceToken());
        }

        for (InstallationImpl validInstallation : validInstallations) {
            assertThat(afterSendInstallationsTokens.contains(validInstallation.getDeviceToken()), is(true));
        }

        assertThat(statistics.gcmMessage, is(notNullValue()));
        assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
        assertThat(statistics.apnsAlert, is(ALERT_MESSAGE));

        ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).unregister(validAndroidInstallation);
        ups.with(iOSInstallationWorker.worker(), getIOSVariant()).unregister(validIOSInstallation);
    }

    @Test
    public void selectiveSendByCommonCategory() {
        String category = UUID.randomUUID().toString();

        List<InstallationImpl> commonAliasInstallations = new ArrayList<InstallationImpl>();
        InstallationImpl androidInstallation = ups.with(AndroidInstallationWorker.worker(), getAndroidVariant())
                .generate().categories(category).persist()
                .detachEntity();
        commonAliasInstallations.add(androidInstallation);

        InstallationImpl iosInstallation = ups.with(iOSInstallationWorker.worker(), getIOSVariant())
                .generate().categories(category).persist()
                .detachEntity();
        commonAliasInstallations.add(iosInstallation);

        List<InstallationImpl> simplePushInstallations = new ArrayList<InstallationImpl>();
        InstallationImpl simplePushInstallation =
                ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant())
                        .generate().categories(category).persist()
                        .detachEntity();
        simplePushInstallations.add(simplePushInstallation);

        SimplePushServerSimulator simulator = SimplePushServerSimulator.create().start();

        ups.with(SenderRequest.request())
                .message()
                .pushApplication(getPushApplication())
                .categories(category)
                .alert(ALERT_MESSAGE)
                .simplePush(SIMPLEPUSH_VERSION)
                .send();

        List<HttpServerExchange> exchanges = simulator.awaitAndStop(simplePushInstallations.size()).getExchanges();

        assertThat(exchanges, is(notNullValue()));
        assertThat(exchanges.size(), is(simplePushInstallations.size()));

        for (InstallationImpl pushInstallation : simplePushInstallations) {
            boolean found = false;
            for (HttpServerExchange exchange : exchanges) {
                if (exchange.getRequestPath().endsWith(pushInstallation.getDeviceToken())) {
                    found = true;
                }
            }
            if (!found) {
                fail("Message not sent for SimplePush installation " + pushInstallation);
            }
        }

        SenderStatistics statistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(commonAliasInstallations.size(), Duration.FIVE_SECONDS);

        for (InstallationImpl commonAliasInstallation : commonAliasInstallations) {
            assertThat(statistics.deviceTokens.contains(commonAliasInstallation.getDeviceToken()), is(true));
        }

        assertThat(statistics.gcmMessage, is(Matchers.notNullValue()));
        assertThat(statistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
        assertThat(statistics.apnsAlert, is(ALERT_MESSAGE));

        ups.with(AndroidInstallationWorker.worker(), getAndroidVariant()).unregister(androidInstallation);
        ups.with(iOSInstallationWorker.worker(), getIOSVariant()).unregister(iosInstallation);
        ups.with(SimplePushInstallationWorker.worker(), getSimplePushVariant()).unregister(simplePushInstallation);
    }

    @Test
    public void selectiveSendByVariants() {
        Map<AbstractVariant, InstallationWorker> variants = new HashMap<AbstractVariant, InstallationWorker>();

        variants.put(getAndroidVariant(), AndroidInstallationWorker.worker());
        variants.put(getIOSVariant(), iOSInstallationWorker.worker());

        selectiveSendByVariants(variants);
    }

    @Test
    public void selectiveSendEmptyPushApplicationId() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);

        ups.with(SenderRequest.request())
                .message()
                .pushApplication(getPushApplication())
                .pushApplicationId("")
                .alert(ALERT_MESSAGE)
                .send();
    }

    @Test
    public void selectiveSendWrongPushApplicationId() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        ups.with(SenderRequest.request())
                .message()
                .pushApplication(getPushApplication())
                .pushApplicationId(UUID.randomUUID().toString())
                .alert(ALERT_MESSAGE)
                .send();
    }

    @Test
    public void selectiveSendWrongMasterSecret() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        ups.with(SenderRequest.request())
                .message()
                .pushApplication(getPushApplication())
                .masterSecret(UUID.randomUUID().toString())
                .alert(ALERT_MESSAGE)
                .send();
    }

    private SenderStatistics selectiveSendByAliases(List<InstallationImpl> installations) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(SenderRequest.request())
                .message()
                .alert(ALERT_MESSAGE);

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliasesWithTtl(List<InstallationImpl> installations, int timeToLive) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(SenderRequest.request())
                .message()
                .alert(ALERT_MESSAGE)
                .timeToLive(timeToLive);

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliasesWithContentAvailable(List<InstallationImpl> installations) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(SenderRequest.request())
                .message()
                .alert(ALERT_MESSAGE)
                .contentAvailable();

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliasesWithAttributes(List<InstallationImpl> installations,
                                                                  Map<String, String> attributes) {
        SenderRequest.UnifiedMessageBlueprint blueprint = ups.with(SenderRequest.request())
                .message()
                .alert(ALERT_MESSAGE);
        for (String key : attributes.keySet()) {
            blueprint.attribute(key, attributes.get(key));
        }

        return selectiveSendByAliases(installations, blueprint);
    }

    private SenderStatistics selectiveSendByAliases(List<InstallationImpl> installations,
                                                    SenderRequest.UnifiedMessageBlueprint messageBlueprint) {

        messageBlueprint.pushApplication(getPushApplication()).aliasesOf(installations).send();

        SenderStatistics senderStatistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(installations.size(), Duration.FIVE_SECONDS);

        assertThat(senderStatistics.deviceTokens.size(), is(installations.size()));
        for (String deviceToken : senderStatistics.deviceTokens) {
            boolean found = false;
            for (InstallationImpl installation : installations) {
                if (deviceToken.equals(installation.getDeviceToken())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Message sent to unknown device token: " + deviceToken);
            }
        }
        return senderStatistics;
    }

    private SenderStatistics selectiveSendByVariants(Map<AbstractVariant, InstallationWorker> variants) {
        ups.with(SenderRequest.request())
                .message()
                .alert(ALERT_MESSAGE)
                .pushApplication(getPushApplication())
                .variants(new ArrayList<Variant>(variants.keySet()))
                .send();

        List<InstallationImpl> installations = new ArrayList<InstallationImpl>();
        for (AbstractVariant variant : variants.keySet()) {
            InstallationWorker worker = variants.get(variant);

            installations.addAll(ups.with(worker, variant).findAll().detachEntities());
        }

        SenderStatistics senderStatistics = ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(installations.size(), Duration.FIVE_SECONDS);

        assertThat(installations.size(), is(senderStatistics.deviceTokens.size()));
        for (InstallationImpl installation : installations) {
            assertThat(senderStatistics.deviceTokens.contains(installation.getDeviceToken()), is(true));
        }

        assertThat(senderStatistics.gcmMessage, is(notNullValue()));
        assertThat(senderStatistics.gcmMessage.getData().get("alert"), is(ALERT_MESSAGE));
        assertThat(senderStatistics.apnsAlert, is(ALERT_MESSAGE));

        return senderStatistics;
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
            Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return exchanges.size() == count;
                }
            });
            return stop();
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
