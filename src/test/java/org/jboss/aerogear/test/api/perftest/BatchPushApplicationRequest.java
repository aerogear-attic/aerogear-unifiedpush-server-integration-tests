/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.test.api.perftest;

import java.util.List;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.AbstractSessionRequest;

import com.jayway.restassured.response.Response;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class BatchPushApplicationRequest extends AbstractSessionRequest<BatchPushApplicationRequest> {

    public static BatchPushApplicationRequest request() {
        return new BatchPushApplicationRequest();
    }

    /**
     * Registers arbitrary number of applications.
     * 
     * @param count number of applications to register.
     * @return this
     */
    public List<String> register(long count) {

        MassPushApplication massiveApps = new MassPushApplication();
        massiveApps.setCount(count);

        Response response = getSession().givenAuthorized()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(massiveApps)
            .post("/rest/mass/applications");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return response.as(MassResponse.class).getAppsIds();
    }

    /**
     * Registers just one push application.
     * 
     * @return id of just created push application
     */
    public List<String> register() {
        return register(1);
    }

    public BatchPushApplicationRequest deleteAll() {
        Response response = getSession().givenAuthorized()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .delete("/rest/mass/applications");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

        return this;
    }

    public Long countOfAllApplications() {
        Response response = getSession().givenAuthorized()
            .header(Headers.acceptJson())
            .get("/rest/mass/applications");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return response.getBody().as(Long.class);
    }

}
