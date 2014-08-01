package org.jboss.reddeer.swt.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.reddeer.common.logging.Logger;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.jboss.reddeer.swt.condition.ShellWithTextIsActive;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.handler.WidgetHandler;
import org.jboss.reddeer.swt.util.Display;
import org.jboss.reddeer.swt.util.ResultRunnable;
import org.jboss.reddeer.swt.wait.TimePeriod;
import org.jboss.reddeer.swt.wait.WaitWhile;

/**
 * Menu lookup provides menu and contextmenu routines for menuitems lookup Works
 * also with dynamic menus.
 * 
 * @author Jiri Peterka
 * @author Rastislav Wagner
 * 
 */
public class MenuLookup {

	private Logger log = Logger.getLogger(this.getClass());
	private static MenuLookup instance = null;
	
	private MenuLookup() { }
	
	public static MenuLookup getInstance() {
		if (instance == null) {
			instance = new MenuLookup();
		}
		return instance;
	}

	/**
	 * Provide lookup for ToolBar menu items.
	 * 
	 * @return list of contribution items related to active view menu.
	 * 
	 */

	public List<IContributionItem> getViewMenus() {
		IWorkbenchPart part = getActivePart(false);

		List<IContributionItem> menuContributionItems = new ArrayList<IContributionItem>();
		IMenuManager m = null;
		try {
			m = ((IViewSite) part.getSite()).getActionBars().getMenuManager();
		} catch (ClassCastException e) {
			throw new SWTLayerException(
					"Trying to found ViewMenu when no view is active", e);
		}
		if (m instanceof MenuManager) {
			menuContributionItems.addAll(Arrays.asList(((MenuManager) m)
					.getItems()));
		}
		if (menuContributionItems.isEmpty()) {
			throw new SWTLayerException("No Menu found in view");
		}
		return menuContributionItems;
	}

	/**
	 * Provide lookup for ToolBar menu items.
	 * 
	 * @return list of MenuManager instances related to toolbar menus
	 * @deprecated since 0.6 use {@link #getViewMenus()}
	 * 
	 */
	public List<IContributionItem> getToolbarMenus(){	
		IWorkbenchPart part = getActivePart(false);
		
		List<IContributionItem> menuContributionItems = new ArrayList<IContributionItem>();
		IMenuManager m = ((IViewSite) part.getSite()).getActionBars().getMenuManager();
		if (m instanceof MenuManager) {
			menuContributionItems.addAll(Arrays.asList(((MenuManager) m).getItems()));
		}
		if(menuContributionItems.isEmpty()){
			throw new SWTLayerException("No Menu found in toolbar");
		}
		return menuContributionItems;
	}
	
	/**
	 * Look for ActionContributionItem matching matchers.
	 * @param cintItems items which will be matched with matchers
	 * @param matchers menuitem text matchers
	 * @return final ActionContibutionItem
	 */
	public ActionContributionItem lookFor(final List<IContributionItem> contItems, final Matcher<String>... matchers) {	
		ActionContributionItem contItem = Display.syncExec(new ResultRunnable<ActionContributionItem>(){

			@Override
			public ActionContributionItem run() {
				ActionContributionItem currentItem = null;
				List<IContributionItem> currentMenuContributionItems = contItems;
				for (Matcher<String> m : matchers) {
					currentItem = null;
					for (IContributionItem i : currentMenuContributionItems) {
						if(i instanceof ActionContributionItem){
							String normalized = ((ActionContributionItem)i).getAction().getText().replace("&", "");
							log.debug("Found item:" + normalized);
							if (m.matches(normalized)) {
								log.info("Item match:" + normalized);
								currentItem =(ActionContributionItem)i;
								break;
							} 
						} else if(i instanceof MenuManager){
							String normalized =((MenuManager)i).getMenuText().replace("&", "");
							log.debug("Found Menu Manager:" + normalized);
							if (m.matches(normalized)) {
								log.debug("Menu Manager match:" + normalized);
								currentMenuContributionItems = Arrays.asList(((MenuManager) i).getItems());
							}
						}
					}
			
				}
				return currentItem;
			}
		});
		return contItem;
	}

	
	/**
	 * Returns ContributionItems from focused control.
	 * Use if menu can contain dynamic menu from e4
	 */
	public List<IContributionItem> getMenuContributionItems() {
		List<IContributionItem> contItems = new ArrayList<IContributionItem>();
		final Control control  = WidgetLookup.getInstance().getFocusControl();
		final Menu menu = getControlMenu(control);
		
		contItems = Display.syncExec(new ResultRunnable<List<IContributionItem>>() {
			@Override
			public List<IContributionItem> run() {
				List<IContributionItem> contItemsRun = new ArrayList<IContributionItem>();
				sendHide(menu, true);
				sendShowUI(menu);
				if(menu.getData() != null && menu.getData() instanceof MenuManager){
					contItemsRun.addAll(Arrays.asList(((MenuManager)menu.getData()).getItems()));
					log.info("Menu manager found");
				} else {
					log.info("Menu manager not found");
				}
	
				return contItemsRun;
			}
		});
		return contItems;
	}
	
	

	/**
	 * Look for MenuItem matching matchers starting topLevel menuItems.
	 * @param topItems top level MenuItem[]
	 * @param matchers menuitem text matchers
	 * @return final MenuItem
	 */
	public MenuItem lookFor(MenuItem[] topItems, Matcher<String>... matchers) {		
		MenuItem lastMenuItem = getMatchingMenuPath(topItems, matchers);
		if (lastMenuItem == null) {
			throw new SWTLayerException("No menu item matching specified path found");
		}
		return lastMenuItem;
	}
	

