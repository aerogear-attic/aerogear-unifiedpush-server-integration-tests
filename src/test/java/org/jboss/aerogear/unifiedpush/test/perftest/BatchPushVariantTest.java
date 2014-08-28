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
package org.jboss.aerogear.unifiedpush.test.perftest;

import org.hamcrest.CoreMatchers;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.aerogear.test.api.perftest.BatchPushApplicationRequest;
import org.jboss.aerogear.test.api.perftest.BatchPushVariantRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Creation of one Push applications for which we register 10 000 (Android) variants
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(ArquillianRules.class)
public class BatchPushVariantTest extends AbstractBatchTest {

    @Test
    public void variantRegistrationTest() {

        String applicationId = ups.with(BatchPushApplicationRequest.request()).register().get(0);

        // create 10 000 variants for just created application

        ups.with(BatchPushVariantRequest.request()).register(10000, applicationId);

        Assert.assertThat(ups.with(BatchPushVariantRequest.request()).countOfAllVariants(applicationId), CoreMatchers.is((long) 10000));

        // deletes all applications so it deletes all variants as well

        ups.with(BatchPushApplicationRequest.request()).deleteAll();

        Assert.assertThat(ups.with(BatchPushApplicationRequest.request()).countOfAllApplications(), CoreMatchers.is((long) 0));
    }
}
