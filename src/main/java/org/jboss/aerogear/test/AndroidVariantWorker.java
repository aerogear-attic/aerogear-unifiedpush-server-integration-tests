package org.jboss.aerogear.test;

import java.util.List;

import org.jboss.aerogear.test.model.AndroidVariant;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;

class AndroidVariantWorker extends AbstractVariantWorker<AndroidVariant> implements
    UPSWorker<AndroidVariant, AndroidVariantWorker> {

    public AndroidVariantWorker() {
        this.entity = new AndroidVariant();
    }

    // builder methods for AndroidVariant
    public AndroidVariantWorker googleKey(String googleKey) {
        entity.setGoogleKey(googleKey);
        return this;
    }

    public AndroidVariantWorker projectNumber(String projectNumber) {
        entity.setGoogleKey(projectNumber);
        return this;
    }

    // custom methods for AndroidVariant

    @Override
    public AndroidVariantWorker generate(Session session) {
        this.entity = null;
        return this;
    }

    @Override
    public AndroidVariantWorker build(Session session) {
        this.entity = null;
        return this;
    }

    @Override
    public AndroidVariant register(Session session, Object... contexts) {

        // session.given();
        return raw();
    }

    @Override
    public JSONObject marshall(Session session) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AndroidVariantWorker demarshall(Session session, JsonPath json) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AndroidVariant> findAll(Session session, Object... contexts) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AndroidVariant findBy(Session session, Object id, Object... contexts) {
        // TODO Auto-generated method stub
        return null;
    }
}