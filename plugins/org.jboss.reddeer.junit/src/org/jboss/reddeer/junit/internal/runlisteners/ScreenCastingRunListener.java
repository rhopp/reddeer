package org.jboss.reddeer.junit.internal.runlisteners;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;

import org.jboss.reddeer.junit.internal.screenrecorder.ScreenRecorderExt;
import org.jboss.reddeer.junit.logging.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ScreenCastingRunListener extends RunListener {

	private static final Logger log = Logger
			.getLogger(ScreenCastingRunListener.class);

	private static boolean SAVE_SCREENCAST = System.getProperty(
			"recordScreenCast", "false").equalsIgnoreCase("true");
	private static ScreenRecorderExt screenRecorderExt = null;

	private File outputVideoFile = null;
	private boolean wasFailure = false;

	@Override
	public void testFailure(Failure failure) throws Exception {
		wasFailure = true;
		Throwable throwable = failure.getException();
		// it's test failure
		if (throwable instanceof AssertionError) {
			log.error("Failed test: " + failure.getDescription(), throwable);
		}
		// it's Exception
		else {
			log.error("Exception in test: " + failure.getDescription(),
					throwable);
		}
		if (SAVE_SCREENCAST) {
			stopScreenRecorder();
		}
		super.testFailure(failure);
	}

	@Override
	public void testFinished(Description description) throws Exception {
		log.info("Finished test: " + description);
		if (SAVE_SCREENCAST && !wasFailure) {
			stopScreenRecorder();
			log.info("Deleting test screencast file: "
					+ outputVideoFile.getAbsolutePath());
			outputVideoFile.delete();
		}
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
		wasFailure = false;
		if (SAVE_SCREENCAST) {
			outputVideoFile = startScreenRecorder(description.toString());
		}
		super.testStarted(description);
	}

	/**
	 * Starts Screen Recorder
	 */
	private static File startScreenRecorder(String className) {
		File outputVideoFile = null;
		if (screenRecorderExt == null) {
			try {
				screenRecorderExt = new ScreenRecorderExt();
			} catch (IOException ioe) {
				throw new RuntimeException(
						"Unable to initialize Screen Recorder.", ioe);
			} catch (AWTException awte) {
				throw new RuntimeException(
						"Unable to initialize Screen Recorder.", awte);
			}
		}
		if (screenRecorderExt != null) {
			if (screenRecorderExt.isState(ScreenRecorderExt.STATE_DONE)) {
				try {
					File screenCastDir = new File("screencasts");
					if (!screenCastDir.exists()) {
						screenCastDir.mkdir();
					}
					final String fileName = "screencasts" + File.separator
							+ className + ".mov";
					log.info("Starting Screen Recorder. Saving Screen Cast to file: "
							+ fileName);
					screenRecorderExt.start(fileName);
					outputVideoFile = new File(fileName);
				} catch (IOException ioe) {
					throw new RuntimeException(
							"Unable to start Screen Recorder.", ioe);
				}
			} else {
				throw new RuntimeException(
						"Unable to start Screen Recorder.\nScreen Recorder is not in state DONE.");
			}
		} else {
			log.error("Screen Recorder was not properly initilized");
		}
		return outputVideoFile;
	}

	/**
	 * Stops Screen Recorder
	 */
	private static void stopScreenRecorder() {
		if (screenRecorderExt != null) {
			if (screenRecorderExt.isState(ScreenRecorderExt.STATE_RECORDING)) {
				try {
					screenRecorderExt.stop();
					log.info("Screen Recorder stopped.");
				} catch (IOException ioe) {
					throw new RuntimeException(
							"Unable to stop Screen Recorder.", ioe);
				}
			} else {
				throw new RuntimeException(
						"Unable to stop Screen Recorder.\nScreen Recorder is not in state RECORDING.");
			}
		} else {
			throw new RuntimeException(
					"Unable to stop Screen Recorder.\nScreen Recorder was not properly initilized");
		}
	}

}
