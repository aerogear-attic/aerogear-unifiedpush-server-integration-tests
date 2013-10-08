package org.jboss.aerogear.unifiedpush.rest.security

import org.jboss.aerogear.unifiedpush.common.AdminUtils
import org.jboss.aerogear.unifiedpush.common.AuthenticationUtils
import org.jboss.aerogear.unifiedpush.common.Deployments
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSpecification
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.shrinkwrap.api.spec.WebArchive
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.core.Response.Status

@ArquillianSpecification
@RunAsClient
@Mixin([AdminUtils, AuthenticationUtils])
class AdminEndpointSpecification extends Specification {

    def static final DEVELOPER_LOGIN_NAME = "second_admin"

    def static final DEVELOPER_PASSWORD = "opensource2013"

    def static final DEVELOPER_NEW_PASSWORD = "opensource2014"

    @Deployment
    def static WebArchive "create deployment"() {
        Deployments.unifiedPushServer()
    }

    @Shared
    def static authCookies

    @ArquillianResource
    URL root

    def "Login"() {
        when:
        def response = secureLogin()
        authCookies = response.getCookies()

        then:
        response != null && response.getStatusCode() == Status.OK.getStatusCode()

        and:
        authCookies != null
    }

    def "Enroll developer"() {
        given:
        def developer = createDeveloper(DEVELOPER_LOGIN_NAME, DEVELOPER_PASSWORD)

        when:
        def response = enrollDeveloper(developer, authCookies)

        then:
        response != null && response.getStatusCode() == Status.OK.getStatusCode()
    }

    def "Try log in with new account"() {
        when:
        def response = loginWorkFlow(DEVELOPER_LOGIN_NAME, DEVELOPER_PASSWORD, DEVELOPER_NEW_PASSWORD)
        authCookies = response.getCookies()

        then:
        response != null && response.getStatusCode() == Status.OK.getStatusCode()

        and:
        authCookies != null && authCookies.size() > 0
    }

}
