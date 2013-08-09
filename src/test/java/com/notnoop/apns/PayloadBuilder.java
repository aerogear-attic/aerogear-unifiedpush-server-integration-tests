/*
 * Copyright 2009, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns;

import java.util.Collection;
import java.util.Map;

/**
 * 
 * This class mocks the original com.notnoop.apns.PayloadBuilder class and is used for testing reasons.
 * 
 */
public final class PayloadBuilder {

    private String alert;

    private String sound;

    private int badge;

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    PayloadBuilder() {
    }

    public PayloadBuilder alertBody(final String alert) {
        this.alert = alert;
        return this;
    }

    public PayloadBuilder sound(final String sound) {
        this.sound = sound;
        return this;
    }

    public PayloadBuilder badge(final int badge) {
        this.badge = badge;
        return this;
    }

    public PayloadBuilder clearBadge() {
        return badge(0);
    }

    public PayloadBuilder actionKey(final String actionKey) {
        return this;
    }

    public PayloadBuilder noActionButton() {
        return this;
    }

    public PayloadBuilder forNewsstand() {
        return this;
    }

    public PayloadBuilder localizedKey(final String key) {
        return this;
    }

    public PayloadBuilder localizedArguments(final Collection<String> arguments) {
        return this;
    }

    public PayloadBuilder localizedArguments(final String... arguments) {
        return this;
    }

    public PayloadBuilder launchImage(final String launchImage) {
        return this;
    }

    public PayloadBuilder customField(final String key, final Object value) {
        return this;
    }

    public PayloadBuilder mdm(final String s) {
        return this;
    }

    public PayloadBuilder customFields(final Map<String, ? extends Object> values) {
        return this;
    }

    public String build() {
        return "alert:" + alert + ",sound:" + sound + ",badge:" + badge;
    }

}