	/**
	 * Returns top level menuitems from focused controls.
	 * Does not work with dynamic menus from e4 @see MenuLookup.getMenuContributionItems()
	 *
	 * @return
	 */
	public MenuItem[] getTopMenuMenuItemsFromFocus() {

		final Control control  = WidgetLookup.getInstance().getFocusControl();
		if (control == null) {
			throw new SWTLayerException(
					"No control has focus. Perhaps something has stolen it? Try to regain focus with for example \"new DefaultShell()\".");
		}
		MenuItem[] items = null;
		final Menu menu = getControlMenu(control);
		
		items = Display.syncExec(new ResultRunnable<MenuItem[]>() {
			@Override
			public MenuItem[] run() {
				sendHide(menu, true);
				sendShowUI(menu);				
				return menu.getItems();
			}
		});

		if (items == null) {
			throw new SWTLayerException(
					"Could not find top menu items, menu doesn't exist or wrong focus");
		}

		return items;
	}
	
	/**
	 * Returns menuitems from active shell menubar.
	 * @return top menuitems of active shell
	 */
	public MenuItem[] getActiveShellTopMenuItems() {
		Shell activeShell = ShellLookup.getInstance().getActiveShell();
		if(activeShell == null){
			throw new SWTLayerException("Cannot find menu bar because there's no active shell");
		}
		String activeShellText = WidgetHandler.getInstance().getText(activeShell);
		MenuItem[] result = null;
		try{
			result = getMenuBarItems(activeShell);	
		} catch (SWTLayerException swtle) {
			// there is a chance that some non expected shell was opened
			// e.g. Progress Dialog
			new WaitWhile(new ShellWithTextIsActive(activeShellText),TimePeriod.NORMAL,false);
			activeShell = ShellLookup.getInstance().getActiveShell();
			if (activeShellText.equals(WidgetHandler.getInstance().getText(activeShell))){
				result = getMenuBarItems(activeShell);
			} else{
				throw swtle;
			}
		}		
		return result;		
	}
	
	/**
	 * Returns menubar items.
	 * @param s given shell where menubar items are searched for
	 * @return array of menuitems fo given shell 
	 */
	private MenuItem[] getMenuBarItems(final Shell s) {

		MenuItem[] items = Display.syncExec(new ResultRunnable<MenuItem[]>() {

			@Override
			public MenuItem[] run() {
				log.info("Getting Menu Bar of shell " + s.getText());
				Menu menu = s.getMenuBar();
				if (menu == null){
					return null;
				}
				MenuItem[] items = menu.getItems();
				return items;
			}
		});
		if(items == null){
			String shellText = WidgetHandler.getInstance().getText(s);
			throw new SWTLayerException("Cannot find a menu bar of shell " + shellText);
		}
		return items;
	}

	/**
	 * Returns Menu of the given control.
	 * @param c given control under which menu is located
	 * @return menu under given control
	 */
	private Menu getControlMenu(final Control c) {

		Menu menu = Display.syncExec(new ResultRunnable<Menu>() {

			@Override
			public Menu run() {
				Menu m = c.getMenu();
				return m;
			}
		});

		if (menu == null) {
			throw new SWTLayerException(
					c.getClass() +" Has no menu");
		}

		return menu;	
	}
	
	/**
	 * Goes through menus path and returns matching menu.
	 * @param topItems menuitems for further searches
	 * @param matchers given matchers
	 * @return matching MenuItem
	 */
	private MenuItem getMatchingMenuPath(final MenuItem[] topItems,
			final Matcher<String>... matchers) {
		MenuItem i = Display.syncExec(new ResultRunnable<MenuItem>() {

			@Override
			public MenuItem run() {
				Menu currentMenu = null;
				MenuItem currentItem = null;;
				MenuItem[] menuItems = topItems;
				for (Matcher<String> m : matchers) {
					currentItem = null;
					for (MenuItem i : menuItems) {
						String normalized = i.getText().replace("&", "");
						log.debug("Found menu:" + normalized);
						if (m.matches(normalized)) {
							log.info("Item match:" + normalized);
							currentItem = i;
							currentMenu = i.getMenu();
							break;
						} 
					}
					if (currentItem == null){
						return null;
					}
					if (m != matchers[matchers.length-1]) {
						currentMenu = currentItem.getMenu();
						sendShowUI(currentMenu);
						menuItems = currentMenu.getItems();
					} 
				}
				return currentItem;
			}
		});
		return i;
	}


	/**
	 * Sends SWT.Show on widget.
	 * @param widget given Widget
	 */
	private void sendShowUI(Widget widget) {
		widget.notifyListeners(SWT.Show, new Event());
	}
		

	/**
	 * Hides menu.
	 * 
	 * @param menu given Menu
	 * @param recur recursion flag
	 */
	private void sendHide(final Menu menu, final boolean recur) {
		Display.syncExec(new Runnable() {

			@Override
			public void run() {

				if (menu != null) {
					menu.notifyListeners(SWT.Hide, new Event());
					if (recur) {
						if (menu.getParentMenu() != null) {
							sendHide(menu.getParentMenu(), recur);
						} else {
							menu.setVisible(false);
						}
					}
				}
			}

		});

	}

	/**
	 * Returns active Workbench Part.
	 * @param restore tries to restore the Part if true
	 * @return active WorkbenchPart
	 */
	private IWorkbenchPart getActivePart(final boolean restore) {
		IWorkbenchPart result = Display.syncExec(new ResultRunnable<IWorkbenchPart>() {

			@Override
			public IWorkbenchPart run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				IWorkbenchPartReference activePartReference = activePage.getActivePartReference();
				IWorkbenchPart part = activePartReference.getPart(restore);
				return part;
			}		
		});
		return result;		
	}

}
