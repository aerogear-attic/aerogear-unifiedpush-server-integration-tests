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

import org.jboss.arquillian.container.test.api.RunAsClient

import java.util.concurrent.Callable

import javax.inject.Inject
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.connectivity.common.AndroidVariantUtils
import org.jboss.aerogear.connectivity.common.AuthenticationUtils
import org.jboss.aerogear.connectivity.common.Deployments
import org.jboss.aerogear.connectivity.common.InstallationUtils
import org.jboss.aerogear.connectivity.common.PushApplicationUtils
import org.jboss.aerogear.connectivity.common.PushNotificationSenderUtils
import org.jboss.aerogear.connectivity.common.SimplePushVariantUtils
import org.jboss.aerogear.connectivity.common.iOSVariantUtils
import org.jboss.aerogear.connectivity.model.AndroidVariant
import org.jboss.aerogear.connectivity.model.InstallationImpl
import org.jboss.aerogear.connectivity.model.PushApplication
import org.jboss.aerogear.connectivity.model.SimplePushVariant
import org.jboss.aerogear.connectivity.model.iOSVariant
import org.jboss.aerogear.connectivity.rest.util.iOSApplicationUploadForm
import org.jboss.aerogear.connectivity.service.ClientInstallationService
import org.jboss.aerogear.connectivity.service.iOSVariantService
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter
// workaround to inject lowercase class
import org.jboss.aerogear.connectivity.service.iOSVariantService as IOSVariantService

import spock.lang.Shared
import spock.lang.Specification

import com.google.android.gcm.server.Sender
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration
import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsService
import com.notnoop.apns.ApnsServiceBuilder
import com.notnoop.apns.PayloadBuilder
import com.notnoop.apns.internal.ApnsServiceImpl
import com.notnoop.exceptions.NetworkIOException


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils, AndroidVariantUtils,
    SimplePushVariantUtils, InstallationUtils, PushNotificationSenderUtils,
    iOSVariantUtils])
class IosSelectiveSendFilteredByAliasSpecification extends Specification {

    def private final static String ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ__1"

    def private final static String ANDROID_VARIANT_NAME = "AndroidVariant__1"

    def private final static String ANDROID_VARIANT_DESC = "awesome variant__1"

    def private final static String PUSH_APPLICATION_NAME = "TestPushApplication__1"

    def private final static String PUSH_APPLICATION_DESC = "awesome app__1"

    def private final static String ANDROID_DEVICE_TOKEN = "gsmToken__1"

    def private final static String ANDROID_DEVICE_TOKEN_2 = "gsmToken__2"

    def private final static String ANDROID_DEVICE_TOKEN_3 = "gsmToken__3"

    def private final static String ANDROID_DEVICE_OS = "ANDROID"

    def private final static String ANDROID_DEVICE_TYPE = "AndroidTablet"

    def private final static String ANDROID_DEVICE_TYPE_2 = "AndroidPhone"

    def private final static String ANDROID_DEVICE_OS_VERSION = "4.2.2"

    def private final static String ANDROID_CLIENT_ALIAS = "qa_android_1@aerogear"

    def private final static String ANDROID_CLIENT_ALIAS_2 = "qa_android_2@mobileteam"

    def private final static String SIMPLE_PUSH_VARIANT_NAME = "SimplePushVariant__1"

    def private final static String SIMPLE_PUSH_VARIANT_DESC = "awesome variant__1"

    def private final static String SIMPLE_PUSH_VARIANT_NETWORK_URL = "http://localhost:8081/endpoint/"

    def private final static String SIMPLE_PUSH_DEVICE_TOKEN = "simplePushToken__1"

    def private final static String SIMPLE_PUSH_DEVICE_TYPE = "web"

    def private final static String SIMPLE_PUSH_DEVICE_OS = "MozillaOS"

    def private final static String NOTIFICATION_ALERT_MSG = "Hello AeroGearers"

    def private final static String NOTIFICATION_SOUND = "default"

    def private final static int NOTIFICATION_BADGE = 7

    def private final static String IOS_VARIANT_NAME = "IOS_Variant__1"

    def private final static String IOS_VARIANT_DESC = "awesome variant__1"

    def private final static String IOS_DEVICE_TOKEN = "abcd123456"

    def private final static String IOS_DEVICE_TOKEN_2 = "abcd456789"

    def private final static String IOS_DEVICE_OS = "IOS"

    def private final static String IOS_DEVICE_TYPE = "IOSTablet"

    def private final static String IOS_DEVICE_OS_VERSION = "6"

    def private final static String IOS_CLIENT_ALIAS = "qa_iOS_1@aerogear"

