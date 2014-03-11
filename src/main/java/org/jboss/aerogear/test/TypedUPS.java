package org.jboss.aerogear.test;

import java.util.ArrayList;
import java.util.List;

public class TypedUPS<ENTITY, WORKER extends UPSWorker<ENTITY, WORKER>> extends UPS {

    private List<WORKER> workers;

    public TypedUPS(Session session, WORKER worker) {
        super(session);
        this.workers = new ArrayList<WORKER>();
        this.workers.add(worker);
    }

    public ENTITY asEntity() {
        // FIXME check for exactly one
        return workers.iterator().next().raw();
    }

    public List<ENTITY> asEntities() {
        List<ENTITY> list = new ArrayList<ENTITY>();
        for (WORKER w : workers) {
            list.add(w.raw());
        }
        return list;
    }

    public TypedUPS<ENTITY, WORKER> create() {
        for (WORKER w : workers) {
            w.build(session);
        }
        return this;
    }

    public TypedUPS<ENTITY, WORKER> generate() {
        for (WORKER w : workers) {
            w.generate(session);
        }
        return this;
    }

    public TypedUPS<ENTITY, WORKER> generate(int count) {

        // FIXME check for exactly one or exact count
        for (int i = 0; i < count; i++) {
            workers.get(0).generate(session);
        }
        return this;
    }

    public ENTITY register(Object... contexts) {
        // FIXME check for exactly one or exact count
        WORKER w = workers.iterator().next();
        return w.register(session, contexts);
    }

    public List<ENTITY> registerAll(Object... contexts) {
        List<ENTITY> list = new ArrayList<ENTITY>();
        for (WORKER w : workers) {
            list.add(w.register(session, contexts));
        }

        return list;
    }

    public ENTITY findBy(Object id, Object... contexts) {
        // FIXME check for exactly one
        return workers.iterator().next().findBy(session, id, contexts);
    }

    public List<ENTITY> findAll(Object... contexts) {
        // FIXME check for exactly one
        return workers.iterator().next().findAll(session, contexts);

    }
}