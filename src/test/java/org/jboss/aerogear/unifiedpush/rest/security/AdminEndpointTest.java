package org.jboss.aerogear.unifiedpush.rest.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.users.Developer;
import org.jboss.aerogear.unifiedpush.utils.AdminUtils;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class AdminEndpointTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private static final String DEVELOPER_LOGIN_NAME = "second_admin";

    private static final String DEVELOPER_PASSWORD = "opensource2013";

    private static final String DEVELOPER_NEW_PASSWORD = "opensource2014";

    private static Map<String, String> authCookies;

    public static Map<String, String> getAuthCookies() {
        return authCookies;
    }

    public static void setAuthCookies(Map<String, String> authCookies) {
        AdminEndpointTest.authCookies = authCookies;
    }

    @Test
    @InSequence(1)
    public void authenticate() {
        Response loginResponse = AuthenticationUtils.adminLogin(getContextRoot());
        assertNotNull(loginResponse);
        assertTrue(loginResponse.getStatusCode() == Status.OK.getStatusCode());
        setAuthCookies(loginResponse.getCookies());
        assertTrue(getAuthCookies() != null);
    }

    @Test
    @InSequence(2)
    public void enroll() {
        assertNotNull(getAuthCookies());
        Developer developer = AdminUtils.createDeveloper(DEVELOPER_LOGIN_NAME, DEVELOPER_PASSWORD);
        Response enrollResponse = AdminUtils.enrollDeveloper(developer, getAuthCookies(), getContextRoot());
        assertNotNull(enrollResponse);
        assertTrue(enrollResponse.getStatusCode() == Status.OK.getStatusCode());
    }

    @Test
    @InSequence(3)
    public void loginWithNewAccount() {
        Response loginResponse = AuthenticationUtils.loginWorkFlow(DEVELOPER_LOGIN_NAME, DEVELOPER_PASSWORD,
                DEVELOPER_NEW_PASSWORD, getContextRoot());
        assertNotNull(loginResponse);
        assertTrue(loginResponse.getStatusCode() == Status.OK.getStatusCode());
    }
}
