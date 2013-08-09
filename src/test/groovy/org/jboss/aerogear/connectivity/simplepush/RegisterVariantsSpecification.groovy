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

import com.jayway.restassured.RestAssured

@ArquillianSpecification
@Mixin(AuthenticationUtils)
class RegisterVariantsSpecification extends Specification {

    @ArquillianResource
    URL root

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    // push application ID is reused in registering mobile variant
    @Shared def pushAppId
    @Shared def authCookies

    def setup() {
        authCookies = authCookies ? authCookies : adminLogin().getDetailedCookies()
        //RestAssured.filters(new RequestLoggingFilter(System.err), new ResponseLoggingFilter(System.err))
    }

    // curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"name" : "ddd", "description" :  "ddd" }' http://localhost:8080/ag-push/rest/
    def "Registering a push application"() {

        given: "Application ddd is about to be registered......"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body(json {
                    name "ddd"
                    description "ddd"
                })

        when: "Application is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications")
        def body = response.body().jsonPath()
        pushAppId = body.get("pushApplicationID")

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Push App Id is returned"
        pushAppId != null

        and: "Application Name is returned"
        body.get("name") == "ddd"
    }


    // curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"pushNetworkURL" : "http://localhost:7777/endpoint/"}' http://localhost:8080/ag-push/rest/applications/{PUSH_ID}/simplePush
    def "Registering a SimplePush variant instance"() {

        given: "SimplePush variant of ddd application is about to be registered......"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body(json {
                    name "ddd simple push variant"
                    pushNetworkURL "http://localhost:7777/endpoint/"
                })

        when: "SimplePush variant is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications/${pushAppId}/simplePush ")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Mobile variant Id is returned"
        body.get("variantID") != null

        and: "Secret is returned"
        body.get("secret") != null

        and: "Mobile Variant name is returned"
        body.get("name") == "ddd simple push variant"
    }

    //    curl -v -b cookies.txt -c cookies.txt
    //    -i -H "Accept: application/json" -H "Content-type: multipart/form-data"
    //    -F "certificate=@/Users/matzew/Desktop/MyCert.p12"
    //    -F "passphrase=TopSecret"
    //
    //    -X POST http://localhost:8080/ag-push/rest/applications/{PUSH_ID}/iOS
    def "Registering an iOS mobile variant instance"() {
        given: "iOS variant of ddd application is about to be registered......"
        def request = RestAssured.given()
                .contentType("multipart/form-data")
                .header("Accept", "application/json")
                .cookies(authCookies)
                // TODO this might be needed to be replaced with real stuff on command line
                .multiPart("certificate", new File("src/test/resources/certs/qaAerogear.p12"))
                .multiPart("passphrase", "aerogear")
                .multiPart("production", "false")

        when: "iOS Mobile variant is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications/${pushAppId}/iOS")
        def body = response.body().jsonPath()

        then: "Response code is 201"
        response.statusCode() == 201

        and: "Mobile variant Id is returned"
        body.get("variantID") != null

        and: "Secret is returned"
        body.get("secret") != null
    }

    //curl -v -b cookies.txt -c cookies.txt
    //  -v -H "Accept: application/json" -H "Content-type: application/json"
    //  -X POST
    //  -d '{"googleKey" : "IDDASDASDSA"}'
    //
    //  http://localhost:8080/ag-push/rest/applications/{PUSH_ID}/android
    def "Registering an Android mobile variant instance"() {
        given: "Android variant of ddd application is about to be registered......"
        def json = new JsonBuilder()
        def request = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(authCookies)
                .body(json { googleKey "IDDASDASDSA" })

        when: "Android Mobile variant is registered"
        def response = RestAssured.given().spec(request).post("${root}rest/applications/${pushAppId}/android")
        def body = response.body().jsonPath()

        then: "Response code 201 is returned"
        response.statusCode() == 201

        and: "Mobile variant Id is returned"
        body.get("variantID") != null

        and: "Secret is returned"
        body.get("secret") != null
    }



}

