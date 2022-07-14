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
package org.olat.modules.invitation.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationListTableModel extends DefaultFlexiTableDataModel<InvitationRow>
implements SortableFlexiTableDataModel<InvitationRow> {
	
	private static final InvitationCols[] COLS = InvitationCols.values();
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public InvitationListTableModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<InvitationRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		InvitationRow invitation = getObject(row);
		return getValueAt(invitation, col);
	}

	@Override
	public Object getValueAt(InvitationRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case invitationDate: return row.getInvitationDate();
				case invitationLink: return row.getUrlLink();
				default: return "ERROR";
			}
		}
		
		int propPos = col - InvitationListController.USER_PROPS_OFFSET;
		UserPropertyHandler userProp = userPropertyHandlers.get(propPos);
		String val = row.getIdentity().getUser().getProperty(userProp.getName(), locale);
		if(!StringHelper.containsNonWhitespace(val)) {
			switch(userProp.getName()) {
				case UserConstants.EMAIL: val = row.getInvitation().getMail(); break;
				case UserConstants.FIRSTNAME: val = row.getInvitation().getFirstName(); break;
				case UserConstants.LASTNAME: val = row.getInvitation().getLastName(); break;
				default: break;
			}
		}
		return val;
	}
	
	public enum  InvitationCols implements FlexiSortableColumnDef {
		
		invitationDate("table.header.invitation"),
		invitationLink("table.header.invitation.link");
		
		private final String i18nKey;
		
		private InvitationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
