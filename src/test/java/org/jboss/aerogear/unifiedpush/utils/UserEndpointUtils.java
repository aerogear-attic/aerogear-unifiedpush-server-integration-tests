package org.jboss.aerogear.unifiedpush.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.Developer;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class UserEndpointUtils {

    private static final int SINGLE = 1;

    private UserEndpointUtils() {
    }

    public static Developer create(String loginName) {
        Developer user = new Developer();

        user.setLoginName(loginName);

        return user;
    }

    public static Developer createAndRegister(String loginName, Session session) {
        Developer user = create(loginName);

        register(user, session);

        return user;
    }

    public static Developer generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<Developer> generate(int count) {
        List<Developer> users = new ArrayList<Developer>();

        for(int i = 0; i < count; i++) {
            String loginName = UUID.randomUUID().toString();

            Developer user = create(loginName);

            users.add(user);
        }

        return users;
    }

    public static Developer generateAndRegister(Session session) {
        return generateAndRegister(SINGLE, session).iterator().next();
    }

    public static List<Developer> generateAndRegister(int count, Session session) {
        List<Developer> users = generate(count);

        for(Developer user : users) {
            register(user, session);
        }

        return users;
    }

    public static Response register(Developer user, Session session) {
        return register(user, session, ContentTypes.json());
    }

    public static Response register(Developer user, Session session, String contentType) {
        Response response = session.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(user))
                .post("/rest/users");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

        setFromJsonPath(response.jsonPath(), user);

        return response;
    }

    public static List<Developer> listAll(Session session) {

        Response response = session.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("/rest/users");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<Developer> users = new ArrayList<Developer>();

        List<Map<String, ?>> items = response.jsonPath().getList("");

        for(Map<String, ?> item : items) {
            Developer user = new Developer();

            user.setId((String) item.get("id"));
            user.setLoginName((String) item.get("loginName"));

            users.add(user);
        }

        return users;
    }

    public static Developer findById(String id, Session session) {
        Response response = session.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("/rest/users/{id}", id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return fromJsonPath(response.jsonPath());
    }

    public static Response update(Developer user, Session session) {
        return update(user, session, ContentTypes.json());
    }

    public static Response update(Developer user, Session session, String contentType) {

        Response response = session.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(user))
                .put("/rest/users");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

        return response;
    }

    public static Response delete(Developer user, Session session) {
        Response response = session.given()
                .cookies(session.getCookies())
                .delete("/rest/users/{id}", user.getId());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

        return response;
    }

    public static JSONObject toJSONObject(Developer user) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", user.getId());
        jsonObject.put("loginName", user.getLoginName());
        return jsonObject;
    }

    public static String toJSONString(Developer user) {
        return toJSONObject(user).toJSONString();
    }

    public static Developer fromJsonPath(JsonPath jsonPath) {
        Developer user = new Developer();

        setFromJsonPath(jsonPath, user);

        return user;
    }

    public static void setFromJsonPath(JsonPath jsonPath, Developer user) {
        user.setId(jsonPath.getString("id"));
        user.setLoginName(jsonPath.getString("loginName"));
    }

    public static void checkEquality(Developer expected, Developer actual) {
        assertEquals("ID doesn't match!", expected.getId(), actual.getId());
        assertEquals("LoginName doesn't match!", expected.getLoginName(), actual.getLoginName());
    }

    public static boolean loginNameExistsInList(String loginName, List<Developer> users) {
        if(loginName != null && !loginName.equals("") && users != null) {
            for(Developer user : users) {
                if(loginName.equals(user.getLoginName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
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
    public static Response update(Map<String, ?> cookies, Developer user, String root) {

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

    public static Developer createDeveloper(String firstName, String lastName, String email, String loginName, String id) {
        Developer user = new Developer();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setLoginName(loginName);
        user.setId(id);
        return user;
    }*/
}
