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


import groovy.json.JsonSlurper

import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Constants
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.aerogear.unifiedpush.common.PushApplicationUtils
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification

import com.jayway.restassured.RestAssured

@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils])
class SecureRegisterReadDeletePushAppSpecification extends Specification {

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    def private final static root = new URL(Constants.SECURE_AG_PUSH_ENDPOINT)

    def setupSpec() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD)
    }

    def private static final pushAppName = "My App"
    def private static final pushAppDesc = "Awesome App"

    @Shared def static authCookies
    @Shared def static pushAppId

    def setup() {
        authCookies = authCookies ? authCookies : secureLogin().getCookies()
        // RestAssured.filters(new RequestLoggingFilter(System.err), new ResponseLoggingFilter(System.err))
    }

    def "Registering a push application"() {

        given: "A PushApplication"
        def pushApp = createPushApplication(pushAppName, pushAppDesc,
                null, null, null)

        when: "The Push Application is registered"
        def response = registerPushApplication(pushApp, authCookies, "application/json")
        def body = response.body().jsonPath()
        pushAppId = body.get("pushApplicationID")

        then: "Response code 201 is returned"
        response.statusCode() == Status.CREATED.getStatusCode()

        and: "Push App Id is not null"
        pushAppId != null

        and: "Push App Name is the expected one"
        pushAppName.equals(body.get("name"))

        and: "Push App Description is the expected one"
        pushAppDesc.equals(body.get("description"))
    }

    def "Retrieve all push applications and found newly registered one in the list"() {

        when: "Apps are retrieved"
        def response = listAllPushApplications(authCookies)
        def responseString = response.asString()
        def slurper = new JsonSlurper()
        def apps = slurper.parseText responseString

        then: "Response code 200 is returned"
        response.statusCode() == Status.OK.getStatusCode()

        and: "pushAppId is in the list"
        def found = apps.find {
            it.get("pushApplicationID") == pushAppId
        }
        found != null
        pushAppName.equals(found.name)
    }

    def "Retrieve registered application"() {

        when: "Application is retrieved"
        def response = findPushApplicationById(authCookies, pushAppId)
        def body = response.body().jsonPath()

        then: "Response code 200 is returned"
        response.statusCode() == Status.OK.getStatusCode()

        and: "App name is the expected one"
        pushAppId.equals(body.get("pushApplicationID"))
        pushAppName.equals(body.get("name"))
    }

    def "Delete registered push app"() {

        when: "Application is deleted"
        def response = deletePushApplication(authCookies, pushAppId)
        def responseString = response.asString()

        then: "Response code 204 is returned"
        response.statusCode() == Status.NO_CONTENT.getStatusCode()

        and: "Content is empty"
        responseString == ""
    }
}
