/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.modules.cl;

import java.util.List;

import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.HrefGenerator;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * TODO: bja Class Description for ChecklistMultiSelectColumnDescriptor
 * 
 * <P>
 * Initial Date:  11.08.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistMultiSelectColumnDescriptor implements ColumnDescriptor {

	private Table table;
	private String headerKey;
	private int column;
	
	public ChecklistMultiSelectColumnDescriptor(String headerKey, int column) {
		this.headerKey = headerKey;
		this.column = column;
	}

	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		// add checkbox
		int currentPosInModel = table.getSortedRow(row);
		boolean checked = (Boolean) table.getTableDataModel().getValueAt(currentPosInModel, column);
		if(renderer == null) {
			// special case for table download
			if(checked) sb.append("x");
		} else {
			sb.append("<input type=\"checkbox\" name=\"tb_ms\" value=\"").append(currentPosInModel).append("\"");
			if(checked) sb.append(" checked=\"checked\"");
			sb.append(" disabled=\"disabled\"");
			sb.append(" />");
		}
	}

	public int compareTo(int rowa, int rowb) {
		boolean rowaChecked = (Boolean) table.getTableDataModel().getValueAt(rowa, column);
		boolean rowbChecked = (Boolean) table.getTableDataModel().getValueAt(rowb, column);
		if (rowaChecked && !rowbChecked) return -1;
		else if (!rowaChecked && rowbChecked) return 1;
		return 0;
	}

	public boolean equals(Object object) {
		if (object instanceof ChecklistMultiSelectColumnDescriptor)
			return true;
		return false;
	}
	
	public String getHeaderKey() {
		return this.headerKey;
	}
	
	public boolean translateHeaderKey() {
		return false;
	}

	public int getAlignment() {
		return ColumnDescriptor.ALIGNMENT_CENTER;
	}

	public String getAction(int row) {
		// no action
		return null;
	}

	public HrefGenerator getHrefGenerator() {
		// no HrefGenerator
		return null;
	}

	public String getPopUpWindowAttributes() {
		// no PopuWindow
		return null;
	}

	public boolean isPopUpWindowAction() {
		return false;
	}

	public boolean isSortingAllowed() {
		return true;
	}

	public void modelChanged() {
		// nothing to do here
	}

	public void otherColumnDescriptorSorted() {
		// nothing to do here
	}

	public void setHrefGenerator(HrefGenerator h) {
		throw new AssertException("Not allowed to set HrefGenerator on MultiSelectColumn.");
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public void sortingAboutToStart() {
		// nothing to do here
	}

	public String toString(int rowid) {
		//return table.getMultiSelectSelectedRows().get(rowid) ? "checked" : "unchecked";
		return "checked";
	}

}