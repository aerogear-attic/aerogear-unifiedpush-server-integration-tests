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
package org.arquillian.extension.smarturl;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
public class URIModifier {

    public static URL augment(URL locatedURL, Annotation... qualifiers) {
        if (locatedURL == null) {
            return null;
        }

        for (Annotation qualifier : qualifiers) {
            if (UriScheme.class.isAssignableFrom(qualifier.annotationType())) {
                return modify(locatedURL, (UriScheme) qualifier);
            }
        }

        return locatedURL;
    }

    private static URL modify(URL locatedURL, UriScheme qualifier) {
        String name = qualifier.name().toString();
        int port = qualifier.port();

        try {
            return new URL(name, locatedURL.getHost().replace("127.0.0.1", "localhost"), port, locatedURL.getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to augment " + locatedURL.toExternalForm() + " with scheme '"
                + name + "' and port '" + port + "'");
        }
    }
}
