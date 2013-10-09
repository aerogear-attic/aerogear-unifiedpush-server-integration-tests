package org.jboss.aerogear.unifiedpush.utils;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.jboss.aerogear.unifiedpush.users.Developer;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class AdminUtils {

    private AdminUtils() {
    }

    public static Developer createDeveloper(String loginName, String password) {
        Developer developer = new Developer();

        developer.setLoginName(loginName);
        developer.setPassword(password);

        return developer;
    }

    @SuppressWarnings("unchecked")
    public static Response enrollDeveloper(Developer developer, Map<String, String> cookies, String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", developer.getLoginName());
        jsonObject.put("password", developer.getPassword());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString()).post(root + "rest/auth/enroll");
        return response;
    }
}
