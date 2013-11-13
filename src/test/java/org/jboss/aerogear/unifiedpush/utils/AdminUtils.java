package org.jboss.aerogear.unifiedpush.utils;

import static org.junit.Assert.assertNotNull;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.Map;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.users.Developer;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class AdminUtils {

    private AdminUtils() {
    }

    public static Developer create(String loginName, String password) {
        Developer developer = new Developer();

        developer.setLoginName(loginName);
        developer.setPassword(password);

        return developer;
    }

    public static Developer createAndEnroll(String loginName, String password, AuthenticationUtils.Session session) {
        Developer developer = create(loginName, password);

        enroll(developer, session);

        return developer;
    }

    public static Developer generate() {
        String loginName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        return create(loginName, password);
    }

    public static Developer generateAndEnroll(AuthenticationUtils.Session session) {
        Developer developer = generate();

        enroll(developer, session);

        return developer;
    }

    public static Response enroll(Developer developer, AuthenticationUtils.Session session) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", developer.getLoginName());
        jsonObject.put("password", developer.getPassword());

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(jsonObject.toJSONString())
                .post("{root}rest/auth/enroll", session.getRoot());

        UnexpectedResponseException.verifyResponse(response, OK);

        return response;
    }
}
