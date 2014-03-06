package org.jboss.aerogear.unifiedpush.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.test.model.AndroidVariant;
import org.jboss.aerogear.test.model.ChromePackagedAppVariant;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.SimplePushVariant;
import org.jboss.aerogear.test.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.AndroidVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.ChromePackagedAppVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.aerogear.unifiedpush.utils.SimplePushVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.iOSVariantUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    protected final static String SIMPLE_PUSH_NETWORK_URL = "http://localhost:8081/endpoint/" +
        SIMPLE_PUSH_DEVICE_TOKEN;

    protected final static String SIMPLE_PUSH_DEVICE_OS = "MozillaOS";

    protected final static String IOS_VARIANT_NAME = "IOS_Variant__1";

    protected final static String IOS_VARIANT_DESC = "awesome variant__1";

    protected final static String IOS_DEVICE_TOKEN = "abcd123456"; // Can be only HEX number

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

    private static Session session;
    private static String loginName;

    private static PushApplication registeredPushApplication;
    private static String registeredPushApplicationName;

    private static AndroidVariant registeredAndroidVariant;
    private static ArrayList<InstallationImpl> registeredAndroidInstallations;

    private static ChromePackagedAppVariant registeredChromePackagedAppVariant;
    private static ArrayList<InstallationImpl> registeredChromeInstallations;

    private static iOSVariant registeredIOSVariant;
    private static ArrayList<InstallationImpl> registeredIOSInstallations;

    private static SimplePushVariant registeredSimplePushVariant;
    private static ArrayList<InstallationImpl> registeredSimplePushInstallations;

    @RunAsClient
    @Test
    @InSequence(1)
    public void authenticate() {
        session = AuthenticationUtils.completeDefaultLogin(getContextRoot());
        assertNotNull(session);
        assertTrue(session.isValid());
    }

    @RunAsClient
    @Test
    @InSequence(2)
    public void registerPushApplication() {
        registeredPushApplication = PushApplicationUtils.generateAndRegister(session);
        assertNotNull(registeredPushApplication);
    }

    @RunAsClient
    @Test
    @InSequence(3)
    public void registerAndroidVariant() {
        registeredAndroidVariant = AndroidVariantUtils.generateAndRegister(registeredPushApplication, session);
        assertNotNull(registeredAndroidVariant);
    }

    @RunAsClient
    @Test
    @InSequence(4)
    public void registerChromeVariant() {
        registeredChromePackagedAppVariant = ChromePackagedAppVariantUtils.generateAndRegister(registeredPushApplication,
            session);
        assertNotNull(registeredChromePackagedAppVariant);
    }

    @RunAsClient
    @Test
    @InSequence(5)
    public void registerSimplePushVariant() {
        registeredSimplePushVariant = SimplePushVariantUtils.generateAndRegister(registeredPushApplication, session);
        assertNotNull(registeredSimplePushVariant);
    }

    @RunAsClient
    @Test
    @InSequence(6)
    public void registerIOSVariant() {
        registeredIOSVariant = iOSVariantUtils.generateAndRegister(IOS_CERTIFICATE_PATH, IOS_CERTIFICATE_PASS_PHRASE,
            false, registeredPushApplication, session);
        assertNotNull(registeredIOSVariant);
    }

    @RunAsClient
    @Test
    @InSequence(7)
    public void registeriOSInstallations() {
        List<InstallationImpl> iosInstallations = InstallationUtils.generateIos(3);

        InstallationUtils.registerAll(iosInstallations, registeredIOSVariant, getSession());

        registeredIOSInstallations = new ArrayList<InstallationImpl>(iosInstallations);
    }

    @RunAsClient
    @Test
    @InSequence(8)
    public void registerAndroidInstallations() {
        List<InstallationImpl> androidInstallations = InstallationUtils.generateAndroid(3);

        InstallationUtils.registerAll(androidInstallations, registeredAndroidVariant, getSession());

        registeredAndroidInstallations = new ArrayList<InstallationImpl>(androidInstallations);
    }

    @RunAsClient
    @Test
    @InSequence(8)
    public void registerChromeInstallations() {
        List<InstallationImpl> chromeInstallations = InstallationUtils.generateChrome(3);

        InstallationUtils.registerAll(chromeInstallations, registeredChromePackagedAppVariant, getSession());

        registeredChromeInstallations = new ArrayList<InstallationImpl>(chromeInstallations);
    }

    @RunAsClient
    @Test
    @InSequence(10)
    public void registerSimplePushInstallation() {
        List<InstallationImpl> simplePushInstallations = InstallationUtils.generateSimplePush(3);

        InstallationUtils.registerAll(simplePushInstallations, registeredSimplePushVariant, getSession());

        registeredSimplePushInstallations = new ArrayList<InstallationImpl>(simplePushInstallations);
    }

    public static Session getSession() {
        return session;
    }

    public static String getLoginName() {
        return loginName;
    }

    public static PushApplication getRegisteredPushApplication() {
        return registeredPushApplication;
    }

    public static String getRegisteredPushApplicationName() {
        return registeredPushApplicationName;
    }

    public static ChromePackagedAppVariant getRegisteredChromePackagedAppVariant() {
        return registeredChromePackagedAppVariant;
    }

    public static AndroidVariant getRegisteredAndroidVariant() {
        return registeredAndroidVariant;
    }

    public static ArrayList<InstallationImpl> getRegisteredAndroidInstallations() {
        return registeredAndroidInstallations;
    }

    public static iOSVariant getRegisteredIOSVariant() {
        return registeredIOSVariant;
    }

    public static ArrayList<InstallationImpl> getRegisteredIOSInstallations() {
        return registeredIOSInstallations;
    }

    public static SimplePushVariant getRegisteredSimplePushVariant() {
        return registeredSimplePushVariant;
    }

    public static ArrayList<InstallationImpl> getRegisteredSimplePushInstallations() {
        return registeredSimplePushInstallations;
    }

    public static ArrayList<InstallationImpl> getRegisteredChromeInstallations() {
        return registeredChromeInstallations;
    }

    protected abstract String getContextRoot();
}
