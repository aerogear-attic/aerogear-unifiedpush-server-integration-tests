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
package org.jboss.aerogear.unifiedpush.test.util;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.aerogear.test.arquillian.container.check.StatusCheck;
import org.jboss.aerogear.test.arquillian.container.check.StatusCheckException;
import org.jboss.arquillian.core.spi.Validate;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * @author <a href="mailto:okiss@redhat.com">Oliver Kiss</a>
 */
public class UpsOpenshiftStatusCheck implements StatusCheck {

    private URI uri;

    private static final Logger logger = Logger.getLogger(UpsOpenshiftStatusCheck.class.getName());

    @Override
    public void target(URI uri) {
        Validate.notNull(uri, "target URI to check status of can not be a null object");
        this.uri = uri.getPath().contains("extension") ? uri.resolve(uri.getPath() + "/status") : uri;
    }

    @Override
    public boolean execute() throws StatusCheckException {
        HttpClient client = HttpClientBuilder.create().build();

        int statusCode;
        try {
            statusCode = client.execute(new HttpGet(uri)).getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new StatusCheckException("Error when checking status code of " + uri.toString(), e);
        }

        logger.fine(String.format("Status code: %s", statusCode));
        return statusCode == HttpStatus.SC_OK;
    }
}