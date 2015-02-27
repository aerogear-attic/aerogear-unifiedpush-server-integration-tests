/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.arquillian.droidium.native_.webdriver.AndroidDriver;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Predicate;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class MessagesPage {

    private static final String REGISTRATION_MESSAGE = "Successfully registered";

    @Drone
    private AndroidDriver driver;

    @FindBy(css = "#messages")
    private WebElement messagesList;

    public void waitForRegistration() {
        driver.switchTo().window("WEBVIEW");
        
        Graphene.waitModel(driver).until().element(messagesList).is().present();

        List<WebElement> messages = messagesList.findElements(By.tagName("li"));

        Assert.assertEquals(1, messages.size());
        
        Graphene.waitModel(driver)
            .withTimeout(15, TimeUnit.SECONDS)
            .pollingEvery(1, TimeUnit.SECONDS)
            .until()
            .element(messages.get(0))
            .text()
            .equalTo(REGISTRATION_MESSAGE);
    }

    public void checkReceivedPushMessage() {

        MessagePredicate messagePredicate = new MessagePredicate();
        
        Graphene.waitModel(driver)
            .withTimeout(30, TimeUnit.SECONDS)
            .pollingEvery(3, TimeUnit.SECONDS)
            .until(messagePredicate);

        List<WebElement> messages = messagePredicate.getMessages();

        // first record is "Successfully registered", second record is actual push message
        Assert.assertEquals(AerogearPushHelloworldCordovaTestCase.PUSH_MESSAGE, messages.get(1).getText());
    }
    
    private class MessagePredicate implements Predicate<WebDriver> {

        private List<WebElement> messages;
        
        @Override
        public boolean apply(WebDriver driver) {
            List<WebElement> messages = driver.findElements(By.tagName("li"));

            if (messages != null && messages.size() == 2) {
                this.messages = messages;
                return true;
            }
            
            return false;
        }
        
        public List<WebElement> getMessages() {
            return messages;
        }
    }
}
