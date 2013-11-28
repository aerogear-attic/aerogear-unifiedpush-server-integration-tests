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
package org.jboss.aerogear.unifiedpush.utils;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;


public final class AuthenticationUtils {

    private static final String ADMIN_LOGIN_NAME = "admin";
    private static final String ADMIN_OLD_PASSWORD = "123";
    private static final String ADMIN_NEW_PASSWORD = "opensource2013";

    private static final String DEVELOPER_LOGIN_NAME = "developer";
    private static final String DEVELOPER_OLD_PASSWORD = "123";
    private static final String DEVELOPER_NEW_PASSWORD = "developer2013";

    private AuthenticationUtils() {
    }

    public static Session login(String loginName, String password, String root) throws NullPointerException,
            UnexpectedResponseException {
        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginName);
        jsonObject.put("password", password);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .body(jsonObject.toJSONString())
                .post("{root}rest/auth/login", root);

        // TODO should we throw or return invalid session?
        if (response.statusCode() == OK.getStatusCode()) {
            return new Session(root, loginName, response.cookies());
        } else if (response.statusCode() == FORBIDDEN.getStatusCode()) {
            throw new ExpiredPasswordException(response);
        } else if (response.statusCode() == UNAUTHORIZED.getStatusCode()) {
            throw new InvalidPasswordException(response);
        } else {
            // This should never happen
            throw new UnexpectedResponseException(response);
        }
    }

    public static boolean changePassword(String loginName, String oldPassword, String newPassword, String root) {
        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginName);
        jsonObject.put("password", oldPassword);
        jsonObject.put("newPassword", newPassword);

        Response response = RestAssured
                .given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .body(jsonObject.toJSONString())
                .put("{root}rest/auth/update", root);

        if (response.statusCode() == OK.getStatusCode()) {
            return true;
        } else if (response.statusCode() == UNAUTHORIZED.getStatusCode()) {
            throw new InvalidPasswordException(response);
        } else {
            throw new UnexpectedResponseException(response);
        }
    }

    public static void logout(Session session) {
        assertNotNull(session);
        assertTrue("Session has to be valid!", session.isValid());

        Response response = RestAssured
                .given()
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .post("{root}rest/auth/logout", session.getRoot());

        if (response.statusCode() == OK.getStatusCode()) {
            session.invalidate();
        } else if (response.statusCode() == UNAUTHORIZED.getStatusCode()) {
            throw new IllegalStateException("Session was marked as valid, but the logout was unsuccessful!");
        } else {
            throw new UnexpectedResponseException(response);
        }

        session.invalidate();
    }

    public static Session completeLogin(String loginName, String oldPassword, String newPassword, String root) {
        try {
            return login(loginName, oldPassword, root);
        } catch (ExpiredPasswordException e) {
            changePassword(loginName, oldPassword, newPassword, root);

            return login(loginName, newPassword, root);
        } catch (InvalidPasswordException e) {
            return login(loginName, newPassword, root);
        }
    }

    public static Session completeDefaultLogin(String root) {
        return completeLogin(ADMIN_LOGIN_NAME, ADMIN_OLD_PASSWORD, ADMIN_NEW_PASSWORD, root);
    }

    public static String getAdminLoginName() {
        return ADMIN_LOGIN_NAME;
    }

    public static String getAdminOldPassword() {
        return ADMIN_OLD_PASSWORD;
    }

    public static String getAdminNewPassword() {
        return ADMIN_NEW_PASSWORD;
    }

    public static String getDeveloperLoginName() {
        return DEVELOPER_LOGIN_NAME;
    }

    public static String getDeveloperOldPassword() {
        return DEVELOPER_OLD_PASSWORD;
    }

    public static String getDeveloperNewPassword() {
        return DEVELOPER_NEW_PASSWORD;
    }

    public static class Session {
        private String root;
        private String loginName;
        private Map<String, ?> cookies;
        private boolean invalid;

        public Session(String root, String loginName, Map<String, ?> cookies) {
            this.root = root;
            this.loginName = loginName;
            this.cookies = Collections.unmodifiableMap(cookies); // TODO should the be modifiable or not?
            this.invalid = false;
        }

        public String getRoot() {
            return root;
        }

        public Map<String, ?> getCookies() {
            return cookies;
        }

        public String getLoginName() {
            return loginName;
        }

        public boolean isValid() {
            return !invalid;
        }

        // TODO is the invalidation needed?
        protected void invalidate() {
            root = null;
            cookies = null;
            invalid = true;
        }

        public static Session createInvalid(String root) {
            Session session = forceCreateValidWithEmptyCookies(root);

            session.invalid = true;

            return session;
        }

        public static Session forceCreateValidWithEmptyCookies(String root) {
            return new Session(root, "", new HashMap<String, Object>());
        }
    }

    public static class ExpiredPasswordException extends RuntimeException {

        private Response response;

        public ExpiredPasswordException(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }
    }

    public static class InvalidPasswordException extends RuntimeException {

        private Response response;

        public InvalidPasswordException(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }

    }
}
