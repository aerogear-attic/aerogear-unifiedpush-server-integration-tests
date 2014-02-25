package org.jboss.aerogear.unifiedpush.utils;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.Developer;
import org.json.simple.JSONObject;

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

    public static Developer createAndEnroll(String loginName, String password, Session session) {
        Developer developer = create(loginName, password);

        enroll(developer, session);

        return developer;
    }

    public static Developer generate() {
        String loginName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        return create(loginName, password);
    }

    public static Developer generateAndEnroll(Session session) {
        Developer developer = generate();

        enroll(developer, session);

        return developer;
    }

    public static Response enroll(Developer developer, Session session) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", developer.getLoginName());
        jsonObject.put("password", developer.getPassword());

        Response response = session.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(jsonObject.toJSONString())
                .post("/rest/auth/enroll");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return response;
    }
}
