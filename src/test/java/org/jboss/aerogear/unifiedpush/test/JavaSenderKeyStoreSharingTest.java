/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.test;

import com.jayway.awaitility.Duration;

import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.extension.CleanupRequest;
import org.jboss.aerogear.test.api.extension.JavaSenderTestRequest;
import org.jboss.aerogear.test.api.extension.SenderStatisticsRequest;
import org.jboss.aerogear.test.api.installation.android.AndroidInstallationWorker;
import org.jboss.aerogear.test.api.variant.android.AndroidVariantWorker;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(ArquillianRules.class)
public class JavaSenderKeyStoreSharingTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {
            with(CleanupRequest.request()).cleanApplications();

            PushApplication application = ups.with(PushApplicationWorker.worker()).generate().persist().detachEntity();

            AndroidVariant variant = ups.with(AndroidVariantWorker.worker(), application).generate().persist()
                    .detachEntity();

            ups.with(AndroidInstallationWorker.worker(), variant).generate().persist();

            return this;
        }
    };

    @After
    public void cleanPushApplications() {
        ups.with(PushApplicationWorker.worker()).findAll().removeAll();
    }

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

    private PushApplication getPushApp() {
        return ups.with(PushApplicationWorker.worker()).findAll().detachEntity();
    }

    @Test
    public void javaSenderSharesKeystoreWithPushApplicationOnSameJvm() throws InterruptedException {
        String appId = getPushApp().getPushApplicationID();
        // send message
        String response = ups.with(JavaSenderTestRequest.request())
                .sendTestMessage(getPushApp(), ups.unifiedPushUrl, "Hello world!");

        assertThat(response, is("Message sent!"));

        // check that the message was sent - just one message was sent
        ups.with(SenderStatisticsRequest.request())
                .awaitGetAndClear(1, Duration.FIVE_SECONDS);
    }

}
