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
package org.jboss.aerogear.unifiedpush.pushapp


import java.util.HashMap
import java.util.Map

import javax.ws.rs.core.Response.Status

import com.jayway.restassured.RestAssured

import groovy.json.JsonBuilder

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.aerogear.unifiedpush.common.PushApplicationUtils
import org.jboss.aerogear.unifiedpush.model.PushApplication
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification

import com.jayway.restassured.filter.log.RequestLoggingFilter
import com.jayway.restassured.filter.log.ResponseLoggingFilter

import org.apache.http.entity.ContentType

@ArquillianSpecification
@Mixin([PushApplicationUtils])
class RegisterPushAppWithoutLoginSpecification extends Specification {

    @ArquillianResource
    URL root

    @Deployment(testable=false)
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    def "Registering a push application without being logged"() {

        given: "A PushApplication"
        def pushAppName = "My App"
        def pushAppDesc = "Awesome App"
        def pushApp = createPushApplication(pushAppName, pushAppDesc,
                null, null, null)

        when: "The Push Application is registered without being logged in"
        def response = registerPushApplication(pushApp, new HashMap<String, ?>(), null)

        then: "Response code 401 is returned"
        response.statusCode() == Status.UNAUTHORIZED.getStatusCode()
    }
}
