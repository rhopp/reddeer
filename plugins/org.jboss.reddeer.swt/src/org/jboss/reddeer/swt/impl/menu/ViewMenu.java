package org.jboss.reddeer.swt.impl.menu;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.swt.widgets.MenuItem;
import org.hamcrest.Matcher;
import org.jboss.reddeer.swt.handler.ActionContributionItemHandler;
import org.jboss.reddeer.swt.matcher.WithMnemonicTextMatchers;

public class ViewMenu extends AbstractMenu {

	private ActionContributionItem item;
	
	public ViewMenu(String... path){
		this(new WithMnemonicTextMatchers(path).getMatchers());
	}
	
	public ViewMenu(Matcher<String>... matchers){
		item = ml.lookFor(ml.getToolbarMenus(), matchers);
		this.matchers = matchers;
	}

	@Override
	public void select() {
		mh.select(item);
	}
	
	@Override
	public boolean isSelected() {
		return false;
	}

	@Override
	public String getText() {
		return item.getAction().getText().replace("&", "");
	}
	
	public MenuItem getSWTWidget(){
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return ActionContributionItemHandler.getInstance().isEnabled(item);
	}

}
