package org.olat.core.commons.services.vfs.ui.management;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

public class VFSOverviewTableContentRow {
	private final String name;
	private final Long size;
	private final Long amount;
	private final FormLink action;
	
	public VFSOverviewTableContentRow(String name, Long amount, Long size, FormLink action) {
		this.name = name;
		this.amount = amount;
		this.size = size;
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public Long getSize() {
		return size;
	}

	public Long getAmount() {
		return amount;
	}

	public FormLink getAction() {
		return action;
	}
}
