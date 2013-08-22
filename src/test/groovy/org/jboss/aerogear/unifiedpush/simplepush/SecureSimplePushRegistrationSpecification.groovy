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
package org.jboss.aerogear.unifiedpush.simplepush

import javax.inject.Inject
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Constants
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.aerogear.unifiedpush.common.InstallationUtils
import org.jboss.aerogear.unifiedpush.common.PushApplicationUtils
import org.jboss.aerogear.unifiedpush.common.PushNotificationSenderUtils
import org.jboss.aerogear.unifiedpush.common.SimplePushVariantUtils
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService
import org.jboss.aerogear.unifiedpush.service.PushApplicationService
import org.jboss.aerogear.unifiedpush.service.SimplePushVariantService
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification

import com.jayway.restassured.RestAssured


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils, SimplePushVariantUtils,
    InstallationUtils, PushNotificationSenderUtils])
class SecureSimplePushRegistrationSpecification extends Specification {

    def private final static PUSH_APPLICATION_NAME = "TestPushApplication__1"

    def private final static PUSH_APPLICATION_DESC = "awesome app__1"

    def private final static SIMPLE_PUSH_VARIANT_NAME = "SimplePushVariant__1"

    def private final static SIMPLE_PUSH_VARIANT_DESC = "awesome variant__1"

    def private final static UPDATED_SIMPLE_PUSH_VARIANT_NAME = "UPD_SimplePushVariant__1"

    def private final static UPDATED_SIMPLE_PUSH_VARIANT_DESC = "UPD_awesome variant__1"

    def private final static SIMPLE_PUSH_DEVICE_TOKEN = "simplePushToken__1"

    def private final static SIMPLE_PUSH_DEVICE_TYPE = "web"

    def private final static UPDATED_SIMPLE_PUSH_DEVICE_TYPE = "upd_web"

    def private final static SIMPLE_PUSH_DEVICE_OS = "MozillaOS"

    def private final static UPDATED_SIMPLE_PUSH_DEVICE_OS = "UPD_MozillaOS"

    def private final static SIMPLE_PUSH_CATEGORY = "1234"

    def private final static UPDATED_SIMPLE_PUSH_CATEGORY = "12345"

    def private final static SIMPLE_PUSH_CLIENT_ALIAS = "qa_simple_push_1@aerogear"

    def private final static UPDATED_SIMPLE_PUSH_CLIENT_ALIAS = "upd_qa_simple_push_1@aerogear"

    def private final static SIMPLE_PUSH_VERSION = "version=15"

    def private final static UPDATED_SIMPLE_PUSH_VERSION = "version=18"

    def private final static SIMPLE_PUSH_NETWORK_URL = "http://localhost:8081/endpoint/"

    def private final static UPDATED_SIMPLE_PUSH_NETWORK_URL = "http://localhost:8081/endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN

    def private final static root = new URL(Constants.SECURE_AG_PUSH_ENDPOINT)

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.customUnifiedPushServerWithClasses(SecureSimplePushRegistrationSpecification.class, Constants.class)
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

