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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.ChromePackagedAppVariant;
import org.jboss.aerogear.test.model.PushApplication;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class ChromePackagedAppVariantUtils {

    private static final int SINGLE = 1;

    private ChromePackagedAppVariantUtils() {
    }

    public static ChromePackagedAppVariant create(String name, String description, String clientId, String clientSecret,
        String refreshToken) {
        ChromePackagedAppVariant chromePackagedAppVariant = new ChromePackagedAppVariant();

        chromePackagedAppVariant.setName(name);
        chromePackagedAppVariant.setDescription(description);
        chromePackagedAppVariant.setClientId(clientId);
        chromePackagedAppVariant.setClientSecret(clientSecret);
        chromePackagedAppVariant.setRefreshToken(refreshToken);

        return chromePackagedAppVariant;
    }

    public static ChromePackagedAppVariant createAndRegister(String name, String description, String clientId,
        String clientSecret, String refreshToken, PushApplication pushApplication, Session session) {
        ChromePackagedAppVariant chromePackagedAppVariant = create(name, description, clientId, clientSecret, refreshToken);

        register(chromePackagedAppVariant, pushApplication, session);

        return chromePackagedAppVariant;
    }

    public static ChromePackagedAppVariant generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<ChromePackagedAppVariant> generate(int count) {
        List<ChromePackagedAppVariant> chromePackagedAppVariants = new ArrayList<ChromePackagedAppVariant>(count);

        for (int i = 0; i < count; i++) {
            String name = UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();
            String clientId = UUID.randomUUID().toString();
            String clientSecret = UUID.randomUUID().toString();
            String refreshToken = UUID.randomUUID().toString();

            ChromePackagedAppVariant chromePackagedAppVariant = create(name, description, clientId, clientSecret, refreshToken);

            chromePackagedAppVariants.add(chromePackagedAppVariant);
        }

        return chromePackagedAppVariants;
    }

    public static ChromePackagedAppVariant generateAndRegister(PushApplication pushApplication,
        Session session) {
        return generateAndRegister(SINGLE, pushApplication, session).iterator().next();
    }

    public static List<ChromePackagedAppVariant> generateAndRegister(int count, PushApplication pushApplication,
        Session session) {
        List<ChromePackagedAppVariant> chromePackagedAppVariants = generate(count);

        for (ChromePackagedAppVariant chromePackagedAppVariant : chromePackagedAppVariants) {
            register(chromePackagedAppVariant, pushApplication, session);
        }

        return chromePackagedAppVariants;
    }

    public static void register(ChromePackagedAppVariant chromePackagedAppVariant, PushApplication pushApplication,
        Session session) {
        register(chromePackagedAppVariant, pushApplication, session, ContentTypes.json());
    }

    public static void register(ChromePackagedAppVariant chromePackagedAppVariant, PushApplication pushApplication,
        Session session, String contentType) throws NullPointerException, UnexpectedResponseException {

        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(chromePackagedAppVariant))
            .post("/rest/applications/{pushApplicationID}/chrome", pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

        setFromJsonPath(response.jsonPath(), chromePackagedAppVariant);
    }

    public static List<ChromePackagedAppVariant> listAll(PushApplication pushApplication, Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/chrome", pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<ChromePackagedAppVariant> chromePackagedAppVariants = new ArrayList<ChromePackagedAppVariant>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            ChromePackagedAppVariant chromePackagedAppVariant = fromJsonPath(jsonPath);

            chromePackagedAppVariants.add(chromePackagedAppVariant);
        }

        return chromePackagedAppVariants;
    }

    public static ChromePackagedAppVariant findById(String variantID, PushApplication pushApplication,
        Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/chrome/{variantID}",
                pushApplication.getPushApplicationID(), variantID);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return fromJsonPath(response.jsonPath());
    }

    public static void update(ChromePackagedAppVariant chromePackagedAppVariant, PushApplication pushApplication,
        Session session) {
        update(chromePackagedAppVariant, pushApplication, session, ContentTypes.json());
    }

    public static void update(ChromePackagedAppVariant chromePackagedAppVariant, PushApplication pushApplication,
        Session session, String contentType) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(chromePackagedAppVariant))
            .put("/rest/applications/{pushApplicationID}/chrome/{variantID}",
                pushApplication.getPushApplicationID(), chromePackagedAppVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static void delete(ChromePackagedAppVariant chromePackagedAppVariant, PushApplication pushApplication,
        Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .delete("/rest/applications/{pushApplicationID}/chrome/{variantID}",
                pushApplication.getPushApplicationID(), chromePackagedAppVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static JSONObject toJSONObject(ChromePackagedAppVariant chromePackagedAppVariant) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", chromePackagedAppVariant.getName());
        jsonObject.put("description", chromePackagedAppVariant.getDescription());
        jsonObject.put("clientId", chromePackagedAppVariant.getClientId());
        jsonObject.put("clientSecret", chromePackagedAppVariant.getClientSecret());
        jsonObject.put("refreshToken", chromePackagedAppVariant.getRefreshToken());
        return jsonObject;
    }

    public static String toJSONString(ChromePackagedAppVariant chromePackagedAppVariant) {
        return toJSONObject(chromePackagedAppVariant).toJSONString();
    }

    public static ChromePackagedAppVariant fromJsonPath(JsonPath jsonPath) {
        ChromePackagedAppVariant chromePackagedAppVariant = new ChromePackagedAppVariant();

        setFromJsonPath(jsonPath, chromePackagedAppVariant);

        return chromePackagedAppVariant;
    }

    public static void setFromJsonPath(JsonPath jsonPath, ChromePackagedAppVariant chromePackagedAppVariant) {
        chromePackagedAppVariant.setId(jsonPath.getString("id"));
        chromePackagedAppVariant.setVariantID(jsonPath.getString("variantID"));
        chromePackagedAppVariant.setDeveloper(jsonPath.getString("developer"));
        chromePackagedAppVariant.setDescription(jsonPath.getString("description"));
        chromePackagedAppVariant.setName(jsonPath.getString("name"));
        chromePackagedAppVariant.setSecret(jsonPath.getString("secret"));
        chromePackagedAppVariant.setClientId(jsonPath.getString("clientId"));
        chromePackagedAppVariant.setClientSecret(jsonPath.getString("clientSecret"));
        chromePackagedAppVariant.setRefreshToken(jsonPath.getString("refreshToken"));
    }

    // TODO there should be "equals" method in the model!
    public static void checkEquality(ChromePackagedAppVariant expected, ChromePackagedAppVariant actual) {
        assertEquals("Name is not equal!", expected.getName(), actual.getName());
        assertEquals("Description is not equal!", expected.getDescription(), actual.getDescription());
        assertEquals("VariantId is not equal!", expected.getVariantID(), actual.getVariantID());
        assertEquals("Secret is not equal!", expected.getSecret(), actual.getSecret());
        assertEquals("Developer is not equal!", expected.getDeveloper(), actual.getDeveloper());

        // TODO we can't do this check as none of variants has the equals method implemented
        // assertEquals(expected.getIOSVariants(), actual.getIOSVariants());
        // assertEquals(expected.getChromePackagedAppVariants(), actual.getChromePackagedAppVariants());
        // assertEquals(expected.getSimplePushVariants(), actual.getSimplePushVariants());
    }

    /*
     *
     * public static ChromePackagedAppVariant createChromePackagedAppVariant(String name, String description, String variantID,
     * String secret,
     * String developer, String googleKey) {
     * ChromePackagedAppVariant variant = new ChromePackagedAppVariant();
     * variant.setName(name);
     * variant.setDescription(description);
     * variant.setVariantID(variantID);
     * variant.setSecret(secret);
     * variant.setDeveloper(developer);
     * variant.setGoogleKey(googleKey);
     * return variant;
     * }
     *
     * @SuppressWarnings("unchecked")
     * public static Response registerChromePackagedAppVariant(String pushAppId, ChromePackagedAppVariant variant, Map<String,
     * ?> cookies,
     * String root) {
     *
     * JSONObject jsonObject = new JSONObject();
     * jsonObject.put("googleKey", variant.getGoogleKey());
     * jsonObject.put("name", variant.getName());
     * jsonObject.put("description", variant.getDescription());
     *
     * Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
     * .cookies(cookies).body(jsonObject.toString())
     * .post("{root}rest/applications/{pushAppId}/chrome", root, pushAppId);
     *
     * return response;
     * }
     *
     * @SuppressWarnings("unchecked")
     * public static Response updateChromePackagedAppVariant(String pushAppId, ChromePackagedAppVariant variant, Map<String, ?>
     * cookies,
     * String variantId, String root) {
     *
     * JSONObject jsonObject = new JSONObject();
     * jsonObject.put("googleKey", variant.getGoogleKey());
     * jsonObject.put("name", variant.getName());
     * jsonObject.put("description", variant.getDescription());
     *
     * Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
     * .cookies(cookies).body(jsonObject.toString())
     * .put("{root}rest/applications/{pushAppId}/chrome/{variantId}", root, pushAppId, variantId);
     *
     * return response;
     * }
     *
     * public static Response listAllChromePackagedAppVariants(String pushAppId, Map<String, ?> cookies, String root) {
     * assertNotNull(root);
     *
     * Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
     * .cookies(cookies).get("{root}rest/applications/{pushAppId}/chrome", root, pushAppId);
     *
     * return response;
     * }
     *
     * public static Response findChromePackagedAppVariantById(String pushAppId, String variantId, Map<String, ?> cookies,
     * String root) {
     * assertNotNull(root);
     *
     * Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
     * .cookies(cookies).get("{root}rest/applications/{pushAppId}/chrome/{variantId}", root, pushAppId,
     * variantId);
     *
     * return response;
     * }
     *
     * public static Response deleteChromePackagedAppVariant(String pushAppId, String variantId, Map<String, ?> cookies,
     * String root) {
     * assertNotNull(root);
     *
     * Response response = RestAssured.given().cookies(cookies)
     * .delete("{root}rest/applications/{pushAppId}/chrome/{variantId}", root, pushAppId, variantId);
     *
     * return response;
     * }
     */
}
