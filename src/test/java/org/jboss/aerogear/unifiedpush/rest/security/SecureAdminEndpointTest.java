package org.jboss.aerogear.unifiedpush.rest.security;

import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.junit.BeforeClass;

import com.jayway.restassured.RestAssured;

public class SecureAdminEndpointTest extends AdminEndpointTest {

    @BeforeClass
    public static void setup() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }

    @Override
    protected String getContextRoot() {
        return Constants.SECURE_AG_PUSH_ENDPOINT;
    }
}
