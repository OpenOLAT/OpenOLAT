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
package org.olat.course.member.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.course.member.wizard.InvitationContext.TransientInvitation;

/**
 * 
 * Initial date: 2 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationUsersListInfosDataModel extends DefaultFlexiTableDataModel<TransientInvitation> 
implements SortableFlexiTableDataModel<TransientInvitation> {

	private static final InfosCols[] COLS = InfosCols.values();
	
	private final Locale locale;
	
	public InvitationUsersListInfosDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<TransientInvitation> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		TransientInvitation invitation = getObject(row);
		return getValueAt(invitation, col);
	}

	@Override
	public Object getValueAt(TransientInvitation invitation, int col) {
		switch(COLS[col]) {
			case id: return invitation.getIdentity() != null ? invitation.getIdentity().getKey() : null;
			case email: return getTransientOrIdentity(invitation, invitation.getEmail(), UserConstants.EMAIL);
			case firstName: return getTransientOrIdentity(invitation, invitation.getFirstName(), UserConstants.FIRSTNAME);
			case lastName: return getTransientOrIdentity(invitation, invitation.getLastName(), UserConstants.LASTNAME);
			default: return "ERROR";
		}
	}

	private String getTransientOrIdentity(TransientInvitation invitation, String transientVal, String propName) {
		Identity identity = invitation.getIdentity();
		if(identity == null) {
			return transientVal;
		}
		return identity.getUser().getProperty(propName, locale);
	}

	public enum InfosCols implements FlexiSortableColumnDef {
		id("table.name.id"),
		email("table.name.email"),
		firstName("table.name.firstName"),
		lastName("table.name.lastName");
		
		private final String i18nKey;
		
		private InfosCols(String i18nKey) {
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
