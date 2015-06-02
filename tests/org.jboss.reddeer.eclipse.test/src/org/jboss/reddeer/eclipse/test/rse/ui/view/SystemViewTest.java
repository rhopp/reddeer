package org.jboss.reddeer.eclipse.test.rse.ui.view;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.jboss.reddeer.eclipse.exception.EclipseLayerException;
import org.jboss.reddeer.eclipse.rse.ui.view.System;
import org.jboss.reddeer.eclipse.rse.ui.wizard.NewConnectionWizardDialog;
import org.jboss.reddeer.eclipse.rse.ui.wizard.NewConnectionWizardSelectionPage.SystemType;
import org.jboss.reddeer.eclipse.ui.views.log.LogView;
import org.jboss.reddeer.swt.api.Shell;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.junit.BeforeClass;
import org.junit.Test;

public class SystemViewTest extends SystemViewTestCase {
	
	private static final String SYSTEM_A = "Test Remote System A";
	private static final String SYSTEM_B = "Test Remote System B";

	@BeforeClass
	public static void setupClass(){
		LogView logView = new LogView();
		if (logView.isOpened()){
			new LogView().close();
		}
	}
	
	@Test
	public void newConnection(){
		wizardDialog = remoteSystemView.newConnection();
		
		Shell shell = new DefaultShell();
		assertThat(shell.getText(), is(NewConnectionWizardDialog.TITLE));
	}
	
	@Test
	public void getSystems_noRemoteSystem(){
		
		List<System> systems = remoteSystemView.getSystems();
		assertThat(systems.size(), is(1)); //only predefined local system
	}
	
	@Test
	public void getSystems(){
		createSystem("localhost", SYSTEM_A, SystemType.SSH_ONLY);
		createSystem("127.0.0.1", SYSTEM_B, SystemType.SSH_ONLY);
		
		List<System> systems = remoteSystemView.getSystems();
		assertThat(systems.size(), is(3)); //+ predefined local
		assertThat(systems.get(1).getLabel(), is(SYSTEM_A));
		assertThat(systems.get(2).getLabel(), is(SYSTEM_B));
	}
	
	@Test(expected=EclipseLayerException.class)
	public void getSystem_noRemoteSystem(){
		remoteSystemView.getSystem("NO Remote System");
	}
	
	@Test(expected=EclipseLayerException.class)
	public void getSystem_notFound(){
		createSystem("127.0.0.1", SYSTEM_A, SystemType.SSH_ONLY);
		remoteSystemView.getSystem("No Remote System");
	}
	
	@Test
	public void getSystem(){
		createSystem("localhost", SYSTEM_A, SystemType.SSH_ONLY);
		createSystem("127.0.0.1", SYSTEM_B, SystemType.SSH_ONLY);

		System system = remoteSystemView.getSystem(SYSTEM_A);
		assertNotNull(system);
		assertThat(system.getLabel(), is(SYSTEM_A));
	}
	
}
