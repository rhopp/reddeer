package org.jboss.reddeer.eclipse.test.ui.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.jboss.reddeer.eclipse.condition.ConsoleHasLaunch;
import org.jboss.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.jboss.reddeer.eclipse.condition.ConsoleHasText;
import org.jboss.reddeer.eclipse.condition.ConsoleIsTerminated;
import org.jboss.reddeer.eclipse.jdt.ui.NewJavaClassWizardDialog;
import org.jboss.reddeer.eclipse.jdt.ui.NewJavaClassWizardPage;
import org.jboss.reddeer.eclipse.jdt.ui.ide.NewJavaProjectWizardDialog;
import org.jboss.reddeer.eclipse.jdt.ui.ide.NewJavaProjectWizardPage;
import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.PackageExplorer;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
import org.jboss.reddeer.eclipse.ui.perspectives.JavaPerspective;
import org.jboss.reddeer.eclipse.utils.DeleteUtils;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.jboss.reddeer.swt.api.StyledText;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.swt.impl.menu.ShellMenu;
import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
import org.jboss.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.jboss.reddeer.common.matcher.RegexMatcher;
import org.jboss.reddeer.core.matcher.WithTextMatchers;
import org.jboss.reddeer.common.wait.AbstractWait;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.workbench.impl.editor.DefaultEditor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RedDeerSuite.class)
@OpenPerspective(JavaPerspective.class)
public class ConsoleViewTest {

	private static ConsoleView consoleView;

	private static final String TEST_PROJECT_NAME = "Project";
	private static final String TEST_CLASS_NAME = "TestClass";
	private static final String TEST_CLASS_NAME1 = "TestClass1";
	private static final String TEST_CLASS_NAME2 = "TestClass2";
	private static final String TEST_CLASS_LOOP_NAME = "TestLoopClass";
	private static final String TEST_CLASS_LOOP2_NAME = "TestLoopClass2";

	@BeforeClass
	public static void setupClass() {
		createTestProject();
	}

	@AfterClass
	public static void tearDownClass() {
		PackageExplorer packageExplorer = new PackageExplorer();
		packageExplorer.open();
		DeleteUtils.forceProjectDeletion(packageExplorer.getProject(TEST_PROJECT_NAME),true);
	}

	private void runTestClassAndWaitToFinish() {
		runTestClass(TEST_CLASS_NAME);
		new WaitUntil(new ConsoleIsTerminated());
	}
	
	@Test
	public void testConsoleSwitching() {
		consoleView = new ConsoleView();
		consoleView.open();
		runTestClass(TEST_CLASS_NAME1);
		consoleView.toggleShowConsoleOnStandardOutChange(false);
		runTestClass(TEST_CLASS_NAME2);
		consoleView.switchConsole(new RegexMatcher(".*" + TEST_CLASS_NAME1
				+ ".*"));
		assertThat(consoleView.getConsoleText(),
				IsEqual.equalTo("Hello World1"));
		consoleView.switchConsole(new RegexMatcher(".*" + TEST_CLASS_NAME2
				+ ".*"));
		assertThat(consoleView.getConsoleText(),
				IsEqual.equalTo("Hello World2"));
		consoleView.toggleShowConsoleOnStandardOutChange(true);
	}

	@Test
	public void testConsoleView() {
		runTestClassAndWaitToFinish();
		testGettingConsoleTest();
		testClearConsole();
	}

	@Test
	public void testRemoveLaunch() {
		runTestClassAndWaitToFinish();
		consoleView = new ConsoleView();
		consoleView.open();
		new WaitUntil(new ConsoleHasLaunch());
		consoleView.removeLaunch();
		new WaitWhile(new ConsoleHasLaunch());
		assertFalse("Some launches remain" , consoleView.consoleHasLaunch());
	}

	@Test
	public void testRemoveAllTerminatedLaunches() {
		runTestClassAndWaitToFinish();
		consoleView = new ConsoleView();
		consoleView.open();
		new WaitUntil(new ConsoleHasLaunch());
		consoleView.removeAllTerminatedLaunches();
		assertFalse("Some launches remain" , consoleView.consoleHasLaunch());
	}

	@Test
	public void testTerminateConsole() {

		runTestClass(TEST_CLASS_LOOP_NAME);
		AbstractWait.sleep(TimePeriod.SHORT);

		consoleView = new ConsoleView();
		consoleView.open();
		consoleView.terminateConsole();

		String text = consoleView.getConsoleText();
		AbstractWait.sleep(TimePeriod.SHORT);
		String text2 = consoleView.getConsoleText();
		assertFalse(text.trim().isEmpty());
		assertEquals(text, text2);

		DefaultToolItem terminate = new DefaultToolItem("Terminate");
		assertFalse(terminate.isEnabled());

	}

