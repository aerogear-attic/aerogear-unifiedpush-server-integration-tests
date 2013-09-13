/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.android

import java.util.concurrent.Callable

import javax.inject.Inject
import javax.ws.rs.core.Response.Status

import org.jboss.aerogear.unifiedpush.common.AndroidVariantUtils
import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Constants
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.aerogear.unifiedpush.common.InstallationUtils
import org.jboss.aerogear.unifiedpush.common.PushApplicationUtils
import org.jboss.aerogear.unifiedpush.common.PushNotificationSenderUtils
import org.jboss.aerogear.unifiedpush.common.SimplePushVariantUtils
import org.jboss.aerogear.unifiedpush.common.iOSVariantUtils
import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm
import org.jboss.aerogear.unifiedpush.service.AndroidVariantService
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.spec.WebArchive

import spock.lang.Shared
import spock.lang.Specification

import com.google.android.gcm.server.Sender
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration


@ArquillianSpecification
@Mixin([AuthenticationUtils, PushApplicationUtils, AndroidVariantUtils,
    InstallationUtils, PushNotificationSenderUtils, SimplePushVariantUtils,
    iOSVariantUtils])
class AndroidSelectiveSendCustomDataSpecification extends Specification {

    def private final static ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ__1"

    def private final static ANDROID_VARIANT_NAME = "AndroidVariant__1"

    def private final static ANDROID_VARIANT_DESC = "awesome variant__1"

    def private final static PUSH_APPLICATION_NAME = "TestPushApplication__1"

    def private final static PUSH_APPLICATION_DESC = "awesome app__1"

    def private final static ANDROID_DEVICE_TOKEN = "gsmToken__1"

    def private final static ANDROID_DEVICE_TOKEN_2 = "gsmToken__2"

    def private final static ANDROID_DEVICE_TOKEN_3 = "gsmToken__3"

    def private final static ANDROID_DEVICE_OS = "ANDROID"

    def private final static ANDROID_DEVICE_TYPE = "AndroidTablet"

    def private final static ANDROID_DEVICE_TYPE_2 = "AndroidPhone"

    def private final static ANDROID_DEVICE_OS_VERSION = "4.2.2"

    def private final static ANDROID_CLIENT_ALIAS = "qa_android_1@aerogear"

    def private final static ANDROID_CLIENT_ALIAS_2 = "qa_android_2@mobileteam"

    def private final static SIMPLE_PUSH_VARIANT_NAME = "SimplePushVariant__1"

    def private final static SIMPLE_PUSH_VARIANT_DESC = "awesome variant__1"

    def private final static SIMPLE_PUSH_DEVICE_TOKEN = "simplePushToken__1"

    def private final static SIMPLE_PUSH_DEVICE_TYPE = "web"

    def private final static SIMPLE_PUSH_NETWORK_URL = "http://localhost:8081/endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN

    def private final static SIMPLE_PUSH_DEVICE_OS = "MozillaOS"

    def private final static NOTIFICATION_ALERT_MSG = "Hello AeroGearers"

    def private final static NOTIFICATION_SOUND = "default"

    def private final static int NOTIFICATION_BADGE = 7

    def private final static IOS_VARIANT_NAME = "IOS_Variant__1"

    def private final static IOS_VARIANT_DESC = "awesome variant__1"

    def private final static IOS_DEVICE_TOKEN = "abcd123456"

    def private final static IOS_DEVICE_TOKEN_2 = "abcd456789"

    def private final static IOS_DEVICE_OS = "IOS"

    def private final static IOS_DEVICE_TYPE = "IOSTablet"

    def private final static IOS_DEVICE_OS_VERSION = "6"

    def private final static IOS_CLIENT_ALIAS = "qa_iOS_1@aerogear"

    def private final static SIMPLE_PUSH_CATEGORY = "1234"

    def private final static SIMPLE_PUSH_CLIENT_ALIAS = "qa_simple_push_1@aerogear"

    def private final static COMMON_IOS_ANDROID_CLIENT_ALIAS = "qa_ios_android@aerogear"

    def private final static CUSTOM_FIELD_DATA_MSG = "custom field msg"

    def private final static SIMPLE_PUSH_VERSION = "version=15"

    def private final static IOS_CERTIFICATE_PATH = "src/test/resources/certs/qaAerogear.p12"

