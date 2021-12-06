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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.vfs.ui.management;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class VFSOverviewTableModel extends DefaultFlexiTableDataModel<VFSOverviewTableContentRow> implements FlexiTableFooterModel{

	private VFSOverviewTableFooterRow footerRow;

	public VFSOverviewTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		VFSOverviewTableContentRow stat = getObject(row);
		return getValueAt(stat, col);
	}

	public Object getValueAt(VFSOverviewTableContentRow row, int col) {
		switch(VFSOverviewColumns.values()[col]) {
		case name: return notNull(row.getName());
		case size: return notNull(row.getSize());
		case amount: return notNull(row.getAmount());
		case action: return notNull(row.getAction());

		default: return "ERROR";
		}
	}
	
	public Object getValueAt(VFSOverviewTableFooterRow row, int col) {
		switch(VFSOverviewColumns.values()[col]) {
		case name: return notNull(row.getName());
		case size: return notNull(row.getSize());
		case amount: return notNull(row.getAmount());
		case action: return notNull(row.getAction());

		default: return "ERROR";
		}
	}

	private Object notNull(Object o) {
		return o != null ? o : "";
	}

	public String getFooterHeader() {
		return "<i class=\"o_icon o_icon-fw o_icon_origin\"></i> " + "Total";
	}

	public Object getFooterValueAt(int col) {
		if (col > 0) {
			return getValueAt(footerRow, col);
		}
		return null;
	}

	public enum VFSOverviewColumns implements FlexiColumnDef {
		name("vfs.overview.name"),
		size("vfs.overview.size"),
		amount("vfs.overview.amount"),
		action("vfs.overview.action");

		private final String i18nHeaderKey;

		private VFSOverviewColumns(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
	
	public void setFooter(VFSOverviewTableFooterRow footerRow) {
		this.footerRow = footerRow;
	}
}
