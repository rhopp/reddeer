package org.jboss.reddeer.junit.internal.runner;

import java.util.ArrayList;
import java.util.List;

import org.jboss.reddeer.junit.extensionpoint.IBeforeTest;
import org.jboss.reddeer.junit.internal.requirement.Requirements;
import org.jboss.reddeer.junit.logging.Logger;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class ParameterizedRunner extends Parameterized {

	private static final Logger log = Logger.getLogger(RedDeerSuite.class);
	private Class<?> suiteKlass;
	private List<IBeforeTest> beforeTestExtensions;
	private Requirements requirements;
	private String configId;
	private RunListener[] runListeners;
	private List<Runner> children;

	public ParameterizedRunner(Class<?> clazz, Requirements requirements,
			String configId, RunListener[] runListeners,
			List<IBeforeTest> beforeTestExtensions) throws Throwable {
		super(clazz);
		suiteKlass = clazz;
		this.requirements = requirements;
		this.configId = configId;
		this.runListeners = runListeners;
		this.beforeTestExtensions = beforeTestExtensions;
	}


	protected Runner createRunner(String pattern, int index, Object[] parameters)
			throws InitializationError {
		System.out.println("======================================JA JSEM TAAADYYY===============================");
		return new BlockJUnit4ClassRunner(getTestClass().getJavaClass());
	}

	@Override
	protected Statement withBeforeClasses(Statement statement) {
		runBeforeTest();
		return super.withBeforeClasses(statement);
	}

	// @Override
	// protected List<Runner> getChildren() {
	// List<Runner> list = new ArrayList<Runner>();
	// try {
	// list.add(new BlockJUnit4ClassRunner(suiteKlass));
	// } catch (InitializationError e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return list;
	// }

	/**
	 * Method is called before test is run. Manages
	 * org.jbossreddeer.junit.before.test extensions
	 */
	private void runBeforeTest() {
		for (IBeforeTest beforeTestExtension : beforeTestExtensions) {
			if (beforeTestExtension.hasToRun()) {
				log.debug("Run method runBeforeTest() of class "
						+ beforeTestExtension.getClass().getCanonicalName());
				beforeTestExtension.runBeforeTest();
			}
		}
	}
}
