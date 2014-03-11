package org.jboss.aerogear.test;

import java.util.List;

import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;

interface UPSWorker<ENTITY, WORKER extends UPSWorker<ENTITY, WORKER>> {

    ENTITY raw();

    WORKER generate(Session session);

    WORKER build(Session session);

    JSONObject marshall(Session session);

    WORKER demarshall(Session session, JsonPath json);

    ENTITY register(Session session, Object... contexts);

    List<ENTITY> findAll(Session session, Object... contexts);

    ENTITY findBy(Session session, Object id, Object... contexts);

}