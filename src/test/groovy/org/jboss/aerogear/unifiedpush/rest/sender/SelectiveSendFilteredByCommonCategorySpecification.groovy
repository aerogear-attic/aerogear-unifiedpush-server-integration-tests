package org.jboss.aerogear.unifiedpush.rest.sender

import com.google.android.gcm.server.Sender
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration
import com.notnoop.apns.internal.ApnsServiceImpl
import org.jboss.aerogear.unifiedpush.common.AndroidVariantUtils
import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.aerogear.unifiedpush.common.InstallationUtils
import org.jboss.aerogear.unifiedpush.common.PushApplicationUtils
import org.jboss.aerogear.unifiedpush.common.PushNotificationSenderUtils
import org.jboss.aerogear.unifiedpush.common.SimplePushVariantUtils
import org.jboss.aerogear.unifiedpush.common.iOSVariantUtils

import org.jboss.aerogear.unifiedpush.service.AndroidVariantService
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService
import org.jboss.aerogear.unifiedpush.service.PushApplicationService
import org.jboss.aerogear.unifiedpush.service.SimplePushVariantService
import org.jboss.aerogear.unifiedpush.service.iOSVariantService as IOSVariantService
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.core.Response
import java.util.concurrent.Callable

@ArquillianSpecification
@Mixin([
    AuthenticationUtils,
    PushApplicationUtils,
    AndroidVariantUtils,
    SimplePushVariantUtils,
    InstallationUtils,
    PushNotificationSenderUtils,
    iOSVariantUtils])
class SelectiveSendFilteredByCommonCategorySpecification extends Specification {

    def private static final PUSH_APPLICATION_NAME = "TestPushApplication__1"

    def private static final PUSH_APPLICATION_DECS = "awesome app__1"

    def private static final ANDROID_VARIANT_NAME = "AndroidVariant__1"

    def private static final ANDROID_VARIANT_DESC = "awesome variant__1"

    def private static final ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ__1"

    def private static final ANDROID_DEVICE_TOKEN_X = "gsmToken__"

    def private static final ANDROID_CLIENT_ALIAS_X = "qa_android__"

    def private static final ANDROID_DEVICE_OS = "ANDROID"

    def private static final ANDROID_DEVICE_OS_VERSION = "4.2.2"

    def private static final ANDROID_DEVICE_TYPE = "AndroidPhone"

    def private static final ANDROID_DEVICE_RANGE = 1 .. 10

    def private static final ANDROID_CATEGORY_X = "category_"

    def private static final IOS_VARIANT_NAME = "iOSVariant__1"

    def private static final IOS_VARIANT_DESC = "iOSVariant description __ 1"

    def private static final IOS_CERTIFICATE_PATH = "src/test/resources/certs/qaAerogear.p12"

    def private static final IOS_CERTIFICATE_PASSPHRASE = "aerogear"

    def private static final IOS_DEVICE_TOKEN_X = "iphonetoken__"

    def private static final IOS_CLIENT_ALIAS_X = "qa_ios__"

    def private static final IOS_DEVICE_OS = "IOS"

    def private static final IOS_DEVICE_TYPE = "iPhone"

    def private static final IOS_DEVICE_OS_VERSION = "6"

    def private static final IOS_DEVICE_RANGE = 1 .. 10

    def private static final IOS_CATEGORY_X = "category_"

    def private static final SIMPLE_PUSH_VARIANT_NAME = "SimplePushVariant__1"

    def private static final SIMPLE_PUSH_VARIANT_DESC = "SimplePush description __ 1"

    def private static final SIMPLE_PUSH_DEVICE_TOKEN_X = "simplepushtoken__"

    def private static final SIMPLE_PUSH_CLIENT_ALIAS_X = "qa_simplepush__"

    def private static final SIMPLE_PUSH_NETWORK_URL = "http://localhost:8081/endpoint/"

    def private static final SIMPLE_PUSH_DEVICE_TYPE = "web"

    def private static final SIMPLE_PUSH_DEVICE_OS = "MozillaOS"

    def private static final SIMPLE_PUSH_DEVICE_OS_VERSION = ""

    def private static final SIMPLE_PUSH_CATEGORY_X = "category_"

    def private static final SIMPLE_PUSH_DEVICE_RANGE = 1 .. 10


    def private static final NOTIFICATION_ALERT_MSG = "TEST ALERT"

    def private static final root = new URL("http://localhost:8080/ag-push/")


    @Deployment(testable = true)
    def static WebArchive "create deployment"() {
        Deployments.customUnifiedPushServerWithClasses(SelectiveSendFilteredByCommonCategorySpecification.class)
    }

    @Shared
    def static authCookies

