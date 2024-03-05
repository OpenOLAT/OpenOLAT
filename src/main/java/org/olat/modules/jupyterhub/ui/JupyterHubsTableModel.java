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
package org.olat.modules.jupyterhub.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubsTableModel extends DefaultFlexiTableDataModel<JupyterHubRow> implements SortableFlexiTableDataModel<JupyterHubRow> {

	private static final JupyterHubCols[] COLS = JupyterHubCols.values();
	private final Locale locale;

	public JupyterHubsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		JupyterHubRow hubRow = getObject(row);
		return getValueAt(hubRow, col);
	}

	@Override
	public Object getValueAt(JupyterHubRow row, int col) {
		switch (COLS[col]) {
			case name -> { return row.getName(); }
			case status -> { return row.getStatus(); }
			case clientId -> { return row.getClientId(); }
			case ram -> {
				if (StringHelper.containsNonWhitespace(row.getRamGuarantee()) && StringHelper.containsNonWhitespace(row.getRamLimit())) {
					return row.getRamGuarantee() + " / " + row.getRamLimit();
				}
				return "";
			}
			case cpu -> {
				if (row.getCpuGuarantee() != null && row.getCpuLimit() != null) {
					return row.getCpuGuarantee().stripTrailingZeros().toPlainString() + " / " + row.getCpuLimit().stripTrailingZeros().toPlainString();
				}
				return "";
			}
			case applications -> { return row.getNumberOfApplications(); }
			case tools -> { return row.getToolLink(); }
			default -> { return "ERROR"; }
		}
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<JupyterHubRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	public enum JupyterHubCols implements FlexiSortableColumnDef {
		name("table.header.hub.name"),
		status("table.header.hub.status"),
		clientId("table.header.hub.clientId"),
		ram("table.header.hub.ram"),
		cpu("table.header.hub.cpu"),
		applications("table.header.hub.applications"),
		tools("table.header.actions");

		private final String i18nHeaderKey;

		JupyterHubCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
