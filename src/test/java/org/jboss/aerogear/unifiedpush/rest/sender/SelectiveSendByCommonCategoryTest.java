package org.jboss.aerogear.unifiedpush.rest.sender;

import com.google.android.gcm.server.Sender;
import com.notnoop.apns.internal.ApnsServiceImpl;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.service.sender.message.SendCriteria;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.aerogear.unifiedpush.utils.SenderStatisticsEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SelectiveSendByCommonCategoryTest extends GenericUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private static final String ANDROID_DEVICE_TOKEN_X = "gsmToken__";

    private static final String ANDROID_CLIENT_ALIAS_X = "qa_android__";

    private static final int ANDROID_DEVICE_LENGTH = 10;

    private static final String ANDROID_CATEGORY_X = "category_";

    private static final String IOS_DEVICE_TOKEN_X = "iphonetoken__";

    private static final String IOS_CLIENT_ALIAS_X = "qa_ios__";

    private static final int IOS_DEVICE_LENGTH = 10;

    private static final String IOS_CATEGORY_X = "category_";

    private static final String SIMPLE_PUSH_DEVICE_TOKEN_X = "simplepushtoken__";

    private static final String SIMPLE_PUSH_CLIENT_ALIAS_X = "qa_simplepush__";

    private static final String SIMPLE_PUSH_CATEGORY_X = "category_";

    private static final int SIMPLE_PUSH_DEVICE_LENGTH = 10;

    private static final String NOTIFICATION_ALERT_MSG = "TEST ALERT";


    private static final String COMMON_CATEGORY = UUID.randomUUID().toString();

    private static List<InstallationImpl> installationsWithCommonCategory;
    private static InstallationImpl simplePushInstallation;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                SelectiveSendByCommonCategoryTest.class);
    }

    @Test
    @InSequence(12)
    public void registerAndroidInstallation() {
        // FIXME this will work only if this test is first in sequence!
        installationsWithCommonCategory = new ArrayList<InstallationImpl>();

        InstallationImpl generatedInstallation = InstallationUtils.generateAndroid();

        HashSet<String> categories = new HashSet<String>();
        categories.add(COMMON_CATEGORY);
        generatedInstallation.setCategories(categories);

        InstallationUtils.register(generatedInstallation, getRegisteredAndroidVariant(), getContextRoot());

        installationsWithCommonCategory.add(generatedInstallation);
    }

    @Test
    @InSequence(13)
    public void registeriOSInstallation() {
        InstallationImpl generatedInstallation = InstallationUtils.generateIos();

        HashSet<String> categories = new HashSet<String>();
        categories.add(COMMON_CATEGORY);
        generatedInstallation.setCategories(categories);

        InstallationUtils.register(generatedInstallation, getRegisteredIOSVariant(), getContextRoot());

        installationsWithCommonCategory.add(generatedInstallation);
    }

    @Test
    @InSequence(14)
    public void registerSimplePushInstallation() {
        InstallationImpl generatedInstallation = InstallationUtils.generateSimplePush();

        HashSet<String> categories = new HashSet<String>();
        categories.add(COMMON_CATEGORY);
        generatedInstallation.setCategories(categories);

        InstallationUtils.register(generatedInstallation, getRegisteredSimplePushVariant(), getContextRoot());

        // FIXME SimplePush should be also done the same way Sender and ApnsServiceImpl are done
        simplePushInstallation = generatedInstallation;
        // installationsWithCommonCategory.add(generatedInstallation);
    }

    // FIXME do we need to verify this or is it verified elsewhere already?
    /*
    @Test
    @InSequence(15)
    public void verifyRegistrations() {
        assertNotNull(pushApplicationService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApplications = pushApplicationService.findAllPushApplicationsForDeveloper
                (AuthenticationUtils
                .getAdminLoginName());
        List<AndroidVariant> androidVariants = androidVariantService.findAllAndroidVariants();
        List<iOSVariant> iOSVariants = iOSVariantService.findAlliOSVariants();
        List<SimplePushVariant> simplePushVariants = simplePushVariantService.findAllSimplePushVariants();

        assertTrue(pushApplications != null && pushApplications.size() == 1
                && PushApplicationUtils.nameExistsInList(PUSH_APPLICATION_NAME, pushApplications));
        assertTrue(androidVariants != null && androidVariants.size() == 1);
        assertTrue(iOSVariants != null && iOSVariants.size() == 1);
        assertTrue(simplePushVariants != null && simplePushVariants.size() == 1);

        AndroidVariant androidVariant = androidVariants.get(0);
        iOSVariant iOSVariant = iOSVariants.get(0);
        SimplePushVariant simplePushVariant = simplePushVariants.get(0);

        assertNotNull(androidVariant);
        assertEquals(androidVariant.getGoogleKey(), ANDROID_VARIANT_GOOGLE_KEY);

        assertNotNull(iOSVariant);
        assertNotNull(simplePushVariant);

        List<String> androidDeviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                androidVariant.getVariantID(), null, null, null);
        List<String> iOSDeviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                iOSVariant.getVariantID(), null, null, null);
        List<String> simplePushDeviceTokens = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(
                simplePushVariant.getVariantID(), null, null, null);

        for (int x = 1; x <= ANDROID_DEVICE_LENGTH; x++) {
            assertTrue(androidDeviceTokens.contains(ANDROID_DEVICE_TOKEN_X + x));
        }

        for (int x = 1; x <= IOS_DEVICE_LENGTH; x++) {
            assertTrue(iOSDeviceTokens.contains(IOS_DEVICE_TOKEN_X + x));
        }

        for (int x = 1; x <= SIMPLE_PUSH_DEVICE_LENGTH; x++) {
            assertTrue(simplePushDeviceTokens.contains(SIMPLE_PUSH_DEVICE_TOKEN_X + x));
        }

        Sender.clear();
        ApnsServiceImpl.clear();
    }*/

    @Test
    @InSequence(16)
    public void selectiveSendByCommonCategory() {
        Sender.clear();
        ApnsServiceImpl.clear();

        List<String> categories = new ArrayList<String>();
        categories.add(COMMON_CATEGORY);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("alert", NOTIFICATION_ALERT_MSG);

        SendCriteria criteria = PushNotificationSenderUtils.createCriteria(null, null, categories, null);

        UnifiedPushMessage message = PushNotificationSenderUtils.createMessage(criteria, data);

        PushNotificationSenderUtils.send(getRegisteredPushApplication(), message, getContextRoot());
    }

    @Test
    @InSequence(17)
    public void verifyPushNotifications() {
        SenderStatisticsEndpoint.SenderStatistics senderStatistics = PushNotificationSenderUtils
                .waitSenderStatisticsAndReset(installationsWithCommonCategory.size(), getSession());

        for (InstallationImpl installation : installationsWithCommonCategory) {
            assertTrue(senderStatistics.deviceTokens.contains(installation.getDeviceToken()));
        }

        assertNotNull(senderStatistics.gcmMessage);
        assertEquals(NOTIFICATION_ALERT_MSG, senderStatistics.gcmMessage.getData().get("alert"));
        assertEquals(NOTIFICATION_ALERT_MSG, senderStatistics.apnsAlert);
    }
}
