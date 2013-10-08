/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class AuthenticationEndpointTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return root.toExternalForm();
    }

    private static Map<String, String> authCookies;

    @Test
    @InSequence(1)
    public void loginDefaultCredentialsLeadsTo403() {
        Response response = AuthenticationUtils.login(AuthenticationUtils.getAdminLoginName(),
                AuthenticationUtils.getAdminPassword(), getContextRoot());
        assertNotNull(response);
        authCookies = response.getCookies();
        assertNotNull(authCookies);
        assertEquals(response.getStatusCode(), Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @InSequence(2)
    public void updatePasswordUsingWrongOldPasswordLeadsTo401() {
        assertNotNull(authCookies);
        String wrongOldPassword = "random";
        Response response = AuthenticationUtils.updatePassword(AuthenticationUtils.getAdminLoginName(), wrongOldPassword,
                AuthenticationUtils.getAdminNewPassword(), authCookies, getContextRoot());
        assertNotNull(response);
        assertEquals(response.getStatusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @InSequence(3)
    public void updatePasswordLeadsTo200() {
        assertNotNull(authCookies);
        Response response = AuthenticationUtils.updatePassword(AuthenticationUtils.getAdminLoginName(),
                AuthenticationUtils.getAdminPassword(), AuthenticationUtils.getAdminNewPassword(), authCookies,
                getContextRoot());
        assertNotNull(response);
        assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
    }

    @Test
    @InSequence(4)
    public void incorrectLoginLeadsTo401() {
        String wrongPassword = "random";
        Response response = AuthenticationUtils.login(AuthenticationUtils.getAdminLoginName(), wrongPassword, getContextRoot());
        assertNotNull(response);
        assertEquals(response.getStatusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @InSequence(5)
    public void properLoginLeadsTo200() {
        Response response = AuthenticationUtils.login(AuthenticationUtils.getAdminLoginName(),
                AuthenticationUtils.getAdminNewPassword(), getContextRoot());
        assertNotNull(response);
        assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
    }
}
