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
package org.jboss.aerogear.test.api.installation.android;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.installation.InstallationContext;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;

public class AndroidInstallationContext extends InstallationContext<AndroidInstallationBlueprint,
        AndroidInstallationEditor, AndroidVariant, AndroidInstallationWorker, AndroidInstallationContext> {

    public AndroidInstallationContext(AndroidInstallationWorker worker, AndroidVariant parent, Session session) {
        super(worker, parent, session);
    }

    @Override
    protected AndroidInstallationContext castInstance() {
        return this;
    }

    @Override
    protected AndroidInstallationEditor createEditor() {
        return new AndroidInstallationEditor(this);
    }

    @Override
    public AndroidInstallationBlueprint create() {
        return new AndroidInstallationBlueprint(this);
    }

    @Override
    public AndroidInstallationBlueprint generate() {
        return create()
                .deviceToken(randomStringOfLength(100))
                .alias(randomString());
    }
}
