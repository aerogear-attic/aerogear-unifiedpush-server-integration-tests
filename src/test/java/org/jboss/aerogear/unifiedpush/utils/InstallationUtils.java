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
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class InstallationUtils {
    private static final int SINGLE = 1;

    private static final String ANDROID_DEFAULT_DEVICE_TYPE = "AndroidPhone";
    private static final String ANDROID_DEFAULT_OPERATING_SYSTEM = "ANDROID";
    private static final String ANDROID_DEFAULT_OPERATING_SYSTEM_VERSION = "4.2.2";
    private static final String[] ANDROID_DEFAULT_CATEGORIES = { };

    private static final String IOS_DEFAULT_DEVICE_TYPE = "IOSPhone";
    private static final String IOS_DEFAULT_OPERATING_SYSTEM = "IOS";
    private static final String IOS_DEFAULT_OPERATING_SYSTEM_VERSION = "6.0";
    private static final String[] IOS_DEFAULT_CATEGORIES = { };

    private static final String SIMPLEPUSH_DEFAULT_DEVICE_TYPE = "web";
    private static final String SIMPLEPUSH_DEFAULT_OPERATING_SYSTEM = "MozillaOS";
    private static final String SIMPLEPUSH_DEFAULT_OPERATING_SYSTEM_VERSION = "1";
    private static final String[] SIMPLEPUSH_DEFAULT_CATEGORIES = { "default_category" };
    private static final String SIMPLEPUSH_DEFAULT_ENDPOINT = "http://localhost:8081/endpoint/%s";

    private InstallationUtils() {
    }

    public static InstallationImpl createAndroid(String deviceToken, String alias) {
        return create(deviceToken, alias, getAndroidDefaultDeviceType(), getAndroidDefaultOperatingSystem(),
                getAndroidDefaultOperatingSystemVersion(), getAndroidDefaultCategories(), null);
    }

    public static InstallationImpl createIOS(String deviceToken, String alias) {
        return create(deviceToken, alias, getIosDefaultDeviceType(), getIosDefaultOperatingSystem(),
                getIosDefaultOperatingSystemVersion(), getIosDefaultCategories(), null);
    }

    public static InstallationImpl createSimplePush(String deviceToken, String alias) {
        return create(deviceToken, alias, getSimplepushDefaultDeviceType(), getSimplepushDefaultOperatingSystem(),
                getSimplepushDefaultOperatingSystemVersion(), getSimplepushDefaultCategories(),
                getSimplepushDefaultEndpoint(deviceToken));
    }

    public static InstallationImpl create(String deviceToken, String alias, String deviceType,
                                          String operatingSystem, String operatingSystemVersion,
                                          Set<String> categories, String simplePushEndpoint) {
        InstallationImpl installation = new InstallationImpl();

        installation.setDeviceToken(deviceToken);
        installation.setDeviceType(deviceType);
        installation.setOperatingSystem(operatingSystem);
        installation.setOsVersion(operatingSystemVersion);
        installation.setAlias(alias);
        installation.setCategories(categories);
        installation.setSimplePushEndpoint(simplePushEndpoint);

        return installation;
    }

    public static InstallationImpl generateAndroid() {
        return generateAndroid(SINGLE).iterator().next();
    }

    public static List<InstallationImpl> generateAndroid(int count) {
        List<InstallationImpl> installations = new ArrayList<InstallationImpl>();

        for (int i = 0; i < count; i++) {
            String deviceToken = UUID.randomUUID().toString();
            String alias = UUID.randomUUID().toString();

            InstallationImpl installation = createAndroid(deviceToken, alias);

            installations.add(installation);
        }

        return installations;
    }

    public static InstallationImpl generateIos() {
        return generateIos(SINGLE).iterator().next();
    }

    public static List<InstallationImpl> generateIos(int count) {
        List<InstallationImpl> installations = new ArrayList<InstallationImpl>();

        for (int i = 0; i < count; i++) {
            String deviceToken = UUID.randomUUID().toString().replaceAll("-", "");
            String alias = UUID.randomUUID().toString();

            InstallationImpl installation = createIOS(deviceToken, alias);

            installations.add(installation);
        }

        return installations;
    }

    public static InstallationImpl generateSimplePush() {
        return generateSimplePush(SINGLE).iterator().next();
    }

    public static List<InstallationImpl> generateSimplePush(int count) {
        List<InstallationImpl> installations = new ArrayList<InstallationImpl>();

        for (int i = 0; i < count; i++) {
            String deviceToken = UUID.randomUUID().toString();
            String alias = UUID.randomUUID().toString();

            InstallationImpl installation = createSimplePush(deviceToken, alias);

            installations.add(installation);
        }

        return installations;
    }

    public static void register(InstallationImpl installation, Variant variant, String root) {
        register(installation, variant, root, ContentTypes.json());
    }

    public static void register(InstallationImpl installation, Variant variant, String root, String contentType) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .contentType(contentType)
                .auth()
                .basic(variant.getVariantID(), variant.getSecret())
                .header(Headers.acceptJson())
                .body(toJSONString(installation))
                .post("{root}rest/registry/device", root);

        UnexpectedResponseException.verifyResponse(response, OK);

        setFromJsonPath(response.jsonPath(), installation);
    }

    public static void registerAll(List<InstallationImpl> installations, Variant variant, String root) {
        registerAll(installations, variant, root, ContentTypes.json());
    }

    public static void registerAll(List<InstallationImpl> installations, Variant variant, String root,
                                   String contentType) {
        for (InstallationImpl installation : installations) {
            register(installation, variant, root, contentType);
        }
    }

    public static void unregister(InstallationImpl installation, Variant variant, String root) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .auth()
                .basic(variant.getVariantID(), variant.getSecret())
                .delete("{root}rest/registry/device/{deviceToken}", root, installation.getDeviceToken());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);
    }

    public static void unregisterAll(List<InstallationImpl> installations, Variant variant, String root) {
        for (InstallationImpl installation : installations) {
            unregister(installation, variant, root);
        }
    }

    public static List<InstallationImpl> listAll(Variant variant, AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/applications/{variantID}/installations", session.getRoot(), variant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, OK);

        List<InstallationImpl> installations = new ArrayList<InstallationImpl>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            InstallationImpl installation = fromJsonPath(jsonPath);

            installations.add(installation);
        }

        return installations;
    }

    public static InstallationImpl findById(String installationID, Variant variant,
                                            AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/applications/{variantID}/installations/{installationID}", session.getRoot(),
                        variant.getVariantID(), installationID);

        UnexpectedResponseException.verifyResponse(response, OK);

        return fromJsonPath(response.jsonPath());
    }

    public static void updateInstallation(InstallationImpl installation, Variant variant,
                                          AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(installation))
                .put("{root}rest/applications/{variantID}/installations/{installationID}", session.getRoot(),
                        variant.getVariantID(), installation.getId());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);
    }

    public static void delete(InstallationImpl installation, Variant variant, AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .delete("{root}rest/applications/{variantID}/installations/{installationID}", session.getRoot(),
                        variant.getVariantID(), installation.getId());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);
    }

    public static JSONObject toJSONObject(InstallationImpl installation) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("deviceToken", installation.getDeviceToken());
        jsonObject.put("deviceType", installation.getDeviceType());
        jsonObject.put("operatingSystem", installation.getOperatingSystem());
        jsonObject.put("osVersion", installation.getOsVersion());
        jsonObject.put("alias", installation.getAlias());

        // JSONObject doesn't understand Set<String>
        JSONArray categories = new JSONArray();
        for(String category : installation.getCategories()) {
            categories.add(category);
        }
        jsonObject.put("categories", categories);

        jsonObject.put("simplePushEndpoint", installation.getSimplePushEndpoint());

        return jsonObject;
    }

    public static String toJSONString(InstallationImpl installation) {
        return toJSONObject(installation).toJSONString();
    }

    public static InstallationImpl fromJsonPath(JsonPath jsonPath) {
        InstallationImpl installation = new InstallationImpl();

        setFromJsonPath(jsonPath, installation);

        return installation;
    }

    public static void setFromJsonPath(JsonPath jsonPath, InstallationImpl installation) {
        installation.setId(jsonPath.getString("id"));
        installation.setPlatform(jsonPath.getString("platform"));
        installation.setEnabled(jsonPath.getBoolean("enabled"));
        installation.setOperatingSystem(jsonPath.getString("operatingSystem"));
        installation.setOsVersion(jsonPath.getString("osVersion"));
        installation.setAlias(jsonPath.getString("alias"));
        installation.setDeviceType(jsonPath.getString("deviceType"));
        installation.setDeviceToken(jsonPath.getString("deviceToken"));
        installation.setSimplePushEndpoint(jsonPath.getString("simplePushEndpoint"));
        HashSet<String> categories = new HashSet<String>();
        List<String> jsonCategories = jsonPath.getList("categories");
        if(jsonCategories != null) {
            for(String category : jsonCategories) {
                categories.add(category);
            }
        }
        installation.setCategories(categories);
    }

    public static void checkEquality(InstallationImpl expected, InstallationImpl actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDeviceToken(), actual.getDeviceToken());
        assertEquals(expected.getDeviceType(), actual.getDeviceType());
        assertEquals(expected.getOperatingSystem(), actual.getOperatingSystem());
        assertEquals(expected.getOsVersion(), actual.getOsVersion());
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCategories(), actual.getCategories());
        assertEquals(expected.getSimplePushEndpoint(), actual.getSimplePushEndpoint());
    }

    public static String getAndroidDefaultDeviceType() {
        return ANDROID_DEFAULT_DEVICE_TYPE;
    }

    public static String getAndroidDefaultOperatingSystem() {
        return ANDROID_DEFAULT_OPERATING_SYSTEM;
    }

    public static String getAndroidDefaultOperatingSystemVersion() {
        return ANDROID_DEFAULT_OPERATING_SYSTEM_VERSION;
    }

    public static Set<String> getAndroidDefaultCategories() {
        HashSet<String> categories = new HashSet<String>();
        Collections.addAll(categories, ANDROID_DEFAULT_CATEGORIES);
        return categories;
    }

    public static String getIosDefaultDeviceType() {
        return IOS_DEFAULT_DEVICE_TYPE;
    }

    public static String getIosDefaultOperatingSystem() {
        return IOS_DEFAULT_OPERATING_SYSTEM;
    }

    public static String getIosDefaultOperatingSystemVersion() {
        return IOS_DEFAULT_OPERATING_SYSTEM_VERSION;
    }

    public static Set<String> getIosDefaultCategories() {
        HashSet<String> categories = new HashSet<String>();
        Collections.addAll(categories, IOS_DEFAULT_CATEGORIES);
        return categories;
    }

    public static String getSimplepushDefaultDeviceType() {
        return SIMPLEPUSH_DEFAULT_DEVICE_TYPE;
    }

    public static String getSimplepushDefaultOperatingSystem() {
        return SIMPLEPUSH_DEFAULT_OPERATING_SYSTEM;
    }

    public static String getSimplepushDefaultOperatingSystemVersion() {
        return SIMPLEPUSH_DEFAULT_OPERATING_SYSTEM_VERSION;
    }

    public static Set<String> getSimplepushDefaultCategories() {
        HashSet<String> categories = new HashSet<String>();
        Collections.addAll(categories, SIMPLEPUSH_DEFAULT_CATEGORIES);
        return categories;
    }

    public static String getSimplepushDefaultEndpoint(String deviceToken) {
        return String.format(SIMPLEPUSH_DEFAULT_ENDPOINT, deviceToken);
    }
}
