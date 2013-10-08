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

import java.util.Map;

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class AndroidVariantUtils {

    private AndroidVariantUtils() {
    }

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
    public static Response registerAndroidVariant(String pushAppId, AndroidVariant variant, Map<String, ?> cookies, String root) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("googleKey", variant.getGoogleKey());
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString()).post(root + "rest/applications/" + pushAppId + "/android");

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
                .put(root + "rest/applications/" + pushAppId + "/android/" + variantId);

        return response;
    }
}
