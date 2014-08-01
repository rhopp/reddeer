package org.jboss.reddeer.swt.test.impl.menu;

import org.jboss.reddeer.eclipse.ui.problems.ProblemsView;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.swt.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.impl.button.CancelButton;
import org.jboss.reddeer.swt.impl.menu.ViewMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.test.utils.ShellTestUtils;
import org.jboss.reddeer.swt.wait.WaitUntil;
import org.jboss.reddeer.swt.wait.WaitWhile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RedDeerSuite.class)
public class ViewMenuTest {

	private static final String NEW_PROBLEMS_VIEW = "New Problems View";

	@Before
	public void setup() {
		new ProblemsView().open();
	}

	@After
	public void teardown() {
		ShellTestUtils.closeShell("shell");
	}

	@Test
	public void testViewMenuSuccess() {
		new ViewMenu("Show", "All Errors");
		new ViewMenu(NEW_PROBLEMS_VIEW).select();
		new DefaultShell("New Problems View");
		new WaitWhile(new ShellWithTextIsAvailable(NEW_PROBLEMS_VIEW));
		new CancelButton().click();
		new WaitUntil(new ShellWithTextIsAvailable(NEW_PROBLEMS_VIEW));
	}

	@Test(expected = SWTLayerException.class)
	public void testViewNotActive() {
		ShellTestUtils.createShell("shell");
		new ViewMenu("New Problems View");
	}

	@Test(expected = SWTLayerException.class)
	public void testNotExistingMenu() {
		new ViewMenu("Not existing menu");
	}

}
