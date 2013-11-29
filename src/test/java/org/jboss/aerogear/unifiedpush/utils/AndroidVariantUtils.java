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
package org.jboss.aerogear.unifiedpush.utils;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class AndroidVariantUtils {

    private static final int SINGLE = 1;

    private AndroidVariantUtils() {
    }

    public static AndroidVariant create(String name, String description, String googleKey) {
        AndroidVariant androidVariant = new AndroidVariant();

        androidVariant.setName(name);
        androidVariant.setDescription(description);
        androidVariant.setGoogleKey(googleKey);

        return androidVariant;
    }

    public static AndroidVariant createAndRegister(String name, String description, String googleKey,
                                                   PushApplication pushApplication,
                                                   AuthenticationUtils.Session session) {
        AndroidVariant androidVariant = create(name, description, googleKey);

        register(androidVariant, pushApplication, session);

        return androidVariant;
    }

    public static AndroidVariant generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<AndroidVariant> generate(int count) {
        List<AndroidVariant> androidVariants = new ArrayList<AndroidVariant>();

        for (int i = 0; i < count; i++) {
            String name = UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();
            String googleKey = UUID.randomUUID().toString();

            AndroidVariant androidVariant = create(name, description, googleKey);

            androidVariants.add(androidVariant);
        }

        return androidVariants;
    }

    public static AndroidVariant generateAndRegister(PushApplication pushApplication,
                                                     AuthenticationUtils.Session session) {
        return generateAndRegister(SINGLE, pushApplication, session).iterator().next();
    }

    public static List<AndroidVariant> generateAndRegister(int count, PushApplication pushApplication,
                                                           AuthenticationUtils.Session session) {
        List<AndroidVariant> androidVariants = generate(count);

        for (AndroidVariant androidVariant : androidVariants) {
            register(androidVariant, pushApplication, session);
        }

        return androidVariants;
    }

    public static void register(AndroidVariant androidVariant, PushApplication pushApplication,
                                AuthenticationUtils.Session session) {
        register(androidVariant, pushApplication, session, ContentTypes.json());
    }

    public static void register(AndroidVariant androidVariant, PushApplication pushApplication,
                                AuthenticationUtils.Session session, String contentType)
            throws NullPointerException, UnexpectedResponseException {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(androidVariant))
                .post("{root}rest/applications/{pushApplicationID}/android", session.getRoot(),
                        pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, CREATED);

        setFromJsonPath(response.jsonPath(), androidVariant);
    }

    public static List<AndroidVariant> listAll(PushApplication pushApplication, AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/applications/{pushApplicationID}/android", session.getRoot(),
                        pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, OK);

        List<AndroidVariant> androidVariants = new ArrayList<AndroidVariant>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            AndroidVariant androidVariant = fromJsonPath(jsonPath);

            androidVariants.add(androidVariant);
        }

        return androidVariants;
    }

    public static AndroidVariant findById(String variantID, PushApplication pushApplication,
                                          AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/applications/{pushApplicationID}/android/{variantID}", session.getRoot(),
                        pushApplication.getPushApplicationID(), variantID);

        UnexpectedResponseException.verifyResponse(response, OK);

        return fromJsonPath(response.jsonPath());
    }

    public static void update(AndroidVariant androidVariant, PushApplication pushApplication,
                              AuthenticationUtils.Session session) {
        update(androidVariant, pushApplication, session, ContentTypes.json());
    }

    public static void update(AndroidVariant androidVariant, PushApplication pushApplication,
                              AuthenticationUtils.Session session, String contentType) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(androidVariant))
                .put("{root}rest/applications/{pushApplicationID}/android/{variantID}", session.getRoot(),
                        pushApplication.getPushApplicationID(), androidVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);
    }

    public static void delete(AndroidVariant androidVariant, PushApplication pushApplication,
                              AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .delete("{root}rest/applications/{pushApplicationID}/android/{variantID}", session.getRoot(),
                        pushApplication.getPushApplicationID(), androidVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);
    }

    public static JSONObject toJSONObject(AndroidVariant androidVariant) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", androidVariant.getName());
        jsonObject.put("description", androidVariant.getDescription());
        jsonObject.put("googleKey", androidVariant.getGoogleKey());
        return jsonObject;
    }

    public static String toJSONString(AndroidVariant androidVariant) {
        return toJSONObject(androidVariant).toJSONString();
    }

    public static AndroidVariant fromJsonPath(JsonPath jsonPath) {
        AndroidVariant androidVariant = new AndroidVariant();

        setFromJsonPath(jsonPath, androidVariant);

        return androidVariant;
    }

    public static void setFromJsonPath(JsonPath jsonPath, AndroidVariant androidVariant) {
        androidVariant.setGoogleKey(jsonPath.getString("googleKey"));
        androidVariant.setId(jsonPath.getString("id"));
        androidVariant.setVariantID(jsonPath.getString("variantID"));
        androidVariant.setDeveloper(jsonPath.getString("developer"));
        androidVariant.setDescription(jsonPath.getString("description"));
        androidVariant.setName(jsonPath.getString("name"));
        androidVariant.setSecret(jsonPath.getString("secret"));
        androidVariant.setProjectNumber(jsonPath.getString("projectNumber"));
    }

    // TODO there should be "equals" method in the model!
    public static void checkEquality(AndroidVariant expected, AndroidVariant actual) {
        assertEquals("Name is not equal!", expected.getName(), actual.getName());
        assertEquals("Description is not equal!", expected.getDescription(), actual.getDescription());
        assertEquals("VariantId is not equal!", expected.getVariantID(), actual.getVariantID());
        assertEquals("Secret is not equal!", expected.getSecret(), actual.getSecret());
        assertEquals("Developer is not equal!", expected.getDeveloper(), actual.getDeveloper());

        // TODO we can't do this check as none of variants has the equals method implemented
        // assertEquals(expected.getIOSVariants(), actual.getIOSVariants());
        // assertEquals(expected.getAndroidVariants(), actual.getAndroidVariants());
        // assertEquals(expected.getSimplePushVariants(), actual.getSimplePushVariants());
    }


