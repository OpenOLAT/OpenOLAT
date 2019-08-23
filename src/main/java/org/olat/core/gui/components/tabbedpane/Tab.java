package org.olat.core.gui.components.tabbedpane;

import org.olat.core.gui.components.Component;

/**
 * @author Martin Schraner
 */
public class Tab implements Comparable<Tab> {

	private final String displayName;
	private final Component component;
	private final int order;

	public Tab(String displayName, Component component, int order) {
		this.displayName = displayName;
		this.component = component;
		this.order = order;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Component getComponent() {
		return component;
	}

	public int getOrder() {
		return order;
	}

	@Override
	public int compareTo(Tab otherTab) {
		return Integer.compare(order, otherTab.order);
	}
}
