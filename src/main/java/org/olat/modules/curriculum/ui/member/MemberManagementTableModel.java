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
package org.olat.modules.curriculum.ui.member;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.instantMessaging.model.Presence;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 15 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberManagementTableModel extends DefaultFlexiTableDataModel<MemberRow>
implements SortableFlexiTableDataModel<MemberRow> {
	
	private static final MemberCols[] COLS = MemberCols.values();
	
	private final Locale locale;
	private final Translator translator;
	private final boolean onlineStatusEnabled;
	
	public MemberManagementTableModel(FlexiTableColumnModel columnModel, Translator translator,
			Locale locale, boolean onlineStatusEnabled) {
		super(columnModel);
		this.locale = locale;
		this.translator = translator;
		this.onlineStatusEnabled = onlineStatusEnabled;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			MemberManagementTableSortDelegate sort = new MemberManagementTableSortDelegate(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}
	
	@Override
	public boolean isSelectable(int row) {
		MemberRow member = getObject(row);
		return member.getInheritanceMode() == GroupMembershipInheritance.root
				|| member.getInheritanceMode() == GroupMembershipInheritance.none;
	}
	
	public MemberRow getObject(Identity identity) {
		if(identity == null) return null;
		
		List<MemberRow> objects = getObjects();
		for(MemberRow obj:objects) {
			if(obj.getIdentityKey().equals(identity.getKey())) {
				return obj;
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		MemberRow member = getObject(row);
		return getValueAt(member, col);
	}

	@Override
	public Object getValueAt(MemberRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case role -> row;
				case registration -> row.getRegistration();
				case asParticipant -> row.getNumOfRole(CurriculumRoles.participant);
				case asCoach -> row.getNumOfRole(CurriculumRoles.coach);
				case asOwner -> row.getNumOfRole(CurriculumRoles.owner);
				case asMasterCoach -> row.getNumOfRole(CurriculumRoles.mastercoach);
				case asElementOwner -> row.getNumOfRole(CurriculumRoles.curriculumelementowner);
				case online -> getChatLink(row);
				case tools -> row.getToolsLink();
				default -> "ERROR";
			};
		}
		
		int propPos = col - CurriculumUserManagementController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private FormLink getChatLink(MemberRow row) {
		FormLink chatLink = row.getChatLink();
		if(chatLink != null) {
			String onlineStatus = row.getOnlineStatus();
			if ("me".equals(onlineStatus)) {
				//no icon
			} else if (!onlineStatusEnabled) {
				// don't show the users status when not configured, only an icon to start a chat/message
				chatLink.setIconLeftCSS("o_icon o_icon_status_chat");
			}
			// standard case: available or unavailable (offline or dnd)
			else if(Presence.available.name().equals(onlineStatus)) {
				chatLink.setIconLeftCSS("o_icon o_icon_status_available");
				chatLink.setTitle(translator.translate("user.info.presence.available"));
			} else if(Presence.dnd.name().equals(onlineStatus)) {
				chatLink.setIconLeftCSS("o_icon o_icon_status_dnd");
				chatLink.setTitle(translator.translate("user.info.presence.dnd"));
			} else {
				chatLink.setIconLeftCSS("o_icon o_icon_status_unavailable");
				chatLink.setTitle(translator.translate("user.info.presence.unavailable"));
			}
			if(chatLink.getComponent() != null) {
				chatLink.getComponent().setDirty(false);
			}
		}
		return chatLink;
	}
	
	public enum MemberCols implements FlexiSortableColumnDef {
		online("table.header.online"),
		role("table.header.role"),
		registration("table.header.registration"),
		asParticipant("table.header.num.as.participant"),
		asCoach("table.header.num.as.coach"),
		asOwner("table.header.num.as.owner"),
		asMasterCoach("table.header.num.as.mastercoach"),
		asElementOwner("table.header.num.as.element.owner"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private MemberCols(String i18nKey) {
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
