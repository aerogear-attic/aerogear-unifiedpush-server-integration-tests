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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.service.sender.message.SendCriteria;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.aerogear.unifiedpush.utils.SenderStatisticsEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.notnoop.apns.internal.ApnsServiceImpl;

public class iOSSelectiveSendByAliasTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String NOTIFICATION_SOUND = "default";

    private final static int NOTIFICATION_BADGE = 7;

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Deployment(testable = false)
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

        // FIXME This is way to repetitive and should be in PushNotificationSenderUtils
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("alert", NOTIFICATION_ALERT_MSG);
        data.put("sound", NOTIFICATION_SOUND);
        data.put("badge", NOTIFICATION_BADGE);

        SendCriteria criteria = PushNotificationSenderUtils.createCriteria(aliases, null, null, null);

        UnifiedPushMessage message = PushNotificationSenderUtils.createMessage(criteria, data);

        PushNotificationSenderUtils.send(getRegisteredPushApplication(), message, getSession());
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
        assertEquals(NOTIFICATION_BADGE, senderStatistics.apnsBadge);
    }

    @Test
    @InSequence(14)
    public void verifyInactiveTokensDeletion() {
        List<InstallationImpl> installations = InstallationUtils.listAll(getRegisteredIOSVariant(), getSession());

        assertNotNull(installations);
        assertEquals(getRegisteredIOSInstallations().size() - 2, installations.size());
    }
}
