/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gcm.server;

import org.jboss.aerogear.test.api.sender.SenderStatistics;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class mocks the original com.google.android.gcm.server.Sender class and is used for testing reasons.
 * FIXME this class should be replaced with Byteman bytecode manipulation
 */
@Deprecated
@ApplicationScoped
public class Sender {

    private static List<String> gcmRegIdsList = null;

    private static Message gcmMessage = null;

    private String key;

    public Sender(String key) {
        this.key = key;
    }

    public Sender() {
    }

    public Result send(Message message, String registrationId, int retries) throws IOException {
        return null;
    }

    public Result sendNoRetry(Message message, String registrationId) throws IOException {
        return null;
    }

    public MulticastResult send(Message message, List<String> regIds, int retries) throws IOException {

        MulticastResult multicatResult = mock(MulticastResult.class);
        ArrayList<Result> resultList = new ArrayList<Result>();

        if (message != null) {
            gcmMessage = message;
        }

        if (regIds != null && !regIds.isEmpty()) {
            gcmRegIdsList = new ArrayList<String>();
            gcmRegIdsList.addAll(regIds);

            for (String regId : regIds) {
                Result result = mock(Result.class);
                if (regId.toLowerCase().startsWith(SenderStatistics.TOKEN_INVALIDATION_PREFIX)) {
                    when(result.getErrorCodeName()).thenReturn(Constants.ERROR_INVALID_REGISTRATION);
                } else {
                    when(result.getErrorCodeName()).thenReturn(null);
                }
                resultList.add(result);
            }
        }

        when(multicatResult.getResults()).thenReturn(resultList);
        return multicatResult;
    }

    public MulticastResult sendNoRetry(Message message, List<String> registrationIds) throws IOException {
        return null;
    }

    public String getKey() {
        return key;
    }

    public static List<String> getGcmRegIdsList() {
        return gcmRegIdsList;
    }

    public static Message getGcmMessage() {
        return gcmMessage;
    }

    public static void clear() {
        gcmRegIdsList = null;
        gcmMessage = null;
    }
}
