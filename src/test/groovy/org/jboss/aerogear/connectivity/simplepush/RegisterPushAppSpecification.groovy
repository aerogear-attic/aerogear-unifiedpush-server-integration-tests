/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.connectivity.simplepush

import groovy.json.JsonBuilder

import org.jboss.aerogear.connectivity.common.AuthenticationUtils
import org.jboss.aerogear.connectivity.common.Deployments
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import com.jayway.restassured.RestAssured
import com.jayway.restassured.config.DecoderConfig
import com.jayway.restassured.config.EncoderConfig
import com.jayway.restassured.config.RestAssuredConfig


@ArquillianSpecification
@Mixin(AuthenticationUtils)
class RegisterPushAppSpecification extends Specification {

    @ArquillianResource
    URL root

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    @Shared def authCookies

    def setupSpec() {
        // RestAssured uses ISO-8859-1 by default to encode all the stuff, this is not the same as curl does
        // so we are changing RestAssuredConfiguration in order to change encoded/decoder config
        // see https://code.google.com/p/rest-assured/wiki/ReleaseNotes16
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
    }

    def cleanupSpec() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"))
    }

    def setup() {
        authCookies = authCookies ? authCookies : adminLogin().getDetailedCookies()
    }

    // curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"name" : "MyApp", "description" :  "awesome app" }'
    // http://localhost:8080/ag-push/rest/applications
    def "Registering a push application"() {

        given: "Application is about to be registered"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body( json {
                    name "MyApp"
                    description "awesome app"
                })

        when: "Application is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Push App Id is not null"
        body.get("pushApplicationID") != null

        and: "Master Secret is not null"
        body.get("masterSecret") != null

        and: "Push App Name is MyApp"
        body.get("name") == "MyApp"
    }

    // note, in json description we cannot use GString ("Description of ${appName}")
    // as it is converted to JSON a different way
    @Unroll
    def "Registering a push application with UTF8"() {

        given: "Application ${appName} is about to be registered"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType(contentType)
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body( json {
                    name appName
                    description "Description of " + appName
                })

        when: "Application ${appName} is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == 201

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
