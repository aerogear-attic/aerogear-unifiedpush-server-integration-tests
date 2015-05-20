/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.aerogear.unifiedpush.test.manual;

import org.arquillian.extension.governor.skipper.api.TestSpec;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class ManualTestSpecs {

    @Test
    @TestSpec(
            steps = "1. obtain file with device tokens, (refer to this JavaDoc for the file format: https://aerogear.org/docs/specs/aerogear-unifiedpush-rest/registry/device/importer/index.html)" +
                    "2. POST /registry/device/importer" +
                    "3. go to variant view in UPS console and check that it contains just imported device tokens",
            assertion = "device tokens can be imported into UPS instance from a JSON file"
    )
    public void jsonImportEndpointTest() {}

    @Test
    @TestSpec(
        steps = "1. start UPS server of version X or higher " +
                "2. use UPS Sender of version (X - n) > 1.0.0.Final " +
                "3. send a message using the sender in step 2",
        assertion = "Message gets send successfully"
    )
    public void backwardCompatibilitySenderTest() {}

    @Test
    @TestSpec(
            steps = "1. send at least 100 messages by UPS Sender to UPS to one registered device.",
            assertion = "Client gets 100 times HTTP response 202 (submitted) and UPS console displays that 100 messages were sent"
    )
    public void manyMessagesInSmallIntervalTest() {}

    @Test
    @TestSpec(
            assertion = "1. when sending to 999 devices, 1 connection will be used " +
                    "2. when sending to 1000 devices, 1 connection will be used " +
                    "3. when sending to 1000 devices, 1 connection will be used " +
                    "4. when sending to 1001 devices, 2 connections will be used " +
                    "5. when sending to 2000 devices, 2 connections will be used ",
            feature = "UPS sends messages to APNS in a batch of 1000 messages"
    )
    public void APNStokenDeliveryTest() {}

    @Test
    @TestSpec(
            steps = "1. create variant A " +
                    "2. create variant B " +
                    "3. create installation A1 in variant A with category C " +
                    "4. create installation B1 in variant B with category C ",
            assertion = "1. only one category is created in database " +
                    "2. sending a message to category C will deliver it to both A1 and B1 " +
                    "3. deleting B1 or A1 will not delete the category C" +
                    "4. deleting both B1 and A1 will also delete category C"
    )
    public void categoriesAreSharedBetweenVariantsTest() {}

    @Test
    @TestSpec(
            issue = "AGPUSH-1347",
            steps = "1. start UPS " +
                    "2. call /sys/info/health and check that status is 'ok' " +
                    "3. simulate push network failure " +
                    "4. call /sys/info/health and check that status is 'warn' " +
                    "5. stop database " +
                    "6. call /sys/info/health and check that status is 'crit' ",
            assertion = "call to /sys/info/health REST endpoint returns appropriate status codes "
    )
    public void healthEndpointTest() {}
}
