package org.olat.core.commons.services.vfs.ui.management;

public class VFSOverviewTableFooterRow {
	private final String name;
	private final Long size;
	private final Long amount;
	private final String action;
	
	public VFSOverviewTableFooterRow(String name, Long amount, Long size, String action) {
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

	public String getAction() {
		return action;
	}
}
