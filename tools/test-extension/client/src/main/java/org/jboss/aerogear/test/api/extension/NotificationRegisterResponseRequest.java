package org.jboss.aerogear.test.api.extension;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.proxy.endpoint.model.NotificationRegisterResponse;
import org.jboss.aerogear.proxy.endpoint.model.NotificationRegisterResponseHelper;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.Utilities;

import com.google.gson.Gson;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.awaitility.core.ConditionTimeoutException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public class NotificationRegisterResponseRequest {

    private static final Logger logger = Logger.getLogger(NotificationRegisterResponseRequest.class.getName());

    private static final String DEFAULT_GCM_NOTIFICATION_ENDPOINT_PORT = "17000";

    private static final String DEFAULT_APNS_NOTIFICATION_ENDPOINT_PORT = "17001";

    private final String gcmNotificationEndpointPort;

    private final String apnsNotificationEndpointPort;

    public NotificationRegisterResponseRequest(String gcmNotificationEndpointPort, String apnsNotificationEndpointPort) {
        this.gcmNotificationEndpointPort = gcmNotificationEndpointPort;
        this.apnsNotificationEndpointPort = apnsNotificationEndpointPort;
    }

    public static NotificationRegisterResponseRequest request() {

        String systemGcmNotificationEndpointPort = System.getProperty("gcmNotificationEndpointPort");
        String systemApnsNotificationEndpointPort = System.getProperty("apnsNotificationEndpointPort");

        String gcmNotificationEndpointPort = ((systemGcmNotificationEndpointPort == null || systemGcmNotificationEndpointPort.isEmpty())
            ? DEFAULT_GCM_NOTIFICATION_ENDPOINT_PORT : systemGcmNotificationEndpointPort);

        String apnsNotificationEndpointPort = ((systemApnsNotificationEndpointPort == null || systemApnsNotificationEndpointPort.isEmpty())
            ? DEFAULT_APNS_NOTIFICATION_ENDPOINT_PORT : systemApnsNotificationEndpointPort);

        return new NotificationRegisterResponseRequest(gcmNotificationEndpointPort, apnsNotificationEndpointPort);
    }

    public NotificationRegisterResponse get() {

        // gcm request

        Response gcmProxyResponse = RestAssured.given()
            .baseUri("http://127.0.0.1:" + gcmNotificationEndpointPort)
            .contentType(Utilities.ContentTypes.json())
            .header(Utilities.Headers.acceptJson())
            .get();

        UnexpectedResponseException.verifyResponse(gcmProxyResponse, HttpStatus.SC_OK);

        NotificationRegisterResponse gcmNotificationRegisterResponse = new Gson().fromJson(gcmProxyResponse.getBody().asString(), NotificationRegisterResponse.class);

        logger.info("GOT FROM GCM PROXY AS GET RESPONSE:" + gcmNotificationRegisterResponse.toString());

        // apns request

        Response apnsProxyResponse = RestAssured.given()
            .baseUri("http://127.0.0.1:" + apnsNotificationEndpointPort)
            .contentType(Utilities.ContentTypes.json())
            .header(Utilities.Headers.acceptJson())
            .get();

        UnexpectedResponseException.verifyResponse(apnsProxyResponse, HttpStatus.SC_OK);

        NotificationRegisterResponse apnsNotificationRegisterResponse = new Gson().fromJson(apnsProxyResponse.getBody().asString(), NotificationRegisterResponse.class);

        logger.info("GOT FROM APNS PROXY AS GET RESPONSE:" + apnsNotificationRegisterResponse.toString());

        return NotificationRegisterResponseHelper.merge(gcmNotificationRegisterResponse, apnsNotificationRegisterResponse);
    }

    public void clear() {

        // clearing GCM proxy
        Response gcmProxyResponse = RestAssured.given()
            .baseUri("http://127.0.0.1:" + gcmNotificationEndpointPort)
            .contentType(Utilities.ContentTypes.json())
            .header(Utilities.Headers.acceptJson())
            .get("/clear");

        logger.info("CLEARING FOR GCM");

        // clearing APNS
        Response apnsProxyResponse = RestAssured.given()
            .baseUri("http://127.0.0.1:" + apnsNotificationEndpointPort)
            .contentType(Utilities.ContentTypes.json())
            .header(Utilities.Headers.acceptJson())
            .get("/clear");

        logger.info("CLEARING FOR APNS");

        UnexpectedResponseException.verifyResponse(gcmProxyResponse, HttpStatus.SC_OK);
        UnexpectedResponseException.verifyResponse(apnsProxyResponse, HttpStatus.SC_OK);
    }

    public void await(final int expectedTokenCount, Duration timeout) {

        final AtomicInteger found = new AtomicInteger();

        try {
            Awaitility.await().atMost(timeout).until(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {

                    //SenderStatistics statistics = get();
                    //found.set(statistics.deviceTokens != null ? statistics.deviceTokens.size() : 0);

                    NotificationRegisterResponse response = get();

                    int foundDevices = NotificationRegisterResponseHelper.getDeviceTokens(response).size();

                    logger.info("setting into AtomicInteger found: " + foundDevices);

                    found.set(foundDevices);

                    return found.get() == expectedTokenCount;
                }
            });
        } catch (ConditionTimeoutException e) {
            System.err.println("NotificationRegisterResponse: Was expecting " + expectedTokenCount + " tokens but " + found.get() + " were found.");
        }
    }

    public NotificationRegisterResponse getAndClear() {
        NotificationRegisterResponse notificationRegisterResponse = get();

        clear();

        return notificationRegisterResponse;
    }

    public NotificationRegisterResponse awaitGetAndClear(int expectedTokenCount, Duration timeout) {

        logger.info("expected token count " + expectedTokenCount);

        await(expectedTokenCount, timeout);

        return getAndClear();
    }
}
