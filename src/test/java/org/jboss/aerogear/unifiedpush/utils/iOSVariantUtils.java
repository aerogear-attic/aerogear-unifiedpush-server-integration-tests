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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public final class iOSVariantUtils {

    private iOSVariantUtils() {
    }

    public static iOSApplicationUploadForm createiOSApplicationUploadForm(Boolean production, String passphrase,
            byte[] certificate, String name, String description) {

        iOSApplicationUploadForm ios = new iOSApplicationUploadForm();

        ios.setCertificate(certificate);
        ios.setDescription(description);
        ios.setName(name);
        ios.setPassphrase(passphrase);
        ios.setProduction(production);

        return ios;
    }

    public static Response registerIOsVariant(String pushAppId, iOSApplicationUploadForm form, Map<String, ?> cookies,
            String certificatePath, String root) {

        assertNotNull(root);

        Response response = RestAssured.given().contentType("multipart/form-data").header("Accept", "application/json")
                .cookies(cookies).multiPart("certificate", new File(certificatePath))
                .multiPart("production", form.getProduction().toString()).multiPart("passphrase", form.getPassphrase())
                .multiPart("name", form.getName()).multiPart("description", form.getDescription())
                .post("{root}rest/applications/{pushAppId}/iOS", root, pushAppId);

        return response;
    }

    public static Response updateIOsVariant(String pushAppId, iOSApplicationUploadForm form, Map<String, ?> cookies,
            String certificatePath, String variantId, String root) {

        assertNotNull(root);

        Response response = RestAssured.given().contentType("multipart/form-data").header("Accept", "application/json")
                .cookies(cookies).multiPart("certificate", new File(certificatePath))
                .multiPart("production", form.getProduction().toString()).multiPart("passphrase", form.getPassphrase())
                .multiPart("name", form.getName()).multiPart("description", form.getDescription())
                .put("{root}rest/applications/{pushAppId}/iOS/{variantId}", root, pushAppId, variantId);

        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response updateIOsVariantPatch(String pushAppId, iOSApplicationUploadForm form, Map<String, ?> cookies,
            String variantId, String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("production", form.getProduction().toString());
        jsonObject.put("name", form.getName());
        jsonObject.put("description", form.getDescription());
        jsonObject.put("passphrase", null);
        jsonObject.put("certificate", null);

        Response response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .body(jsonObject.toString())
                .patch("{root}rest/applications/{pushAppId}/iOS/{variantId}", root, pushAppId, variantId);

        return response;
    }

    public static Response listAlliOSVariants(String pushAppId, Map<String, ?> cookies, String root) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .get("{root}rest/applications/{pushAppId}/iOS/", root, pushAppId);

        return response;
    }

    public static Response findiOSVariantById(String pushAppId, String variantId, Map<String, ?> cookies, String root) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .get("{root}rest/applications/{pushAppId}/iOS/{variantId}", root, pushAppId, variantId);

        return response;
    }

    public static Response deleteiOSVariant(String pushAppid, String variantId, Map<String, ?> cookies, String root) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .cookies(cookies)
                .delete("{root}rest/applications/{pushAppId}/iOS/{variantId}", root, pushAppid, variantId);

        return response;
    }

}
