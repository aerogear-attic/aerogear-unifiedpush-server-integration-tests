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
package org.jboss.aerogear.unifiedpush.pushapp

import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Constants
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.aerogear.unifiedpush.common.PushApplicationUtils
import org.jboss.aerogear.unifiedpush.model.PushApplication
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import com.jayway.restassured.RestAssured
import com.jayway.restassured.config.DecoderConfig
import com.jayway.restassured.config.EncoderConfig
import com.jayway.restassured.config.RestAssuredConfig


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils])
class SecureRegisterPushAppSpecification extends Specification {

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    def private final static root = new URL(Constants.SECURE_AG_PUSH_ENDPOINT)

    @Shared def static authCookies

    def setupSpec() {
        // RestAssured uses ISO-8859-1 by default to encode all the stuff, this is not the same as curl does
        // so we are changing RestAssuredConfiguration in order to change encoded/decoder config
        // see https://code.google.com/p/rest-assured/wiki/ReleaseNotes16
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))

        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD)
    }

    def cleanupSpec() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"))
    }

    def setup() {
        authCookies = authCookies ? authCookies : secureLogin().getCookies()
    }

    // curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"name" : "MyApp", "description" :  "awesome app" }'
    // http://localhost:8080/ag-push/rest/applications
    def "Registering a push application"() {

        given: "A PushApplication"
        def pushAppName = "My App"
        def pushAppDesc = "Awesome App"
        def pushApp = createPushApplication(pushAppName, pushAppDesc,
                null, null, null)

        when: "The Push Application is registered"
        def response = registerPushApplication(pushApp, authCookies, "application/json")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == Status.CREATED.getStatusCode()

        and: "Push App Id is not null"
        body.get("pushApplicationID") != null

        and: "Master Secret is not null"
        body.get("masterSecret") != null

        and: "Push App Name is the expected one"
        body.get("name") == pushAppName

        and: "Push App Decsription is the expected one"
        body.get("description") == pushAppDesc
    }

    // note, in json description we cannot use GString ("Description of ${appName}")
    // as it is converted to JSON a different way
    @Unroll
    def "Registering a push application with UTF8"() {

        given: "Application ${appName} is about to be registered"
        def pushAppDesc = "Awesome App"
        def pushApp = createPushApplication(appName, pushAppDesc,
                null, null, null)

        when: "Application ${appName} is registered"
        def response = registerPushApplication(pushApp, authCookies, contentType)
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == Status.CREATED.getStatusCode()

        and: "Push Application Id is not null"
        body.get("pushApplicationID") != null

        and: "Master Secret is not null"
        body.get("masterSecret") != null

        and: "Push App Name is ${appName}"
        body.get("name") == appName

        where:
        appName                 | contentType
        "AwesomeAppěščřžýáíéňľ" | "application/json; charset=utf-8"
        "AwesomeAppவான்வழிe"   |  "application/json; charset=utf-8"
        "AwesomeAppěščřžýáíéňľ" | "application/json"
        "AwesomeAppவான்வழிe"   | "application/json"
    }

}
