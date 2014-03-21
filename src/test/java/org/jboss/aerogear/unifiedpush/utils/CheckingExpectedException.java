package org.jboss.aerogear.unifiedpush.utils;

import org.hamcrest.Matcher;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public abstract class CheckingExpectedException implements TestRule {

    private final ExpectedException delegate = ExpectedException.none();

    public CheckingExpectedException() {
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new CheckingExpectedExceptionStatement(delegate.apply(statement, description));
    }

    protected abstract void afterExceptionAssert();

    public void expectUnexpectedResponseException(final int expectedStatus) {
        expect(UnexpectedResponseException.class);
        expect(UnexpectedResponseException.Matcher.expect(expectedStatus));
    }

    public void expect(Class<? extends Throwable> type) {
        delegate.expect(type);
    }

    public void expect(Matcher<?> matcher) {
        delegate.expect(matcher);
    }

    @Deprecated
    public static CheckingExpectedException none() {
        return new CheckingExpectedException() {
            @Override
            protected void afterExceptionAssert() { }
        };
    }

    private class CheckingExpectedExceptionStatement extends Statement {
        private final Statement base;

        public CheckingExpectedExceptionStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                afterExceptionAssert();
            }
        }
    }
}
