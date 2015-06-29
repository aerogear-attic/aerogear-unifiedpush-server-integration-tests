package org.jboss.aerogear.proxy.apns;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.proxy.endpoint.model.ApnsNotification;

public class ApnsNotificationRegister {

    private static final List<ApnsNotification> notifications = new ArrayList<ApnsNotification>();

    public static synchronized void addNotification(ApnsNotification notification) {
        notifications.add(notification);
    }

    public static synchronized List<ApnsNotification> getNotifications() {
        return notifications;
    }

    public static synchronized void clear() {
        notifications.clear();
    }

}