/*

    public static AndroidVariant createAndroidVariant(String name, String description, String variantID, String secret,
            String developer, String googleKey) {
        AndroidVariant variant = new AndroidVariant();
        variant.setName(name);
        variant.setDescription(description);
        variant.setVariantID(variantID);
        variant.setSecret(secret);
        variant.setDeveloper(developer);
        variant.setGoogleKey(googleKey);
        return variant;
    }

    @SuppressWarnings("unchecked")
    public static Response registerAndroidVariant(String pushAppId, AndroidVariant variant, Map<String, ?> cookies,
    String root) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("googleKey", variant.getGoogleKey());
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString())
                .post("{root}rest/applications/{pushAppId}/android", root, pushAppId);

        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response updateAndroidVariant(String pushAppId, AndroidVariant variant, Map<String, ?> cookies,
            String variantId, String root) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("googleKey", variant.getGoogleKey());
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString())
                .put("{root}rest/applications/{pushAppId}/android/{variantId}", root, pushAppId, variantId);

        return response;
    }

    public static Response listAllAndroidVariants(String pushAppId, Map<String, ?> cookies, String root) {
        assertNotNull(root);

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).get("{root}rest/applications/{pushAppId}/android", root, pushAppId);

        return response;
    }

    public static Response findAndroidVariantById(String pushAppId, String variantId, Map<String, ?> cookies,
    String root) {
        assertNotNull(root);

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).get("{root}rest/applications/{pushAppId}/android/{variantId}", root, pushAppId,
                variantId);

        return response;
    }

    public static Response deleteAndroidVariant(String pushAppId, String variantId, Map<String, ?> cookies,
    String root) {
        assertNotNull(root);

        Response response = RestAssured.given().cookies(cookies)
                .delete("{root}rest/applications/{pushAppId}/android/{variantId}", root, pushAppId, variantId);

        return response;
    }*/
}
