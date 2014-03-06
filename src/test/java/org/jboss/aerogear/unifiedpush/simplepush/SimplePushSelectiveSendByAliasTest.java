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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.aerogear.unifiedpush.utils.ServerSocketUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

public class SimplePushSelectiveSendByAliasTest extends GenericUnifiedPushTest {

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    private final static String SIMPLE_PUSH_VERSION = "version=15";

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses();
    }

    @Test
    @InSequence(12)
    public void simplePushSelectiveSendByAliasesAndDeviceType() throws UnknownHostException, IOException {
        InstallationImpl registeredInstallation = getRegisteredSimplePushInstallations().get(0);

        List<String> aliases = new ArrayList<String>();
        aliases.add(registeredInstallation.getAlias());

        List<String> deviceTypes = new ArrayList<String>();
        deviceTypes.add(registeredInstallation.getDeviceType());

        UnifiedMessage.Builder message = new UnifiedMessage.Builder().aliases(aliases)
            .deviceType(deviceTypes).categories(registeredInstallation.getCategories().toArray(new String[0]))
            .simplePush("15")
            .pushApplicationId(getRegisteredPushApplication().getPushApplicationID())
            .masterSecret(getRegisteredPushApplication().getMasterSecret());

        ServerSocket serverSocket = ServerSocketUtils.createServerSocket(Constants.SOCKET_SERVER_PORT);
        assertNotNull(serverSocket);

        PushNotificationSenderUtils.send(message.build(), getSession());

        final String serverInput = ServerSocketUtils.readUntilMessageIsShown(serverSocket, NOTIFICATION_ALERT_MSG);

        assertNotNull(serverInput);
        assertTrue(serverInput.contains(SIMPLE_PUSH_VERSION));
        assertTrue(serverInput.contains("PUT /endpoint/" + registeredInstallation.getDeviceToken()));
    }

}
