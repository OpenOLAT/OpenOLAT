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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.instantMessaging.model.Presence;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 15 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberManagementTableModel extends DefaultFlexiTableDataModel<MemberRow>
implements SortableFlexiTableDataModel<MemberRow>, FilterableFlexiTableModel {
	
	private static final MemberCols[] COLS = MemberCols.values();
	
	private final Locale locale;
	private final Translator translator;
	private final boolean onlineStatusEnabled;
	private List<MemberRow> backupRows;
	
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
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			String searchString = quickSearch == null ? null : quickSearch.toLowerCase();
			List<CurriculumRoles> roles = getFilterByRoles(filters);
			ConfirmationByEnum confirmationBy = getFilterByConfirmationBy(filters);
			boolean confirmationDate = getFilterByConfirmationDate(filters);
			
			List<MemberRow> filteredRows = backupRows.stream()
					.filter(row -> acceptSearch(row, searchString)
							&& acceptRoles(row, roles)
							&& isConfirmationBy(row, confirmationBy)
							&& isConfirmationDate(row, confirmationDate))
					.toList();
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private List<CurriculumRoles> getFilterByRoles(List<FlexiTableFilter> filters) {
		FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, CurriculumElementMemberUsersController.FILTER_ROLE);
		if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(CurriculumRoles::valueOf)
						.toList();
			}
		}
		return List.of();
	}

	private ConfirmationByEnum getFilterByConfirmationBy(List<FlexiTableFilter> filters) {
		FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, CurriculumElementPendingUsersController.FILTER_CONFIRMATION_BY);
		if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			String value = extendedFilter.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				return ConfirmationByEnum.valueOf(value);
			}
		}
		return null;
	}
	
	private boolean getFilterByConfirmationDate(List<FlexiTableFilter> filters) {
		FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, CurriculumElementPendingUsersController.FILTER_CONFIRMATION_DATE);
		if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			String value = extendedFilter.getValue();
			if(StringHelper.containsNonWhitespace(value) && "true".equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean acceptSearch(MemberRow row, String searchString) {
		if(searchString == null) return true;
		
		String[] userProps = row.getIdentityProps();
		for(String userProp:userProps) {
			if(userProp != null && userProp.toLowerCase().contains(searchString)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean acceptRoles(MemberRow row, List<CurriculumRoles> roles) {
		if(roles == null || roles.isEmpty()) return true;
		
		for(CurriculumRoles role:roles) {
			int numOfRole = row.getNumOfRole(role);
			if(numOfRole > 0) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isConfirmationBy(MemberRow row, ConfirmationByEnum by) {
		if(by == null) return true;
		
		List<ResourceReservation> reservations = row.getReservations();
		if(reservations != null && !reservations.isEmpty()) {
			for(ResourceReservation reservation:reservations) {
				Boolean confirmationByUser = reservation.getUserConfirmable();
				if(by == ConfirmationByEnum.ADMINISTRATIVE_ROLE && confirmationByUser != null && !confirmationByUser.booleanValue()) {
					return true;
				}
				if(by == ConfirmationByEnum.PARTICIPANT && (confirmationByUser == null || confirmationByUser.booleanValue())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isConfirmationDate(MemberRow row, boolean hasDate) {
		if(!hasDate) return true;
		
		List<ResourceReservation> reservations = row.getReservations();
		if(reservations != null && !reservations.isEmpty()) {
			for(ResourceReservation reservation:reservations) {
				if(reservation.getExpirationDate() != null) {
					return true;
				}
			}
		}
		return false;
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
				case pending -> row.getNumOfReservations();
				case online -> getChatLink(row);
				case tools -> row.getToolsLink();
				default -> "ERROR";
			};
		}
		
		int propPos = col - AbstractMembersController.USER_PROPS_OFFSET;
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
	
	@Override
	public void setObjects(List<MemberRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
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
		pending("table.header.num.pending"),
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
