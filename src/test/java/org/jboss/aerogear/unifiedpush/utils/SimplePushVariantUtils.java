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

import java.util.Map;

import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class SimplePushVariantUtils {

    private SimplePushVariantUtils() {
    }

    public static SimplePushVariant createSimplePushVariant(String name, String description, String variantID, String secret,
            String developer) {
        SimplePushVariant variant = new SimplePushVariant();
        variant.setName(name);
        variant.setDescription(description);
        variant.setVariantID(variantID);
        variant.setSecret(secret);
        variant.setDeveloper(developer);
        return variant;
    }

    @SuppressWarnings("unchecked")
    public static Response registerSimplePushVariant(String pushAppId, SimplePushVariant variant, Map<String, ?> cookies,
            String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString())
                .post("{root}rest/applications/{pushAppId}/simplePush", root, pushAppId);

        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response updateSimplePushVariant(String pushAppId, SimplePushVariant variant, Map<String, ?> cookies,
            String variantId, String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString())
                .put("{root}rest/applications/{pushAppId}/simplePush/{variantId}", root, pushAppId, variantId);

        return response;
    }
}
