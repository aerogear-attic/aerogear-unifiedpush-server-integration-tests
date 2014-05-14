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
package org.jboss.aerogear.test.api.variant.ios;

import org.jboss.aerogear.test.FileUtils;
import org.jboss.aerogear.test.model.iOSVariant;

import java.io.File;

public abstract class iOSVariantExtension<EXTENSION extends iOSVariantExtension<EXTENSION>> extends iOSVariant {
    protected final iOSVariantContext context;

    public iOSVariantExtension(iOSVariantContext context) {
        this.context = context;
    }

    public EXTENSION name(String name) {
        setName(name);
        return castInstance();
    }

    public EXTENSION description(String description) {
        setDescription(description);
        return castInstance();
    }

    public EXTENSION certificate(String certificate) {
        return certificate(new File(certificate));
    }

    public EXTENSION certificate(File certificate) {
        return certificate(FileUtils.toByteArray(certificate));
    }

    public EXTENSION certificate(byte[] certificate) {
        setCertificate(certificate);
        return castInstance();
    }

    public EXTENSION passphrase(String passphrase) {
        setPassphrase(passphrase);
        return castInstance();
    }

    public EXTENSION production(boolean production) {
        setProduction(production);
        return castInstance();
    }

    private EXTENSION castInstance() {
        return (EXTENSION) this;
    }
}