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
package org.jboss.aerogear.unifiedpush.android;

import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.junit.BeforeClass;

import com.jayway.restassured.RestAssured;

public class SecureAndroidSelectiveSendByAliasTest extends AndroidSelectiveSendByAliasTest {

    @BeforeClass
    public static void setup() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }
    
    @Override
    protected String getContextRoot() {
        return Constants.SECURE_AG_PUSH_ENDPOINT;
    }
}
