package org.jboss.aerogear.test;

public abstract class AbstractVariantWorker<ENTITY> {

    protected ENTITY entity;

    public ENTITY raw() {
        return entity;
    }
}