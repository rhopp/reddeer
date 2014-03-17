package org.jboss.reddeer.junit.internal.runlisteners;

import org.jboss.reddeer.junit.logging.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class LoggingRunListener extends RunListener{
	
	private static final Logger log = Logger
			.getLogger(LoggingRunListener.class);

	@Override
	public void testFailure(Failure failure) throws Exception {
		Throwable throwable = failure.getException();
		// it's test failure
		if (throwable instanceof AssertionError){
			log.error("Failed test: " + failure.getDescription(),throwable);
		}
		// it's Exception
		else {
			log.error("Exception in test: " + failure.getDescription(),throwable);
		}
		super.testFailure(failure);
	}
	@Override
	public void testFinished(Description description) throws Exception {
		log.info("Finished test: " + description);
		super.testFinished(description);
	}
	@Override
	public void testIgnored(Description description) throws Exception {
		log.info("Ignored test: " + description);
		super.testIgnored(description);
	}
	@Override
	public void testStarted(Description description) throws Exception {
		log.info("Started test: " + description);
		super.testStarted(description);
	}
	
}
