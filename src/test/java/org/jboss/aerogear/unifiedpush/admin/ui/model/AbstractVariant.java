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
package org.jboss.aerogear.unifiedpush.admin.ui.model;

public class AbstractVariant {

    private int installations;

    private VariantType variantType;

    private String name;
    
    private String projectNumber;

    private String description;

    private String variantID;

    private String secret;

    private String developer;

    public AbstractVariant(String name, String projectNumber, String description, String type, int installations) {
        this.name = name;
        this.projectNumber = projectNumber;
        this.description = description;
        this.setVariantType(VariantType.ANDROID.getTypeName().equalsIgnoreCase(type) ? VariantType.ANDROID : VariantType.IOS
                .getTypeName().equalsIgnoreCase(type) ? VariantType.IOS : VariantType.SIMPLE_PUSH);
        this.installations = installations;
    }

    public int getInstallations() {
        return installations;
    }

    public void setInstallations(int installations) {
        this.installations = installations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVariantID() {
        return variantID;
    }

    public void setVariantID(String variantID) {
        this.variantID = variantID;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }
}
