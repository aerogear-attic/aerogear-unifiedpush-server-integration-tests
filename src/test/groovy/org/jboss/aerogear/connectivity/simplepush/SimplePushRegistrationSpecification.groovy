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
package org.jboss.aerogear.connectivity.simplepush

import javax.inject.Inject
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.connectivity.common.AuthenticationUtils
import org.jboss.aerogear.connectivity.common.Deployments
import org.jboss.aerogear.connectivity.common.InstallationUtils
import org.jboss.aerogear.connectivity.common.PushApplicationUtils
import org.jboss.aerogear.connectivity.common.PushNotificationSenderUtils
import org.jboss.aerogear.connectivity.common.SimplePushVariantUtils
import org.jboss.aerogear.connectivity.model.InstallationImpl
import org.jboss.aerogear.connectivity.model.PushApplication
import org.jboss.aerogear.connectivity.model.SimplePushVariant
import org.jboss.aerogear.connectivity.service.ClientInstallationService
import org.jboss.aerogear.connectivity.service.PushApplicationService
import org.jboss.aerogear.connectivity.service.SimplePushVariantService
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter

import spock.lang.Shared
import spock.lang.Specification


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils, SimplePushVariantUtils,
    InstallationUtils, PushNotificationSenderUtils])
class SimplePushRegistrationSpecification extends Specification {

    def private final static String PUSH_APPLICATION_NAME = "TestPushApplication__1"

    def private final static String PUSH_APPLICATION_DESC = "awesome app__1"

    def private final static String SIMPLE_PUSH_VARIANT_NAME = "SimplePushVariant__1"

    def private final static String SIMPLE_PUSH_VARIANT_DESC = "awesome variant__1"

    def private final static String SIMPLE_PUSH_VARIANT_NETWORK_URL = "http://localhost:8081/endpoint/"

    def private final static String SIMPLE_PUSH_DEVICE_TOKEN = "simplePushToken__1"

    def private final static String SIMPLE_PUSH_DEVICE_TYPE = "web"

    def private final static String SIMPLE_PUSH_DEVICE_OS = "MozillaOS"

    def private final static String SIMPLE_PUSH_CATEGORY = "1234"

    def private final static String SIMPLE_PUSH_CLIENT_ALIAS = "qa_simple_push_1@aerogear"

    def private final static String SIMPLE_PUSH_VERSION = "version=15"

    def private final static URL root = new URL("http://localhost:8080/ag-push/")

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.customUnifiedPushServerWithClasses(SimplePushRegistrationSpecification.class)
    }

    @Shared def static authCookies

    @Shared def static pushApplicationId

    @Shared def static masterSecret

    @Shared def static simplePushVariantId

    @Shared def static simplePushSecret

    @Inject
    private PushApplicationService pushAppService

    @Inject
    private SimplePushVariantService simplePushVariantService

    @Inject
    private ClientInstallationService clientInstallationService

    @RunAsClient
    def "Authenticate"() {
        when:
        authCookies = adminLogin().getCookies()

        then:
        authCookies != null
    }

    @RunAsClient
    def "Register a Push Application"() {
        given: "A Push Application"
        PushApplication pushApp = createPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC,
                null, null, null)

        when: "Application is registered"
        def response = registerPushApplication(pushApp, authCookies, null)
        def body = response.body().jsonPath()
        pushApplicationId = body.get("pushApplicationID")
        masterSecret = body.get("masterSecret")

        then: "Response code 201 is returned"
        response.statusCode() == Status.CREATED.getStatusCode()

        and: "Push App Id is not null"
        pushApplicationId != null

        and: "Master secret is not null"
        masterSecret != null

        and: "Push App Name is the expected one"
        body.get("name") == PUSH_APPLICATION_NAME
    }


    @RunAsClient
    def "Register a Simple Push Variant"() {
        given: "A SimplePush Variant"
        SimplePushVariant variant = createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC,
                null, null, null, SIMPLE_PUSH_VARIANT_NETWORK_URL)

        when: "Simple Push Variant is registered"
        def response = registerSimplePushVariant(pushApplicationId, variant, authCookies)
        def body = response.body().jsonPath()
        simplePushVariantId = body.get("variantID")
        simplePushSecret = body.get("secret")

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Status.CREATED.getStatusCode()

        and: "Simple Push Variant id is not null"
        simplePushVariantId != null

        and: "Secret is not null"
        simplePushSecret != null
    }

    @RunAsClient
    def "Register a Simple Push Variant - Bad Case - Missing auth cookies"() {
        given: "A SimplePush Variant"
        SimplePushVariant variant = createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC,
                null, null, null, null)

        when: "Simple Push Variant is registered"
        def response = registerSimplePushVariant(pushApplicationId, variant, new HashMap<String, ?>())

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 401"
        response != null && response.statusCode() == Status.UNAUTHORIZED.getStatusCode()
    }

    @RunAsClient
    def "Register a Simple Push Variant - Bad Case - Missing network url"() {
        given: "A SimplePush Variant"
        SimplePushVariant variant = createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC,
                null, null, null, null)

        when: "Simple Push Variant is registered"
        def response = registerSimplePushVariant(pushApplicationId, variant, authCookies)
        def body = response.body().jsonPath()

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 400"
        response != null && response.statusCode() == Status.BAD_REQUEST.getStatusCode()
    }

    @RunAsClient
    def "Register an installation for a Simple Push device"() {

        given: "An installation for a Simple Push device"
        InstallationImpl simplePushInstallation = createInstallation(SIMPLE_PUSH_DEVICE_TOKEN, SIMPLE_PUSH_DEVICE_TYPE,
                SIMPLE_PUSH_DEVICE_OS, "", SIMPLE_PUSH_CLIENT_ALIAS, SIMPLE_PUSH_CATEGORY)

        when: "Installation is registered"
        def response = registerInstallation(simplePushVariantId, simplePushSecret, simplePushInstallation)

        then: "Variant id and secret is not null"
        simplePushVariantId != null && simplePushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Verify that registrations were done"() {

        when: "Getting all the Push Applications for the user"
        def List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils.ADMIN_LOGIN_NAME)

        and: "Getting the Simple Push variants"
        def List<SimplePushVariant> simplePushVariants = simplePushVariantService.findAllSimplePushVariants()
        def SimplePushVariant simplePushVariant = simplePushVariants != null ? simplePushVariants.get(0) : null

        and: "Getting the registered tokens by variant id"
        def List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(simplePushVariant.getVariantID())

        then: "Injections have been done"
        pushAppService != null && simplePushVariantService != null && clientInstallationService != null

        and: "The previously registered push app is included in the list"
        pushApps != null && pushApps.size() == 1 && nameExistsInList(PUSH_APPLICATION_NAME, pushApps)

        and: "A Simple Push variant exists"
        simplePushVariants != null && simplePushVariants.size() == 1 && simplePushVariant != null

        and: "The android variant has the expected name"
        SIMPLE_PUSH_VARIANT_NAME.equals(simplePushVariant.getName())

        and: "The registered device tokens should not be null"
        deviceTokens != null

        and: "The registered device tokens should contain the registered SimplePush token"
        deviceTokens.contains(SIMPLE_PUSH_DEVICE_TOKEN)
    }
}
