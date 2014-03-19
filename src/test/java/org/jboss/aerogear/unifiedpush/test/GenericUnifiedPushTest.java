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