    def private final static IOS_CERTIFICATE_PASS_PHRASE = "aerogear"

    def private final static root = new URL(Constants.INSECURE_AG_PUSH_ENDPOINT)

    @Deployment(testable=true)
    def static WebArchive "create deployment"() {
        Deployments.customUnifiedPushServerWithClasses(AndroidSelectiveSendCustomDataSpecification.class, Constants.class)
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
    private AndroidVariantService androidVariantService

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
    def "Register an Android Variant"() {
        given: "An Android Variant"
        def variant = createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC,
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

        and: "Android Secret is not null"
        androidSecret != null
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

        and: "Simple Push secret is not empty"
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
        def iOSInstallation = createInstallation(IOS_DEVICE_TOKEN, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, IOS_CLIENT_ALIAS, null, null)

        when: "Installation is registered"
        def response = registerInstallation(iOSVariantId, iOSPushSecret, iOSInstallation)

        then: "Variant id and secret are not null"
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

        then: "Variant id and secret are not null"
        iOSVariantId != null && iOSPushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register an installation for an Android device"() {

        given: "An installation for an Android device"
        def androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS, null, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret are not null"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register a second installation for an Android device"() {

        given: "An installation for an Android device"
        def androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN_2, ANDROID_DEVICE_TYPE_2,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS_2, null, null)

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
        def androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN_3, ANDROID_DEVICE_TYPE,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_CLIENT_ALIAS, null, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret are not null"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Register an installation for a Simple Push device"() {

        given: "An installation for a Simple Push device"
        def simplePushInstallation = createInstallation(SIMPLE_PUSH_DEVICE_TOKEN, SIMPLE_PUSH_DEVICE_TYPE,
                SIMPLE_PUSH_DEVICE_OS, "", SIMPLE_PUSH_CLIENT_ALIAS, SIMPLE_PUSH_CATEGORY, SIMPLE_PUSH_NETWORK_URL)

        when: "Installation is registered"
        def response = registerInstallation(simplePushVariantId, simplePushSecret, simplePushInstallation)

        then: "Variant id and secret are not null"
        simplePushVariantId != null && simplePushSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    @RunAsClient
    def "Selective send to Android by aliases - Custom data case"() {

        given: "A List of aliases"
        def aliases = new ArrayList<String>()
        aliases.add(ANDROID_CLIENT_ALIAS)
        aliases.add(ANDROID_CLIENT_ALIAS_2)
        Sender.clear()

        and: "A message"
        def messages = new HashMap<String, Object>()
        messages.put("custom", NOTIFICATION_ALERT_MSG)
        messages.put("test", CUSTOM_FIELD_DATA_MSG)

        when: "Selective send to aliases"
        def response = selectiveSend(pushApplicationId, masterSecret, aliases, null, messages, null, null)

        then: "Push application id and master secret are not null"
        pushApplicationId != null && masterSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Status.OK.getStatusCode()
    }

    def "Verify that right GCM notifications were sent - Custom data case"() {

        expect: "Custom GCM Sender send is called with 2 token ids"
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 2
                    }
                }
                )

        and: "The list contains the correct token ids"
        Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN) && Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN_2)

        and: "The messages sent are the correct"
        Sender.getGcmMessage() != null && NOTIFICATION_ALERT_MSG.equals(Sender.getGcmMessage().getData().get("custom"))

        and: "The messages sent are the correct"
        CUSTOM_FIELD_DATA_MSG.equals(Sender.getGcmMessage().getData().get("test"))
    }

    // The GCM Sender returns the tokens as inactive so they should have been deleted
    def "Verify that the inactive tokens were deleted"() {

        when: "Getting the Android variants"
        def androidVariants = androidVariantService.findAllAndroidVariants()
        def androidVariant = androidVariants != null ? androidVariants.get(0) : null

        and: "Getting the registered tokens by variant id"
        def deviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(androidVariant.getVariantID())

        then: "Injections have been done"
        androidVariantService != null && clientInstallationService != null

        and: "The inactive device tokens do not exist"
        deviceTokens != null && !deviceTokens.contains(ANDROID_DEVICE_TOKEN) && !deviceTokens.contains(ANDROID_DEVICE_TOKEN_2)
    }
}
