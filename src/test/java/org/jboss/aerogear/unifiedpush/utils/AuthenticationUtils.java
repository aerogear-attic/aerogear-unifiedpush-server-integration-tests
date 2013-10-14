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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class AuthenticationUtils {

    private static final String NEWPASSWORD = "aerogear123";

    private static final String ADMIN_LOGIN_NAME = "admin";

    private static final String DEVELOPER_LOGIN_NAME = "developer";

    private static final String DEVELOPER_PASSWORD = "123";

    private static final String ADMIN_PASSWORD = "123";

    private static final String SECURE_ADMIN_NEW_PASSWORD = "aerogear";

    public static String getDeveloperLoginName() {
        return DEVELOPER_LOGIN_NAME;
    }

    public static String getDeveloperPassword() {
        return DEVELOPER_PASSWORD;
    }

    public static String getDeveloperNewPassword() {
        return NEWPASSWORD;
    }

    public static String getAdminLoginName() {
        return ADMIN_LOGIN_NAME;
    }

    public static String getAdminPassword() {
        return ADMIN_PASSWORD;
    }

    public static String getAdminNewPassword() {
        return NEWPASSWORD;
    }

    private AuthenticationUtils() {
    }

    @SuppressWarnings("unchecked")
    public static Response login(String loginNameStr, String passwordStr, String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginNameStr);
        jsonObject.put("password", passwordStr);

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .body(jsonObject.toJSONString()).post("{root}rest/auth/login", root);

        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response updatePassword(String loginNameStr, String oldPassword, String newPasswd, Map<String, ?> cookies,
            String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginNameStr);
        jsonObject.put("password", oldPassword);
        jsonObject.put("newPassword", newPasswd);

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString()).put("{root}rest/auth/update", root);

        return response;
    }

    public static Response loginWorkFlow(String loginNameStr, String passwordStr, String newPassword, String root) {

        assertNotNull(root);

        // login with default password
        Response response = login(loginNameStr, passwordStr, root);

        // we need to change the password
        if (response.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
            Map<String, String> cookies = response.getCookies();
            // update password
            response = updatePassword(loginNameStr, passwordStr, newPassword, cookies, root);
            // try to login with new password
            response = login(loginNameStr, newPassword, root);
        }

        assertTrue(response.getStatusCode() == Status.OK.getStatusCode());

        return response;
    }

    public static Response logout(Map<String, String> cookies, String root) {

        Response response = RestAssured.given().header("Accept", "application/json").cookies(cookies)
                .post("{root}rest/auth/logout", root);

        return response;
    }

    public static Response adminLogin(String root) {
        assertNotNull(root);
        return loginWorkFlow(ADMIN_LOGIN_NAME, ADMIN_PASSWORD, NEWPASSWORD, root);
    }

    public static Response secureLogin(String root) {
        assertNotNull(root);
        Response response = loginWorkFlow(ADMIN_LOGIN_NAME, ADMIN_PASSWORD, SECURE_ADMIN_NEW_PASSWORD, root);
        if (response.getStatusCode() == Status.UNAUTHORIZED.getStatusCode()) {
            response = login(ADMIN_LOGIN_NAME, SECURE_ADMIN_NEW_PASSWORD, root);
        }
        return response;
    }
}
