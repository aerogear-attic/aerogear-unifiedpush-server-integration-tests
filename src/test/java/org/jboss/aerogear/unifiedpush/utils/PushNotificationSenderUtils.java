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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.service.sender.message.SendCriteria;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;
import org.mockito.Mockito;

import com.google.android.gcm.server.Message;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class PushNotificationSenderUtils {

    private static final int DEFAULT_BADGE = -1;
    private static final int DEFAULT_TTL = -1;

    private PushNotificationSenderUtils() {
    }

    public static SendCriteria createCriteria(List<String> aliases, List<String> deviceTypes, List<String> categories,
        List<String> variants) {
        Map<String, Object> data = criteriaToMap(aliases, deviceTypes, categories, variants);

        return new SendCriteria(data);
    }

    public static UnifiedPushMessage createMessage(SendCriteria criteria) {
        return createMessage(criteria, null);
    }

    public static UnifiedPushMessage createMessage(SendCriteria criteria, Map<String, Object> customData) {
        return createMessage(criteria, null, customData);
    }

    public static UnifiedPushMessage createMessage(SendCriteria criteria, String simplePush, Map<String, Object> customData) {
        return createMessage(criteria, simplePush, DEFAULT_TTL, customData);
    }

    public static UnifiedPushMessage createMessage(SendCriteria criteria, String simplePush, int timeToLive,
        Map<String, Object> customData) {
        return createMessage(criteria, simplePush, null, null, DEFAULT_BADGE, timeToLive, customData);
    }

    public static UnifiedPushMessage createMessage(SendCriteria criteria, String simplePush, String alert, String sound,
        int badge, int timeToLive, Map<String, Object> customData) {
        Map<String, Object> messageMap = messageToMap(criteria, simplePush, alert, sound, badge, timeToLive, customData);

        return createMessage(messageMap);
    }

    private static UnifiedPushMessage createMessage(Map<String, Object> messageMap) {
        return new UnifiedPushMessage(messageMap);
    }

    private static boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }

    private static boolean isEmpty(Collection<?> s) {
        return s == null || s.isEmpty();
    }

    private static boolean isEmpty(Map<?, ?> s) {
        return s == null || s.isEmpty();
    }

    // TODO: better implementation
    private static UnifiedMessage createUnifiedMessage(UnifiedPushMessage message, PushApplication pushApplication) {
        UnifiedMessage.Builder unifiedMessage = new UnifiedMessage.Builder()
            .pushApplicationId(pushApplication.getPushApplicationID())
            .masterSecret(pushApplication.getMasterSecret());

        if (!isEmpty(message.getData())) {
            unifiedMessage.attributes(message.getData());
        }

        if (!isEmpty(message.getAlert())) {
            unifiedMessage.alert(message.getAlert());
        }

        if (message.getSendCriteria() != null && !isEmpty(message.getSendCriteria().getAliases())) {
            unifiedMessage.aliases(message.getSendCriteria().getAliases());
        }

        if (message.getBadge() != -1) {
            unifiedMessage.badge(Integer.toString(message.getBadge()));
        }

        if (message.getTimeToLive() != -1) {
            unifiedMessage.timeToLive(message.getTimeToLive());
        }

        if (message.getSendCriteria() != null && !isEmpty(message.getSendCriteria().getCategories())) {
            unifiedMessage.categories(new HashSet<String>(message.getSendCriteria().getCategories()));
        }

        if (message.getSendCriteria() != null && !isEmpty(message.getSendCriteria().getDeviceTypes())) {
            unifiedMessage.deviceType(message.getSendCriteria().getDeviceTypes());
        }

        if (!isEmpty(message.getSimplePush())) {
            unifiedMessage.simplePush(message.getSimplePush());
        }

        if (!isEmpty(message.getSound())) {
            unifiedMessage.sound(message.getSound());
        }

        if (message.getSendCriteria() != null && !isEmpty(message.getSendCriteria().getVariants())) {
            unifiedMessage.variants(message.getSendCriteria().getVariants());
        }

        return unifiedMessage.build();
    }

    public static void send(PushApplication pushApplication, UnifiedPushMessage message, Session session) {
        assertNotNull(pushApplication);

        // FIXME, there are problems with https!
        JavaSender sender = new SenderClient(Constants.INSECURE_AG_PUSH_ENDPOINT);

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
            }
        };

        sender.send(createUnifiedMessage(message, pushApplication), callback);

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

    private static Map<String, Object> criteriaToMap(SendCriteria criteria) {
        return criteriaToMap(criteria.getAliases(), criteria.getDeviceTypes(), criteria.getCategories(), criteria.getVariants());
    }

    private static Map<String, Object> criteriaToMap(List<String> aliases, List<String> deviceTypes, List<String> categories,
        List<String> variants) {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("alias", aliases);
        data.put("deviceType", deviceTypes);
        data.put("categories", categories);
        data.put("variants", variants);
        return data;
    }

    private static Map<String, Object> messageToMap(UnifiedPushMessage message) {
        return messageToMap(message.getSendCriteria(), message.getSimplePush(), message.getAlert(), message.getSound(),
            message.getBadge(), message.getTimeToLive(), message.getData());
    }

    private static Map<String, Object> messageToMap(SendCriteria criteria, String simplePush, String alert, String sound,
        int badge, int timeToLive, Map<String, Object> customData) {
        Map<String, Object> data = new HashMap<String, Object>();

        if (criteria != null) {
            data.putAll(criteriaToMap(criteria));
        }

        Map<String, Object> messageMap = new HashMap<String, Object>();

        messageMap.put("alert", alert);
        messageMap.put("sound", sound);
        messageMap.put("badge", badge);
        if (customData != null) {
            messageMap.putAll(customData);
        }
        data.put("ttl", timeToLive);
        data.put("simple-push", simplePush);
        data.put("message", messageMap);

        return data;
    }

}
