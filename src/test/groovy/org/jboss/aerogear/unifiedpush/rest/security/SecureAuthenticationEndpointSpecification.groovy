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
package org.jboss.aerogear.unifiedpush.rest.security

import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Constants
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification

import com.jayway.restassured.RestAssured

@ArquillianSpecification
@RunAsClient
@Mixin(AuthenticationUtils)
class SecureAuthenticationEndpointSpecification extends Specification {

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    @Shared def static authCookies

    def private final static root = new URL(Constants.SECURE_AG_PUSH_ENDPOINT)

    def setupSpec() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD)
    }

    def "Login with default credentials returns HTTP 403"() {
        when: "Performing login with default credentials"
        def response = login(AuthenticationUtils.ADMIN_LOGIN_NAME, AuthenticationUtils.ADMIN_PASSWORD)
        authCookies = response.getCookies()

        then: "Response code 403 is returned"
        response != null && response.statusCode() == Status.FORBIDDEN.getStatusCode()

        and: "Cookies exist"
        authCookies != null
    }

    def "Update password using wrong old password returns HTTP 401"() {
        when: "Updating password using wrong old password"
        def wrongOldPassword = "random"
        def response = updatePassword(AuthenticationUtils.ADMIN_LOGIN_NAME, wrongOldPassword, AuthenticationUtils.NEWPASSWORD, authCookies)

        then: "Response code 401 is returned"
        response != null && response.statusCode() == Status.UNAUTHORIZED.getStatusCode()
    }

    def "Update password returns HTTP 200"() {
        when: "Updating password properly"
        def response = updatePassword(AuthenticationUtils.ADMIN_LOGIN_NAME, AuthenticationUtils.ADMIN_PASSWORD, AuthenticationUtils.NEWPASSWORD, authCookies)

        then: "Response code 200 is returned"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Incorrect login returns HTTP 401"() {
        when: "Performing login using wrong credentials"
        def wrongPassword = "random"
        def response = login(AuthenticationUtils.ADMIN_LOGIN_NAME, wrongPassword)

        then: "Response code 401 is returned"
        response != null && response.statusCode() == Status.UNAUTHORIZED.getStatusCode()
    }

    def "Proper login returns HTTP 200"() {
        when: "Performing proper login"
        def response = login(AuthenticationUtils.ADMIN_LOGIN_NAME, AuthenticationUtils.NEWPASSWORD)
        authCookies = response.getCookies()

        then: "Response code 200 is returned"
        response != null && response.statusCode() == Status.OK.getStatusCode()

        and: "Cookies exist"
        authCookies != null
    }
}
