package org.jboss.aerogear.test;

import com.jayway.restassured.response.Header;
import com.jayway.restassured.specification.RequestSpecification;
import org.keycloak.representations.AccessTokenResponse;


public class RequestSpecificationHolder {

    private final RequestSpecification requestSpecification;

    private final AccessTokenResponse accessTokenResponse;

    public RequestSpecificationHolder(RequestSpecification requestSpecification, AccessTokenResponse accessTokenResponse) {
        this.requestSpecification = requestSpecification;
        this.accessTokenResponse = accessTokenResponse;
    }

    public RequestSpecification get() {
        return requestSpecification;
    }

    public RequestSpecification authorized() {
        String accessToken = null;

        if(accessTokenResponse.getToken() == null) {
            accessToken = "";
        } else {
            accessToken = accessTokenResponse.getToken();
        }
        return requestSpecification.header(new Header("Authorization", "Bearer" + accessToken));
    }

}
