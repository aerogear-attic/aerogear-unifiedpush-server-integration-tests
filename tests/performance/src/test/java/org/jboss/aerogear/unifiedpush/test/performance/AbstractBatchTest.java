/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.aerogear.unifiedpush.test.performance;

import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.aerogear.unifiedpush.test.util.Deployments;
import org.jboss.aerogear.unifiedpush.test.util.UnifiedPushServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(ArquillianRules.class)
public abstract class AbstractBatchTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {

        @Override
        protected UnifiedPushServer setup() {
            return this;
        }
    };

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

    @BeforeClass
    public static void setup() {
        RestAssured.config = RestAssuredConfig.newConfig()
            .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
            .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"));

    }

    @AfterClass
    public static void cleanup() {
        RestAssured.config = RestAssuredConfig.newConfig()
            .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
            .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"));
    }
}
