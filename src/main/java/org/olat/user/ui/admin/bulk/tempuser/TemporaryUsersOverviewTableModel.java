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
package org.olat.user.ui.admin.bulk.tempuser;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 14 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TemporaryUsersOverviewTableModel extends DefaultFlexiTableDataModel<TemporaryUserRow> {
	
	private static final TransientIdentityCols[] COLS = TransientIdentityCols.values();
	
	public TemporaryUsersOverviewTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		TemporaryUserRow identityRow = getObject(row);
		TransientIdentity identity = identityRow.getIdentity();
		switch(COLS[col]) {
			case status: return identityRow;
			case username: return identity.getName();
			case firstname: return identity.getFirstName();
			case lastname: return identity.getLastName();
			case expiration: return identity.getExpirationDate();
			default: return "ERROR";
		}
	}

	@Override
	public DefaultFlexiTableDataModel<TemporaryUserRow> createCopyWithEmptyList() {
		return new TemporaryUsersOverviewTableModel(getTableColumnModel());
	}
	
	public enum TransientIdentityCols implements FlexiSortableColumnDef {
		status("table.header.status"),
		username("table.header.username"),
		firstname("table.header.firstname"),
		lastname("table.header.lastname"),
		expiration("table.header.expiration");
		
		private final String i18nKey;
		
		private TransientIdentityCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
