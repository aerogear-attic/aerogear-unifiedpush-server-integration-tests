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
package org.jboss.aerogear.unifiedpush.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import category.AdminUI;
import category.ChromePackagedApp;
import category.SimplePush;

/**
 * Helper to determine whether a test class or method should be run based on external specification of categories.
 *
 * Categories are specified as comma separated list of fully qualified class names via
 * -DexcludedGroups for excluded groups and -Dgroups properties.
 *
 * If a category is present in both excludedGroups and groups, it is not executed.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class TestUtils {

    public static boolean simplePushTestsEnabled() {
        return shouldRun(SimplePush.class);

    }

    public static boolean chromePackagedAppTestsEnabled() {
        return shouldRun(ChromePackagedApp.class);
    }

    public static boolean adminUiTestsEnabled() {
        return shouldRun(AdminUI.class);
    }

    private static boolean shouldRun(Class<?> category) {
        List<String> excludedGroups = Arrays.asList(System.getProperty("excludedGroups", "").split(","));
        List<String> includedGroups = Arrays.asList(System.getProperty("groups", "").split(","));

        // trim
        for (int i = 0; i < excludedGroups.size(); i++) {
            excludedGroups.set(i, excludedGroups.get(i).trim());
        }
        for (int i = 0; i < includedGroups.size(); i++) {
            includedGroups.set(i, includedGroups.get(i).trim());
        }

        // delete empty strings
        Iterator<String> excluded = excludedGroups.iterator();
        while (excluded.hasNext()) {
            if (excluded.next().isEmpty()) {
                excluded.remove();
            }
        }
        Iterator<String> included = includedGroups.iterator();
        while (included.hasNext()) {
            if (included.next().isEmpty()) {
                included.remove();
            }
        }

        return (includedGroups.isEmpty() || includedGroups.contains(category.getCanonicalName())) &&
            (excludedGroups.isEmpty() || !excludedGroups.contains(category.getCanonicalName()));
    }
}
