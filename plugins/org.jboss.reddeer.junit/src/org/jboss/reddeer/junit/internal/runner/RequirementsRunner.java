package org.jboss.reddeer.junit.internal.runner;

import java.util.List;

import org.jboss.reddeer.junit.extensionpoint.IBeforeTest;
import org.jboss.reddeer.junit.internal.requirement.Requirements;
import org.jboss.reddeer.junit.internal.requirement.inject.RequirementsInjector;
import org.jboss.reddeer.junit.internal.runlisteners.LoggingRunListener;
import org.jboss.reddeer.junit.internal.runlisteners.ScreenCastingRunListener;
import org.jboss.reddeer.junit.internal.screenshot.CaptureScreenshot;
import org.jboss.reddeer.junit.internal.screenshot.CaptureScreenshotException;
import org.jboss.reddeer.junit.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Fulfills the requirements before {@link BeforeClass} is called and
 * injects requirements to proper injection points 
 * 
 * @author Lucia Jelinkova, Vlado Pakan
 *
 */
public class RequirementsRunner extends BlockJUnit4ClassRunner {
	
	private static final Logger log = Logger.getLogger(RequirementsRunner.class);
	
	private Requirements requirements;
	
	private RunListener[] runListeners;

	private RequirementsInjector requirementsInjector = new RequirementsInjector();
	
	private String configId;
	
	private List<IBeforeTest> beforeTestExtensions;
	
	public RequirementsRunner(Class<?> clazz, Requirements requirements, String configId, RunListener[] runListeners,List<IBeforeTest> beforeTestExtensions) throws InitializationError {
		super(clazz);
		this.requirements = requirements;
		this.configId = configId;
		this.runListeners = runListeners;
		this.beforeTestExtensions = beforeTestExtensions;
	}

	public RequirementsRunner(Class<?> clazz, Requirements requirements, String configId) throws InitializationError {
		this(clazz,requirements,configId,null,null);
	}

	@Override
	protected Statement withBeforeClasses(Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(BeforeClass.class);
        Statement s = befores.isEmpty() ? statement : new RunBefores(statement, befores, null);
		runBeforeTest();
		return new FulfillRequirementsStatement(requirements, s);
	}
	
	@Override
	protected Object createTest() throws Exception {
		Object testInstance = super.createTest();
		log.debug("Injecting fulfilled requirements into test instance");
		requirementsInjector.inject(testInstance, requirements);
		return testInstance;
	}
	
	@Override
	protected String testName(FrameworkMethod method) {
		return method.getName()+" "+configId;
	}
	
	@Override
	public void run(RunNotifier arg0) {
		LoggingRunListener loggingRunListener = new LoggingRunListener();
		ScreenCastingRunListener screenCastingRunListener = new ScreenCastingRunListener();
		arg0.addListener(loggingRunListener);
		arg0.addListener(screenCastingRunListener);
		if (runListeners != null){
			for (RunListener listener : runListeners){
				arg0.addListener(listener);
			}
		}
		super.run(arg0);
		if (runListeners != null){
			for (RunListener listener : runListeners){
				arg0.removeListener(listener);
			}
		}
		arg0.removeListener(screenCastingRunListener);
		arg0.removeListener(loggingRunListener);
	}
	public void setRequirementsInjector(RequirementsInjector requirementsInjector) {
		this.requirementsInjector = requirementsInjector;
	}

	/**
	 * Method is called before test is run.
	 * Manages org.jbossreddeer.junit.before.test extensions
	 */
	private void runBeforeTest() {
		for (IBeforeTest beforeTestExtension : beforeTestExtensions){
			if (beforeTestExtension.hasToRun()){
				log.debug("Run method runBeforeTest() of class " + beforeTestExtension.getClass().getCanonicalName());
				beforeTestExtension.runBeforeTest();
			}
		}
	}
	
	private class InvokeMethodWithException extends Statement {
	    private final FrameworkMethod fTestMethod;
	    private Object fTarget;

	    public InvokeMethodWithException(FrameworkMethod testMethod, Object target) {
	        fTestMethod = testMethod;
	        fTarget = target;
	    }

	    @Override
	    public void evaluate() throws Throwable {
	    	try{
	    		fTestMethod.invokeExplosively(fTarget);	
	    	} catch (Throwable t){
	    		Test annotation = (Test) fTestMethod.getAnnotations()[0];
	    		if (annotation.expected().getName().equals("org.junit.Test$None") ||
	    			!annotation.expected().getName().equals(t.getClass().getName())) {
		    			CaptureScreenshot screenshot = new CaptureScreenshot();
		    			try {
		    				screenshot.captureScreenshot(fTarget.getClass().getCanonicalName() + "-" + fTestMethod.getName());	    			
		    			} catch (CaptureScreenshotException ex) {
		    				ex.printInfo(log);
		    			}
	    		}	
	    		throw t;
	    	}
	        
	    }
	}
	
	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
	    return new InvokeMethodWithException(method, test);
	}
	
	@Override
	protected Statement withAfters(FrameworkMethod method, Object target,
            Statement statement) {
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters,
                target);
    }
		
	@Override
	protected Statement withBefores(FrameworkMethod method, Object target,
            Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(
                Before.class);
        return befores.isEmpty() ? statement : new RunBefores(statement,
                befores, target);
    }
	
	@Override
	 protected Statement withAfterClasses(Statement statement) {
        List<FrameworkMethod> afters = getTestClass()
                .getAnnotatedMethods(AfterClass.class);
        return afters.isEmpty() ? statement :
                new RunAfters(statement, afters, null);
    }

}
