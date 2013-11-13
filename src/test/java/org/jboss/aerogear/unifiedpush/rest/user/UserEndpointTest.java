package org.jboss.aerogear.unifiedpush.rest.user;

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

import static org.junit.Assert.*;

public class UserEndpointTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    private static AuthenticationUtils.Session session;
    private static User adminUser;
    private static User developerUser;

    private static final String UPD_USER_FIRST_NAME = "aerogear-qa";
    private static final String UPD_USER_LAST_NAME = "redhat";
    private static final String UPD_USER_EMAIL = "qa@example.com";
    private static final String UPD_USER_LOGIN_NAME = "qa";

    @Test
    @InSequence(1)
    public void authenticate() {
        assertNotNull(getContextRoot());
        session = AuthenticationUtils.completeDefaultLogin(getContextRoot());
        assertNotNull(session);
    }

    @Test
    @InSequence(2)
    public void listAll() {
        // FIXME remove dependency on ".size() == 2"!
        List<User> users = UserEndpointUtils.listAll(session);
        assertEquals(2, users.size());

        assertTrue(UserEndpointUtils.loginNameExistsInList("admin", users));
        assertTrue(UserEndpointUtils.loginNameExistsInList("developer", users));

        for(User user : users) {
            if(adminUser == null && user.getLoginName().equals("admin")) {
                adminUser = user;
            } else if(developerUser == null && user.getLoginName().equals("developer")) {
                developerUser = user;
            } else {
                // This should never happen!
                assertTrue(false);
            }
        }

        assertNotNull(adminUser);
        assertNotNull(developerUser);
    }

    @Test
    @InSequence(3)
    public void updateUser() {
        User user = UserEndpointUtils.generate();

        developerUser.setLoginName(user.getLoginName());

        UserEndpointUtils.update(developerUser, session);
    }

    @Test
    @InSequence(4)
    public void verifyUserUpdate() {
        User user = UserEndpointUtils.findById(developerUser.getId(), session);

        UserEndpointUtils.checkEquality(developerUser, user);

        AuthenticationUtils.Session developerSession = AuthenticationUtils.completeLogin(user.getLoginName(), "123",
                "opensource2013", getContextRoot());

        assertNotNull(developerSession);
        assertTrue(developerSession.isValid());

        AuthenticationUtils.logout(developerSession);

        assertFalse(developerSession.isValid());
    }

    @Test
    @InSequence(5)
    public void deleteUser() {
        UserEndpointUtils.delete(developerUser, session);
    }

    @Test
    @InSequence(6)
    public void verifyUserDeletion() {
        List<User> users = UserEndpointUtils.listAll(session);

        assertEquals(1, users.size());

        User user = users.get(0);

        UserEndpointUtils.checkEquality(adminUser, user);

    }

    @Test(expected = AuthenticationUtils.InvalidPasswordException.class)
    @InSequence(7)
    public void tryLoginWithDeletedUser() {
        AuthenticationUtils.Session developerSession = AuthenticationUtils.completeLogin(developerUser.getLoginName(),
                "123", "opensource2013", getContextRoot());

        // we shouldn't get that far
        assertFalse(developerSession.isValid());
    }
}
