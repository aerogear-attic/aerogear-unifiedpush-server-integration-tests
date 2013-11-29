package org.jboss.aerogear.unifiedpush.rest.sender;

import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.junit.BeforeClass;

import com.jayway.restassured.RestAssured;

public class SecureSelectiveSendByCommonCategoryTest extends SelectiveSendByCommonCategoryTest {

    @BeforeClass
    public static void setup() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }

    @Override
    protected String getContextRoot() {
        return Constants.SECURE_AG_PUSH_ENDPOINT;
    }
}
