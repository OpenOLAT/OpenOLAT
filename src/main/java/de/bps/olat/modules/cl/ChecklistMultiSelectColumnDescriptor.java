/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.modules.cl;

import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

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

	@Override
	public int getDataColumn() {
		return column;
	}

	@Override
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

	@Override
	public int compareTo(int rowa, int rowb) {
		boolean rowaChecked = (Boolean) table.getTableDataModel().getValueAt(rowa, column);
		boolean rowbChecked = (Boolean) table.getTableDataModel().getValueAt(rowb, column);
		if (rowaChecked && !rowbChecked) return -1;
		else if (!rowaChecked && rowbChecked) return 1;
		return 0;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ChecklistMultiSelectColumnDescriptor)
			return true;
		return false;
	}
	
	@Override
	public String getHeaderKey() {
		return this.headerKey;
	}
	
	@Override
	public boolean translateHeaderKey() {
		return false;
	}

	@Override
	public int getAlignment() {
		return ColumnDescriptor.ALIGNMENT_CENTER;
	}
	
	@Override
	public int getHeaderAlignment() {
		return ColumnDescriptor.ALIGNMENT_LEFT;
	}

	@Override
	public String getAction(int row) {
		// no action
		return null;
	}

	@Override
	public String getPopUpWindowAttributes() {
		// no PopuWindow
		return null;
	}

	@Override
	public boolean isPopUpWindowAction() {
		return false;
	}

	@Override
	public boolean isSortingAllowed() {
		return true;
	}

	@Override
	public void modelChanged() {
		// nothing to do here
	}

	@Override
	public void otherColumnDescriptorSorted() {
		// nothing to do here
	}

	@Override
	public void setTable(Table table) {
		this.table = table;
	}

	@Override
	public void sortingAboutToStart() {
		// nothing to do here
	}

	public String toString(int rowid) {
		//return table.getMultiSelectSelectedRows().get(rowid) ? "checked" : "unchecked";
		return "checked";
	}

}