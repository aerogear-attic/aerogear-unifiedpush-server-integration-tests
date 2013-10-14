package org.jboss.aerogear.unifiedpush.utils;

import java.util.Map;

import org.json.simple.JSONObject;
import org.picketlink.idm.model.basic.User;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class UserEndpointUtils {

    private UserEndpointUtils() {
    }

    public static Response listAll(Map<String, ?> cookies, String root) {
        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).get("{root}rest/users", root);
        return response;
    }

    public static Response findById(Map<String, ?> cookies, String id, String root) {
        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).get("{root}rest/users/{id}", root, id);
        return response;
    }

    public static Response delete(Map<String, ?> cookies, String id, String root) {
        Response response = RestAssured.given().cookies(cookies).delete("{root}rest/users/{id}", root, id);
        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response update(Map<String, ?> cookies, User user, String root) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("firstName", user.getFirstName());
        jsonObject.put("lastName", user.getLastName());
        jsonObject.put("email", user.getEmail());
        jsonObject.put("id", user.getId());
        jsonObject.put("loginName", user.getLoginName());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString()).put("{root}rest/users", root);
        return response;
    }

    public static User createUser(String firstName, String lastName, String email, String loginName, String id) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setLoginName(loginName);
        user.setId(id);
        return user;
    }
}
