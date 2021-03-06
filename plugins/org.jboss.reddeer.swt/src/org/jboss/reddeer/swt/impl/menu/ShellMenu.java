/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.reddeer.swt.impl.menu;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.dialogs.AboutDialog;
import org.hamcrest.Matcher;
import org.jboss.reddeer.common.logging.Logger;
import org.jboss.reddeer.common.platform.RunningPlatform;
import org.jboss.reddeer.swt.api.Menu;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.core.handler.WidgetHandler;
import org.jboss.reddeer.core.lookup.ShellLookup;
import org.jboss.reddeer.core.matcher.WithMnemonicTextMatchers;
import org.jboss.reddeer.core.util.Display;

/**
 * Shell menu implementation
 * @author Jiri Peterka
 *
 */
@SuppressWarnings("restriction")
public class ShellMenu extends AbstractMenu implements Menu {
	
	private static final Logger log = Logger.getLogger(ShellMenu.class);
	private boolean isSubmenuOfMacEclipseMenu = false;
	private MacEclipseMenuCommand macEclipseMenuCommand = null;

	
	private enum MacEclipseMenuCommand {
		PREFERENCES("Preferences"), ABOUT("About");
		public String text;

		MacEclipseMenuCommand(String text) {
			this.text = text;
		}
		
	}
	
	/**
	 * Uses WithMnemonicMatcher to match menu item label. It means that all ampersands
	 * and shortcuts within menu item label are ignored when searching for menu
	 *
	 * @param path the path
	 */
	public ShellMenu(final String... path) {
		this(new WithMnemonicTextMatchers(path).getMatchers());
	}
	
	
	/**
	 * Instantiates a new shell menu.
	 *
	 * @param matchers the matchers
	 */
	@SuppressWarnings("unchecked")
	public ShellMenu(final Matcher<String>... matchers) {
		this.matchers = matchers;
		setMacOsMenuProperties();
		if (!isSubmenuOfMacEclipseMenu) {
			menuItem = ml.lookFor(ml.getActiveShellTopMenuItems(), matchers);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.reddeer.swt.impl.menu.AbstractMenu#select()
	 */
	@Override
	public void select() {
		log.info("Select shell menu with text " + getText());
		if (!isSubmenuOfMacEclipseMenu){
			mh.select(menuItem);
		} else {
			if (macEclipseMenuCommand.equals(MacEclipseMenuCommand.PREFERENCES)){
				log.debug("Running MacOS, open preferences dialog programatically");
				openPreferencesDialog();
			}else if (macEclipseMenuCommand.equals(MacEclipseMenuCommand.ABOUT)){
				log.debug("Running MacOS, open about dialog programatically");
				openAboutDialog();
			} else {
				throw new SWTLayerException("Unsupported Mac Eclispe menu command: " + macEclipseMenuCommand);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.reddeer.swt.impl.menu.AbstractMenu#isSelected()
	 */
	@Override
	public boolean isSelected() {
		if(menuItem != null){
			return mh.isSelected(menuItem);
		} else {
			return false;
		}
	}
	

	private void openAboutDialog() {
		Display.asyncExec(new Runnable() {
			@Override
			public void run() {
				 new AboutDialog(ShellLookup.getInstance().getActiveShell()).open();
			}
		});
		Display.syncExec(new Runnable() {
			@Override
			public void run() {
			}
		});
	}

	private void openPreferencesDialog() {
		Display.asyncExec(new Runnable() {
			@Override
			public void run() {
				 PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, null, null, null);
				 dialog.open();
			}
		});
		Display.syncExec(new Runnable() {
			@Override
			public void run() {
				// do nothing just process UI events
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.jboss.reddeer.swt.impl.menu.AbstractMenu#getText()
	 */
	@Override
	public String getText() {
		if (!isSubmenuOfMacEclipseMenu){
			if (menuItem == null) {
				menuItem = ml.lookFor(ml.getActiveShellTopMenuItems(), matchers);
			}
			String text = mh.getMenuItemText(menuItem);
			return text;
		}
		else{
			return "&" + macEclipseMenuCommand.text;
		}
	}
	
	private void setMacOsMenuProperties(){
		isSubmenuOfMacEclipseMenu = false;
		macEclipseMenuCommand = null;
		if (RunningPlatform.isOSX() && 
			matchers.length == 2){
			if (matchers[0].matches("Window") && matchers[1].matches(MacEclipseMenuCommand.PREFERENCES.text)){
				isSubmenuOfMacEclipseMenu = true;
				macEclipseMenuCommand = MacEclipseMenuCommand.PREFERENCES;
			}else if(matchers[0].matches("Help") && matchers[1].matches(MacEclipseMenuCommand.ABOUT.text)){
				isSubmenuOfMacEclipseMenu = true;
				macEclipseMenuCommand = MacEclipseMenuCommand.ABOUT;				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.reddeer.swt.api.Menu#getSWTWidget()
	 */
	public MenuItem getSWTWidget(){
		return menuItem;
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.reddeer.swt.widgets.Widget#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return WidgetHandler.getInstance().isEnabled(menuItem);
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.reddeer.swt.widgets.Widget#isDisposed()
	 */
	@Override
	public boolean isDisposed() {
		return WidgetHandler.getInstance().isDisposed(menuItem);
	}
}
