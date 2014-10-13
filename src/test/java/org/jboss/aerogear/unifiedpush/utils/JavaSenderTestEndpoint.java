/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.utils;

import javax.ejb.Stateless;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;

/**
 * End-point for {@link JavaSenderKeyStoreSharingTest}
 */
@Stateless
@Path("/javaSenderTest")
public class JavaSenderTestEndpoint {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String sendPushMessage(@FormParam("pushAppId") String appId, @FormParam("secret") String secret) {
        JavaSender defaultJavaSender = new SenderClient.Builder("https://localhost:8443/ag-push").build();
        
        UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
            .pushApplicationId(appId)
            .masterSecret(secret)
            .alert("Hello from Java Sender API!")
            .build();
        
        defaultJavaSender.send(unifiedMessage);
        
        return "Message sent!";
    }
    
}
