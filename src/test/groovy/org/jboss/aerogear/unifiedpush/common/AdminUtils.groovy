package org.jboss.aerogear.unifiedpush.common

import com.jayway.restassured.RestAssured
import groovy.json.JsonBuilder
import org.jboss.aerogear.unifiedpush.users.Developer

class AdminUtils {

    def createDeveloper(String loginName, String password) {
        def developer = new Developer();

        developer.setLoginName(loginName);
        developer.setPassword(password);

        return developer;
    }

    def enrollDeveloper(Developer developer, Map<String, ?> cookies) {
        assert root != null

        def json = new JsonBuilder()
        def response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .body( json {
                    loginName developer.getLoginName()
                    password developer.getPassword()
                })
                .post("${root}rest/auth/enroll")
        return response
    }

}
