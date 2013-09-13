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

import org.apache.commons.lang3.StringUtils
import org.jboss.aerogear.unifiedpush.model.PushApplication

import com.jayway.restassured.RestAssured

class PushApplicationUtils {

    def createPushApplication(String name, String description, String pushApplicationID,
            String masterSecret, String developer) {
        PushApplication pushApp = new PushApplication()
        pushApp.setName(name)
        pushApp.setDescription(description)
        pushApp.setPushApplicationID(pushApplicationID)
        pushApp.setMasterSecret(masterSecret)
        pushApp.setDeveloper(developer)
        return pushApp
    }

    def appIdExistsInList(String pushAppId, List<PushApplication> pushAppsList) {
        if (!StringUtils.isEmpty(pushAppId) && pushAppsList != null) {
            for (PushApplication pushApp : pushAppsList) {
                if (pushApp != null && pushAppId.equals(pushApp.getPushApplicationID())) {
                    return true
                }
            }
        }
        return false
    }

    def nameExistsInList(String pushAppName, List<PushApplication> pushAppsList) {
        if (!StringUtils.isEmpty(pushAppName) && pushAppsList != null) {
            for (PushApplication pushApp : pushAppsList) {
                if (pushApp != null && pushAppName.equals(pushApp.getName())) {
                    return true
                }
            }
        }
        return false
    }

    def registerPushApplication(PushApplication pushApp, Map<String, ?> cookies, String contentType) {

        assert root !=null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType(contentType == null ? "application/json" : contentType)
                .header("Accept", "application/json")
                .cookies(cookies)
                .body( json {
                    name pushApp.getName()
                    description pushApp.getDescription()
                }).post("${root}rest/applications")

        return response
    }

    def listAllPushApplications(Map<String, ?> cookies) {

        assert root !=null

        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies).get("${root}rest/applications")

        return response
    }

    def findPushApplicationById(Map<String, ?> cookies, String pushAppId) {

        assert root !=null

        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies).get("${root}rest/applications/${pushAppId}")

        return response
    }

    def deletePushApplication(Map<String, ?> cookies, String pushApplicationId) {

        assert root !=null

        def json = new JsonBuilder()
        def response = RestAssured.given()
                //.contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                //.body( json { pushAppId pushApplicationId })
                .delete("${root}rest/applications/${pushApplicationId}")

        return response
    }
}
