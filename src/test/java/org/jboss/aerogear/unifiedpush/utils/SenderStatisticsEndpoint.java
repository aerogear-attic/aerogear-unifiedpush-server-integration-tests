package org.jboss.aerogear.unifiedpush.utils;

import com.google.android.gcm.server.Sender;
import com.notnoop.apns.internal.ApnsServiceImpl;
import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.test.api.sender.SenderStatistics;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
@Stateless
@TransactionAttribute
@Path("/senderStats")
@Secure( { "admin" } )
// FIXME move to src/main
public class SenderStatisticsEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllStatistics() {

        List<String> deviceTokens = new ArrayList<String>();
        if(Sender.getGcmRegIdsList() != null) {
            for(String deviceToken : Sender.getGcmRegIdsList()) {
                deviceTokens.add(deviceToken);
            }
        }

        if(ApnsServiceImpl.getTokensList() != null) {
            for(String deviceToken : ApnsServiceImpl.getTokensList()) {
                deviceTokens.add(deviceToken);
            }
        }

        SenderStatistics senderStatistics = new SenderStatistics();
        senderStatistics.deviceTokens = deviceTokens;
        senderStatistics.gcmMessage = Sender.getGcmMessage();
        senderStatistics.apnsAlert = ApnsServiceImpl.getAlert();
        senderStatistics.apnsBadge = ApnsServiceImpl.getBadge();
        senderStatistics.apnsSound = ApnsServiceImpl.getSound();
        senderStatistics.apnsCustomFields = ApnsServiceImpl.getCustomFields();

        return Response.ok(senderStatistics).build();
    }

    @DELETE
    public Response resetStatistics() {
        Sender.clear();
        ApnsServiceImpl.clear();
        return Response.noContent().build();
    }


}
