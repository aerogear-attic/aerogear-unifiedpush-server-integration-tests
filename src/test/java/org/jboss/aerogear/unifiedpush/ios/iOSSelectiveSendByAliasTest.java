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
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
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

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.response.Response;
import com.notnoop.apns.internal.ApnsServiceImpl;

public class iOSSelectiveSendByAliasTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private final static String NOTIFICATION_SOUND = "default";

    private final static int NOTIFICATION_BADGE = 7;

    private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers";

    @Inject
    private PushApplicationService pushAppService;
    
    @Inject
    private ClientInstallationService clientInstallationService;

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class, iOSSelectiveSendByAliasTest.class);
    }

    @RunAsClient
    @Test
    @InSequence(12)
    public void iOSCustomDataSelectiveSendByAliases() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getMasterSecret());

        List<String> aliases = new ArrayList<String>();
        aliases.add(IOS_CLIENT_ALIAS);
        ApnsServiceImpl.clear();

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);
        messages.put("sound", NOTIFICATION_SOUND);
        messages.put("badge", NOTIFICATION_BADGE);

        Response response = PushNotificationSenderUtils.selectiveSend(getPushApplicationId(), getMasterSecret(), aliases,
                null, messages, null, null, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(13)
    public void verifyiOSnotifications() {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return ApnsServiceImpl.getTokensList() != null && ApnsServiceImpl.getTokensList().size() == 1;
            }
        });

        assertTrue(ApnsServiceImpl.getTokensList().contains(IOS_DEVICE_TOKEN));

        assertEquals(NOTIFICATION_ALERT_MSG, ApnsServiceImpl.getAlert());
        assertEquals(NOTIFICATION_SOUND, ApnsServiceImpl.getSound());
        assertEquals(NOTIFICATION_BADGE, ApnsServiceImpl.getBadge());
    }

    @Test
    @InSequence(14)
    public void verifyInactiveTokensDeletion() {
        assertNotNull(clientInstallationService);
        
        List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils
                .getAdminLoginName());

        assertTrue(pushApps != null && pushApps.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApps));

        PushApplication pushApp = pushApps.iterator().next();
        
        Set<iOSVariant> iOSVariants = pushApp.getIOSVariants();
        assertTrue(iOSVariants != null && iOSVariants.size() == 1);

        iOSVariant iOSVariant = iOSVariants != null ? iOSVariants.iterator().next() : null;
        
        List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                iOSVariant.getVariantID(), null, null, null);

        assertNotNull(deviceTokens);
        assertTrue(!deviceTokens.contains(IOS_DEVICE_TOKEN));
    }
}
