package org.jboss.aerogear.unifiedpush.message.sender;
/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GCMForChromePushNotificationSender implements Serializable {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final Logger logger = Logger.getLogger(GCMForChromePushNotificationSender.class.getName());

    private static List<String> channelIDs = new ArrayList<String>();
    private static String alert;

    public void sendMessage(ChromePackagedAppVariant chromePackagedAppVariant, List<String> channelIDs,
                            UnifiedPushMessage unifiedPushMessage) {
        logger.warning("Sending to " + channelIDs.size() + " channel IDs.");

        // no need to send empty list
        if (channelIDs.isEmpty()) {
            return;
        }
        alert = unifiedPushMessage.getAlert();
        GCMForChromePushNotificationSender.channelIDs.addAll(channelIDs);
    }

    public static String getAlert() { return alert; }

    public static List<String> getChannelIDs() {
        return channelIDs;
    }

    public static void clear() {
        alert = null;
        channelIDs.clear();
    }
}
