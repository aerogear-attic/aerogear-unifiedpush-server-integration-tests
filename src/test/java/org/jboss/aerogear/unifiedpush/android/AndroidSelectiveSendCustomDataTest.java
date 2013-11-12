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
package org.jboss.aerogear.unifiedpush.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.google.android.gcm.server.Sender;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.response.Response;

public class AndroidSelectiveSendCustomDataTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    private final static String CUSTOM_FIELD_DATA_MSG = "custom field msg";

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                AndroidSelectiveSendCustomDataTest.class);
    }

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    private ClientInstallationService clientInstallationService;

    @RunAsClient
    @Test
    @InSequence(12)
    public void androidCustomDataSelectiveSendByAliases() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getMasterSecret());

        List<String> aliases = new ArrayList<String>();
        aliases.add(ANDROID_CLIENT_ALIAS);
        aliases.add(ANDROID_CLIENT_ALIAS_2);
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("custom", NOTIFICATION_ALERT_MSG);
        messages.put("test", CUSTOM_FIELD_DATA_MSG);

        Response response = PushNotificationSenderUtils.selectiveSend(getPushApplicationId(), getMasterSecret(), aliases, null,
                messages, null, null, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(13)
    public void verifyGCMnotifications() {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 2;
            }
        });

        assertTrue(Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN)
                && Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN_2));

        assertNotNull(Sender.getGcmMessage());
        assertEquals(NOTIFICATION_ALERT_MSG, Sender.getGcmMessage().getData().get("custom"));
        assertEquals(CUSTOM_FIELD_DATA_MSG, Sender.getGcmMessage().getData().get("test"));
    }

    // The GCM Sender returns the tokens as inactive so they should have been deleted
    @Test
    @InSequence(14)
    public void verifyInactiveTokensDeletion() {
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());

        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));

        PushApplication pushApp = pushApps.iterator().next();

        Set<AndroidVariant> androidVariants = pushApp.getAndroidVariants();
        AndroidVariant androidVariant = androidVariants != null ? androidVariants.iterator().next() : null;
        List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                androidVariant.getVariantID(), null, null, null);

        assertNotNull(deviceTokens);
        assertFalse("Android device tokens " + ANDROID_DEVICE_TOKEN + " " + ANDROID_DEVICE_TOKEN_2 + " were inactivated",
                deviceTokens.contains(ANDROID_DEVICE_TOKEN) || deviceTokens.contains(ANDROID_DEVICE_TOKEN_2));
    }
}
