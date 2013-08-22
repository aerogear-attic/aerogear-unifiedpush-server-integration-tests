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
package org.jboss.aerogear.unifiedpush.common

import java.util.Map

import javax.ws.rs.core.Response.Status

import groovy.json.JsonBuilder

import org.jboss.aerogear.unifiedpush.users.Developer

import com.jayway.restassured.RestAssured

class AuthenticationUtils {

    def static final NEWPASSWORD = "aerogear123"

    def static final ADMIN_LOGIN_NAME = "admin"

    def static final ADMIN_PASSWORD = "123"
    
    def static final SECURE_ADMIN_NEW_PASSWORD = "aerogear"

    def login(String loginNameStr, String passwordStr) {
        assert root !=null

        def json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json {
                    loginName loginNameStr
                    password passwordStr
                })
                .post("${root}rest/auth/login")

        return response
    }

    def updatePassword(String loginNameStr, String oldPassword, String newPasswd, Map<String, ?> cookies) {
        assert root !=null

        def json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .body( json {
                    loginName loginNameStr
                    password oldPassword
                    newPassword newPasswd
                })
                .put("${root}rest/auth/update")

        return response
    }

    def loginWorkFlow(String loginNameStr, String passwordStr, String newPassword) {
        assert root !=null

        // login with default password
        def response = login(loginNameStr, passwordStr)

        // we need to change the password
        if(response.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
            def cookies = response.getCookies()
            assert cookies !=null
            response = updatePassword(loginNameStr, passwordStr, newPassword, cookies)

            assert response.getStatusCode() == 200


            // try to login with new password
            response = login(loginNameStr, newPassword)
        }

        return response
    }

    def adminLogin() {
        return loginWorkFlow(ADMIN_LOGIN_NAME, ADMIN_PASSWORD, NEWPASSWORD)
    }

    def secureLogin() {
        def response = loginWorkFlow(ADMIN_LOGIN_NAME, ADMIN_PASSWORD, SECURE_ADMIN_NEW_PASSWORD)

        if (response.getStatusCode() == Status.UNAUTHORIZED.statusCode) {
            response = login(ADMIN_LOGIN_NAME, SECURE_ADMIN_NEW_PASSWORD)
        }
        return response
    }

    def createDeveloper(String loginName, String password) {
        def developer = new Developer()
        developer.setLoginName(loginName)
        developer.setPassword(password)
        return developer
    }
}
