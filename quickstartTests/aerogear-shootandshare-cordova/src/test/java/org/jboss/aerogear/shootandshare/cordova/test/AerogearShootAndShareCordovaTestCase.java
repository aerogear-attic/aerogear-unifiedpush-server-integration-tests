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
package org.jboss.aerogear.shootandshare.cordova.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.native_.webdriver.AndroidDriver;
import org.arquillian.extension.recorder.screenshooter.Screenshooter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;

@RunAsClient
@RunWith(Arquillian.class)
public class AerogearShootAndShareCordovaTestCase {

    private static final String FACEBOOK_EMAIL = System.getProperty("facebookEmail");

    private static final String FACEBOOK_PASSWORD = System.getProperty("facebookPassword");

    @ArquillianResource
    private Screenshooter screenshooter;
    
    @Drone
    private AndroidDriver driver;

    @Deployment
    @Instrumentable
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("selendroid.test.app")));
    }

    @Test
    @InSequence(1)
    public void test() {

    	driver.switchTo().window("WEBVIEW_0");

    	TabsPredicate tabsPredicate = new TabsPredicate();
    	
        Graphene.waitModel(driver)
        	.withTimeout(30, TimeUnit.SECONDS)
        	.pollingEvery(3, TimeUnit.SECONDS)
        	.until(tabsPredicate);

        Assert.assertEquals(3, tabsPredicate.getTabButtons().size());
        
        WebElement button = tabsPredicate.getButton("facebook");
        
        screenshooter.takeScreenshot("before_clicking_on_facebook");
        
        button.click();
        
        driver.switchTo().window("NATIVE_APP");

        WebElement webOauth = driver.findElement(By.id("web_oauth"));

        Assert.assertNotNull(webOauth);
        
        driver.switchTo().window("WEBVIEW_0");

        WebElement email = driver.findElement(By.name("email"));
        
        Assert.assertNotNull(email);

        email.sendKeys(FACEBOOK_EMAIL);

        screenshooter.takeScreenshot("filling_email");

        WebElement password = driver.findElement(By.name("pass"));
        
        Assert.assertNotNull(password);
        
        password.sendKeys(FACEBOOK_PASSWORD);
        
        screenshooter.takeScreenshot("filling_password");
        
        WebElement loginButton = driver.findElement(By.name("login"));

        Assert.assertNotNull(loginButton);
        
        loginButton.click();
        
        screenshooter.takeScreenshot("pushing_logging_button");
    }
    
    private class TabsPredicate implements Predicate<WebDriver> {

        private List<WebElement> tabButtons = new ArrayList<WebElement>();
        
        @Override
        public boolean apply(WebDriver driver) {
            List<WebElement> tabButtons = driver.findElements(By.tagName("a"));

            if (tabButtons != null && tabButtons.size() == 3) {
                this.tabButtons.addAll(tabButtons);
                return true;
            }
            
            return false;
        }
        
        public List<WebElement> getTabButtons() {
            return tabButtons;
        }
        
        public WebElement getButton(String name) {
        	
        	for (WebElement button : tabButtons) {
        		System.out.println(button.getText());
        		if (button != null && button.getText() != null && button.getText().equals(name)) {
        			return button;
        		}
        	}
        	
        	return null;
        }
    }
}
