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
package org.olat.user.ui.admin.lifecycle;

import org.olat.basesecurity.model.DeletedIdentity;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 22 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedUsersTableModel extends DefaultFlexiTableDataSourceModel<DeletedIdentity> {
	
	private final UserManager userManager;
	
	public DeletedUsersTableModel(FlexiTableDataSourceDelegate<DeletedIdentity> source, UserManager userManager, FlexiTableColumnModel columnModel) {
		super(source, columnModel);
		this.userManager = userManager;
	}

	@Override
	public Object getValueAt(int row, int col) {
		DeletedIdentity identity = getObject(row);
		switch(DeletedCols.values()[col]) {
			case username: return identity.getIdentityName();
			case firstName: return identity.getIdentityFirstName();
			case lastName: return identity.getIdentityLastName();
			case deletedDate: return identity.getDeletedDate();
			case lastLogin: return identity.getLastLogin();
			case creationDate: return identity.getCreationDate();
			case deletedBy: return identity.getDeletedBy();
			case deletedRoles: return identity.getDeletedRoles();
			case clear: return StringHelper.containsNonWhitespace(identity.getIdentityFirstName())
					|| StringHelper.containsNonWhitespace(identity.getIdentityLastName());
		}
		return null;
	}

	@Override
	public DefaultFlexiTableDataSourceModel<DeletedIdentity> createCopyWithEmptyList() {
		return new DeletedUsersTableModel(getSourceDelegate(), userManager, getTableColumnModel());
	}
	
	public enum DeletedCols implements FlexiSortableColumnDef {
		username("table.identity.deleted.name"),
		firstName("table.name.firstName"),
		lastName("table.name.lastName"),
		deletedDate("table.identity.deleteddate"),
		lastLogin("table.identity.lastlogin"),
		creationDate("table.identity.creationdate"),
		deletedRoles("table.identity.deletedroles"),
		deletedBy("table.identity.deletedby"),
		clear("clear");

		private final String i18nKey;
		
		private DeletedCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return !name().equals(deletedRoles.name());
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
