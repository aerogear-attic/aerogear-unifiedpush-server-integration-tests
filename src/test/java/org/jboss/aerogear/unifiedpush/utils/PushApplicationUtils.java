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

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class PushApplicationUtils {

    private PushApplicationUtils() {
    }

    public static PushApplication createPushApplication(String name, String description, String pushApplicationID,
            String masterSecret, String developer) {
        PushApplication pushApp = new PushApplication();
        pushApp.setName(name);
        pushApp.setDescription(description);
        pushApp.setPushApplicationID(pushApplicationID);
        pushApp.setMasterSecret(masterSecret);
        pushApp.setDeveloper(developer);
        return pushApp;
    }

    public static boolean appIdExistsInList(String pushAppId, List<PushApplication> pushAppsList) {
        if (!StringUtils.isEmpty(pushAppId) && pushAppsList != null) {
            for (PushApplication pushApp : pushAppsList) {
                if (pushApp != null && pushAppId.equals(pushApp.getPushApplicationID())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean nameExistsInList(String pushAppName, List<PushApplication> pushAppsList) {
        if (!StringUtils.isEmpty(pushAppName) && pushAppsList != null) {
            for (PushApplication pushApp : pushAppsList) {
                if (pushApp != null && pushAppName.equals(pushApp.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static Response registerPushApplication(PushApplication pushApp, Map<String, ?> cookies, String contentType,
            String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", pushApp.getName());
        jsonObject.put("description", pushApp.getDescription());

        Response response = RestAssured.given().contentType(contentType == null ? "application/json" : contentType)
                .header("Accept", "application/json").cookies(cookies).body(jsonObject.toString())
                .post("{root}rest/applications", root);

        return response;
    }

    public static Response listAllPushApplications(Map<String, ?> cookies, String root) {

        assertNotNull(root);

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).get("{root}rest/applications", root);

        return response;
    }

    public static Response findPushApplicationById(Map<String, ?> cookies, String pushAppId, String root) {

        assertNotNull(root);

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).get("{root}rest/applications/{pushAppId}", root, pushAppId);

        return response;
    }

    public static Response deletePushApplication(Map<String, ?> cookies, String pushApplicationId, String root) {

        assertNotNull(root);

        Response response = RestAssured.given().header("Accept", "application/json").cookies(cookies)
                .delete("{root}rest/applications/{pushApplicationId}", root, pushApplicationId);

        return response;
    }
}
