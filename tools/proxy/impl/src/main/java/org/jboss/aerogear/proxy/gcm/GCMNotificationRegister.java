package org.jboss.aerogear.proxy.gcm;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.jboss.aerogear.proxy.endpoint.model.GCMNotification;

public class GCMNotificationRegister {

    private static final List<GCMNotification> notifications = new ArrayList<GCMNotification>();

    public static synchronized void addNotification(GCMNotification notification) {
        notifications.add(notification);
    }

    public static synchronized List<GCMNotification> getNotifications() {
        return notifications;
    }

    public static synchronized void clear() {
        notifications.clear();
    }

    @Override
    public String toString() {
        return new Gson().toJson(notifications);
    }
}
