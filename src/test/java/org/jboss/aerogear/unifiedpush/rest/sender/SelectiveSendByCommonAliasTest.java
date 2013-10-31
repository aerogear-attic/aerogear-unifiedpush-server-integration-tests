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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
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

import com.google.android.gcm.server.Sender;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.response.Response;
import com.notnoop.apns.internal.ApnsServiceImpl;

public class SelectiveSendByCommonAliasTest extends GenericUnifiedPushTest {

    private final static String SIMPLE_PUSH_VARIANT_NETWORK_URL = "http://localhost:" + Constants.SOCKET_SERVER_PORT
            + "/endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN;

    private final static String SIMPLE_PUSH_VERSION = "version=15";

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String COMMON_IOS_ANDROID_SIMPLE_PUSH_CLIENT_ALIAS = "qa_ios_android_simplepush@aerogear";

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class, SelectiveSendByCommonAliasTest.class);
    }

    @RunAsClient
    @Test
    @InSequence(12)
    public void registeriOSInstallation() {
        assertNotNull(getiOSVariantId());
        assertNotNull(getiOSPushSecret());

        InstallationImpl iOSInstallation = InstallationUtils.createInstallation(IOS_DEVICE_TOKEN, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, IOS_CLIENT_ALIAS, null, null);
        Response response = InstallationUtils.registerInstallation(getiOSVariantId(), getiOSPushSecret(), iOSInstallation,
                getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(13)
    public void registerSecondiOSInstallation() {
        assertNotNull(getiOSVariantId());
        assertNotNull(getiOSPushSecret());

        InstallationImpl iOSInstallation = InstallationUtils.createInstallation(IOS_DEVICE_TOKEN_2, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_SIMPLE_PUSH_CLIENT_ALIAS, null, null);
        Response response = InstallationUtils.registerInstallation(getiOSVariantId(), getiOSPushSecret(), iOSInstallation,
                getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(14)
    public void registerAndroidInstallation() {

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS, null, null);
        Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                androidInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(15)
    public void registerSecondAndroidInstallation() {

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN_2,
                ANDROID_DEVICE_TYPE_2, ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS_2, null, null);
        Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                androidInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(16)
    public void registerThirdAndroidInstallation() {

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN_3,
                ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_SIMPLE_PUSH_CLIENT_ALIAS,
                null, null);
        Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                androidInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(17)
    public void registerSimplePushInstallation() {

        assertNotNull(getSimplePushVariantId());
        assertNotNull(getSimplePushSecret());

        InstallationImpl simplePushInstallation = InstallationUtils.createInstallation(SIMPLE_PUSH_DEVICE_TOKEN,
                SIMPLE_PUSH_DEVICE_TYPE, SIMPLE_PUSH_DEVICE_OS, "", COMMON_IOS_ANDROID_SIMPLE_PUSH_CLIENT_ALIAS,
                SIMPLE_PUSH_CATEGORY, SIMPLE_PUSH_VARIANT_NETWORK_URL);
        Response response = InstallationUtils.registerInstallation(getSimplePushVariantId(), getSimplePushSecret(),
                simplePushInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(18)
    public void selectiveSendByCommonAlias() throws UnknownHostException, IOException {

        assertNotNull(getPushApplicationId());
        assertNotNull(getMasterSecret());

        List<String> aliases = new ArrayList<String>();
        aliases.add(COMMON_IOS_ANDROID_SIMPLE_PUSH_CLIENT_ALIAS);
        ApnsServiceImpl.clear();
        Sender.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        // TODO shouldn't we pass "SIMPLE_PUSH_CATEGORY" instead "null" as the last but one parameter
        Response response = PushNotificationSenderUtils.selectiveSend(getPushApplicationId(), getMasterSecret(), aliases, null,
                messages, SIMPLE_PUSH_VERSION, null, getContextRoot());


        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());

        ServerSocket socketServer = ServerSocketUtils.createServerSocket(Constants.SOCKET_SERVER_PORT);
        assertNotNull(socketServer);

        final String serverInput = ServerSocketUtils.readUntilMessageIsShown(socketServer, NOTIFICATION_ALERT_MSG);

        assertNotNull(serverInput);
        assertTrue(serverInput.contains(SIMPLE_PUSH_VERSION));
        assertTrue(serverInput.contains("PUT /endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN));
    }

    @Test
    @InSequence(19)
    public void verifyGCMandAPNnotifications() {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 1
                        && ApnsServiceImpl.getTokensList() != null && ApnsServiceImpl.getTokensList().size() == 1;
            }
        });

        assertTrue(Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN_3));
        assertTrue(Sender.getGcmMessage() != null
                && NOTIFICATION_ALERT_MSG.equals(Sender.getGcmMessage().getData().get("alert")));
        assertTrue(ApnsServiceImpl.getTokensList().contains(IOS_DEVICE_TOKEN_2));
        assertEquals(NOTIFICATION_ALERT_MSG, ApnsServiceImpl.getAlert());
    }
}
