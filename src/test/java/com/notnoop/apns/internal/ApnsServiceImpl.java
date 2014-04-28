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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.notnoop.apns.ApnsService;
import org.jboss.aerogear.test.api.sender.SenderStatistics;

/**
 *
 * This class mocks the original com.notnoop.apns.internal.ApnsServiceImpl class and is used for testing reasons.
 * This class should be replaced by Byteman based bytecode manipulation
 */
@Deprecated
public class ApnsServiceImpl implements ApnsService {

    public ApnsServiceImpl() {
    }

    private static Collection<String> tokensList = null;

    private static String alert = null;

    private static String sound = null;

    private static int badge = -1;

    private static String customFields;

    public Map<String, Date> getInactiveDevices() {
        final HashMap<String, Date> inactiveTokensHM = new HashMap<String, Date>();

        if (tokensList != null) {
            for (String token : tokensList) {
                if(token.toLowerCase().startsWith(SenderStatistics.TOKEN_INVALIDATION_PREFIX)) {
                    inactiveTokensHM.put(token, new Date());
                }
            }
        }
        return inactiveTokensHM;
    }

    @SuppressWarnings("rawtypes")
    public Collection push(Collection<String> tokens, String message, Date expiry) {
        Logger.getLogger(ApnsServiceImpl.class.getName()).warning(message);

        if (message != null) {
            Map<String, String> parts = parseMessage(message);

            Logger.getLogger(ApnsServiceImpl.class.getName()).warning(parts.toString());

            alert = parts.get("alert");
            sound = parts.get("sound");
            badge = parts.get("badge") != null ? Integer.parseInt(parts.get("badge")) : -1;
            customFields = parts.get("customFields");

        }
        if (tokens != null) {
            tokensList = new ArrayList<String>();
            tokensList.addAll(tokens);
        }
        return null;
    }

    private Map<String, String> parseMessage(String message) {
        Map<String, String> messageParts = new HashMap<String, String>();

        int openBrackets = 0;
        boolean key = true;
        StringBuilder currentKey = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            int backSlashes = 0;
            int charIndex = i - 1;
            while(charIndex > 0) {
                char cc = message.charAt(charIndex--);
                if(cc == '\\') {
                    backSlashes++;
                } else {
                    break;
                }
            }
            boolean isCharEscaped = backSlashes % 2 != 0;
            if(c == ',' && openBrackets == 0 && !isCharEscaped) {
                messageParts.put(currentKey.toString(), currentValue.toString());
                currentKey = new StringBuilder();
                currentValue = new StringBuilder();
                key = true;
            } else if(c == ':' && openBrackets == 0 && !isCharEscaped) {
                key = false;
            } else if(c == '{' && !isCharEscaped) {
                openBrackets++;
            } else if(c == '}' && !isCharEscaped) {
                openBrackets--;
            } else if(key) {
                currentKey.append(c);
            } else {
                currentValue.append(c);
            }
        }
        messageParts.put(currentKey.toString(), currentValue.toString());

        return messageParts;
    }

    public void start() {
    }

    public void stop() {
    }

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

    public static String getCustomFields() {
        return customFields;
    }

    public static void clear() {
        tokensList = null;
        sound = null;
        badge = -1;
        alert = null;
        customFields = null;
    }
}