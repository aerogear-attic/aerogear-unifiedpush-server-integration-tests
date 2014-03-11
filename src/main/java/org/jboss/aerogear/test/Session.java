package org.jboss.aerogear.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class Session {

    private URL baseUrl;
    private String baseUri;
    private int port;
    private String basePath;

    private String loginName;
    private String password;
    private Map<String, ?> cookies;
    private boolean invalid;

    public Session(URL baseUrl, String loginName, String password, Map<String, ?> cookies) {

        this.baseUrl = baseUrl;
        this.baseUri = baseUrl.getProtocol() + "://" + baseUrl.getHost();
        this.port = baseUrl.getPort() == -1 ? ("https".equals(baseUrl.getProtocol()) ? 443 : 80) : baseUrl.getPort();
        this.basePath = baseUrl.getPath();

        this.loginName = loginName;
        this.password = password;
        this.cookies = Collections.unmodifiableMap(cookies); // TODO should the be modifiable or not?
        this.invalid = false;

    }

    public Session(String baseUrl, String loginName, String password, Map<String, ?> cookies) {
        this(UrlUtils.from(baseUrl), loginName, password, cookies);
    }

    public Session login(String loginName, String password) throws NullPointerException,
        UnexpectedResponseException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginName);
        jsonObject.put("password", password);

        Response response = Session.newSession(baseUrl.toExternalForm()).given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(jsonObject.toJSONString())
            .post("/rest/auth/login");

        // TODO should we throw or return invalid session?
        if (response.statusCode() == HttpStatus.SC_OK) {
            return new Session(baseUrl, loginName, password, response.cookies());
        } else if (response.statusCode() == HttpStatus.SC_FORBIDDEN) {
            throw new ExpiredPasswordException(response);
        } else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new InvalidPasswordException(response);
        } else {
            // This should never happen
            throw new UnexpectedResponseException(response);
        }
    }

    public boolean changePassword(String oldPassword, String newPassword) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginName);
        jsonObject.put("password", oldPassword);
        jsonObject.put("newPassword", newPassword);

        // FIXME should not this be using already existing session?
        Response response = Session.newSession(baseUrl.toExternalForm())
            .given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(jsonObject.toJSONString())
            .put("/rest/auth/update");

        if (response.statusCode() == HttpStatus.SC_OK) {
            return true;
        } else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new InvalidPasswordException(response);
        } else {
            throw new UnexpectedResponseException(response);
        }
    }

    public void logout(Session session) {
        Validate.notNull(session);
        if (session.isValid() == false) {
            throw new IllegalStateException("Session has to be valid!");
        }

        Response response = session.given()
            .header(Headers.acceptJson())
            .post("/rest/auth/logout");

        if (response.statusCode() == HttpStatus.SC_OK) {
            session.invalidate();
        } else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new IllegalStateException("Session was marked as valid, but the logout was unsuccessful!");
        } else {
            throw new UnexpectedResponseException(response);
        }

        session.invalidate();
    }

    public Session completeLogin(String oldPassword) {
        try {
            return login(loginName, oldPassword);
        } catch (ExpiredPasswordException e) {
            changePassword(oldPassword, password);
            return login(loginName, password);
        } catch (InvalidPasswordException e) {
            return login(loginName, password);
        }
    }

    public RequestSpecification given() {

        RestAssured.baseURI = baseUri;
        RestAssured.port = port;
        RestAssured.basePath = basePath;

        return RestAssured.given().cookies(cookies);
    }

    public Map<String, ?> getCookies() {
        return cookies;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isValid() {
        return !invalid;
    }

    public static Session newSession(String baseUrl) {
        return new Session(baseUrl, null, null, new HashMap<String, Object>());
    }

    public Session invalidate() {
        this.invalid = true;
        this.cookies = new HashMap<String, Object>();
        this.baseUrl = null;
        this.loginName = "";
        this.password = "";
        return this;
    }

    public static Session createInvalid(String root) {
        Session session = forceCreateValidWithEmptyCookies(root);
        session.invalid = true;
        return session;
    }

    public static Session forceCreateValidWithEmptyCookies(String root) {
        return new Session(root, "", "", new HashMap<String, Object>());
    }

    private static final class UrlUtils {
        static final URL from(String url) throws IllegalArgumentException {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Unable to convert " + url + "to URL object");
            }
        }
    }

    public static class ExpiredPasswordException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private Response response;

        public ExpiredPasswordException(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }
    }

    public static class InvalidPasswordException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private Response response;

        public InvalidPasswordException(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }

    }
}