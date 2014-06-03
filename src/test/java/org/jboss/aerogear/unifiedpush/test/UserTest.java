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
package org.jboss.aerogear.unifiedpush.test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.api.ModelAsserts;
import org.jboss.aerogear.test.api.user.UserContext;
import org.jboss.aerogear.test.api.user.UserWorker;
import org.jboss.aerogear.test.model.Developer;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

// Ignored because UnifiedPush server no longer
@Ignore
@RunWith(ArquillianRules.class)
public class UserTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {
            return this;
        }
    };

    @Rule
    public CheckingExpectedException thrown = CheckingExpectedException.none();

    @BeforeClass
    public static void setup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"));

        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }

    @AfterClass
    public static void cleanup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"));
    }

    @Deployment(name = Deployments.AUTH_SERVER, testable = false, order = 1)
    @TargetsContainer("main-server-group")
    public static WebArchive createAuthServerDeployment() {
        return Deployments.authServer();
    }

    @Deployment(name = Deployments.AG_PUSH, testable = false, order = 2)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }

    @Test
    public void findByNonexistendId() {
        thrown.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(UserWorker.worker()).find(UUID.randomUUID().toString());
    }

    @Test
    public void testCRUD() {
        performCRUD(UserWorker.worker());
    }

    @Test
    public void testCRUDUTF8() {
        performCRUD(UserWorker.worker().contentType(ContentTypes.jsonUTF8()));
    }

    private void performCRUD(UserWorker worker) {
        // READ ALL
        List<Developer> defaultUsers = ups.with(UserWorker.worker()).findAll().detachEntities();
        int defaultUserCount = defaultUsers.size();

        Developer adminUser = null;
        Developer developerUser = null;

        List<String> defaultUserNames = new ArrayList<String>();
        for (Developer defaultUser : defaultUsers) {
            String loginName = defaultUser.getLoginName();
            if (loginName.equals("admin")) {
                if (adminUser == null) {
                    adminUser = defaultUser;
                } else {
                    fail("There can be no more than one admin user!");
                }
            } else if (loginName.equals("developer")) {
                if (developerUser == null) {
                    developerUser = defaultUser;
                } else {
                    fail("There can be no more than one developer user!");
                }
            }

            defaultUserNames.add(loginName);
        }

        assertThat(defaultUserCount, is(2));
        assertThat(defaultUserNames, Matchers.containsInAnyOrder("admin", "developer"));
        assertThat(adminUser, is(notNullValue()));
        assertThat(developerUser, is(notNullValue()));

        // CREATE
        List<Developer> persistedUsers = ups.with(worker)
                .generate().persist()
                .generate().persist()
                .detachEntities();

        assertThat(persistedUsers.size(), is(2));

        Developer persistedUser = persistedUsers.get(0);
        Developer persistedUser1 = persistedUsers.get(1);

        // READ
        UserContext context = ups.with(worker).findAll();
        List<Developer> readUsers = context.detachEntities();
        assertThat(readUsers.size(), is(2 + defaultUserCount));

        ModelAsserts.assertModelsEqual(persistedUser, context.detachEntity(persistedUser.getId()));
        ModelAsserts.assertModelsEqual(persistedUser1, context.detachEntity(persistedUser1.getId()));

        // UPDATE
        ups.with(worker)
                .edit(persistedUser.getId()).loginName("newloginname").merge();
        Developer readUser = ups.with(worker)
                .find(persistedUser.getId())
                .detachEntity();
        assertThat(readUser.getLoginName(), is("newloginname"));

        // DELETE
        readUsers = ups.with(worker)
                .removeById(persistedUser.getId())
                .removeById(persistedUser1.getId())
                .findAll()
                .detachEntities();
        assertThat(readUsers.size(), is(defaultUserCount));
    }


}
