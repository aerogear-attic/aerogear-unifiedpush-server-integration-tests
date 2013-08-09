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
package org.jboss.aerogear.connectivity.common

import groovy.json.JsonBuilder

import org.jboss.aerogear.connectivity.users.Developer

import com.jayway.restassured.RestAssured

class AuthenticationUtils {

    def static final String NEWPASSWORD = "aerogear123"

    def static final String ADMIN_LOGIN_NAME = "admin"

    def static final String ADMIN_PASSWORD = "123"

    def login(String loginNameStr, String passwordStr) {
        assert root !=null

        // login with default password
        def json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json {
                    loginName loginNameStr
                    password passwordStr
                })
                .post("${root}rest/auth/login")

        // we need to change the password
        if(response.getStatusCode()==403) {
            def cookies = response.getDetailedCookies()
            assert cookies !=null
            response = RestAssured.given()
                    .contentType("application/json")
                    .header("Accept", "application/json")
                    .cookies(cookies)
                    .body( json {
                        loginName loginNameStr
                        password passwordStr
                        newPassword NEWPASSWORD
                    })
                    .put("${root}rest/auth/update")

            assert response.getStatusCode() == 200
        }

        // try to login with new password
        response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .body( json {
                    loginName loginNameStr
                    password NEWPASSWORD
                })
                .expect()
                .statusCode(200)
                .when().post("${root}rest/auth/login")

        return response
    }

    def adminLogin() {
        return login(ADMIN_LOGIN_NAME, ADMIN_PASSWORD)
    }

    def createDeveloper(String loginName, String password) {
        def developer = new Developer()
        developer.setLoginName(loginName)
        developer.setPassword(password)
        return developer
    }
}
