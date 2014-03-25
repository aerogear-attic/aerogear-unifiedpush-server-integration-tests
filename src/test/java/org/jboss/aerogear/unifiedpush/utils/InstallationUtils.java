/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.model.InstallationImpl;
import org.jboss.aerogear.test.model.Variant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class InstallationUtils {
    private InstallationUtils() {
    }


    public static void checkEquality(InstallationImpl expected, InstallationImpl actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDeviceToken(), actual.getDeviceToken());
        assertEquals(expected.getDeviceType(), actual.getDeviceType());
        assertEquals(expected.getOperatingSystem(), actual.getOperatingSystem());
        assertEquals(expected.getOsVersion(), actual.getOsVersion());
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCategories(), actual.getCategories());
        assertEquals(expected.getSimplePushEndpoint(), actual.getSimplePushEndpoint());
    }

}
