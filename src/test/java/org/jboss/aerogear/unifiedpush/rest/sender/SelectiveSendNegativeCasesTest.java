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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.ExpectedException;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;

import com.google.android.gcm.server.Sender;
import com.notnoop.apns.internal.ApnsServiceImpl;

public class SelectiveSendNegativeCasesTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Test
    @InSequence(12)
    public void selectiveSendEmptyPushAppId() {
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        PushApplication generatedPushApplication = PushApplicationUtils.generate();

        generatedPushApplication.setPushApplicationID("");
        generatedPushApplication.setMasterSecret(getRegisteredPushApplication().getMasterSecret());

        UnifiedPushMessage message = PushNotificationSenderUtils.createMessage(null, messages);

        thrown.expectUnexpectedResponseException(Status.UNAUTHORIZED);
        PushNotificationSenderUtils.send(generatedPushApplication, message, getContextRoot());
    }

    @Test
    @InSequence(13)
    public void selectiveSendWrongPushAppId() {
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        PushApplication generatedPushApplication = PushApplicationUtils.generate();

        generatedPushApplication.setPushApplicationID(UUID.randomUUID().toString());
        generatedPushApplication.setMasterSecret(getRegisteredPushApplication().getMasterSecret());

        UnifiedPushMessage message = PushNotificationSenderUtils.createMessage(null, messages);

        thrown.expectUnexpectedResponseException(Status.UNAUTHORIZED);
        PushNotificationSenderUtils.send(generatedPushApplication, message, getContextRoot());
    }

    @Test
    @InSequence(14)
    public void selectiveSendWrongMasterSecret() {
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        PushApplication generatedPushApplication = PushApplicationUtils.generate();

        generatedPushApplication.setPushApplicationID(getRegisteredPushApplication().getPushApplicationID());
        generatedPushApplication.setMasterSecret(UUID.randomUUID().toString());

        UnifiedPushMessage message = PushNotificationSenderUtils.createMessage(null, messages);

        thrown.expectUnexpectedResponseException(Status.UNAUTHORIZED);
        PushNotificationSenderUtils.send(generatedPushApplication, message, getContextRoot());
    }

}
