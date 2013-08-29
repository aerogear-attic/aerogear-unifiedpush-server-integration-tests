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
package org.jboss.aerogear.unifiedpush.common

import groovy.json.JsonBuilder

import com.jayway.restassured.RestAssured

class PushNotificationSenderUtils {

    def selectiveSend(String pushApplicationId, String masterSecret,
            List<String> aliases, List<String> deviceTypes,
            Map<String, Object> messages, Map<String, String> simplePush, List<String> mobileOS) {

        assert root !=null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .auth().basic(pushApplicationId, masterSecret)
                .header("Accept", "application/json")
                .body( json {
                    alias aliases
                    deviceType deviceTypes
                    message messages
                    "simple-push" simplePush
                    mobileOperatingSystem mobileOS
                }).post("${root}rest/sender/selected")

        return response
    }

    // TODO parameter categories should be List<String> when there is possibility to send array and not just string
    def selectiveSendByCategories(String pushApplicationId, String masterSecret, String categories,
                                  Map<String, Object> messages, Map<String, String> simplePush) {
        assert root != null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .auth().basic(pushApplicationId, masterSecret)
                .header("Accept", "application/json")
                .body( json {
                    category categories
                    message messages
                    "simple-push" simplePush
                }).post("${root}rest/sender/selected")


        return response;
    }

}
