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

public class Installation {

    private String deviceToken;

    private String deviceType;

    private String operatingSystem;

    private String osVersion;

    private String alias;

    private String category;

    private String platform;

    private String status;

    private String simplePushEndpoint;

    public Installation(String deviceToken, String deviceType, String operatingSystem, String alias, String platform,
            String status, String simplePushEndpoint, String category) {
        this.deviceToken = deviceToken;
        this.deviceType = deviceType;
        this.operatingSystem = operatingSystem;
        this.alias = alias;
        this.platform = platform;
        this.status = status;
        this.simplePushEndpoint = simplePushEndpoint;
        this.category = category;
    }

    public String getSimplePushEndpoint() {
        return simplePushEndpoint;
    }

    public void setSimplePushEndpoint(String simplePushEndpoint) {
        this.simplePushEndpoint = simplePushEndpoint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
