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
public class BatchPushVariantRequest extends AbstractSessionRequest<BatchPushVariantRequest> {

    public static BatchPushVariantRequest request() {
        return new BatchPushVariantRequest();
    }

    /**
     * Creates random number of variants for given application ID. When there is no such application on UPS side, it will be
     * created. If there is such ID, such number of variants will be added to that already created application.
     * 
     * @param numberOfVariants number of variants to create for given {@code applicationId}
     * @param applicationId ID of application for which variants will be created
     * @return this
     */
    public List<String> register(long numberOfVariants, String applicationId) {

        MassPushVariant massiveVariants = new MassPushVariant();
        massiveVariants.setNumberOfVariants(numberOfVariants);
        massiveVariants.setApplicationId(applicationId);

        Response response = getSession().givenAuthorized()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(massiveVariants)
            .post("/rest/mass/variants");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return response.as(MassResponse.class).getVarsIds();
    }

    /**
     * 
     * 
     * @param applicationId id of application for which we want to create some variant
     * @return variantId of created variant
     */
    public List<String> register(String applicationId) {
        return register(1, applicationId);
    }

    /**
     * 
     * @param applicationId
     * @return number of variants for given {@code applicationId}
     */
    public long countOfAllVariants(String applicationId) {

        Response response = getSession().givenAuthorized()
            .header(Headers.acceptJson())
            .get("/rest/mass/variants/{applicationId}", applicationId);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return response.getBody().as(Long.class);
    }
}
