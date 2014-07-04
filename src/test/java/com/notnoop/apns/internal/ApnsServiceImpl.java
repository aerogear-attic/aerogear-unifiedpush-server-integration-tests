/*
 * Copyright 2009, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.internal;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.exceptions.NetworkIOException;
import org.jboss.aerogear.test.api.sender.SenderStatistics;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class mocks the original com.notnoop.apns.internal.ApnsServiceImpl class and is used for testing reasons.
 * This class should be replaced by Byteman based bytecode manipulation
 */
@Deprecated
public class ApnsServiceImpl extends AbstractApnsService {

    public ApnsServiceImpl(ApnsConnection connection, ApnsFeedbackConnection feedback) {
        super(feedback);
    }

    private static Collection<String> tokensList = null;

    private static String alert = null;

    private static String sound = null;

    private static int badge = -1;

    private static JSONObject customFields;

    private static long expiry = -1;

    @Override
    public Map<String, Date> getInactiveDevices() {
        final HashMap<String, Date> inactiveTokensHM = new HashMap<String, Date>();

        if (tokensList != null) {
            for (String token : tokensList) {
                if (token.toLowerCase().startsWith(SenderStatistics.TOKEN_INVALIDATION_PREFIX)) {
                    inactiveTokensHM.put(token, new Date());
                }
            }
        }
        return inactiveTokensHM;
    }

    @Override
    public Collection<EnhancedApnsNotification> push(Collection<String> deviceTokens, String payload,
                                                     Date expiry) throws NetworkIOException {
        if (payload != null) {
            JSONObject jsonPayload = new JSONObject(payload);
            JSONObject aps = jsonPayload.getJSONObject("aps");

            System.out.println("Payload JSON: " + jsonPayload.toString());
            System.out.println("APS JSON: " + aps.toString());

            alert = aps.optString("alert");
            sound = aps.optString("sound");
            badge = aps.optInt("badge", -1);

            jsonPayload.remove("aps");
            customFields = jsonPayload;

            ApnsServiceImpl.expiry = expiry.getTime();
        }
        if (deviceTokens != null) {
            tokensList = new ArrayList<String>();
            tokensList.addAll(deviceTokens);
        }
        return null;
    }

    //@SuppressWarnings("rawtypes")
    @Override
    public void push(ApnsNotification notification) {
        // Nothing to do here
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void testConnection() {

    }

    public static Collection<String> getTokensList() {
        return tokensList;
    }

    public static String getAlert() {
        return alert;
    }

    public static String getSound() {
        return sound;
    }

    public static int getBadge() {
        return badge;
    }

    public static JSONObject getCustomFields() {
        return customFields;
    }

    public static long getExpiry() {
        return expiry;
    }

    public static void clear() {
        tokensList = null;
        sound = null;
        badge = -1;
        alert = null;
        customFields = null;
        expiry = -1;
    }
}