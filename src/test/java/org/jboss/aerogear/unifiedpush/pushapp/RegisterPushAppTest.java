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

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.ContentTypes;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.arquillian.junit.InSequence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;

public class RegisterPushAppTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return root.toExternalForm();
    }

    private static Session session;

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
    public void authenticate() {
        assertNotNull(getContextRoot());
        session = AuthenticationUtils.completeDefaultLogin(getContextRoot());
        assertNotNull(session);
        assertTrue(session.isValid());
    }

    @Test
    @InSequence(2)
    public void registeringPushApplication() {
        String pushAppName = "My App";
        String pushAppDesc = "Awesome App";

        PushApplication pushApplication = PushApplicationUtils.createAndRegister(pushAppName, pushAppDesc, session);

        assertNotNull(pushApplication);
        assertEquals(pushAppName, pushApplication.getName());
        assertEquals(pushAppDesc, pushApplication.getDescription());
    }

    @Test
    @InSequence(3)
    public void registeringPushApplicationUTF8() {
        Map<String, String> nameTypeHM = new HashMap<String, String>();
        nameTypeHM.put("AwesomeAppěščřžýáíéňľ", ContentTypes.jsonUTF8());
        nameTypeHM.put("AwesomeAppவான்வழிe", ContentTypes.jsonUTF8());
        nameTypeHM.put("AwesomeAppěščřžýáíéňľ_", ContentTypes.json());
        nameTypeHM.put("AwesomeAppவான்வழிe_", ContentTypes.json());

        String pushAppDesc = "Awesome App";
        for (String pushAppName : nameTypeHM.keySet()) {
            PushApplication pushApplication = PushApplicationUtils.create(pushAppName, pushAppDesc);
            assertNotNull(pushApplication);
            PushApplicationUtils.register(pushApplication, session, nameTypeHM.get(pushAppName));

            assertEquals(pushAppName, pushApplication.getName());
            assertEquals(pushAppDesc, pushApplication.getDescription());
        }
    }
}
