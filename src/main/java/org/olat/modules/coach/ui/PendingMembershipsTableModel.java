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
package org.olat.modules.coach.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-10-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipsTableModel extends DefaultFlexiTableDataModel<PendingMembershipRow> 
		implements SortableFlexiTableDataModel<PendingMembershipRow> {

	private static final PendingMembershipsCols[] COLS = PendingMembershipsCols.values();
	private final UserManager userManager;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final Locale locale;

	public PendingMembershipsTableModel(UserManager userManager, List<UserPropertyHandler> userPropertyHandlers, FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.userManager = userManager;
		this.userPropertyHandlers = userPropertyHandlers;
		this.locale = locale;
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<PendingMembershipRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		PendingMembershipRow rowObj = getObject(row);
		return getValueAt(rowObj, col);
	}

	@Override
	public Object getValueAt(PendingMembershipRow row, int col) {
		if (col >= 0 && col < COLS.length) {
			return switch (COLS[col]) {
				case title -> row.getTitle();
				case extRef -> row.getExtRef();
				case begin ->  row.getBegin();
				case end -> row.getEnd();
				case type -> row.getType();
				case confirmationUntil -> row.getConfirmationUntil();
			};
		}
		
		int propPos = col - PendingMembershipsController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum PendingMembershipsCols implements FlexiSortableColumnDef {
		title("table.header.title"),
		extRef("table.header.ext.ref"),
		begin("table.header.lifecycle.start"),
		end("table.header.lifecycle.end"),
		type("table.header.type"),
		confirmationUntil("table.header.confirmation.until");
		
		private final String i18nKey;
		
		private PendingMembershipsCols(String i18nKey) {
			this.i18nKey = i18nKey;
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
			return i18nKey;
		}
	}
}
