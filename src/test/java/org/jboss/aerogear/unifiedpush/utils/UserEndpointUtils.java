package org.jboss.aerogear.unifiedpush.utils;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jayway.restassured.path.json.JsonPath;
import org.json.simple.JSONObject;
import org.picketlink.idm.model.basic.User;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class UserEndpointUtils {

    private static final int SINGLE = 1;

    private UserEndpointUtils() {
    }

    public static User create(String loginName) {
        User user = new User();

        user.setLoginName(loginName);

        return user;
    }

    public static User createAndRegister(String loginName, AuthenticationUtils.Session session) {
        User user = create(loginName);

        register(user, session);

        return user;
    }

    public static User generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<User> generate(int count) {
        List<User> users = new ArrayList<User>();

        for(int i = 0; i < count; i++) {
            String loginName = UUID.randomUUID().toString();

            User user = create(loginName);

            users.add(user);
        }

        return users;
    }

    public static User generateAndRegister(AuthenticationUtils.Session session) {
        return generateAndRegister(SINGLE, session).iterator().next();
    }

    public static List<User> generateAndRegister(int count, AuthenticationUtils.Session session) {
        List<User> users = generate(count);

        for(User user : users) {
            register(user, session);
        }

        return users;
    }

    public static Response register(User user, AuthenticationUtils.Session session) {
        return register(user, session, ContentTypes.json());
    }

    public static Response register(User user, AuthenticationUtils.Session session, String contentType) {
        Response response = RestAssured.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(user))
                .post("{root}rest/users", session.getRoot());

        UnexpectedResponseException.verifyResponse(response, CREATED);

        setFromJsonPath(response.jsonPath(), user);

        return response;
    }

    public static List<User> listAll(AuthenticationUtils.Session session) {

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/users", session.getRoot());

        UnexpectedResponseException.verifyResponse(response, OK);

        List<User> users = new ArrayList<User>();

        List<Map<String, ?>> items = response.jsonPath().getList("");

        for(Map<String, ?> item : items) {
            User user = new User();

            user.setId((String) item.get("id"));
            user.setLoginName((String) item.get("loginName"));

            users.add(user);
        }

        return users;
    }

    public static User findById(String id, AuthenticationUtils.Session session) {
        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/users/{id}", session.getRoot(), id);

        UnexpectedResponseException.verifyResponse(response, OK);

        return fromJsonPath(response.jsonPath());
    }

    public static Response update(User user, AuthenticationUtils.Session session) {
        return update(user, session, ContentTypes.json());
    }

    public static Response update(User user, AuthenticationUtils.Session session, String contentType) {

        Response response = RestAssured.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(user))
                .put("{root}rest/users", session.getRoot());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);

        return response;
    }

    public static Response delete(User user, AuthenticationUtils.Session session) {
        Response response = RestAssured.given()
                .cookies(session.getCookies())
                .delete("{root}rest/users/{id}", session.getRoot(), user.getId());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);

        return response;
    }

    public static JSONObject toJSONObject(User user) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", user.getId());
        jsonObject.put("loginName", user.getLoginName());
        return jsonObject;
    }

    public static String toJSONString(User user) {
        return toJSONObject(user).toJSONString();
    }

    public static User fromJsonPath(JsonPath jsonPath) {
        User user = new User();

        setFromJsonPath(jsonPath, user);

        return user;
    }

    public static void setFromJsonPath(JsonPath jsonPath, User user) {
        user.setId(jsonPath.getString("id"));
        user.setLoginName(jsonPath.getString("loginName"));
    }

    public static void checkEquality(User expected, User actual) {
        assertEquals("ID doesn't match!", expected.getId(), actual.getId());
        assertEquals("LoginName doesn't match!", expected.getLoginName(), actual.getLoginName());
    }

    public static boolean loginNameExistsInList(String loginName, List<User> users) {
        if(loginName != null && !loginName.equals("") && users != null) {
            for(User user : users) {
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
    }*/
}
