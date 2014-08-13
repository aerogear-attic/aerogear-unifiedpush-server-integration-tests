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

import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.AndroidVariantForm;
import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.ModalDialog;
import org.jboss.arquillian.graphene.findby.FindByJQuery;

public class AndroidVariantEditPage {

    @FindByJQuery("[ng-show='variant.type == \'android\'']")
    private AndroidVariantForm androidForm;

    @FindByJQuery(".modal")
    private ModalDialog modal;

    public String getGoogleApiKey() {
        return androidForm.getApiKey();
    }

    /**
     * Input: name, desc, google api key
     */
    public void updateVariant(String... input) {
        modal.setName(input[0]);
        modal.setDescription(input[1]);
        androidForm.setApiKey(input[2]);
        modal.ok();
    }

    public String getName() {
        return modal.getName();
    }

    public String getDescription() {
        return modal.getDescription();
    }
}
