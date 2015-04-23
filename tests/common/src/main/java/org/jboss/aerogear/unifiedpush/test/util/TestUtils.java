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
package org.jboss.aerogear.unifiedpush.test.util;

import category.APNS;
import category.GCM;
import category.SimplePush;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import org.jboss.aerogear.test.FileUtils;
import org.jboss.aerogear.test.api.sender.SenderRequest;
import org.jboss.shrinkwrap.api.ShrinkWrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to determine whether a test class or method should be run based on external specification of categories.
 * <p/>
 * Categories are specified as comma separated list of fully qualified class names via
 * -DexcludedGroups for excluded groups and -Dgroups properties.
 * <p/>
 * If a category is present in both excludedGroups and groups, it is not executed.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class TestUtils {

    public static boolean ignoreHttp() {
        return Boolean.getBoolean("http.ignore");
    }

    public static boolean ignoreHttps() {
        return Boolean.getBoolean("https.ignore");
    }

    public static boolean simplePushTestsEnabled() {
        return shouldRun(SimplePush.class);
    }

    public static boolean apnsTestsEnabled() {
        return shouldRun(APNS.class);
    }

    public static boolean gcmTestsEnabled() {
        return shouldRun(GCM.class);
    }

    public static void setupRestAssured() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"));
    }

    public static void teardownRestAssured() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"));
    }

    public static SenderRequest prepareSenderRequest() {
        return SenderRequest.request();
    }

    public static byte[] getDefaultApnsCertificate() {
        InputStream inputStream = TestUtils.class.getResourceAsStream("/certs/qaAerogear.p12");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } catch(IOException e) {
            throw new IllegalStateException("Could not read default apns certificate!", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }

        return outputStream.toByteArray();
    }

    public static String getDefaultApnsCertificatePassword() {
        return "aerogear";
    }

    private static boolean shouldRun(Class<?> category) {
        List<String> excludedGroups = new ArrayList<String>();
        for (String excluded : System.getProperty("excludedGroups", "").split(",")) {
            if (excluded != null) {
                excluded = excluded.trim();
                if (!excluded.equals("")) {
                    excludedGroups.add(excluded);
                }
            }
        }

        List<String> includedGroups = new ArrayList<String>();
        for (String included : System.getProperty("groups", "").split(",")) {
            if (included != null) {
                included = included.trim();
                if (!included.equals("")) {
                    includedGroups.add(included);
                }
            }
        }

        return (includedGroups.isEmpty() || includedGroups.contains(category.getCanonicalName())) &&
                (excludedGroups.isEmpty() || !excludedGroups.contains(category.getCanonicalName()));
    }

}
