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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.arquillian.junit.InSequence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class RegisterPushAppTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return root.toExternalForm();
    }

    private static Map<String, String> authCookies;

    @BeforeClass
    public static void setup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"));
    }

    @AfterClass
    public static void cleanup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"));
    }

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

        String pushAppName = "My App";
        String pushAppDesc = "Awesome App";
        PushApplication pushApp = PushApplicationUtils.createPushApplication(pushAppName, pushAppDesc, null, null, null);

        Response response = PushApplicationUtils.registerPushApplication(pushApp, authCookies, "application/json",
                getContextRoot());
        JsonPath body = response.getBody().jsonPath();

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.CREATED.getStatusCode());
        assertNotNull(body.get("pushApplicationID"));
        assertNotNull(body.get("masterSecret"));
        assertEquals(body.get("name"), pushAppName);
        assertEquals(body.get("description"), pushAppDesc);
    }

    @Test
    @InSequence(3)
    public void registeringPushApplicationUTF8() {

        Map<String, String> nameTypeHM = new HashMap<String, String>();
        nameTypeHM.put("AwesomeAppěščřžýáíéňľ", "application/json; charset=utf-8");
        nameTypeHM.put("AwesomeAppவான்வழிe", "application/json; charset=utf-8");
        nameTypeHM.put("AwesomeAppěščřžýáíéňľ_", "application/json");
        nameTypeHM.put("AwesomeAppவான்வழிe_", "application/json");

        String pushAppDesc = "Awesome App";
        for (String appName : nameTypeHM.keySet()) {
            PushApplication pushApp = PushApplicationUtils.createPushApplication(appName, pushAppDesc, null, null, null);
            Response response = PushApplicationUtils.registerPushApplication(pushApp, authCookies, nameTypeHM.get(appName),
                    getContextRoot());
            JsonPath body = response.getBody().jsonPath();

            assertNotNull(response);
            assertEquals(response.statusCode(), Status.CREATED.getStatusCode());

            assertNotNull(body.get("pushApplicationID"));
            assertNotNull(body.get("masterSecret"));
            assertEquals(body.get("name"), appName);
            assertEquals(body.get("description"), pushAppDesc);
        }
    }
}
