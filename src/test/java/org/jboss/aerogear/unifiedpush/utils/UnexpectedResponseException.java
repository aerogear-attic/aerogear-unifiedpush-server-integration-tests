package org.jboss.aerogear.unifiedpush.utils;

import static org.junit.Assert.assertNotNull;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.jayway.restassured.response.Response;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class UnexpectedResponseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Response response;

    public UnexpectedResponseException(Response response) {
        this(response, null);
    }

    public UnexpectedResponseException(@NotNull Response response, Status expectedStatus) {
        super("Unexpected response status code: " + response.getStatusCode() + "!"
                + (expectedStatus != null ? " (expected: " + expectedStatus.getStatusCode() + ")" : ""));
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public static void verifyResponse(Response response, Status expectedStatus) throws UnexpectedResponseException,
            NullPointerException {
        assertNotNull(expectedStatus);
        if (response.statusCode() != expectedStatus.getStatusCode()) {
            throw new UnexpectedResponseException(response, expectedStatus);
        }
    }

    public static class Matcher extends TypeSafeMatcher<UnexpectedResponseException> {
        private int expectedResponseCode;
        private int foundResponseCode;

        private Matcher(Status expectedResponseCode) {
            this.expectedResponseCode = expectedResponseCode.getStatusCode();
        }

        @Override
        protected boolean matchesSafely(UnexpectedResponseException e) {
            this.foundResponseCode = e.getResponse().statusCode();
            return foundResponseCode == expectedResponseCode;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("The found response code: ").appendValue(foundResponseCode)
                    .appendText(" doesn't match expected code: ").appendValue(expectedResponseCode);
        }

        public static Matcher expect(Status expectedStatus) {
            return new Matcher(expectedStatus);
        }
    }
}
