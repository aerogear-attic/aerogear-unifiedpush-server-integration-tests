/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.jboss.aerogear.test.model.Developer;
import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.aerogear.unifiedpush.utils.UserEndpointUtils;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;

public class UserEndpointTest extends GenericSimpleUnifiedPushTest {

    @ArquillianResource
    private URL context;

    @Override
    protected String getContextRoot() {
        return context.toExternalForm();
    }

    private static Session session;
    private static Developer adminUser;
    private static Developer developerUser;

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
        List<Developer> users = UserEndpointUtils.listAll(session);
        assertEquals(2, users.size());

        assertTrue(UserEndpointUtils.loginNameExistsInList("admin", users));
        assertTrue(UserEndpointUtils.loginNameExistsInList("developer", users));

        for (Developer user : users) {
            if (adminUser == null && user.getLoginName().equals("admin")) {
                adminUser = user;
            } else if (developerUser == null && user.getLoginName().equals("developer")) {
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
        Developer user = UserEndpointUtils.generate();

        developerUser.setLoginName(user.getLoginName());

        UserEndpointUtils.update(developerUser, session);
    }

    @Test
    @InSequence(4)
    public void verifyUserUpdate() {
        Developer user = UserEndpointUtils.findById(developerUser.getId(), session);

        UserEndpointUtils.checkEquality(developerUser, user);

        Session developerSession = AuthenticationUtils.completeLogin(user.getLoginName(), "123",
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
        List<Developer> users = UserEndpointUtils.listAll(session);

        assertEquals(1, users.size());

        Developer user = users.get(0);

        UserEndpointUtils.checkEquality(adminUser, user);

    }

    @Test(expected = AuthenticationUtils.InvalidPasswordException.class)
    @InSequence(7)
    public void tryLoginWithDeletedUser() {
        Session developerSession = AuthenticationUtils.completeLogin(developerUser.getLoginName(),
            "123", "opensource2013", getContextRoot());

        // we shouldn't get that far
        assertFalse(developerSession.isValid());
    }
}
