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

import java.io.InputStream;
import java.net.Proxy;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;

import com.notnoop.apns.internal.ApnsServiceImpl;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.RuntimeIOException;

/**
 * 
 * This class mocks the original com.notnoop.apns.ApnsServiceBuilder class and is used for testing reasons.
 * 
 */
public class ApnsServiceBuilder {

    public ApnsServiceBuilder() {
    }

    public ApnsServiceBuilder withCert(String fileName, String password) throws RuntimeIOException, InvalidSSLConfig {

        return this;
    }

    public ApnsServiceBuilder withCert(InputStream stream, String password) throws InvalidSSLConfig {
        return this;
    }

    public ApnsServiceBuilder withCert(KeyStore keyStore, String password) throws InvalidSSLConfig {
        return this;
    }

    public ApnsServiceBuilder withSSLContext(SSLContext sslContext) {
        return this;
    }

    public ApnsServiceBuilder withGatewayDestination(String host, int port) {
        return this;
    }

    public ApnsServiceBuilder withFeedbackDestination(String host, int port) {
        return this;
    }

    public ApnsServiceBuilder withAppleDestination(boolean isProduction) {
        return this;
    }

    public ApnsServiceBuilder withSandboxDestination() {
        return this;
    }

    public ApnsServiceBuilder withProductionDestination() {
        return this;
    }

    public ApnsServiceBuilder withReconnectPolicy(ReconnectPolicy rp) {
        return this;
    }

    public ApnsServiceBuilder withAutoAdjustCacheLength(boolean autoAdjustCacheLength) {
        return this;
    }

    public ApnsServiceBuilder withReconnectPolicy(ReconnectPolicy.Provided rp) {
        return this;
    }

    public ApnsServiceBuilder withSocksProxy(String host, int port) {
        return this;
    }

    public ApnsServiceBuilder withProxy(Proxy proxy) {
        return this;
    }

    public ApnsServiceBuilder withCacheLength(int cacheLength) {
        return this;
    }

    @Deprecated
    public ApnsServiceBuilder withProxySocket(Socket proxySocket) {
        return this;
    }

    public ApnsServiceBuilder asPool(int maxConnections) {
        return this;
    }

    public ApnsServiceBuilder asPool(ExecutorService executor, int maxConnections) {
        return this;
    }

    public ApnsServiceBuilder asQueued() {
        return this;
    }

    public ApnsServiceBuilder asBatched() {
        return this;
    }

    public ApnsServiceBuilder asBatched(int waitTimeInSec, int maxWaitTimeInSec) {
        return this;
    }

    public ApnsServiceBuilder asBatched(int waitTimeInSec, int maxWaitTimeInSec, ThreadFactory threadFactory) {
        return this;
    }

    public ApnsServiceBuilder withDelegate(ApnsDelegate delegate) {
        return this;
    }

    public ApnsServiceBuilder withNoErrorDetection() {
        return this;
    }

    public ApnsService build() {
        return new ApnsServiceImpl();
    }
}
