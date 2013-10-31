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

import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class PushNotificationSenderUtils {

    private PushNotificationSenderUtils() {
    }

    @SuppressWarnings("unchecked")
    public static Response selectiveSend(String pushApplicationId, String masterSecret, List<String> aliases,
            List<String> deviceTypes, Map<String, Object> messages, String simplePush, String categories,
            String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("alias", aliases);
        jsonObject.put("deviceType", deviceTypes);
        jsonObject.put("message", messages);
        jsonObject.put("simple-push", simplePush);
        jsonObject.put("category", categories);

        Response response = RestAssured.given().contentType("application/json")
                .auth().basic(pushApplicationId, masterSecret)
                .header("Accept", "application/json").body(jsonObject.toString()).post("{root}rest/sender", root);

        return response;
    }

}
