/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.jpa.PersistentObject;
import org.jboss.aerogear.unifiedpush.jpa.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PushDaoTest {

    @Deployment
    public static Archive<?> testArchive() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClasses(PushApplicationDao.class, PushApplicationDaoImpl.class)
                .addPackage(PushApplication.class.getPackage()).addPackage(PersistentObject.class.getPackage())
                .addPackage(Variant.class.getPackage()).addAsManifestResource("META-INF/beans.xml", "beans.xml")
                .addAsManifestResource("META-INF/persistence-pushee-only.xml", "persistence.xml");

        JavaArchive[] it = Maven.resolver().resolve("org.ow2.asm:asm:4.1").withoutTransitivity().as(JavaArchive.class);
        for (JavaArchive jarch : it) {
            jar = jar.merge(jarch);
        }
        return jar;
    }

    @Produces
    @PersistenceContext(unitName = "unifiedpush-default", type = PersistenceContextType.EXTENDED)
    @Default
    private EntityManager entityManager;

    @Inject
    private PushApplicationDao pushAppDao;

    @Test
    public void findRegisteredApps() {
        assertNotNull(pushAppDao);
        List<PushApplication> apps = pushAppDao.findAllForDeveloper("admin");
        assertNotNull(apps);
        assertEquals(0, apps.size());
    }

    @UsingDataSet("pushapps.yml")
    @Transactional(value = TransactionMode.DISABLED)
    @Test
    public void findAppRegisteredByAPE() {
        assertNotNull(pushAppDao);
        List<PushApplication> apps = pushAppDao.findAllForDeveloper("admin");
        assertNotNull("There are some applications registered", apps);
        assertEquals("There is one application registered via Arquillian APE", 1, apps.size());
    }
}