	@Test
	public void consoleHasNoChangeTest() {
		runTestClass(TEST_CLASS_LOOP2_NAME);
		new WaitUntil(new ConsoleHasText("Start"));
		new WaitUntil(new ConsoleHasNoChange(TimePeriod.getCustom(11)), TimePeriod.LONG);
		consoleView.open();
		consoleView.terminateConsole();
		// compare the text without white spaces
		assertEquals("StartHelloApplication", consoleView.getConsoleText().replaceAll("\\s", ""));
	}
	
	@Test
	public void toggleShowConsoleOnStandardOutChange() {
		runTestClassAndWaitToFinish();
		consoleView = new ConsoleView();
		consoleView.open();
		consoleView.toggleShowConsoleOnStandardOutChange(true);
		consoleView.toggleShowConsoleOnStandardOutChange(false);
	}

	@After
	public void tearDown(){
		consoleView = new ConsoleView();
		consoleView.open();
		// clean up all launches
		while (consoleView.consoleHasLaunch()){
			consoleView.toggleShowConsoleOnStandardOutChange(true);
			consoleView.terminateConsole();
			consoleView.removeLaunch();
		}		
	}
	
	private void testGettingConsoleTest() {
		consoleView = new ConsoleView();
		consoleView.open();
		String text = consoleView.getConsoleText();
		assertThat(text, IsNull.notNullValue());
		assertThat(text, IsEqual.equalTo("Hello World"));
	}

	private void testClearConsole() {
		consoleView = new ConsoleView();
		consoleView.open();
		consoleView.clearConsole();
		String text = consoleView.getConsoleText();
		assertThat(text, IsEqual.equalTo(""));
	}

	private static void createTestProject() {
		PackageExplorer packageExplorer = new PackageExplorer();
		packageExplorer.open();
		if (!packageExplorer.containsProject(TEST_PROJECT_NAME)) {
			createJavaProject();
			createJavaClass(TEST_CLASS_NAME, "System.out.print(\"Hello World\");");
			createJavaClass(TEST_CLASS_LOOP_NAME, "int i = 0; while (true) {System.out.println(i++);}");
			createJavaClass(TEST_CLASS_LOOP2_NAME, "try {System.out.println(\"Start\");\n"
					+ "Thread.sleep(10 * 1000);\n" + "System.out.println(\"Hello Application\");\n"
					+ "Thread.sleep(20 * 1000);\n" + "System.out.println(\"Finish\");\n"
					+ "} catch (InterruptedException e) {e.printStackTrace();}");
			createJavaClass(
					TEST_CLASS_NAME1,
					"System.out.print(\"Hello World1\");\ntry {\nThread.sleep(10*1000);\n} catch (InterruptedException e) {e.printStackTrace();}");
			createJavaClass(
					TEST_CLASS_NAME2,
					"System.out.print(\"Hello World2\");\ntry {\nThread.sleep(10*1000);\n} catch (InterruptedException e) {e.printStackTrace();}");
		}
		packageExplorer.getProject(TEST_PROJECT_NAME).select();
	}

	private static void createJavaProject() {
		NewJavaProjectWizardDialog javaProject = new NewJavaProjectWizardDialog();
		javaProject.open();

		NewJavaProjectWizardPage javaWizardPage = new NewJavaProjectWizardPage();
		javaWizardPage.setProjectName(TEST_PROJECT_NAME);

		javaProject.finish(false);
	}

	private static void createJavaClass(String name, String text) {
		NewJavaClassWizardDialog javaClassDialog = new NewJavaClassWizardDialog();
		javaClassDialog.open();

		NewJavaClassWizardPage wizardPage = new NewJavaClassWizardPage();
		wizardPage.setName(name);
		wizardPage.setPackage("test");
		wizardPage.setStaticMainMethod(true);
		javaClassDialog.finish();

		StyledText dst = new DefaultStyledText();
		dst.insertText(7, 0, text);
		new DefaultEditor().save();
	}

	private static void runTestClass(String name) {
		PackageExplorer packageExplorer = new PackageExplorer();
		packageExplorer.open();
		packageExplorer.getProject(TEST_PROJECT_NAME).getProjectItem("src", "test", name + ".java").select();
		RegexMatcher[] array = { new RegexMatcher("Run.*"), new RegexMatcher("Run As.*"),
				new RegexMatcher(".*Java Application.*") };
		WithTextMatchers m = new WithTextMatchers(array);
		new ShellMenu(m.getMatchers()).select();
		new WaitWhile(new JobIsRunning());
	}
}
