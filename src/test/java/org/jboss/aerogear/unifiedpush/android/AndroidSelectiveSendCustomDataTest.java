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

import com.google.android.gcm.server.Sender;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.sender.message.SendCriteria;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

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
    private ClientInstallationService clientInstallationService;

    @Inject
    private PushApplicationService pushApplicationService;

    @RunAsClient
    @Test
    @InSequence(12)
    public void androidCustomDataSelectiveSendByAliases() {
        List<String> aliases = new ArrayList<String>();
        for (int i = 0; i < 2; i++) {
            InstallationImpl installation = getRegisteredAndroidInstallations().get(i);

            aliases.add(installation.getAlias());
        }

        Sender.clear();

        Map<String, Object> customData = new HashMap<String, Object>();
        customData.put("custom", NOTIFICATION_ALERT_MSG);
        customData.put("test", CUSTOM_FIELD_DATA_MSG);

        SendCriteria criteria = PushNotificationSenderUtils.createCriteria(aliases, null, null, null);

        UnifiedPushMessage message = PushNotificationSenderUtils.createMessage(criteria, customData);

        PushNotificationSenderUtils.send(getRegisteredPushApplication(), message, getContextRoot());
    }

    @Test
    @InSequence(13)
    public void verifyGCMnotifications() {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 2;
            }
        });

        for (int i = 0; i < 2; i++) {
            InstallationImpl installation = getRegisteredAndroidInstallations().get(i);

            assertTrue(Sender.getGcmRegIdsList().contains(installation.getDeviceToken()));
        }

        assertNotNull(Sender.getGcmMessage());
        assertEquals(NOTIFICATION_ALERT_MSG, Sender.getGcmMessage().getData().get("custom"));
        assertEquals(CUSTOM_FIELD_DATA_MSG, Sender.getGcmMessage().getData().get("test"));
    }

    // The GCM Sender returns the tokens as inactive so they should have been deleted
    @Test
    @InSequence(14)
    public void verifyInactiveTokensDeletion() {
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApplications = pushApplicationService.findAllPushApplicationsForDeveloper
                (getSession().getLoginName());

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());
        assertTrue(PushApplicationUtils.nameExistsInList(getRegisteredPushApplication().getName(), pushApplications));

        PushApplication pushApplication = pushApplications.iterator().next();

        Set<AndroidVariant> androidVariants = pushApplication.getAndroidVariants();
        AndroidVariant androidVariant = androidVariants != null ? androidVariants.iterator().next() : null;

        List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                androidVariant.getVariantID(), null, null, null);

        assertNotNull(deviceTokens);
        assertFalse("Android device tokens " + ANDROID_DEVICE_TOKEN + " " + ANDROID_DEVICE_TOKEN_2 + " were " +
                "innactivated", deviceTokens.contains(ANDROID_DEVICE_TOKEN) || deviceTokens.contains
                (ANDROID_DEVICE_TOKEN_2));
    }
}
