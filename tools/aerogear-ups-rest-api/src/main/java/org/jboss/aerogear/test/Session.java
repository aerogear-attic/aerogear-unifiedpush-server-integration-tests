package org.jboss.aerogear.test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.representations.AccessTokenResponse;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.specification.RequestSpecification;

public class Session {

    private static final int HTTPS_PORT = 443;
    private static final int HTTP_PORT = 80;

    private URL baseUrl;
    private String baseUri;
    private int port;
    private String basePath;

    private AccessTokenResponse accessTokenResponse;

    private Map<String, ?> cookies;
    private boolean invalid;

    public Session(URL baseUrl, AccessTokenResponse accessTokenResponse) {
        this.baseUrl = baseUrl;
        this.baseUri = baseUrl.getProtocol() + "://" + baseUrl.getHost();
        this.port = baseUrl.getPort() == -1 ? ("https".equals(baseUrl.getProtocol()) ? HTTPS_PORT : HTTP_PORT) : baseUrl.getPort();
        this.basePath = baseUrl.getPath();

        this.accessTokenResponse = accessTokenResponse;

        this.cookies = new HashMap<String, Object>();
        this.invalid = false;

    }

    public Session(String baseUrl, AccessTokenResponse accessTokenResponse) {
        this(Utilities.Urls.from(baseUrl), accessTokenResponse);
    }

    public RequestSpecificationHolder given() {

        RestAssured.baseURI = baseUri;
        RestAssured.port = port;
        RestAssured.basePath = basePath;

        RequestSpecification specification = RestAssured
                .given()
                .redirects().follow(false)
                .cookies(cookies);

        return new RequestSpecificationHolder(specification, accessTokenResponse);
    }

    public Map<String, ?> getCookies() {
        return cookies;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public boolean isValid() {
        return !invalid;
    }

    public static Session newSession(String url) {
        return new Session(url, new AccessTokenResponse());
    }

    public Session invalidate() {
        this.invalid = true;
        this.cookies = new HashMap<String, Object>();
        this.baseUrl = null;
        this.accessTokenResponse = new AccessTokenResponse();
        return this;
    }

}