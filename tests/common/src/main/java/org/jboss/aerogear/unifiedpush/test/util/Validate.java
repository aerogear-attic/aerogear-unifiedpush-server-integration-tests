package org.jboss.aerogear.unifiedpush.test.util;

public class Validate {

    public static void notNull(Object object) {
        if (object == null) {
            throw new IllegalStateException("Object must not be null");
        }
    }
}
