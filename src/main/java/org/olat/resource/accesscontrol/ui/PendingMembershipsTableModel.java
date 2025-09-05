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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * Initial date: 2025-09-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipsTableModel extends DefaultFlexiTableDataModel<PendingMembershipRow> 
		implements SortableFlexiTableDataModel<PendingMembershipRow> {
	
	private static final PendingMembershipCol[] COLS = PendingMembershipCol.values();

	public PendingMembershipsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		PendingMembershipRow pendingMembership = getObject(row);
		return getValueAt(pendingMembership, col);
	}
	
	@Override
	public Object getValueAt(PendingMembershipRow row, int col) {
		return switch (COLS[col]) {
			case title -> row.getTitle();
			case extRef ->  row.getExtRef();
			case begin -> row.getBegin();
			case end -> row.getEnd();
			case type -> row.getType();
			case confirmationUntil -> row.getConfirmationUntil();
			case tools -> row.getToolsLink();
		};
	}
	
	public enum PendingMembershipCol implements FlexiSortableColumnDef {
		title("table.header.title"),
		extRef("table.header.ext.ref"),
		begin("table.header.begin"),
		end("table.header.end"),
		type("table.header.type"),
		confirmationUntil("table.header.confirmation.until"),
		tools("table.header.tools");
		
		private final String i18nKey;

		PendingMembershipCol(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
