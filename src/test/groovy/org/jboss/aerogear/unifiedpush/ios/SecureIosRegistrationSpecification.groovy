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
package org.jboss.aerogear.unifiedpush.ios

import javax.inject.Inject
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Constants
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.aerogear.unifiedpush.common.InstallationUtils
import org.jboss.aerogear.unifiedpush.common.PushApplicationUtils
import org.jboss.aerogear.unifiedpush.common.PushNotificationSenderUtils
import org.jboss.aerogear.unifiedpush.common.iOSVariantUtils
import org.jboss.aerogear.unifiedpush.model.iOSVariant
import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService
import org.jboss.aerogear.unifiedpush.service.PushApplicationService
import org.jboss.aerogear.unifiedpush.service.iOSVariantService as IOSVariantService
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.spec.WebArchive

import com.jayway.restassured.RestAssured;

import spock.lang.Shared
import spock.lang.Specification


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils,InstallationUtils,
    PushNotificationSenderUtils,iOSVariantUtils])
class SecureIosRegistrationSpecification extends Specification {

    def private final static PUSH_APPLICATION_NAME = "TestPushApplication__1"

    def private final static PUSH_APPLICATION_DESC = "awesome app__1"

    def private final static NOTIFICATION_ALERT_MSG = "Hello AeroGearers"

    def private final static NOTIFICATION_SOUND = "default"

    def private final static NOTIFICATION_BADGE = 7

    def private final static IOS_VARIANT_NAME = "IOS_Variant__1"

    def private final static UPDATED_IOS_VARIANT_NAME = "IOS_Variant__2"

    def private final static IOS_VARIANT_DESC = "awesome variant__1"

    def private final static UPDATED_IOS_VARIANT_DESC = "awesome variant__2"

    def private final static IOS_DEVICE_TOKEN = "abcd123456"

    def private final static IOS_DEVICE_TOKEN_2 = "abcd456789"

    def private final static IOS_DEVICE_OS = "IOS"

    def private final static UPDATED_IOS_DEVICE_OS = "IOS6"

    def private final static IOS_DEVICE_TYPE = "IOSTablet"

    def private final static UPDATED_IOS_DEVICE_TYPE = "IPhone"

    def private final static IOS_DEVICE_OS_VERSION = "6"

    def private final static UPDATED_IOS_DEVICE_OS_VERSION = "5"

    def private final static IOS_CLIENT_ALIAS = "qa_iOS_1@aerogear"

    def private final static UPDATED_IOS_CLIENT_ALIAS = "upd_qa_iOS_1@aerogear"

    def private final static COMMON_IOS_ANDROID_CLIENT_ALIAS = "qa_ios_android@aerogear"

    def private final static IOS_CERTIFICATE_PATH = "src/test/resources/certs/qaAerogear.p12"

    def private final static IOS_CERTIFICATE_PASS_PHRASE = "aerogear"

