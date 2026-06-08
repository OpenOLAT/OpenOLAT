/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;

/**
 * 
 * Initial date: 23 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersListDataModel extends DefaultFlexiTableDataModel<CommitteeMember>
	implements SortableFlexiTableDataModel<CommitteeMember> {
	
	private static final MCols[] COLS = MCols.values();

	private final Locale locale;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private final MembersValidator membersValidator;
	
	public MembersListDataModel(List<UserPropertyHandler> userPropertyHandlers, FlexiTableColumnModel columnsModel,
			Translator translator, Locale locale) {
		super(new ArrayList<>(), columnsModel);
		this.locale = locale;
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
		membersValidator = new MembersValidator(userPropertyHandlers, locale);
	}
	
	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		CommitteeMember member = getObject(row);
		return getValueAt(member, col);
	}

	@Override
	public Object getValueAt(CommitteeMember row, int col) {
		if(col < PositionCommitteeController.USER_PROP_OFFSET) {
			switch(COLS[col]) {
				case ok: return row.getStatus();
				case name: return getFullname(row.getIdentity().getUser());
				case role: {
					if(StringHelper.containsNonWhitespace(row.getRole())) {
						PositionRole role =  PositionRole.role(row.getRole());
						if(role != null) {
							return translator.translate(role.role());
						} else {
							return translator.translate(row.getRole());
						}
					}
					return "";
				}
				case institution: return row.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALNAME, locale);
				case skip: return row.getStatus() != CommitteeMemberStatus.skipped;
				case edit: return row.getStatus() != CommitteeMemberStatus.skipped;
			}
		}
		
		int propIndex = col - PositionCommitteeController.USER_PROP_OFFSET;
		UserPropertyHandler prop = userPropertyHandlers.get(propIndex);
		return prop.getUserProperty(row.getIdentity().getUser(), locale);
	}
	
	private String getFullname(User user) {
		StringBuilder sb = new StringBuilder(32);
		if(StringHelper.containsNonWhitespace(user.getProperty(UserConstants.LASTNAME, locale))) {
			sb.append(user.getProperty(UserConstants.LASTNAME, locale));
		}
		if(StringHelper.containsNonWhitespace(user.getProperty(UserConstants.FIRSTNAME, locale))) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(user.getProperty(UserConstants.FIRSTNAME, locale));
		}
		return sb.toString();
	}
	
	public void validateRows() {
		List<CommitteeMember> rows = getObjects();
		for(CommitteeMember row:rows) {
			row.setStatus(membersValidator.valid(row, getObjects()));
		}
	}
	
	public void validate(CommitteeMember row) {
		row.setStatus(membersValidator.valid(row, getObjects()));
	}
	
	public enum MCols implements FlexiSortableColumnDef {
		ok("ok"),
		name("edit.committee.name"),
		role("edit.committee.role"),
		institution("edit.committee.institution"),
		skip("skip"),
		edit("edit");
		
		private final String i18nHeaderKey;
		
		private MCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
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