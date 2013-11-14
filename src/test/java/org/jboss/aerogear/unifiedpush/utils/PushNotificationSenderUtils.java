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

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.service.sender.message.SendCriteria;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    public static UnifiedPushMessage createMessage(SendCriteria criteria, String simplePush,
                                                   Map<String, Object> customData) {
        return createMessage(criteria, simplePush, DEFAULT_TTL, customData);
    }

    public static UnifiedPushMessage createMessage(SendCriteria criteria, String simplePush, int timeToLive,
                                                   Map<String, Object> customData) {
        return createMessage(criteria, simplePush, null, null, DEFAULT_BADGE, timeToLive, customData);
    }

    public static UnifiedPushMessage createMessage(SendCriteria criteria, String simplePush, String alert,
                                                   String sound, int badge, int timeToLive,
                                                   Map<String, Object> customData) {
        Map<String, Object> messageMap = messageToMap(criteria, simplePush, alert, sound, badge, timeToLive,
                customData);

        return createMessage(messageMap);
    }

    private static UnifiedPushMessage createMessage(Map<String, Object> messageMap) {
        return new UnifiedPushMessage(messageMap);
    }

    public static void send(PushApplication pushApplication, UnifiedPushMessage message, String root) {
        assertNotNull(root);
        assertNotNull(pushApplication);

        JSONObject jsonObject = new JSONObject();

        jsonObject.putAll(messageToMap(message));

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .auth()
                .basic(pushApplication.getPushApplicationID(), pushApplication.getMasterSecret())
                .header(Headers.acceptJson())
                .body(jsonObject)
                .post("{root}rest/sender", root);

        UnexpectedResponseException.verifyResponse(response, OK);
    }

    public static List<String> getNotifiedDeviceTokens(AuthenticationUtils.Session session) {

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/senderStats", session.getRoot());

        UnexpectedResponseException.verifyResponse(response, OK);

        return response.jsonPath().getList("");
    }

    public static void resetNotifiedDeviceTokensState(AuthenticationUtils.Session session) {

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .delete("{root}rest/senderStats", session.getRoot());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);

    }

    public static List<String> getNotifiedDeviceTokensAndReset(AuthenticationUtils.Session session) {
        List<String> deviceTokens = getNotifiedDeviceTokens(session);

        resetNotifiedDeviceTokensState(session);

        return deviceTokens;
    }

    public static List<String> waitNotifiedDeviceTokens(final int expectedCount,
                                                        final AuthenticationUtils.Session session) {
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                List<String> deviceTokens = getNotifiedDeviceTokens(session);

                return deviceTokens.size() == expectedCount;
            }
        });

        List<String> deviceTokens = getNotifiedDeviceTokens(session);

        assertNotNull(deviceTokens);
        assertEquals(expectedCount, deviceTokens.size());

        return deviceTokens;
    }

    public static List<String> waitNotifiedDeviceTokensAndReset(int expectedCount,
                                                                AuthenticationUtils.Session session) {
        List<String> deviceTokens = waitNotifiedDeviceTokens(expectedCount, session);

        resetNotifiedDeviceTokensState(session);

        return deviceTokens;
    }

    private static Map<String, Object> criteriaToMap(SendCriteria criteria) {
        return criteriaToMap(criteria.getAliases(), criteria.getDeviceTypes(), criteria.getCategories(),
                criteria.getVariants());
    }

    private static Map<String, Object> criteriaToMap(List<String> aliases, List<String> deviceTypes,
                                                     List<String> categories,
                                                     List<String> variants) {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("alias", aliases);
        data.put("deviceType", deviceTypes);
        data.put("categories", categories);
        data.put("variants", variants);
        return data;
    }

    private static Map<String, Object> messageToMap(UnifiedPushMessage message) {
        return messageToMap(message.getSendCriteria(), message.getSimplePush(), message.getAlert(),
                message.getSound(), message.getBadge(), message.getTimeToLive(), message.getData());
    }

    private static Map<String, Object> messageToMap(SendCriteria criteria, String simplePush, String alert,
                                                    String sound, int badge, int timeToLive,
                                                    Map<String, Object> customData) {
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