    @Shared
    def static pushApplicationId

    @Shared
    def static masterSecret

    @Shared
    def static androidVariantId

    @Shared
    def static androidSecret

    @Shared
    def static iOSVariantId

    @Shared
    def static iOSSecret

    @Shared
    def static simplePushVariantId

    @Shared
    def static simplePushSecret

    @Inject
    private AndroidVariantService androidVariantService

    @Inject
    private IOSVariantService iOSVariantService

    @Inject
    private SimplePushVariantService simplePushVariantService

    @Inject
    private PushApplicationService pushApplicationService

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
        def pushApplication = createPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DECS, null, null, null)

        when: "Application is registered"
        def response = registerPushApplication(pushApplication, authCookies, null)
        def body = response.body().jsonPath()
        pushApplicationId = body.get("pushApplicationID")
        masterSecret = body.get("masterSecret")

        then: "Response code 201 is returned"
        response.statusCode() == Response.Status.CREATED.getStatusCode()

        and: "Push Application Id is not null"
        pushApplicationId != null

        and: "Master secret is not null"
        masterSecret != null

        and: "Push Application Name is the expected one"
        body.get("name") == PUSH_APPLICATION_NAME
    }

    @RunAsClient
    def "Register an Android variant"() {
        given: "An Android variant"
        def variant = createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC, null, null, null, ANDROID_VARIANT_GOOGLE_KEY)

        when: "Android variant is registered"
        def response = registerAndroidVariant(pushApplicationId, variant, authCookies)
        def body = response.body().jsonPath()
        androidVariantId = body.get("variantID")
        androidSecret = body.get("secret")

        then: "Push Application id is not empty"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Response.Status.CREATED.getStatusCode()

        and: "Android variant id is not null"
        androidVariantId != null

        and: "Android secret is not null"
        androidSecret != null
    }

    @RunAsClient
    def "Register Android device installations"() {
        given: "Android device installation"
        def androidInstallation = createInstallation(ANDROID_DEVICE_TOKEN_X + x,
                ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION,
                ANDROID_CLIENT_ALIAS_X + x, ANDROID_CATEGORY_X + x, null)

        when: "Installation is registered"
        def response = registerInstallation(androidVariantId, androidSecret, androidInstallation)

        then: "Variant id and secret are not null"
        androidVariantId != null && androidSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Response.Status.OK.getStatusCode()

        where:
        x << ANDROID_DEVICE_RANGE
    }

    @RunAsClient
    def "Register an iOS variant"() {
        given: "An iOS application form"
        def variant = createiOSApplicationUploadForm(Boolean.FALSE, IOS_CERTIFICATE_PASSPHRASE, null,
                IOS_VARIANT_NAME, IOS_VARIANT_DESC)

        when: "iOS variant is registered"
        def response = registerIOsVariant(pushApplicationId, variant, authCookies, IOS_CERTIFICATE_PATH)
        def body = response.body().jsonPath()
        iOSVariantId = body.get("variantID")
        iOSSecret = body.get("secret")

        then: "Push Application id is not null"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Response.Status.CREATED.getStatusCode()

        and: "iOS variant id is not null"
        iOSVariantId != null

        and: "iOS secret is not null"
        iOSSecret != null

    }

    @RunAsClient
    def "Register iOS device installations"() {
        given: "iOS device installation"
        def iOSInstallation = createInstallation(IOS_DEVICE_TOKEN_X + x,
                IOS_DEVICE_TYPE, IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION,
                IOS_CLIENT_ALIAS_X + x, IOS_CATEGORY_X + x, null)

        when: "Installation is registered"
        def response = registerInstallation(iOSVariantId, iOSSecret, iOSInstallation)

        then: "Variant id and secret are not null"
        iOSVariantId != null && iOSSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Response.Status.OK.getStatusCode()

        where:
        x << IOS_DEVICE_RANGE
    }

    @RunAsClient
    def "Register a SimplePush variant"() {
        given: "A SimplePush variant"
        def variant = createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME, SIMPLE_PUSH_VARIANT_DESC, null, null, null);

        when: "SimplePush variant is registered"
        def response = registerSimplePushVariant(pushApplicationId, variant, authCookies)
        def body = response.body().jsonPath()
        simplePushVariantId = body.get("variantID")
        simplePushSecret = body.get("secret")

        then: "Push application id is not null"
        pushApplicationId != null

        and: "Response status code is 201"
        response != null && response.statusCode() == Response.Status.CREATED.getStatusCode()

        and: "SimplePush variant id is not null"
        simplePushVariantId != null

        and: "SimplePush secret is not null"
        simplePushSecret != null
    }

    @RunAsClient
    def "Register SimplePush device"() {
        given: "SimplePush device installation"
        def simplePushInstallation = createInstallation(SIMPLE_PUSH_DEVICE_TOKEN_X + x, SIMPLE_PUSH_DEVICE_TYPE,
                SIMPLE_PUSH_DEVICE_OS, SIMPLE_PUSH_DEVICE_OS_VERSION, SIMPLE_PUSH_CLIENT_ALIAS_X + x,
                SIMPLE_PUSH_CATEGORY_X + x, SIMPLE_PUSH_NETWORK_URL + SIMPLE_PUSH_DEVICE_TOKEN_X + x);

        when: "Installation is registered"
        def response = registerInstallation(simplePushVariantId, simplePushSecret, simplePushInstallation);

        then: "Variant id and secret are not null"
        simplePushVariantId != null && simplePushSecret != null

        and: "response status code is 200"
        response != null && response.statusCode() == Response.Status.OK.getStatusCode()

        where:
        x << SIMPLE_PUSH_DEVICE_RANGE
    }

    def "Verify that registrations were done"() {
        when: "Getting all Push Applications for the admin user"
        def pushApplications = pushApplicationService.findAllPushApplicationsForDeveloper(ADMIN_LOGIN_NAME)

        and: "Getting all Android variants"
        def androidVariants = androidVariantService.findAllAndroidVariants()

        and: "Getting all iOS variants"
        def iOSVariants = iOSVariantService.findAlliOSVariants()

        and: "Getting all SimplePush variants"
        def simplePushVariants = simplePushVariantService.findAllSimplePushVariants()

        then: "Injections have been done"
        pushApplicationService != null && androidVariantService != null && iOSVariantService != null &&
                simplePushVariantService != null && clientInstallationService != null

        and: "Push application was successfully registered"
        pushApplications != null && pushApplications.size() == 1 && nameExistsInList(PUSH_APPLICATION_NAME, pushApplications)

        and: "Android variant was successfully registered"
        androidVariants != null && androidVariants.size() == 1

        and: "iOS variant was successfully registered"
        iOSVariants != null && iOSVariants.size() == 1

        and: "SimplePush variant was successfully registered"
        simplePushVariants != null && simplePushVariants.size() == 1

        when: "Getting android variant"
        def androidVariant = androidVariants.get(0)

        and: "Getting iOS variant"
        def iOSVariant = iOSVariants.get(0)

        and: "Getting SimplePush variant"
        def simplePushVariant = simplePushVariants.get(0)

        and: "Getting all registered android device tokens by variant id"
        def androidDeviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(androidVariant.getVariantID())

        and: "Getting all registered iOS device tokens by variant id"
        def iOSDeviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(iOSVariant.getVariantID())

        and: "Getting all registered SimplePush device tokens by variant id"
        def simplePushDeviceTokens = clientInstallationService.findAllDeviceTokenForVariantID(simplePushVariant.getVariantID())

        then: "Android variant is valid and has valid google key"
        androidVariant != null && androidVariant.getGoogleKey() == ANDROID_VARIANT_GOOGLE_KEY

        and: "iOS variant is valid"
        iOSVariant != null

        and:
        androidDeviceTokens != null && androidDeviceTokens.size() == 10

        and:
        iOSDeviceTokens != null && iOSDeviceTokens.size() == 10

        and:
        simplePushDeviceTokens != null && simplePushDeviceTokens.size() == 10

        Sender.clear()
    }

    @RunAsClient
    @Ignore
    def "Selective send by categories"() {

        given: "A category"
        def category = ANDROID_CATEGORY_X + 1

        and: "A message"
        Map<String, Object> messages = new HashMap<String, Object>()
        messages.put("alert", NOTIFICATION_ALERT_MSG)

        when: "Selective send to tokens"
        def response = selectiveSendByCategories(pushApplicationId, masterSecret, category, messages, null)

        then: "Push application id and master secret are not null"
        pushApplicationId != null && masterSecret != null

        and: "Response status code is 200"
        response != null && response.statusCode() == Response.Status.OK.getStatusCode()
    }

    def "Verify that push notifications were sent - filtering by category"() {
        expect:
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 1 &&
                                ApnsServiceImpl.getTokensList() != null && ApnsServiceImpl.getTokensList().size() == 1
                        // TODO add simplepush sending service
                    }
                })

        and:
        Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN_X + 1)

        and:
        Sender.getGcmMessage() != null && Sender.getGcmMessage().getData().get("alert") == NOTIFICATION_ALERT_MSG

        and:
        ApnsServiceImpl.getTokensList().contains(IOS_DEVICE_TOKEN_X + 1)
    }

}
