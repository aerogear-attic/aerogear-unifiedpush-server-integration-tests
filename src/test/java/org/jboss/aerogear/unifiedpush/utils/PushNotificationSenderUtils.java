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
package org.jboss.aerogear.unifiedpush.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.mockito.Mockito;

import com.google.android.gcm.server.Message;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class PushNotificationSenderUtils {

    /*
     * private static final int DEFAULT_BADGE = -1;
     * private static final int DEFAULT_TTL = -1;
     */

    private PushNotificationSenderUtils() {
    }

    public static UnifiedMessage.Builder build(List<String> aliases, List<String> deviceTypes, Set<String> categories,
        List<String> variants) {
        UnifiedMessage.Builder builder = new UnifiedMessage.Builder();
        builder.aliases(aliases).deviceType(deviceTypes).categories(categories).variants(variants);
        return builder;
    }

    public static void send(UnifiedMessage message, Session session) {

        JavaSender sender = new SenderClient(session.getBaseUrl().toExternalForm());

        final CountDownLatch latch = new CountDownLatch(1);
        final List<Integer> returnedStatusList = new ArrayList<Integer>(1);
        final AtomicBoolean onFailCalled = new AtomicBoolean(false);

        MessageResponseCallback callback = new MessageResponseCallback() {
            @Override
            public void onComplete(int statusCode) {
                returnedStatusList.add(statusCode);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                onFailCalled.set(true);
                latch.countDown();
                throw new RuntimeException(throwable);
            }
        };

        sender.send(message, callback);

        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }

        assertFalse(onFailCalled.get());

        // The aerogear-unifiedpush-java-client send method receives a MessageResponseCallback which exposes the HTTP status
        // code for the request made. In order to continue using the UnexpectedResponseException.verifyResponse, I had to mock
        // the restassured Response since there is not an available constructor to use for creating an instance. An alternative
        // solution would be to modify the UnexpectedResponseException and add a method which receives and verifies an HTTP
        // status. However, both solutions are ugly. The best practice would be to avoid carrying the restassured Response
        // outside the sender classes.
        // TODO: Create a custom Value Object which will wrap the restassured Response and will be used as a data
        // transfer object inside the integration suite.
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.statusCode()).thenReturn(returnedStatusList.get(0));

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
    }

    public static SenderStatisticsEndpoint.SenderStatistics getSenderStatistics(Session session) {
        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .cookies(session.getCookies())
            .get("/rest/senderStats");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        JsonPath jsonPath = response.jsonPath();

        SenderStatisticsEndpoint.SenderStatistics senderStatistics = new SenderStatisticsEndpoint.SenderStatistics();

        if (jsonPath.getJsonObject("gcmMessage") != null) {
            Message.Builder gcmMessageBuilder = new Message.Builder();

            if (jsonPath.get("gcmMessage.delayWhileIdle") != null) {
                gcmMessageBuilder.delayWhileIdle(jsonPath.getBoolean("gcmMessage.delayWhileIdle"));
            }
            if (jsonPath.get("gcmMessage.collapseKey") != null) {
                gcmMessageBuilder.collapseKey(jsonPath.getString("gcmMessage.collapseKey"));
            }
            if (jsonPath.get("gcmMessage.timeToLive") != null) {
                gcmMessageBuilder.timeToLive(jsonPath.getInt("gcmMessage.timeToLive"));
            }
            Map<String, String> gcmMessageData = jsonPath.getJsonObject("gcmMessage.data");
            for (String key : gcmMessageData.keySet()) {
                gcmMessageBuilder.addData(key, gcmMessageData.get(key));
            }
            senderStatistics.gcmMessage = gcmMessageBuilder.build();
        }

        senderStatistics.deviceTokens = jsonPath.getList("deviceTokens");
        senderStatistics.apnsAlert = jsonPath.getString("apnsAlert");
        senderStatistics.apnsBadge = jsonPath.getInt("apnsBadge");
        senderStatistics.apnsCustomFields = jsonPath.getString("apnsCustomFields");
        senderStatistics.apnsSound = jsonPath.getString("apnsSound");

        return senderStatistics;
    }

    public static void resetSenderStatistics(Session session) {

        Response response = session.given().contentType(ContentTypes.json()).header(Headers.acceptJson())
            .cookies(session.getCookies()).delete("/rest/senderStats");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

    }

    public static SenderStatisticsEndpoint.SenderStatistics getSenderStatisticsAndReset(Session session) {
        SenderStatisticsEndpoint.SenderStatistics senderStatistics = getSenderStatistics(session);

        resetSenderStatistics(session);

        return senderStatistics;
    }

    public static SenderStatisticsEndpoint.SenderStatistics waitSenderStatistics(final int expectedTokenCount,
        final Session session) {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                SenderStatisticsEndpoint.SenderStatistics senderStatistics = getSenderStatistics(session);

                return senderStatistics.deviceTokens != null && senderStatistics.deviceTokens.size() == expectedTokenCount;
            }
        });

        SenderStatisticsEndpoint.SenderStatistics senderStatistics = getSenderStatistics(session);

        assertNotNull(senderStatistics);
        assertNotNull(senderStatistics.deviceTokens);
        assertEquals(expectedTokenCount, senderStatistics.deviceTokens.size());

        return senderStatistics;
    }

    public static SenderStatisticsEndpoint.SenderStatistics waitSenderStatisticsAndReset(int expectedTokenCount,
        Session session) {
        SenderStatisticsEndpoint.SenderStatistics senderStatistics = waitSenderStatistics(expectedTokenCount, session);

        resetSenderStatistics(session);

        return senderStatistics;
    }
}
