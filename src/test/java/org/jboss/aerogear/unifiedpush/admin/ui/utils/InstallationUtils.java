/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.admin.ui.utils;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.jboss.aerogear.unifiedpush.admin.ui.model.Installation;
import org.json.JSONObject;

public class InstallationUtils {

    public static Response registerInstallation(String contextPath, String variantID, String secret, Installation installation) {

        Response response = null;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("deviceToken", installation.getDeviceToken());
            jsonObject.put("deviceType", installation.getDeviceType());
            jsonObject.put("operatingSystem", installation.getOperatingSystem());
            jsonObject.put("osVersion", installation.getOsVersion());
            jsonObject.put("alias", installation.getAlias());
            jsonObject.put("simplePushEndpoint", installation.getSimplePushEndpoint());

            response = RestAssured.given().contentType("application/json").auth().basic(variantID, secret)
                    .header("Accept", "application/json").body(jsonObject.toString())
                    .post(contextPath + "ag-push/rest/registry/device");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int c = response.statusCode();

        return response;
    }
}
