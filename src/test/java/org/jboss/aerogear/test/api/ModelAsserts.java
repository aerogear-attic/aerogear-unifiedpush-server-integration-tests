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
package org.jboss.aerogear.test.api;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;

import static org.junit.Assert.assertEquals;

// FIXME everything here should be in "equals" methods in actual models
public class ModelAsserts {

    public static void assertModelsEqual(AndroidVariant expected, AndroidVariant actual) {
        assertEquals("Name is not equal!", expected.getName(), actual.getName());
        assertEquals("Description is not equal!", expected.getDescription(), actual.getDescription());
        assertEquals("VariantId is not equal!", expected.getVariantID(), actual.getVariantID());
        assertEquals("Secret is not equal!", expected.getSecret(), actual.getSecret());
        assertEquals("Developer is not equal!", expected.getDeveloper(), actual.getDeveloper());

        // TODO we can't do this check as none of variants has the equals method implemented
        // assertEquals(expected.getIOSVariants(), actual.getIOSVariants());
        // assertEquals(expected.getAndroidVariants(), actual.getAndroidVariants());
        // assertEquals(expected.getSimplePushVariants(), actual.getSimplePushVariants());
    }

    public static void assertModelsEqual(Installation expected, Installation actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDeviceToken(), actual.getDeviceToken());
        assertEquals(expected.getDeviceType(), actual.getDeviceType());
        assertEquals(expected.getOperatingSystem(), actual.getOperatingSystem());
        assertEquals(expected.getOsVersion(), actual.getOsVersion());
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCategories(), actual.getCategories());
    }

    public static void assertModelsEqual(iOSVariant expected, iOSVariant actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getVariantID(), actual.getVariantID());
        assertEquals(expected.getSecret(), actual.getSecret());
        assertEquals(expected.getDeveloper(), actual.getDeveloper());
        // FIXME add to unit tests as rest api does no longer show the current passphrase
        // assertEquals(expected.getPassphrase(), actual.getPassphrase());
    }

    public static void assertModelsEqual(PushApplication expected, PushApplication actual) {
        assertEquals("Name is not equal!", expected.getName(), actual.getName());
        assertEquals("Description is not equal!", expected.getDescription(), actual.getDescription());
        assertEquals("PushApplicationId is not equal!", expected.getPushApplicationID(), actual.getPushApplicationID());
        assertEquals("MasterSecret is not equal!", expected.getMasterSecret(), actual.getMasterSecret());
        assertEquals("Developer is not equal!", expected.getDeveloper(), actual.getDeveloper());

        // TODO we can't do this check as none of variants has the equals method implemented
        // assertEquals(expected.getIOSVariants(), actual.getIOSVariants());
        // assertEquals(expected.getAndroidVariants(), actual.getAndroidVariants());
        // assertEquals(expected.getSimplePushVariants(), actual.getSimplePushVariants());
    }

    public static void assertModelsEqual(SimplePushVariant expected, SimplePushVariant actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getVariantID(), actual.getVariantID());
        assertEquals(expected.getSecret(), actual.getSecret());
        assertEquals(expected.getDeveloper(), actual.getDeveloper());
    }


}
