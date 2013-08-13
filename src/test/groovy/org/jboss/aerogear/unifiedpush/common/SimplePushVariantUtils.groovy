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

import org.jboss.aerogear.unifiedpush.model.AndroidVariant
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant

import com.jayway.restassured.RestAssured

class SimplePushVariantUtils {

    def createSimplePushVariant(String name, String description, String variantID, String secret,
            String developer) {
        SimplePushVariant variant = new SimplePushVariant()
        variant.setName(name)
        variant.setDescription(description)
        variant.setVariantID(variantID)
        variant.setSecret(secret)
        variant.setDeveloper(developer)
        return variant
    }

    def registerSimplePushVariant(String pushAppId, SimplePushVariant variant, Map<String, ?> cookies) {

        assert root !=null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .body( json {
                    name variant.getName()
                    description variant.getDescription()
                }).post("${root}rest/applications/${pushAppId}/simplePush")

        return response
    }

    def updateSimplePushVariant(String pushAppId, SimplePushVariant variant, Map<String, ?> cookies,
            String variantId) {

        assert root !=null

        JsonBuilder json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .body( json {
                    name variant.getName()
                    description variant.getDescription()
                })
                .put("${root}rest/applications/${pushAppId}/simplePush/${variantId}")

        return response
    }
}
