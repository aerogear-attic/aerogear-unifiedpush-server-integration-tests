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
package org.jboss.aerogear.unifiedpush.api.performance;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Helper;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.AbstractSessionRequest;
import org.jboss.aerogear.unifiedpush.api.Installation;

import com.jayway.restassured.response.Response;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class BatchPushInstallationRequest extends AbstractSessionRequest<BatchPushInstallationRequest> {

    public static BatchPushInstallationRequest request() {
        return new BatchPushInstallationRequest();
    }

    /**
     * 
     * @param count count of installations we want to register
     * @param variantId installations against this variant
     * @return
     */
    public void register(int count, String variantId) {

        MassInstallation massiveInstallation = new MassInstallation();
        massiveInstallation.setVariantId(variantId);

        List<Installation> installations = new ArrayList<Installation>();

        for (int i = 0; i < count; i++) {
            Installation inst = new Installation();
            inst.setDeviceToken(Helper.randomStringOfLength(100));
            installations.add(inst);
        }

        massiveInstallation.setInstallations(installations);

        Response response = getSession().givenAuthorized()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(massiveInstallation)
            .post("/rest/mass/installations");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
    }

    public long countAllInstallations(String variantId) {

        Response response = getSession().givenAuthorized()
            .header(Headers.acceptJson())
            .get("/rest/mass/installations/{variantId}", variantId);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return response.getBody().as(Long.class);
    }
}
