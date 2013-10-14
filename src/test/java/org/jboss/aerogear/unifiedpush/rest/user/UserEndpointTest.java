package org.jboss.aerogear.unifiedpush.rest.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.UserEndpointUtils;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.picketlink.idm.model.basic.User;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class UserEndpointTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private static Map<String, String> authCookies;
    private static String developerUserId;

    private static final String UPD_USER_FIRST_NAME = "aerogear-qa";
    private static final String UPD_USER_LAST_NAME = "redhat";
    private static final String UPD_USER_EMAIL = "qa@example.com";
    private static final String UPD_USER_LOGIN_NAME = "qa";

    @Test
    @InSequence(1)
    public void authenticate() {
        assertNotNull(getContextRoot());
        authCookies = AuthenticationUtils.adminLogin(getContextRoot()).getCookies();
        assertTrue(authCookies != null);
    }

    @Test
    @InSequence(2)
    public void listAll() {
        assertNotNull(authCookies);
        Response listAllResponse = UserEndpointUtils.listAll(authCookies, getContextRoot());
        assertNotNull(listAllResponse);
        assertEquals(Status.OK.getStatusCode(), listAllResponse.getStatusCode());
        JsonPath jsonPath = listAllResponse.getBody().jsonPath();
        assertTrue(jsonPath != null);
        List<Map<String, String>> usersList = jsonPath.getList("");
        assertTrue(usersList != null && usersList.size() == 2);
        String loginName_1 = usersList.get(0).get("loginName");
        String loginName_2 = usersList.get(1).get("loginName");
        assertTrue(("developer".equals(loginName_1) && "admin".equals(loginName_2))
                || ("developer".equals(loginName_2) && "admin".equals(loginName_1)));
        developerUserId = "developer".equals(loginName_1) ? usersList.get(0).get("id") : usersList.get(1).get("id");
        assertNotNull(developerUserId);
    }

    @Test
    @InSequence(3)
    public void updateUser() {
        assertNotNull(authCookies);
        User user = UserEndpointUtils.createUser(UPD_USER_FIRST_NAME, UPD_USER_LAST_NAME, UPD_USER_EMAIL, UPD_USER_LOGIN_NAME,
                developerUserId);
        Response updateResponse = UserEndpointUtils.update(authCookies, user, getContextRoot());
        assertNotNull(updateResponse);
        assertEquals(Status.NO_CONTENT.getStatusCode(), updateResponse.getStatusCode());
    }

    @Test
    @InSequence(4)
    public void verifyUserUpdate() {
        assertNotNull(authCookies);
        Response listAllResponse = UserEndpointUtils.listAll(authCookies, getContextRoot());
        assertNotNull(listAllResponse);
        assertEquals(Status.OK.getStatusCode(), listAllResponse.getStatusCode());
        JsonPath jsonPath = listAllResponse.getBody().jsonPath();
        assertTrue(jsonPath != null);
        List<Map<String, String>> usersList = jsonPath.getList("");
        assertTrue(usersList != null && usersList.size() == 2);
        Map<String, String> qaUserMap = usersList.get(0).values().contains("qa") ? usersList.get(0) : usersList.get(1);
        assertNotNull(qaUserMap);
        assertEquals(UPD_USER_FIRST_NAME, qaUserMap.get("firstName"));
        assertEquals(UPD_USER_LAST_NAME, qaUserMap.get("lastName"));
        assertEquals(UPD_USER_EMAIL, qaUserMap.get("email"));
        assertEquals(UPD_USER_LOGIN_NAME, qaUserMap.get("loginName"));
        assertEquals(developerUserId, qaUserMap.get("id"));

        Response loginResponse = AuthenticationUtils.loginWorkFlow("qa", "123", "aerogear", getContextRoot());
        assertNotNull(loginResponse);
        assertEquals(Status.OK.getStatusCode(), loginResponse.getStatusCode());
    }

    @Test
    @InSequence(5)
    public void findById() {
        assertNotNull(authCookies);
        Response findByIdResponse = UserEndpointUtils.findById(authCookies, developerUserId, getContextRoot());
        assertNotNull(findByIdResponse);
        JsonPath jsonPath = findByIdResponse.getBody().jsonPath();
        assertTrue(jsonPath != null);
        assertEquals(UPD_USER_LOGIN_NAME, jsonPath.get("loginName"));
    }

    @Test
    @InSequence(6)
    public void deleteUser() {
        assertNotNull(authCookies);
        Response deleteResponse = UserEndpointUtils.delete(authCookies, developerUserId, getContextRoot());
        assertNotNull(deleteResponse);
        assertEquals(Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatusCode());
    }

    @Test
    @InSequence(7)
    public void verifyUserDeletion() {
        assertNotNull(authCookies);
        Response listAllResponse = UserEndpointUtils.listAll(authCookies, getContextRoot());
        assertNotNull(listAllResponse);
        assertEquals(Status.OK.getStatusCode(), listAllResponse.getStatusCode());
        JsonPath jsonPath = listAllResponse.getBody().jsonPath();
        assertTrue(jsonPath != null);
        List<Map<String, String>> usersList = jsonPath.getList("");
        assertTrue(usersList != null && usersList.size() == 1);
        assertEquals("admin", usersList.get(0).get("loginName"));

        Response loginResponse = AuthenticationUtils.login(AuthenticationUtils.getDeveloperLoginName(),
                AuthenticationUtils.getDeveloperPassword(), getContextRoot());
        assertNotNull(loginResponse);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), loginResponse.getStatusCode());
    }
}
