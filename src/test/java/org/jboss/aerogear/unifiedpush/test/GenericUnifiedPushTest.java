package org.jboss.aerogear.unifiedpush.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm;
import org.jboss.aerogear.unifiedpush.utils.AndroidVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.SimplePushVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.iOSVariantUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

@RunWith(Arquillian.class)
public abstract class GenericUnifiedPushTest {

    protected final static String ANDROID_VARIANT_GOOGLE_KEY = "IDDASDASDSAQ__1";

    protected final static String ANDROID_VARIANT_NAME = "AndroidVariant__1";

    protected final static String ANDROID_VARIANT_DESC = "awesome variant__1";

    protected final static String PUSH_APPLICATION_NAME = "TestPushApplication__1";

    protected final static String PUSH_APPLICATION_DESC = "awesome app__1";

    protected final static String ANDROID_DEVICE_TOKEN = "gsmToken__1";

    protected final static String ANDROID_DEVICE_TOKEN_2 = "gsmToken__2";

    protected final static String ANDROID_DEVICE_TOKEN_3 = "gsmToken__3";

    protected final static String ANDROID_DEVICE_OS = "ANDROID";

    protected final static String ANDROID_DEVICE_TYPE = "AndroidTablet";

    protected final static String ANDROID_DEVICE_TYPE_2 = "AndroidPhone";

    protected final static String ANDROID_DEVICE_OS_VERSION = "4.2.2";

    protected final static String ANDROID_CLIENT_ALIAS = "qa_android_1@aerogear";

    protected final static String ANDROID_CLIENT_ALIAS_2 = "qa_android_2@mobileteam";

    protected final static String SIMPLE_PUSH_VARIANT_NAME = "SimplePushVariant__1";

    protected final static String SIMPLE_PUSH_VARIANT_DESC = "awesome variant__1";

    protected final static String SIMPLE_PUSH_DEVICE_TOKEN = "simplePushToken__1";

    protected final static String SIMPLE_PUSH_DEVICE_TYPE = "web";

    protected final static String SIMPLE_PUSH_NETWORK_URL = "http://localhost:8081/endpoint/" + SIMPLE_PUSH_DEVICE_TOKEN;

    protected final static String SIMPLE_PUSH_DEVICE_OS = "MozillaOS";

    protected final static String IOS_VARIANT_NAME = "IOS_Variant__1";

    protected final static String IOS_VARIANT_DESC = "awesome variant__1";

    protected final static String IOS_DEVICE_TOKEN = "abcd123456";

    protected final static String IOS_DEVICE_TOKEN_2 = "abcd456789";

    protected final static String IOS_DEVICE_OS = "IOS";

    protected final static String IOS_DEVICE_TYPE = "IOSTablet";

    protected final static String IOS_DEVICE_OS_VERSION = "6";

    protected final static String IOS_CLIENT_ALIAS = "qa_iOS_1@aerogear";

    protected final static String SIMPLE_PUSH_CATEGORY = "1234";

    protected final static String SIMPLE_PUSH_CLIENT_ALIAS = "qa_simple_push_1@aerogear";

    protected final static String COMMON_IOS_ANDROID_CLIENT_ALIAS = "qa_ios_android@aerogear";

    protected final static String IOS_CERTIFICATE_PATH = "src/test/resources/certs/qaAerogear.p12";

    protected final static String IOS_CERTIFICATE_PASS_PHRASE = "aerogear";

    private static Map<String, String> authCookies;

    private static String pushApplicationId;

    private static String masterSecret;

    private static String androidVariantId;

    private static String androidSecret;

    private static String simplePushVariantId;

    private static String simplePushSecret;

    private static String iOSVariantId;

    private static String iOSPushSecret;

    @RunAsClient
    @Test
    @InSequence(1)
    public void Authenticate() {
        setAuthCookies(AuthenticationUtils.adminLogin(getContextRoot()).getCookies());
        assertTrue(getAuthCookies() != null);
    }

