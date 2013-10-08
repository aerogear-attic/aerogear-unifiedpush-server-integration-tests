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
package org.jboss.aerogear.unifiedpush.simplepush;

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

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.aerogear.unifiedpush.utils.ServerSocketUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class SimplePushSelectiveSendByAliasTest extends GenericUnifiedPushTest {

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";
    
    private final static String SIMPLE_PUSH_VERSION = "version=15";
    
    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                SimplePushSelectiveSendByAliasTest.class);
    }

    @RunAsClient
    @Test
    @InSequence(12)
    public void simplePushSelectiveSendByAliasesAndDeviceType() throws UnknownHostException, IOException {

        assertNotNull(getPushApplicationId());
        assertNotNull(getMasterSecret());

        List<String> aliases = new ArrayList<String>();
        aliases.add(SIMPLE_PUSH_CLIENT_ALIAS);

        List<String> deviceTypes = new ArrayList<String>();
        deviceTypes.add(SIMPLE_PUSH_DEVICE_TYPE);

        Map<String, String> simplePush = new HashMap<String, String>();
        simplePush.put(SIMPLE_PUSH_CATEGORY, SIMPLE_PUSH_VERSION);

        Response response = PushNotificationSenderUtils.selectiveSend(getPushApplicationId(), getMasterSecret(), aliases, null,
                new HashMap<String, Object>(), simplePush, null, null, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());

        ServerSocket socketServer = ServerSocketUtils.createServerSocket(Constants.SOCKET_SERVER_PORT);
        assertNotNull(socketServer);

        final String serverInput = ServerSocketUtils.readUntilMessageIsShown(socketServer, NOTIFICATION_ALERT_MSG);

        assertNotNull(serverInput);
        assertTrue(serverInput.contains(SIMPLE_PUSH_VERSION));
        assertTrue(serverInput.contains("PUT /endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN));
    }

}