    def private final static String SIMPLE_PUSH_CATEGORY = "1234"

    def private final static String SIMPLE_PUSH_CLIENT_ALIAS = "qa_simple_push_1@aerogear"

    def private final static String COMMON_IOS_ANDROID_CLIENT_ALIAS = "qa_ios_android@aerogear"

    def private final static String CUSTOM_FIELD_DATA_MSG = "custom field msg"

    def private final static String SIMPLE_PUSH_VERSION = "version=15"

    def private final static String IOS_CERTIFICATE_PATH = "src/test/resources/certs/qaAerogear.p12"

    def private final static String IOS_CERTIFICATE_PASS_PHRASE = "aerogear"

    def private final static URL root = new URL("http://localhost:8080/ag-push/")

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.customUnifiedPushServerWithClasses(IosSelectiveSendFilteredByAliasSpecification.class)
    }

    @Shared def static authCookies

    @Shared def static pushApplicationId

    @Shared def static masterSecret

    @Shared def static androidVariantId

    @Shared def static androidSecret

    @Shared def static simplePushVariantId

    @Shared def static simplePushSecret

    @Shared def static iOSVariantId

    @Shared def static iOSPushSecret

    @Inject
    private IOSVariantService iosVariantService

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
    def "Register an Android Variant"() {
        given: "An Android Variant"
        AndroidVariant variant = createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC,
                null, null, null, ANDROID_VARIANT_GOOGLE_KEY)

        when: "Android Variant is registered"
        def response = registerAndroidVariant(pushApplicationId, variant, authCookies)
        def body = response.body().jsonPath()
        androidVariantId = body.get("variantID")
        androidSecret = body.get("secret")

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Status.CREATED.getStatusCode()

        and: "Android Variant id is not null"
        androidVariantId != null

        and: "Secret is not null"
        androidSecret != null
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

    @RunAsClient
    def "Register an installation for an Android device"() {

        given: "An installation for an Android device"
        InstallationImpl androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret is not null"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register a second installation for an Android device"() {

        given: "An installation for an Android device"
        InstallationImpl androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN_2, ANDROID_DEVICE_TYPE_2,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS_2, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret are not null"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register a third installation for an Android device"() {

        given: "An installation for an Android device"
        InstallationImpl androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN_3, ANDROID_DEVICE_TYPE,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_CLIENT_ALIAS, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret is not null"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
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

    @RunAsClient
    def "Selective send to IOS by aliases - Filtering by aliases case"() {

        given: "A List of aliases"
        List<String> aliases = new ArrayList<String>()
        aliases.add(IOS_CLIENT_ALIAS)
        ApnsServiceImpl.clear()

        and: "A message"
        Map<String, Object> messages = new HashMap<String, Object>()
        messages.put("alert", NOTIFICATION_ALERT_MSG)
        messages.put("sound", NOTIFICATION_SOUND)
        messages.put("badge", NOTIFICATION_BADGE)

        when: "Selective send to aliases"
        def response = selectiveSend(pushApplicationId, masterSecret, aliases, null, messages, null, null)

        then: "Push application id and master secret are not null"
        pushApplicationId != null && masterSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Verify that the right iOS notifications were sent - Filtering by aliases case"() {

        expect: "Custom iOS Sender push is called with 1 token id"
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return ApnsServiceImpl.tokensList != null && ApnsServiceImpl.tokensList.size() == 1 // The condition that must be fulfilled
                    }
                }
                )

        and: "The list contains 1 registration token id"
        ApnsServiceImpl.tokensList.contains(IOS_DEVICE_TOKEN)

        and: "The message is the expected one"
        NOTIFICATION_ALERT_MSG.equals(ApnsServiceImpl.alert)

        and: "The sound is the expected one"
        NOTIFICATION_SOUND.equals(ApnsServiceImpl.sound)

        and: "The badge is the expected one"
        NOTIFICATION_BADGE == ApnsServiceImpl.badge
    }

    // The APNS Sender returns the tokens as inactive so they should have been deleted
    def "Verify that the inactive tokens were deleted"() {

        when: "Getting the iOS variants"
        def List<iOSVariant> iosVariants = iosVariantService.findAlliOSVariants()
        def iosVariant = iosVariants != null ? iosVariants.get(0) : null

        and: "Getting the registered tokens by variant id"
        def List<String> deviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(iosVariant.getVariantID())

        then: "Injections have been done"
        iosVariantService != null && clientInstallationService != null

        and: "The inactive device tokens do not exist"
        deviceTokens != null && !deviceTokens.contains(IOS_DEVICE_TOKEN)
    }
}
