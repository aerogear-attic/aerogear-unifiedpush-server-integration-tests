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
package org.jboss.aerogear.unifiedpush.ios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.aerogear.unifiedpush.utils.SenderStatisticsEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.notnoop.apns.internal.ApnsServiceImpl;

public class iOSSelectiveSendCustomDataTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String CUSTOM_FIELD_DATA_KEY = "custom";

    private final static String CUSTOM_FIELD_DATA_MSG = "custom field msg";

    private final static String NOTIFICATION_SOUND = "default";

    private final static String NOTIFICATION_BADGE = "7";

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Test
    @InSequence(12)
    public void iOSCustomDataSelectiveSendByAliases() {
        List<String> aliases = new ArrayList<String>();

        for (int i = 0; i < 2; i++) {
            InstallationImpl installation = getRegisteredIOSInstallations().get(i);

            aliases.add(installation.getAlias());
        }

        ApnsServiceImpl.clear();

        UnifiedMessage.Builder message = new UnifiedMessage.Builder().aliases(aliases)
            .alert(NOTIFICATION_ALERT_MSG)
            .badge(NOTIFICATION_BADGE)
            .sound(NOTIFICATION_SOUND)
            .attribute(CUSTOM_FIELD_DATA_KEY, CUSTOM_FIELD_DATA_MSG)
            .pushApplicationId(getRegisteredPushApplication().getPushApplicationID())
            .masterSecret(getRegisteredPushApplication().getMasterSecret());

        PushNotificationSenderUtils.send(message.build(), getSession());
    }

    @Test
    @InSequence(13)
    public void verifyiOSnotifications() {

        SenderStatisticsEndpoint.SenderStatistics senderStatistics = PushNotificationSenderUtils.waitSenderStatisticsAndReset(
            2, getSession());

        for (int i = 0; i < 2; i++) {
            InstallationImpl installation = getRegisteredIOSInstallations().get(i);

            assertTrue(senderStatistics.deviceTokens.contains(installation.getDeviceToken()));
        }

        assertEquals(NOTIFICATION_ALERT_MSG, senderStatistics.apnsAlert);
        assertEquals(NOTIFICATION_SOUND, senderStatistics.apnsSound);
        assertEquals(Integer.parseInt(NOTIFICATION_BADGE), senderStatistics.apnsBadge);

        // assertTrue(ApnsServiceImpl.getCustomFields() != null
        // && ApnsServiceImpl.getCustomFields().contains(CUSTOM_FIELD_DATA_KEY + "=" + CUSTOM_FIELD_DATA_MSG));
    }

    @Test
    @InSequence(14)
    public void verifyInactiveTokensDeletion() {
        List<InstallationImpl> installations = InstallationUtils.listAll(getRegisteredIOSVariant(), getSession());

        assertNotNull(installations);
        assertEquals(getRegisteredIOSInstallations().size() - 2, installations.size());
    }
}
