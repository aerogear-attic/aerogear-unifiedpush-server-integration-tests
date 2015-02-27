/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.aerogear.push.helloworld.cordova.test;

import java.io.File;

import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.extension.recorder.screenshooter.Screenshooter;
import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunAsClient
@RunWith(Arquillian.class)
public class AerogearPushHelloworldCordovaTestCase {

    private static final String PUSH_URL = System.getProperty("pushUrl");

    private static final String PUSH_APP_ID = System.getProperty("pushApplicationId");

    private static final String PUSH_APP_SECRET = System.getProperty("pushApplicationSecret");

    public static final String PUSH_MESSAGE = "Hello Cordova!";

    @ArquillianResource
    Screenshooter screenshooter;

    @Deployment
    @Instrumentable
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("selendroid.test.app")));
    }

    @Page
    private MessagesPage messagesPage;

    @Test
    public void sendPushNotificationTest() {

        messagesPage.waitForRegistration();

        screenshooter.takeScreenshot("after_registration");

        JavaSender sender = new SenderClient.Builder(PUSH_URL).build();

        UnifiedMessage message = new UnifiedMessage.Builder()
            .pushApplicationId(PUSH_APP_ID)
            .masterSecret(PUSH_APP_SECRET)
            .alert(PUSH_MESSAGE)
            .build();

        TestMessageResponseCallback callback = new TestMessageResponseCallback();

        sender.send(message, callback);

        Assert.assertEquals(200, callback.getStatusCode());

        messagesPage.checkReceivedPushMessage();
        
        screenshooter.takeScreenshot("after_receiving_notification");
    }
}
