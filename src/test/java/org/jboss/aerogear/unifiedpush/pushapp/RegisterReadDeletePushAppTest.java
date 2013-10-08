/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.pushapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class RegisterReadDeletePushAppTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return root.toExternalForm();
    }

    private static final String pushAppName = "My App";
    private static final String pushAppDesc = "Awesome App";

    private static Map<String, String> authCookies;
    private static String pushAppId;

    @Test
    @InSequence(1)
    public void Authenticate() {
        assertNotNull(getContextRoot());
        authCookies = AuthenticationUtils.adminLogin(getContextRoot()).getCookies();
        assertTrue(authCookies != null);
    }

    @Test
    @InSequence(2)
    public void registeringPushApplication() {

        PushApplication pushApp = PushApplicationUtils.createPushApplication(pushAppName, pushAppDesc, null, null, null);

        Response response = PushApplicationUtils.registerPushApplication(pushApp, authCookies, "application/json",
                getContextRoot());
        JsonPath body = response.getBody().jsonPath();
        pushAppId = body.get("pushApplicationID");

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.CREATED.getStatusCode());
        assertNotNull(body.get("pushApplicationID"));
        assertNotNull(body.get("masterSecret"));
        assertEquals(body.get("name"), pushAppName);
        assertEquals(body.get("description"), pushAppDesc);
    }

    @Test
    @InSequence(3)
    public void retrievePushApplications() {

        assertNotNull(authCookies);
        Response response = PushApplicationUtils.listAllPushApplications(authCookies, getContextRoot());
        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());

        JsonPath body = response.getBody().jsonPath();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Map<?, ?>> pushAppsList = (List<Map<?, ?>>) body.get();

        assertNotNull(pushAppsList);
        assertEquals(pushAppsList.size(), 1);

        assertEquals(pushAppId, (String) pushAppsList.get(0).get("pushApplicationID"));
        assertEquals((String) pushAppsList.get(0).get("name"), pushAppName);
        assertEquals((String) pushAppsList.get(0).get("description"), pushAppDesc);
    }

    @Test
    @InSequence(4)
    public void retrieveRegisteredApplication() {

        assertNotNull(authCookies);
        assertNotNull(pushAppId);
        Response response = PushApplicationUtils.findPushApplicationById(authCookies, pushAppId, getContextRoot());
        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());

        JsonPath body = response.getBody().jsonPath();
        assertNotNull(body);

        Map<?, ?> pushApp = (Map<?, ?>) body.get();

        assertNotNull(pushApp);
        assertEquals(pushApp.get("pushApplicationID"), pushAppId);
        assertEquals(pushApp.get("name"), pushAppName);
    }

    @Test
    @InSequence(5)
    public void deleteRegisteredApplication() {

        assertNotNull(authCookies);
        assertNotNull(pushAppId);
        Response response = PushApplicationUtils.deletePushApplication(authCookies, pushAppId, getContextRoot());
        assertNotNull(response);
        assertEquals(response.statusCode(), Status.NO_CONTENT.getStatusCode());
    }
}