    @RunAsClient
    @Test
    @InSequence(2)
    public void registerPushApplication() {
        assertNotNull(getAuthCookies());
        PushApplication pushApp = PushApplicationUtils.createPushApplication(PUSH_APPLICATION_NAME, PUSH_APPLICATION_DESC,
                null, null, null);
        Response response = PushApplicationUtils.registerPushApplication(pushApp, authCookies, null, getContextRoot());
        JsonPath body = response.getBody().jsonPath();
        setPushApplicationId((String) body.get("pushApplicationID"));
        setMasterSecret((String) body.get("masterSecret"));

        assertEquals(response.statusCode(), Status.CREATED.getStatusCode());
        assertNotNull(getPushApplicationId());
        assertNotNull(getMasterSecret());
        assertEquals(body.get("name"), PUSH_APPLICATION_NAME);
    }

    @RunAsClient
    @Test
    @InSequence(3)
    public void registerAndroidVariant() {
        assertNotNull(getPushApplicationId());
        assertNotNull(getAuthCookies());
        AndroidVariant variant = AndroidVariantUtils.createAndroidVariant(ANDROID_VARIANT_NAME, ANDROID_VARIANT_DESC, null,
                null, null, ANDROID_VARIANT_GOOGLE_KEY);
        Response response = AndroidVariantUtils.registerAndroidVariant(getPushApplicationId(), variant, getAuthCookies(),
                getContextRoot());
        JsonPath body = response.getBody().jsonPath();
        setAndroidVariantId((String) body.get("variantID"));
        setAndroidSecret((String) body.get("secret"));

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.CREATED.getStatusCode());

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());
    }

    @RunAsClient
    @Test
    @InSequence(4)
    public void registerSimplePushVariant() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getAuthCookies());

        SimplePushVariant variant = SimplePushVariantUtils.createSimplePushVariant(SIMPLE_PUSH_VARIANT_NAME,
                SIMPLE_PUSH_VARIANT_DESC, null, null, null);
        Response response = SimplePushVariantUtils.registerSimplePushVariant(getPushApplicationId(), variant, getAuthCookies(),
                getContextRoot());

        JsonPath body = response.getBody().jsonPath();
        setSimplePushVariantId((String) body.get("variantID"));
        setSimplePushSecret((String) body.get("secret"));

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.CREATED.getStatusCode());

        assertNotNull(getSimplePushVariantId());
        assertNotNull(getSimplePushSecret());
    }

    @RunAsClient
    @Test
    @InSequence(5)
    public void registerIOSVariant() {
        assertNotNull(getPushApplicationId());
        assertNotNull(getAuthCookies());

        iOSApplicationUploadForm form = iOSVariantUtils.createiOSApplicationUploadForm(Boolean.FALSE,
                IOS_CERTIFICATE_PASS_PHRASE, null, IOS_VARIANT_NAME, IOS_VARIANT_DESC);
        Response response = iOSVariantUtils.registerIOsVariant(getPushApplicationId(), (iOSApplicationUploadForm) form,
                getAuthCookies(), IOS_CERTIFICATE_PATH, getContextRoot());

        JsonPath body = response.getBody().jsonPath();
        setiOSVariantId((String) body.get("variantID"));
        setiOSPushSecret((String) body.get("secret"));

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.CREATED.getStatusCode());

        assertNotNull(getiOSVariantId());
        assertNotNull(getiOSPushSecret());
    }

    @RunAsClient
    @Test
    @InSequence(6)
    public void registeriOSInstallation() {
        assertNotNull(getiOSVariantId());
        assertNotNull(getiOSPushSecret());

        InstallationImpl iOSInstallation = InstallationUtils.createInstallation(IOS_DEVICE_TOKEN, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, IOS_CLIENT_ALIAS, null, null);
        Response response = InstallationUtils.registerInstallation(getiOSVariantId(), getiOSPushSecret(), iOSInstallation,
                getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(7)
    public void registerSecondiOSInstallation() {
        assertNotNull(getiOSVariantId());
        assertNotNull(getiOSPushSecret());

        InstallationImpl iOSInstallation = InstallationUtils.createInstallation(IOS_DEVICE_TOKEN_2, IOS_DEVICE_TYPE,
                IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_CLIENT_ALIAS, null, null);
        Response response = InstallationUtils.registerInstallation(getiOSVariantId(), getiOSPushSecret(), iOSInstallation,
                getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(8)
    public void registerAndroidInstallation() {

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN, ANDROID_DEVICE_TYPE,
                ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS, null, null);
        Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                androidInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(9)
    public void registerSecondAndroidInstallation() {

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN_2,
                ANDROID_DEVICE_TYPE_2, ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS_2, null, null);
        Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                androidInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(10)
    public void registerThirdAndroidInstallation() {

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN_3,
                ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, COMMON_IOS_ANDROID_CLIENT_ALIAS, null, null);
        Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                androidInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @RunAsClient
    @Test
    @InSequence(11)
    public void registerSimplePushInstallation() {

        assertNotNull(getSimplePushVariantId());
        assertNotNull(getSimplePushSecret());

        InstallationImpl simplePushInstallation = InstallationUtils.createInstallation(SIMPLE_PUSH_DEVICE_TOKEN,
                SIMPLE_PUSH_DEVICE_TYPE, SIMPLE_PUSH_DEVICE_OS, "", SIMPLE_PUSH_CLIENT_ALIAS, SIMPLE_PUSH_CATEGORY,
                SIMPLE_PUSH_NETWORK_URL);
        Response response = InstallationUtils.registerInstallation(getSimplePushVariantId(), getSimplePushSecret(),
                simplePushInstallation, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    public static Map<String, String> getAuthCookies() {
        return authCookies;
    }

    public static void setAuthCookies(Map<String, String> authCookies) {
        GenericUnifiedPushTest.authCookies = authCookies;
    }

    public static String getPushApplicationId() {
        return pushApplicationId;
    }

    public static void setPushApplicationId(String pushApplicationId) {
        GenericUnifiedPushTest.pushApplicationId = pushApplicationId;
    }

    public static String getMasterSecret() {
        return masterSecret;
    }

    public static void setMasterSecret(String masterSecret) {
        GenericUnifiedPushTest.masterSecret = masterSecret;
    }

    public static String getAndroidVariantId() {
        return androidVariantId;
    }

    public static void setAndroidVariantId(String androidVariantId) {
        GenericUnifiedPushTest.androidVariantId = androidVariantId;
    }

    public static String getAndroidSecret() {
        return androidSecret;
    }

    public static void setAndroidSecret(String androidSecret) {
        GenericUnifiedPushTest.androidSecret = androidSecret;
    }

    public static String getSimplePushVariantId() {
        return simplePushVariantId;
    }

    public static void setSimplePushVariantId(String simplePushVariantId) {
        GenericUnifiedPushTest.simplePushVariantId = simplePushVariantId;
    }

    public static String getSimplePushSecret() {
        return simplePushSecret;
    }

    public static void setSimplePushSecret(String simplePushSecret) {
        GenericUnifiedPushTest.simplePushSecret = simplePushSecret;
    }

    public static String getiOSVariantId() {
        return iOSVariantId;
    }

    public static void setiOSVariantId(String iOSVariantId) {
        GenericUnifiedPushTest.iOSVariantId = iOSVariantId;
    }

    public static String getiOSPushSecret() {
        return iOSPushSecret;
    }

    public static void setiOSPushSecret(String iOSPushSecret) {
        GenericUnifiedPushTest.iOSPushSecret = iOSPushSecret;
    }

    protected abstract String getContextRoot();
}