    def private final static root = new URL(Constants.SECURE_AG_PUSH_ENDPOINT)

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.customUnifiedPushServerWithClasses(SecureIosRegistrationSpecification.class, Constants.class)
    }

    @Shared def static authCookies

    @Shared def static pushApplicationId

    @Shared def static masterSecret

    @Shared def static iOSVariantId

    @Shared def static iOSPushSecret

    @Inject
    private PushApplicationService pushAppService

    @Inject
    private ClientInstallationService clientInstallationService

    @Inject
    private IOSVariantService iosVariantService

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
    def "Register an iOS Variant - Bad Case - Missing auth cookies"() {
        given: "An iOS application form"
        def form = createiOSApplicationUploadForm(Boolean.FALSE, IOS_CERTIFICATE_PASS_PHRASE, null,
                IOS_VARIANT_NAME, IOS_VARIANT_DESC)

        when: "iOS Variant is registered"
        def response = registerIOsVariant(pushApplicationId, (iOSApplicationUploadForm)form, new HashMap<String, ?>(),
                IOS_CERTIFICATE_PATH)

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 401"
        response != null && response.statusCode() == Status.UNAUTHORIZED.getStatusCode()
    }

    @RunAsClient
    def "Register an iOS Variant"() {
        given: "An iOS application form"
        def form = createiOSApplicationUploadForm(Boolean.FALSE, IOS_CERTIFICATE_PASS_PHRASE, null,
                IOS_VARIANT_NAME, IOS_VARIANT_DESC)

        when: "iOS Variant is registered"
        def response = registerIOsVariant(pushApplicationId, (iOSApplicationUploadForm)form, authCookies,
                IOS_CERTIFICATE_PATH)
        def body = response.body().jsonPath()
        iOSVariantId = body.get("variantID")
        iOSPushSecret = body.get("secret")

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Status.CREATED.getStatusCode()

        and: "iOS Variant id is not null"
        iOSVariantId != null

        and: "iOS Secret is not null"
        iOSPushSecret != null
    }

    @RunAsClient
    def "Update an iOS Variant - Patch Case"() {
        given: "An iOS application form"
        def form = createiOSApplicationUploadForm(Boolean.TRUE, null, null,
                UPDATED_IOS_VARIANT_NAME, UPDATED_IOS_VARIANT_DESC)

        when: "iOS Variant is updated"
        def response = updateIOsVariantPatch(pushApplicationId, (iOSApplicationUploadForm)form, authCookies,
                iOSVariantId)

        then: "Push Application id and iOSVariantId are not null"
        pushApplicationId != null && iOSVariantId != null

        and: "Response status code is 204"
        response != null && response.statusCode() == Status.NO_CONTENT.getStatusCode()
    }

    def "Verify that update patch was done"() {

        when: "Getting the iOS variants"
        def List<iOSVariant> iOSVariants = iosVariantService.findAlliOSVariants()
        def iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null

        then: "Injections have been done"
        iosVariantService != null

        and: "An iOS variant exists"
        iOSVariants != null && iOSVariants.size() == 1 && iOSVariant != null

        and: "The iOS variant has the expected name"
        UPDATED_IOS_VARIANT_NAME.equals(iOSVariant.getName())

        and: "The iOS variant has the expected desc"
        UPDATED_IOS_VARIANT_DESC.equals(iOSVariant.getDescription())

        and: "The iOS variant has the expected prod flag"
        !iOSVariant.isProduction()
    }

    @RunAsClient
    def "Update an iOS Variant - Normal Case"() {
        given: "An iOS application form"
        def form = createiOSApplicationUploadForm(Boolean.TRUE, IOS_CERTIFICATE_PASS_PHRASE, null,
                IOS_VARIANT_NAME, IOS_VARIANT_DESC)

        when: "iOS Variant is updated"
        def response = updateIOsVariant(pushApplicationId, (iOSApplicationUploadForm)form, authCookies,
                IOS_CERTIFICATE_PATH, iOSVariantId)

        then: "Push Application id and iOSVariantId are not null"
        pushApplicationId != null && iOSVariantId != null

        and: "Response status code is 204"
        response != null && response.statusCode() == Status.NO_CONTENT.getStatusCode()
    }

    def "Verify that update was done"() {

        when: "Getting the iOS variants"
        def iOSVariants = iosVariantService.findAlliOSVariants()
        def iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null

        then: "Injections have been done"
        iosVariantService != null

        and: "An iOS variant exists"
        iOSVariants != null && iOSVariants.size() == 1 && iOSVariant != null

        and: "The iOS variant has the expected name"
        IOS_VARIANT_NAME.equals(iOSVariant.getName())

        and: "The iOS variant has the expected desc"
        IOS_VARIANT_DESC.equals(iOSVariant.getDescription())

        and: "The iOS variant has the expected prod flag"
        iOSVariant.isProduction()
    }

    @RunAsClient
    def "Register an installation for an iOS device"() {

        given: "An installation for an iOS device"
        def iOSInstallation = createInstallation(IOS_DEVICE_TOKEN, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, IOS_CLIENT_ALIAS, null, null)

        when: "Installation is registered"
        def response = registerInstallation(iOSVariantId, iOSPushSecret, iOSInstallation)

        then: "Variant id and secret is not null"
        iOSVariantId != null && iOSPushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register a second installation for an iOS device"() {

        given: "An installation for an iOS device"
        def iOSInstallation = createInstallation(IOS_DEVICE_TOKEN_2, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_CLIENT_ALIAS, null, null)

        when: "Installation is registered"
        def response = registerInstallation(iOSVariantId, iOSPushSecret, iOSInstallation)

        then: "Variant id and secret is not null"
        iOSVariantId != null && iOSPushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Verify that registrations were done"() {

        when: "Getting all the Push Applications for the user"
        def pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils.ADMIN_LOGIN_NAME)

        and: "Getting the iOS variants"
        def iOSVariants = iosVariantService.findAlliOSVariants()
        def iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null

        and: "Getting the registered tokens by variant id"
        def deviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(iOSVariant.getVariantID())

        then: "Injections have been done"
        pushAppService != null && iosVariantService != null && clientInstallationService != null

        and: "The previously registered push app is included in the list"
        pushApps != null && pushApps.size() == 1 && nameExistsInList(PUSH_APPLICATION_NAME, pushApps)

        and: "An iOS variant exists"
        iOSVariants != null && iOSVariants.size() == 1 && iOSVariant != null

        and: "The iOS variant has the expected name"
        IOS_VARIANT_NAME.equals(iOSVariant.getName())

        and: "The registered device tokens should not be empty"
        deviceTokens != null

        and: "The registered device tokens should contain the 2 registered iOS tokens"
        deviceTokens.contains(IOS_DEVICE_TOKEN) && deviceTokens.contains(IOS_DEVICE_TOKEN_2)
    }

    @RunAsClient
    def "Update an iOS installation"() {

        given: "An iOS installation"
        def iOSInstallation = createInstallation(IOS_DEVICE_TOKEN, UPDATED_IOS_DEVICE_TYPE,
                UPDATED_IOS_DEVICE_OS, UPDATED_IOS_DEVICE_OS_VERSION, UPDATED_IOS_CLIENT_ALIAS, null, null)

        when: "Installation is registered/updated"
        def response = registerInstallation(iOSVariantId, iOSPushSecret, iOSInstallation)

        then: "IOS variant id and secret are not null"
        iOSVariantId != null && iOSPushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Verify that iOS installation update was done"() {

        when: "Getting all the Push Applications for the user"
        def pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils.ADMIN_LOGIN_NAME)

        and: "Getting the iOS variants"
        def iOSVariants = iosVariantService.findAlliOSVariants()
        def iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null

        and: "Getting the installation by device token"
        def installation = clientInstallationService.findInstallationForVariantByDeviceToken(iOSVariant.getVariantID(), IOS_DEVICE_TOKEN)

        then: "Injections have been done"
        pushAppService != null && iosVariantService != null && clientInstallationService != null

        and: "The previously registered push app is included in the list"
        pushApps != null && pushApps.size() == 1 && nameExistsInList(PUSH_APPLICATION_NAME, pushApps)

        and: "An iOS variant exists"
        iOSVariants != null && iOSVariants.size() == 1 && iOSVariant != null

        and: "The installation's data are updated"
        installation != null && UPDATED_IOS_DEVICE_TYPE.equals(installation.getDeviceType()) && UPDATED_IOS_DEVICE_OS.equals(installation.getOperatingSystem())

        and:
        UPDATED_IOS_DEVICE_OS_VERSION.equals(installation.getOsVersion()) && UPDATED_IOS_CLIENT_ALIAS.equals(installation.getAlias())
    }
}
