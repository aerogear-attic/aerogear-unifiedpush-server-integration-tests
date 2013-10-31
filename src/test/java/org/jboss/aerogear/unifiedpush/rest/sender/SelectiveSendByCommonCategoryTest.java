package org.jboss.aerogear.unifiedpush.rest.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.service.AndroidVariantService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.SimplePushVariantService;
import org.jboss.aerogear.unifiedpush.service.iOSVariantService;
import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.aerogear.unifiedpush.test.GenericUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushNotificationSenderUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.google.android.gcm.server.Sender;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.response.Response;
import com.notnoop.apns.internal.ApnsServiceImpl;

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

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return Deployments.customUnifiedPushServerWithClasses(GenericUnifiedPushTest.class,
                SelectiveSendByCommonCategoryTest.class);
    }

    @Inject
    private AndroidVariantService androidVariantService;

    @Inject
    private iOSVariantService iOSVariantService;

    @Inject
    private SimplePushVariantService simplePushVariantService;

    @Inject
    private PushApplicationService pushApplicationService;

    @Inject
    private ClientInstallationService clientInstallationService;

    @RunAsClient
    @Test
    @InSequence(12)
    public void registerAndroidInstallation() {

        assertNotNull(getAndroidVariantId());
        assertNotNull(getAndroidSecret());

        for (int x = 1; x <= ANDROID_DEVICE_LENGTH; x++) {
            InstallationImpl androidInstallation = InstallationUtils.createInstallation(ANDROID_DEVICE_TOKEN_X + x,
                    ANDROID_DEVICE_TYPE, ANDROID_DEVICE_OS, ANDROID_DEVICE_OS_VERSION, ANDROID_CLIENT_ALIAS_X + x,
                    ANDROID_CATEGORY_X + x, null);
            Response response = InstallationUtils.registerInstallation(getAndroidVariantId(), getAndroidSecret(),
                    androidInstallation, getContextRoot());

            assertNotNull(response);
            assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
        }
    }

    @RunAsClient
    @Test
    @InSequence(13)
    public void registeriOSInstallation() {
        assertNotNull(getiOSVariantId());
        assertNotNull(getiOSPushSecret());

        for (int x = 1; x <= IOS_DEVICE_LENGTH; x++) {
            InstallationImpl iOSInstallation = InstallationUtils.createInstallation(IOS_DEVICE_TOKEN_X + x, IOS_DEVICE_TYPE,
                    IOS_DEVICE_OS, IOS_DEVICE_OS_VERSION, IOS_CLIENT_ALIAS_X + x, IOS_CATEGORY_X + x, null);
            Response response = InstallationUtils.registerInstallation(getiOSVariantId(), getiOSPushSecret(), iOSInstallation,
                    getContextRoot());

            assertNotNull(response);
            assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
        }
    }

    @RunAsClient
    @Test
    @InSequence(14)
    public void registerSimplePushInstallation() {

        assertNotNull(getSimplePushSecret());
        assertNotNull(getSimplePushVariantId());

        for (int x = 1; x <= SIMPLE_PUSH_DEVICE_LENGTH; x++) {
            InstallationImpl simplePushInstallation = InstallationUtils.createInstallation(SIMPLE_PUSH_DEVICE_TOKEN_X + x,
                    SIMPLE_PUSH_DEVICE_TYPE, SIMPLE_PUSH_DEVICE_OS, "", SIMPLE_PUSH_CLIENT_ALIAS_X + x, SIMPLE_PUSH_CATEGORY_X
                            + x, SIMPLE_PUSH_NETWORK_URL + SIMPLE_PUSH_DEVICE_TOKEN_X + x);
            Response response = InstallationUtils.registerInstallation(getSimplePushVariantId(), getSimplePushSecret(),
                    simplePushInstallation, getContextRoot());

            assertNotNull(response);
            assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
        }
    }

    @Test
    @InSequence(15)
    public void verifyRegistrations() {
        assertNotNull(pushApplicationService);
        assertNotNull(androidVariantService);
        assertNotNull(iOSVariantService);
        assertNotNull(simplePushVariantService);
        assertNotNull(clientInstallationService);

        List<PushApplication> pushApplications = pushApplicationService.findAllPushApplicationsForDeveloper(AuthenticationUtils
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
    }

    @RunAsClient
    @Test
    @InSequence(16)
    public void selectiveSendByCommonAlias() {

        assertNotNull(getPushApplicationId());
        assertNotNull(getMasterSecret());
        
        Sender.clear();
        ApnsServiceImpl.clear();
        
        String category = ANDROID_CATEGORY_X + 1;

        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("alert", NOTIFICATION_ALERT_MSG);

        Response response = PushNotificationSenderUtils.selectiveSend(getPushApplicationId(), getMasterSecret(), null, null,
                messages, null, category, getContextRoot());

        assertNotNull(response);
        assertEquals(response.statusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(17)
    public void verifyPushNotifications() {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().size() == 1
                        && ApnsServiceImpl.getTokensList() != null && ApnsServiceImpl.getTokensList().size() == 1;
            }
        });

        assertTrue(Sender.getGcmRegIdsList() != null && Sender.getGcmRegIdsList().contains(ANDROID_DEVICE_TOKEN_X + 1));
        assertTrue(Sender.getGcmMessage() != null && Sender.getGcmMessage().getData() != null);
        assertEquals(NOTIFICATION_ALERT_MSG, Sender.getGcmMessage().getData().get("alert"));
        assertTrue(ApnsServiceImpl.getTokensList() != null && ApnsServiceImpl.getTokensList().contains(IOS_DEVICE_TOKEN_X + 1));
        assertEquals(NOTIFICATION_ALERT_MSG, ApnsServiceImpl.getAlert());
    }
}
