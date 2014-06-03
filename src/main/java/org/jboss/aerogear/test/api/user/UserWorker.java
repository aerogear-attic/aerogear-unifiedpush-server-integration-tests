/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.aerogear.test.api.user;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.AbstractUPSWorker;
import org.jboss.aerogear.test.model.Developer;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// FIXME remove because of keycloak?
public class UserWorker extends AbstractUPSWorker<Developer, String, UserBlueprint, UserEditor, Void,
        UserContext, UserWorker> {


    @Override
    public UserContext createContext(Session session, Void parent) {
        return new UserContext(this, parent, session);
    }

    @Override
    public JSONObject marshall(Developer developer) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", developer.getId());
        jsonObject.put("loginName", developer.getLoginName());
        return jsonObject;
    }

    @Override
    public UserEditor demarshall(UserContext context, JsonPath json) {
        UserEditor editor = new UserEditor(context);
        editor.setId(json.getString("id"));
        editor.setLoginName(json.getString("loginName"));
        return editor;
    }

    @Override
    public List<UserEditor> create(UserContext context, Collection<? extends UserBlueprint> blueprints) {
        List<UserEditor> editors = new ArrayList<UserEditor>();
        for (UserBlueprint blueprint : blueprints) {
            JSONObject marshalledBlueprint = marshall(blueprint);

            // we need to add password here, because it has to be only present in enrollment
            marshalledBlueprint.put("password", blueprint.getPassword());

            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshalledBlueprint)
                    .post("/rest/auth/enroll");

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

            editors.add(demarshall(context, response.jsonPath()));
        }

        return editors;
    }

    @Override
    public List<UserEditor> readAll(UserContext context) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/users");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<UserEditor> editors = new ArrayList<UserEditor>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            UserEditor editor = demarshall(context, jsonPath);
            editors.add(editor);
        }

        return editors;
    }

    @Override
    public UserEditor read(UserContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/users/{id}", id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return demarshall(context, response.jsonPath());
    }

    @Override
    public void update(UserContext context, Collection<? extends Developer> developers) {
        for (Developer developer : developers) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(developer))
                    .put("/rest/users");

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
        }
    }

    @Override
    public void deleteById(UserContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .delete("/rest/users/{id}", id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static UserWorker worker() {
        return new UserWorker();
    }
}
