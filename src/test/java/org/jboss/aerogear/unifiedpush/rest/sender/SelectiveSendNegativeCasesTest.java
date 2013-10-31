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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.google.android.gcm.server.Sender;
import com.jayway.restassured.response.Response;
import com.notnoop.apns.internal.ApnsServiceImpl;

public class SelectiveSendNegativeCasesTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String SIMPLE_PUSH_VERSION = "version=15";

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class, SelectiveSendNegativeCasesTest.class);
    }

    @RunAsClient
    @Test
    @InSequence(12)
    public void selectiveSendEmptyPushAppId() {

        assertNotNull(getMasterSecret());

        List<String> aliases = new ArrayList<String>();
        aliases.add(ANDROID_CLIENT_ALIAS);
        aliases.add(ANDROID_CLIENT_ALIAS_2);
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        Response response = PushNotificationSenderUtils.selectiveSend("", getMasterSecret(), aliases, null, messages,
                SIMPLE_PUSH_VERSION, SIMPLE_PUSH_CATEGORY, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(13)
    public void selectiveSendWrongPushAppId() {

        assertNotNull(getMasterSecret());

        List<String> aliases = new ArrayList<String>();
        aliases.add(ANDROID_CLIENT_ALIAS);
        aliases.add(ANDROID_CLIENT_ALIAS_2);
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        String wrongPushAppId = "random";
        Response response = PushNotificationSenderUtils.selectiveSend(wrongPushAppId, getMasterSecret(), aliases, null,
                messages, SIMPLE_PUSH_VERSION, SIMPLE_PUSH_CATEGORY, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(14)
    public void selectiveSendWrongMasterSecret() {

        assertNotNull(getPushApplicationId());

        List<String> aliases = new ArrayList<String>();
        aliases.add(ANDROID_CLIENT_ALIAS);
        aliases.add(ANDROID_CLIENT_ALIAS_2);
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        String wrongMasterSecret = "random";
        Response response = PushNotificationSenderUtils.selectiveSend(getPushApplicationId(), wrongMasterSecret, aliases, null,
                messages, SIMPLE_PUSH_VERSION, SIMPLE_PUSH_CATEGORY, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

}
