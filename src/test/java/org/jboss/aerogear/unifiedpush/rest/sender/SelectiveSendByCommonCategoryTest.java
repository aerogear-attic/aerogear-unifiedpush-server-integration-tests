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
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.aerogear.unifiedpush.utils.SenderStatisticsEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.google.android.gcm.server.Sender;
import com.notnoop.apns.internal.ApnsServiceImpl;

public class SelectiveSendByCommonCategoryTest extends GenericUnifiedPushTest {

    @ArquillianResource
    private URL context;

    @Override
    protected String getContextRoot() {
        return context.toExternalForm();
    }

    private static final String NOTIFICATION_ALERT_MSG = "TEST ALERT";

    private static final String COMMON_CATEGORY = UUID.randomUUID().toString();

    private static List<InstallationImpl> installationsWithCommonCategory;
    private static InstallationImpl simplePushInstallation;

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Test
    @InSequence(12)
    public void registerAndroidInstallation() {
        // FIXME this will work only if this test is first in sequence!
        installationsWithCommonCategory = new ArrayList<InstallationImpl>();

        InstallationImpl generatedInstallation = InstallationUtils.generateAndroid();

        HashSet<String> categories = new HashSet<String>();
        categories.add(COMMON_CATEGORY);
        generatedInstallation.setCategories(categories);

        InstallationUtils.register(generatedInstallation, getRegisteredAndroidVariant(), getSession());

        installationsWithCommonCategory.add(generatedInstallation);
    }

    @Test
    @InSequence(13)
    public void registeriOSInstallation() {
        InstallationImpl generatedInstallation = InstallationUtils.generateIos();

        HashSet<String> categories = new HashSet<String>();
        categories.add(COMMON_CATEGORY);
        generatedInstallation.setCategories(categories);

        InstallationUtils.register(generatedInstallation, getRegisteredIOSVariant(), getSession());

        installationsWithCommonCategory.add(generatedInstallation);
    }

    @Override
    @Test
    @InSequence(14)
    public void registerSimplePushInstallation() {
        InstallationImpl generatedInstallation = InstallationUtils.generateSimplePush();

        HashSet<String> categories = new HashSet<String>();
        categories.add(COMMON_CATEGORY);
        generatedInstallation.setCategories(categories);

        InstallationUtils.register(generatedInstallation, getRegisteredSimplePushVariant(), getSession());

        // FIXME SimplePush should be also done the same way Sender and ApnsServiceImpl are done
        simplePushInstallation = generatedInstallation;
        // installationsWithCommonCategory.add(generatedInstallation);
    }

    @Test
    @InSequence(16)
    public void selectiveSendByCommonCategory() {
        Sender.clear();
        ApnsServiceImpl.clear();

        List<String> categories = new ArrayList<String>();
        categories.add(COMMON_CATEGORY);

        UnifiedMessage.Builder message = new UnifiedMessage.Builder().categories(COMMON_CATEGORY)
            .alert(NOTIFICATION_ALERT_MSG)
            .pushApplicationId(getRegisteredPushApplication().getPushApplicationID())
            .masterSecret(getRegisteredPushApplication().getMasterSecret());

        PushNotificationSenderUtils.send(message.build(), getSession());
    }

    @Test
    @InSequence(17)
    public void verifyPushNotifications() {
        SenderStatisticsEndpoint.SenderStatistics senderStatistics = PushNotificationSenderUtils.waitSenderStatisticsAndReset(
            installationsWithCommonCategory.size(), getSession());

        for (InstallationImpl installation : installationsWithCommonCategory) {
            assertTrue(senderStatistics.deviceTokens.contains(installation.getDeviceToken()));
        }

        assertNotNull(senderStatistics.gcmMessage);
        assertEquals(NOTIFICATION_ALERT_MSG, senderStatistics.gcmMessage.getData().get("alert"));
        assertEquals(NOTIFICATION_ALERT_MSG, senderStatistics.apnsAlert);
    }
}
