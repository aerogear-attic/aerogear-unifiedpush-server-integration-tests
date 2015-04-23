package org.jboss.aerogear.unifiedpush.test.util;

import com.jayway.restassured.response.Header;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class Headers {

    public static Header acceptJson() {
        return new Header("Accept", "application/json");
    }

}
