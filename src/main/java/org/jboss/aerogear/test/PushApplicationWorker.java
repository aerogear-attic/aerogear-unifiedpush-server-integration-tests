package org.jboss.aerogear.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.model.PushApplication;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class PushApplicationWorker implements UPSWorker<PushApplication, PushApplicationWorker> {

    private PushApplication application;

    public PushApplicationWorker() {
        this.application = new PushApplication();
    }

    public PushApplicationWorker name(String name) {
        application.setName(name);
        return this;
    }

    public PushApplicationWorker description(String description) {
        application.setDescription(description);
        return this;
    }

    @Override
    public PushApplication raw() {
        return application;
    }

    @Override
    public PushApplicationWorker generate(Session session) {

        if (application.getName() == null) {
            name(UUID.randomUUID().toString());
        }
        if (application.getDescription() == null) {
            description(UUID.randomUUID().toString());
        }

        return this;
    }

    @Override
    public PushApplicationWorker build(Session session) {
        return this;
    }

    @Override
    public JSONObject marshall(Session session) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", application.getName());
        jsonObject.put("description", application.getDescription());
        return jsonObject;
    }

    @Override
    public PushApplicationWorker demarshall(Session session, JsonPath jsonPath) {
        application.setName(jsonPath.getString("name"));
        application.setDescription(jsonPath.getString("description"));
        application.setPushApplicationID(jsonPath.getString("pushApplicationID"));
        application.setMasterSecret(jsonPath.getString("masterSecret"));
        application.setDeveloper(jsonPath.getString("developer"));
        return this;
    }

    @Override
    public PushApplication register(Session session, Object... contexts) {
        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(marshall(session))
            .post("/rest/applications");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

        return demarshall(session, response.jsonPath()).raw();

    }

    @Override
    public List<PushApplication> findAll(Session session, Object... contexts) {

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<PushApplication> pushApplications = new ArrayList<PushApplication>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            // FIXME this might not be the right implementation
            // it can actually leak values of previous implementation
            PushApplication pushApplication = demarshall(session, jsonPath).raw();
            pushApplications.add(pushApplication);
        }

        return pushApplications;
    }

    @Override
    public PushApplication findBy(Session session, Object id, Object... contexts) {
        // TODO Auto-generated method stub
        return null;
    }

}