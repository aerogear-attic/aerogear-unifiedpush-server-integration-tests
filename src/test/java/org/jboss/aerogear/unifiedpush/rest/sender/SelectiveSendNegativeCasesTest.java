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
package org.jboss.aerogear.unifiedpush.rest.sender;

import java.net.URL;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

import com.google.android.gcm.server.Sender;
import com.notnoop.apns.internal.ApnsServiceImpl;

public class SelectiveSendNegativeCasesTest extends GenericUnifiedPushTest {

    @ArquillianResource
    private URL context;

    @Override
    protected String getContextRoot() {
        return context.toExternalForm();
    }

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Rule
    public CheckingExpectedException thrown = CheckingExpectedException.none();

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Test
    @InSequence(12)
    public void selectiveSendEmptyPushAppId() {
        ApnsServiceImpl.clear();
        Sender.clear();

        UnifiedMessage.Builder message = new UnifiedMessage.Builder();
        message.pushApplicationId("")
            .alert(NOTIFICATION_ALERT_MSG)
            .masterSecret(getRegisteredPushApplication().getMasterSecret());

        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        PushNotificationSenderUtils.send(message.build(), getSession());
    }

    @Test
    @InSequence(13)
    public void selectiveSendWrongPushAppId() {
        ApnsServiceImpl.clear();
        Sender.clear();

        PushApplication generatedPushApplication = PushApplicationUtils.generate();

        UnifiedMessage.Builder message = new UnifiedMessage.Builder();
        message.pushApplicationId(generatedPushApplication.getPushApplicationID())
            .alert(NOTIFICATION_ALERT_MSG)
            .masterSecret(getRegisteredPushApplication().getMasterSecret());

        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        PushNotificationSenderUtils.send(message.build(), getSession());

    }

    @Test
    @InSequence(14)
    public void selectiveSendWrongMasterSecret() {
        ApnsServiceImpl.clear();
        Sender.clear();

        PushApplication generatedPushApplication = PushApplicationUtils.generate();

        UnifiedMessage.Builder message = new UnifiedMessage.Builder();
        message.pushApplicationId(getRegisteredPushApplication().getPushApplicationID())
            .alert(NOTIFICATION_ALERT_MSG)
            .masterSecret(generatedPushApplication.getMasterSecret());

        thrown.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);
        PushNotificationSenderUtils.send(message.build(), getSession());
    }

}
