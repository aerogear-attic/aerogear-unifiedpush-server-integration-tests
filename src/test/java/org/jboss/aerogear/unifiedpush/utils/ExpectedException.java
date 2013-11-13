package org.jboss.aerogear.unifiedpush.utils;

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class ExpectedException implements TestRule {

    private final org.junit.rules.ExpectedException delegate = org.junit.rules.ExpectedException.none();

    private ExpectedException() {
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return delegate.apply(statement, description);
    }

    public void expectUnexpectedResponseException(final Response.Status expectedStatus) {
        expect(UnexpectedResponseException.class);
        expect(UnexpectedResponseException.Matcher.expect(expectedStatus));
    }

    public void expect(Class<? extends Throwable> type) {
        delegate.expect(type);
    }

    public void expect(Matcher<?> matcher) {
        delegate.expect(matcher);
    }

    public static ExpectedException none() {
        return new ExpectedException();
    }
}
