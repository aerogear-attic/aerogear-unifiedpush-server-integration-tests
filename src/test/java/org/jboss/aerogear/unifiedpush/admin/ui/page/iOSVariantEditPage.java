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
package org.jboss.aerogear.unifiedpush.admin.ui.page;

import java.io.File;

import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.IOSVariantForm;
import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.ModalDialog;
import org.jboss.arquillian.graphene.findby.FindByJQuery;

public class iOSVariantEditPage {

    @FindByJQuery("[ng-show='variant.type == \'ios\'']")
    private IOSVariantForm iOSForm;

    @FindByJQuery(".modal")
    private ModalDialog modal;

    public boolean isProd() {
        return iOSForm.isProduction();
    }

    public void updateVariant(String... input) {
        if (input[0] != null && input[1] != null) {
            modal.setName(input[0]);
            modal.setDescription(input[1]);
        }
        if (input[2] != null) {
            iOSForm.setCertificate((new File(input[2])).getAbsolutePath());
        }
        if (input[3] != null) {
            iOSForm.setPassphrase(input[3]);
        }

        modal.ok();
    }

    public String getName() {
        return modal.getName();
    }

    public String getDescription() {
        return modal.getDescription();
    }
}
