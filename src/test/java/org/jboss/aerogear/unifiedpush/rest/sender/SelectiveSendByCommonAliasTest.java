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

import com.google.android.gcm.server.Sender;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.notnoop.apns.internal.ApnsServiceImpl;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.service.sender.message.SendCriteria;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.aerogear.unifiedpush.utils.ServerSocketUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class SelectiveSendByCommonAliasTest extends GenericUnifiedPushTest {

    private final static String SIMPLE_PUSH_VARIANT_NETWORK_URL = "http://localhost:" + Constants.SOCKET_SERVER_PORT
            + "/endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN;

    private final static String SIMPLE_PUSH_VERSION = "version=15";

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    private static final String COMMON_ALIAS = UUID.randomUUID().toString();

    private static List<InstallationImpl> installationsWithCommonAlias = new ArrayList<InstallationImpl>();
    private static InstallationImpl simplePushInstallation;

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String COMMON_IOS_ANDROID_SIMPLE_PUSH_CLIENT_ALIAS = "qa_ios_android_simplepush@aerogear";

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                SelectiveSendByCommonAliasTest.class);
    }

    @RunAsClient
    @Test
    @InSequence(12)
    public void registeriOSInstallation() {
        InstallationImpl generatedInstallation = InstallationUtils.generateIos();

        generatedInstallation.setAlias(COMMON_ALIAS);

        InstallationUtils.register(generatedInstallation, getRegisteredIOSVariant(), getContextRoot());

        installationsWithCommonAlias.add(generatedInstallation);
    }

    @RunAsClient
    @Test
    @InSequence(13)
    public void registerAndroidInstallation() {
        InstallationImpl generatedInstallation = InstallationUtils.generateAndroid();

        generatedInstallation.setAlias(COMMON_ALIAS);

        InstallationUtils.register(generatedInstallation, getRegisteredAndroidVariant(), getContextRoot());

        installationsWithCommonAlias.add(generatedInstallation);
    }

    @RunAsClient
    @Test
    @InSequence(14)
    public void registerSimplePushInstallation() {
        InstallationImpl generatedInstallation = InstallationUtils.generateSimplePush();

        generatedInstallation.setAlias(COMMON_ALIAS);

        InstallationUtils.register(generatedInstallation, getRegisteredSimplePushVariant(), getContextRoot());

        // FIXME SimplePush should be also done the same way Sender and ApnsServiceImpl are done
        simplePushInstallation = generatedInstallation;
        // installationsWithCommonAlias.add(generatedInstallation);
    }

    @RunAsClient
    @Test
    @InSequence(15)
    public void selectiveSendByCommonAlias() throws UnknownHostException, IOException {
        List<String> aliases = new ArrayList<String>();
        aliases.add(COMMON_ALIAS);
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("alert", NOTIFICATION_ALERT_MSG);
        data.put("simple-push", SIMPLE_PUSH_VERSION);

        SendCriteria criteria = PushNotificationSenderUtils.createCriteria(aliases, null, null, null);

        UnifiedPushMessage message = PushNotificationSenderUtils.createMessage(criteria, data);

        PushNotificationSenderUtils.send(getRegisteredPushApplication(), message, getContextRoot());

        ServerSocket serverSocket = ServerSocketUtils.createServerSocket(Constants.SOCKET_SERVER_PORT);
        assertNotNull(serverSocket);

        final String serverInput = ServerSocketUtils.readUntilMessageIsShown(serverSocket, NOTIFICATION_ALERT_MSG);

        assertNotNull(serverInput);
        assertTrue(serverInput.contains(SIMPLE_PUSH_VERSION));
        assertTrue(serverInput.contains("PUT /endpoint/" + simplePushInstallation.getDeviceToken()));
    }

    @Test
    @InSequence(16)
    public void verifyGCMandAPNnotifications() {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 1
                        && ApnsServiceImpl.getTokensList() != null && ApnsServiceImpl.getTokensList().size() == 1;
            }
        });

        for (InstallationImpl installation : installationsWithCommonAlias) {
            assertTrue(Sender.getGcmRegIdsList().contains(installation.getDeviceToken()) ||
                    ApnsServiceImpl.getTokensList().contains(installation.getDeviceToken()));
        }

        assertTrue(Sender.getGcmMessage() != null
                && NOTIFICATION_ALERT_MSG.equals(Sender.getGcmMessage().getData().get("alert")));
        assertEquals(NOTIFICATION_ALERT_MSG, ApnsServiceImpl.getAlert());
    }
}
