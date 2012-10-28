package org.testevol.engine;

public class TestResult {

    public static enum TestOutcome {
        PASS, COMPILATION_ERROR, RUNTIME_ERROR, ASSERT_FAILURE, IGNORE
    };

    private TestOutcome outcome;

    private Throwable exception; // null for PASS and IGNORE outcomes

    public TestResult(TestOutcome outcome, Throwable exception) {
        this.outcome = outcome;
        this.exception = exception;
    }

    public TestOutcome getTestOutcome() {
        return outcome;
    }

    public Throwable getException() {
        return exception;
    }

}
