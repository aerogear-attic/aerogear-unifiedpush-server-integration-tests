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

import java.util.logging.Logger;

import org.hamcrest.CoreMatchers;
import org.jboss.aerogear.unifiedpush.api.performance.BatchPushApplicationRequest;
import org.jboss.aerogear.unifiedpush.api.performance.BatchPushInstallationRequest;
import org.jboss.aerogear.unifiedpush.api.performance.BatchPushVariantRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Creates 1 application, for that application there is created 1 variant and for that variant we register 10 000 installations.
 *
 * Variant is of Android platform.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class BatchPushInstallationTest extends AbstractBatchTest {

    private static final Logger logger = Logger.getLogger(BatchPushInstallationTest.class.getName());

    @Test
    public void registerInstallationsTest() {

        String pushApplicationId = ups.with(BatchPushApplicationRequest.request()).register().get(0);

        logger.info("Created pushapp with ID " + pushApplicationId);

        String variantId = ups.with(BatchPushVariantRequest.request()).register(pushApplicationId).get(0);

        logger.info("Created variant with ID " + variantId);

        ups.with(BatchPushInstallationRequest.request()).register(10000, variantId);

        long count = ups.with(BatchPushInstallationRequest.request()).countAllInstallations(variantId);

        Assert.assertThat(count, CoreMatchers.is((long) 10000));
    }
}
