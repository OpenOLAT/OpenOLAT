package org.olat.admin.user;

import org.olat.core.gui.components.tabbedpane.Tab;

import java.util.List;

/**
 * @author Martin Schraner
 */
public class UserAdminControllerAdditionalTabs {

	private final List<Tab> tabs;

	public UserAdminControllerAdditionalTabs(List<Tab> tabs) {
		this.tabs = tabs;
	}

	public List<Tab> getTabs() {
		return tabs;
	}
}
