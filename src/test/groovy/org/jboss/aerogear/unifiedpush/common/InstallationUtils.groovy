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

import java.util.Map

import groovy.json.JsonBuilder

import org.jboss.aerogear.unifiedpush.model.InstallationImpl

import com.jayway.restassured.RestAssured

class InstallationUtils {

    def createInstallation(String deviceToken, String deviceType,
            String operatingSystem, String osVersion, String alias, String category, String simplePushEndpoint) {
        InstallationImpl installation = new InstallationImpl()
        installation.setDeviceToken(deviceToken)
        installation.setDeviceType(deviceType)
        installation.setOperatingSystem(operatingSystem)
        installation.setOsVersion(osVersion)
        installation.setAlias(alias)
        installation.setCategory(category)
        installation.setSimplePushEndpoint(simplePushEndpoint)
        return installation
    }

    def registerInstallation(String variantID, String secret, InstallationImpl installation) {

        assert root !=null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .auth().basic(variantID, secret)
                .header("Accept", "application/json")
                .body( json {
                    deviceToken installation.getDeviceToken()
                    deviceType installation.getDeviceType()
                    operatingSystem installation.getOperatingSystem()
                    osVersion installation.getOsVersion()
                    alias installation.getAlias()
                    category installation.getCategory()
                    simplePushEndpoint installation.getSimplePushEndpoint()
                }).post("${root}rest/registry/device")

        return response
    }

    /* methods of the InstallationManagementEndpoint used by the Admin UI begin */

    def findInstallations(String variantID, Map<String, ?> cookies) {

        assert root !=null

        def response = RestAssured.given()
                .contentType("application/json")
                .cookies(cookies)
                .header("Accept", "application/json")
                .get("${root}rest/applications/${variantID}/installations/")

        return response
    }

    def findInstallation(String variantID, String installationID, Map<String, ?> cookies) {

        assert root !=null

        def response = RestAssured.given()
                .contentType("application/json")
                .cookies(cookies)
                .header("Accept", "application/json")
                .get("${root}rest/applications/${variantID}/installations/${installationID}")

        return response
    }

    def updateInstallation(String variantID, String installationID, InstallationImpl installation, Map<String, ?> cookies) {

        assert root !=null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .cookies(cookies)
                .header("Accept", "application/json")
                .body( json {
                    deviceToken installation.getDeviceToken()
                    deviceType installation.getDeviceType()
                    operatingSystem installation.getOperatingSystem()
                    osVersion installation.getOsVersion()
                    alias installation.getAlias()
                    category installation.getCategory()
                    simplePushEndpoint installation.getSimplePushEndpoint()
                }).put("${root}rest/applications/${variantID}/installations/${installationID}")

        return response
    }

    def removeInstallation(String variantID, String installationID, Map<String, ?> cookies) {

        assert root !=null

        def response = RestAssured.given()
                //.contentType("application/json")
                .cookies(cookies)
                .header("Accept", "application/json")
                .delete("${root}rest/applications/${variantID}/installations/${installationID}")

        return response
    }

    /* methods of the InstallationManagementEndpoint used by the Admin UI end */
}