    def setupSpec() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD)
    }

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
        def pushApp = createPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC,
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
        def variant = createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC,
                null, null, null)

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
        def variant = createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC,
                null, null, null)

        when: "Simple Push Variant is registered"
        def response = registerSimplePushVariant(pushApplicationId, variant, new HashMap<String, ?>())

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 401"
        response != null && response.statusCode() == Status.UNAUTHORIZED.getStatusCode()
    }

    @RunAsClient
    def "Register an installation for a Simple Push device"() {

        given: "An installation for a Simple Push device"
        def simplePushInstallation = createInstallation(SIMPLE_PUSH_DEVICE_TOKEN, SIMPLE_PUSH_DEVICE_TYPE,
                SIMPLE_PUSH_DEVICE_OS, "", SIMPLE_PUSH_CLIENT_ALIAS, SIMPLE_PUSH_CATEGORY, SIMPLE_PUSH_NETWORK_URL)

        when: "Installation is registered"
        def response = registerInstallation(simplePushVariantId, simplePushSecret, simplePushInstallation)
        def body = response.body().jsonPath()
        def simplePushEndpointURL = body.get("simplePushEndpoint")

        then: "Variant id and secret is not null"
        simplePushVariantId != null && simplePushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()

        and: "SimplePush endpoint returned is the correct one"
        SIMPLE_PUSH_NETWORK_URL.equals(simplePushEndpointURL)
    }

    def "Verify that registrations were done"() {

        when: "Getting all the Push Applications for the user"
        def pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils.ADMIN_LOGIN_NAME)

        and: "Getting the Simple Push variants"
        def simplePushVariants = simplePushVariantService.findAllSimplePushVariants()
        def simplePushVariant = simplePushVariants != null ? simplePushVariants.get(0) : null

        and: "Getting the registered tokens by variant id"
        def deviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(simplePushVariant.getVariantID())

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

    @RunAsClient
    def "Update a SimplePush Variant"() {

        given: "A SimplePush Variant"
        def variant = createSimplePushVariant(UPDATED_SIMPLE_PUSH_VARIANT_NAME, UPDATED_SIMPLE_PUSH_VARIANT_DESC,
                null, null, null)

        when: "SimplePush Variant is updated"
        def response = updateSimplePushVariant(pushApplicationId, variant, authCookies, simplePushVariantId)

        then: "Push Application id and SimplePush variant Id are not empty"
        pushApplicationId != null && simplePushVariantId != null

        and: "Response status code is 204"
        response != null && response.statusCode() == Status.NO_CONTENT.getStatusCode()
    }

    def "Verify that update was done"() {

        when: "Getting the Android variants"
        def simplePushVariants = simplePushVariantService.findAllSimplePushVariants()
        def simplePushVariant = simplePushVariants != null ? simplePushVariants.get(0) : null

        then: "Injections have been done"
        simplePushVariantService != null

        and: "A SimplePush variant exists"
        simplePushVariants != null && simplePushVariants.size() == 1 && simplePushVariant != null

        and: "The SimplePush variant has the expected name"
        UPDATED_SIMPLE_PUSH_VARIANT_NAME.equals(simplePushVariant.getName())

        and: "The SimplePush variant has the expected desc"
        UPDATED_SIMPLE_PUSH_VARIANT_DESC.equals(simplePushVariant.getDescription())
    }

    @RunAsClient
    def "Update an SimplePush installation"() {

        given: "A SimplePush installation"
        def simplePushInstallation = createInstallation(SIMPLE_PUSH_DEVICE_TOKEN, UPDATED_SIMPLE_PUSH_DEVICE_TYPE,
                UPDATED_SIMPLE_PUSH_DEVICE_OS, "", UPDATED_SIMPLE_PUSH_CLIENT_ALIAS, UPDATED_SIMPLE_PUSH_CATEGORY, UPDATED_SIMPLE_PUSH_NETWORK_URL)

        when: "Installation is registered/updated"
        def response = registerInstallation(simplePushVariantId, simplePushSecret, simplePushInstallation)

        then: "SimplePush variant id and secret are not null"
        simplePushVariantId != null && simplePushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Verify that SimplePush installation update was done"() {

        when: "Getting all the Push Applications for the user"
        def pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils.ADMIN_LOGIN_NAME)

        and: "Getting the Simple Push variants"
        def simplePushVariants = simplePushVariantService.findAllSimplePushVariants()
        def simplePushVariant = simplePushVariants != null ? simplePushVariants.get(0) : null

        and: "Getting the installation by device token"
        def installation = clientInstallationService.findInstallationForVariantByDeviceToken(simplePushVariant.getVariantID(), SIMPLE_PUSH_DEVICE_TOKEN)

        then: "Injections have been done"
        pushAppService != null && simplePushVariantService != null && clientInstallationService != null

        and: "The previously registered push app is included in the list"
        pushApps != null && pushApps.size() == 1 && nameExistsInList(PUSH_APPLICATION_NAME, pushApps)

        and: "A SimplePush variant exists"
        simplePushVariants != null && simplePushVariants.size() == 1 && simplePushVariant != null

        and: "The installation's data is updated"
        installation != null && UPDATED_SIMPLE_PUSH_DEVICE_TYPE.equals(installation.getDeviceType()) && UPDATED_SIMPLE_PUSH_DEVICE_OS.equals(installation.getOperatingSystem())

        and:
        UPDATED_SIMPLE_PUSH_CLIENT_ALIAS.equals(installation.getAlias()) && UPDATED_SIMPLE_PUSH_NETWORK_URL.equals(installation.getSimplePushEndpoint())

        and:
        UPDATED_SIMPLE_PUSH_CATEGORY.equals(installation.getCategory())
    }
}
