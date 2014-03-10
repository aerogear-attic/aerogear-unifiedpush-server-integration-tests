package org.jboss.aerogear.unifiedpush.rest.sender;

import java.net.URL;

import org.arquillian.extension.smarturl.SchemeName;
import org.arquillian.extension.smarturl.UriScheme;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.BeforeClass;

import com.jayway.restassured.RestAssured;

public class SecureSelectiveSendByCommonCategoryTest extends SelectiveSendByCommonCategoryTest {

    @ArquillianResource
    @UriScheme(name = SchemeName.HTTPS, port = 8443)
    private URL context;

    @BeforeClass
    public static void setup() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }

    @Override
    protected String getContextRoot() {
        return context.toExternalForm();
    }
}
