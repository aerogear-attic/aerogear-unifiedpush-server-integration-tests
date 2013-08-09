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
package org.jboss.aerogear.connectivity.iospush

import java.util.List

import javax.inject.Inject
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.connectivity.common.AuthenticationUtils
import org.jboss.aerogear.connectivity.common.Deployments
import org.jboss.aerogear.connectivity.common.InstallationUtils
import org.jboss.aerogear.connectivity.common.PushApplicationUtils
import org.jboss.aerogear.connectivity.common.PushNotificationSenderUtils
import org.jboss.aerogear.connectivity.common.iOSVariantUtils
import org.jboss.aerogear.connectivity.model.InstallationImpl
import org.jboss.aerogear.connectivity.model.PushApplication
import org.jboss.aerogear.connectivity.model.iOSVariant
import org.jboss.aerogear.connectivity.rest.util.iOSApplicationUploadForm
import org.jboss.aerogear.connectivity.service.ClientInstallationService
import org.jboss.aerogear.connectivity.service.PushApplicationService
// workaround to inject lowercase class
import org.jboss.aerogear.connectivity.service.iOSVariantService as IOSVariantService
import org.jboss.aerogear.connectivity.service.impl.iOSVariantServiceImpl
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter

import spock.lang.Shared
import spock.lang.Specification

import com.google.android.gcm.server.Sender
import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsService
import com.notnoop.apns.ApnsServiceBuilder
import com.notnoop.apns.PayloadBuilder
import com.notnoop.apns.internal.ApnsServiceImpl
import com.notnoop.exceptions.NetworkIOException


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils,InstallationUtils,
    PushNotificationSenderUtils,iOSVariantUtils])
class IosRegistrationSpecification extends Specification {

    def private final static String PUSH_APPLICATION_NAME = "TestPushApplication__1"

    def private final static String PUSH_APPLICATION_DESC = "awesome app__1"

    def private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers"

    def private final static String NOTIFICATION_SOUND = "default"

    def private final static int NOTIFICATION_BADGE = 7

    def private final static String IOS_VARIANT_NAME = "IOS_Variant__1"

    def private final static String UPDATED_IOS_VARIANT_NAME = "IOS_Variant__2"

    def private final static String IOS_VARIANT_DESC = "awesome variant__1"

    def private final static String UPDATED_IOS_VARIANT_DESC = "awesome variant__2"

    def private final static String IOS_DEVICE_TOKEN = "abcd123456"

    def private final static String IOS_DEVICE_TOKEN_2 = "abcd456789"

    def private final static String IOS_DEVICE_OS = "IOS"

    def private final static String IOS_DEVICE_TYPE = "IOSTablet"

    def private final static String IOS_DEVICE_OS_VERSION = "6"

    def private final static String IOS_CLIENT_ALIAS = "qa_iOS_1@aerogear"

    def private final static String COMMON_IOS_ANDROID_CLIENT_ALIAS = "qa_ios_android@aerogear"

    def private final static String IOS_CERTIFICATE_PATH = "src/test/resources/certs/qaAerogear.p12"

    def private final static String IOS_CERTIFICATE_PASS_PHRASE = "aerogear"

    def private final static URL root = new URL("http://localhost:8080/ag-push/")

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.customUnifiedPushServerWithClasses(IosRegistrationSpecification.class)
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
        def List<iOSVariant> iOSVariants = iosVariantService.findAlliOSVariants()
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
        InstallationImpl iOSInstallation = createInstallation(IOS_DEVICE_TOKEN, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, IOS_CLIENT_ALIAS, null)

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
        InstallationImpl iOSInstallation = createInstallation(IOS_DEVICE_TOKEN_2, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_CLIENT_ALIAS, null)

        when: "Installation is registered"
        def response = registerInstallation(iOSVariantId, iOSPushSecret, iOSInstallation)

        then: "Variant id and secret is not null"
        iOSVariantId != null && iOSPushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Verify that registrations were done"() {

        when: "Getting all the Push Applications for the user"
        def List<PushApplication> pushApps = pushAppService.findAllPushApplicationsForDeveloper(AuthenticationUtils.ADMIN_LOGIN_NAME)

        and: "Getting the iOS variants"
        def List<iOSVariant> iOSVariants = iosVariantService.findAlliOSVariants()
        def iOSVariant = iOSVariants != null ? iOSVariants.get(0) : null

        and: "Getting the registered tokens by variant id"
        def List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(iOSVariant.getVariantID())

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
}
