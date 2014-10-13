/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.test.api.sender;

import java.util.List;

import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.api.AbstractSessionRequest;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/** 
 *  Request for retreiving Push Message Information
 */
public class PushMessageInformationRequest extends AbstractSessionRequest<PushMessageInformationRequest> {

    public List<PushMessageInformation> get(String applicationId) {
        
        Response response = getSession().givenAuthorized()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .get("http://localhost:8080/ag-push/rest/metrics/messages/application/" + applicationId);
        
        String json = response.asString();
        return JsonPath.from(json).get("");
    }
    
    public static PushMessageInformationRequest request() {
        return new PushMessageInformationRequest();
    }
    
}
