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
package org.jboss.aerogear.unifiedpush.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.service.AndroidVariantService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
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
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.response.Response;

public class AndroidSelectiveSendByOsTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private static final String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class, AndroidSelectiveSendByOsTest.class);
    }

    @Inject
    private AndroidVariantService androidVariantService;

    @Inject
    private ClientInstallationService clientInstallationService;

    @RunAsClient
    @Test
    @InSequence(12)
    public void androidSelectiveSendByAliases() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getMasterSecret());

        List<String> platforms = new ArrayList<String>();
        platforms.add(ANDROID_DEVICE_OS);
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        Response response = PushNotificationSenderUtils.selectiveSend(getPushApplicationId(), getMasterSecret(), null, null,
                messages, null, platforms, null, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(13)
    public void verifyGCMnotifications() {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 3;
            }
        });

        assertTrue(Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN)
                && Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN_2)
                && Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN_3));

        assertNotNull(Sender.getGcmMessage());
        assertEquals(NOTIFICATION_ALERT_MSG, Sender.getGcmMessage().getData().get("alert"));
    }

    // The GCM Sender returns the tokens as inactive so they should have been deleted
    @Test
    @InSequence(14)
    public void verifyInactiveTokensDeletion() {
        assertNotNull(androidVariantService);
        assertNotNull(clientInstallationService);

        List<AndroidVariant> androidVariants = androidVariantService.findAllAndroidVariants();
        AndroidVariant androidVariant = androidVariants != null ? androidVariants.get(0) : null;
        List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                androidVariant.getVariantID(), null, null, null);

        assertNotNull(deviceTokens);
        assertTrue(!deviceTokens.contains(ANDROID_DEVICE_TOKEN) && !deviceTokens.contains(ANDROID_DEVICE_TOKEN_2)
                && !deviceTokens.contains(ANDROID_DEVICE_TOKEN_3));
    }
}
