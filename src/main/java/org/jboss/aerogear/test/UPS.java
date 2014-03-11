package org.jboss.aerogear.test;

/**
 * Representation of UnifiedPush Server.
 *
 * Once logged to Unified Push Server, it is able to perform CRUD operation on entities defined in model
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class UPS {

    protected Session session;

    public UPS(Session session) {
        this.session = session;
    }

    /**
     * Creates an UPS session with dedicated worker
     *
     * @param worker
     * @return
     */
    public <ENTITY, WORKER extends UPSWorker<ENTITY, WORKER>> TypedUPS<ENTITY, WORKER> with(WORKER worker) {
        return new TypedUPS<ENTITY, WORKER>(session, worker);
    }

    /**
     * Creates an UPS session with dedicated worker. Instantiates worker if no data is needed
     *
     * @param worker
     * @return
     */
    public <ENTITY, WORKER extends UPSWorker<ENTITY, WORKER>> TypedUPS<ENTITY, WORKER> with(Class<WORKER> workerType)
        throws Exception {
        // FIXME this could be done in a nicer way
        return new TypedUPS<ENTITY, WORKER>(session, (WORKER) workerType.getConstructors()[0].newInstance());
    }

    @Override
    public String toString() {
        return "UPS (" + session.getBaseUrl() + ")";
    }

}
