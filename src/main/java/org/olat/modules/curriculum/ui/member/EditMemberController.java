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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.GroupMembershipHistoryComparator;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.modules.curriculum.ui.event.AcceptMembershipEvent;
import org.olat.modules.curriculum.ui.event.DeclineMembershipEvent;
import org.olat.modules.curriculum.ui.member.EditMemberCurriculumElementTableModel.MemberElementsCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditMemberController extends FormBasicController {

	protected static final int ROLES_OFFSET = 500;

	private static final String CMD_ADD = "add";
	private static final String CMD_ACTIVE = "active";
	private static final String CMD_PENDING = "pending";
	private static final String CMD_CHANGE = "change";
	
	private FormLink backButton;
	private FormLink resetButton;
	private FormLink applyCustomNotificationButton;
	private FormLink applyWithoutNotificationButton;
	private FlexiTableElement tableEl;
	private FlexiTableColumnModel columnsModel;
	private EditMemberCurriculumElementTableModel tableModel;
	
	private Identity member;
	private final Curriculum curriculum;
	private final UserInfoProfileConfig profileConfig;
	private final List<CurriculumElement> curriculumElements;

	private CloseableCalloutWindowController calloutCtrl;
	private AddMembershipCalloutController addMembershipCtrl;
	private ChangeMembershipCalloutController changeMembershipCtrl;
	private ConfirmMembershipCalloutController confirmMembershipCtrl;
	
	@Autowired
	private ACService acService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private CurriculumService curriculumService;
	
	public EditMemberController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, List<CurriculumElement> curriculumElements,
			Identity member, UserInfoProfileConfig profileConfig) {
		super(ureq, wControl, "edit_member");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));
		this.member = member;
		this.curriculum = curriculum;
		this.profileConfig = profileConfig;
		this.curriculumElements = new ArrayList<>(curriculumElements);
		
		initForm(ureq);
		loadModel();
	}
	
	public Identity getMember() {
		return member;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backButton = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);

		// Profile
		UserInfoProfile memberConfig = userInfoService.createProfile(member);
		MemberUserDetailsController profile = new MemberUserDetailsController(ureq, getWindowControl(), mainForm,
				member, profileConfig, memberConfig);
		listenTo(profile);
		formLayout.add("profil", profile.getInitialFormItem());
		
		initTableForm(formLayout);
		
		uifactory.addFormSubmitButton("apply", formLayout);
		applyCustomNotificationButton = uifactory.addFormLink("apply.custom.notifications", formLayout, Link.LINK);
		DropdownItem moreMenu = uifactory.addDropdownMenu("action.more", null, null, formLayout, getTranslator());
		moreMenu.setCarretIconCSS("o_icon o_icon_caret");
		moreMenu.setOrientation(DropdownOrientation.normal);
		moreMenu.addElement(applyCustomNotificationButton);
		
		applyWithoutNotificationButton = uifactory.addFormLink("apply.without.notifications", formLayout, Link.BUTTON);
		resetButton = uifactory.addFormLink("reset", formLayout, Link.BUTTON);
		resetButton.setIconLeftCSS("o_icon o_icon-fw o_icon_reset_data");
	}
	
	private void initTableForm(FormItemContainer formLayout) {
		columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberElementsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberElementsCols.modifications,
				new ModificationCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberElementsCols.displayName,
				new TreeNodeFlexiCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberElementsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberElementsCols.externalId));

		GroupMembershipStatusRenderer statusRenderer = new GroupMembershipStatusRenderer(getLocale());
		for(CurriculumRoles role:CurriculumRoles.values()) {
			String i18nLabel = "role.".concat(role.name());
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(i18nLabel, role.ordinal() + ROLES_OFFSET, null, false, null, statusRenderer);
			col.setDefaultVisible(true);
			col.setAlwaysVisible(false);
			columnsModel.addFlexiColumnModel(col);
		}
		
		tableModel = new EditMemberCurriculumElementTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "editRolesTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setFooter(true);
	}
	
	protected void loadModel() {
		// Memberships
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculum, member);
		Map<Long, CurriculumElementMembership> membershipsMap = memberships.stream()
				.collect(Collectors.toMap(CurriculumElementMembership::getCurriculumElementKey, u -> u, (u, v) -> u));
		
		// Reservations
		List<ResourceReservation> reservations = acService.getReservations(member);
		Map<ResourceToRoleKey,ResourceReservation> reservationsMap = reservations.stream()
				.filter(reservation -> StringHelper.containsNonWhitespace(reservation.getType()))
				.filter(reservation -> reservation.getType().startsWith(CurriculumService.RESERVATION_PREFIX))
				.collect(Collectors.toMap(reservation -> new ResourceToRoleKey(ResourceToRoleKey.reservationToRole(reservation.getType()), reservation.getResource()) , r -> r, (u, v) -> u));
		
		// History
		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setCurriculum(curriculum);
		searchParams.setIdentities(List.of(member));
		List<CurriculumElementMembershipHistory> curriculumHistory = curriculumService.getCurriculumElementMembershipsHistory(searchParams);
		Map<Long, CurriculumElementMembershipHistory> historyMap = curriculumHistory.stream()
				.collect(Collectors.toMap(CurriculumElementMembershipHistory::getCurriculumElementKey, u -> u, (u, v) -> u));
		
		List<EditMemberCurriculumElementRow> rows = new ArrayList<>();
		Map<Long, EditMemberCurriculumElementRow> rowsMap = new HashMap<>();
		EnumMap<CurriculumRoles,Boolean> usedRoles = new EnumMap<>(CurriculumRoles.class);
		for(CurriculumElement element:curriculumElements) {
			EditMemberCurriculumElementRow row = new EditMemberCurriculumElementRow(element);
			rows.add(row);
			rowsMap.put(row.getKey(), row);
			
			CurriculumElementMembership membership = membershipsMap.get(element.getKey());
			CurriculumElementMembershipHistory history = historyMap.get(element.getKey());
			forgeLinks(row, membership, history, reservationsMap, usedRoles);
		}
		
		for(EditMemberCurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(rowsMap.get(row.getParentKey()));
			}
		}
		
		// Update columns visibility
		for(CurriculumRoles role:CurriculumRoles.values()) {
			FlexiColumnModel col = columnsModel.getColumnModelByIndex(role.ordinal() + ROLES_OFFSET);
			if(col instanceof DefaultFlexiColumnModel) {
				tableEl.setColumnModelVisible(col, usedRoles.containsKey(role));
			}
		}
		
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeLinks(EditMemberCurriculumElementRow row, CurriculumElementMembership membership, CurriculumElementMembershipHistory history,
			Map<ResourceToRoleKey,ResourceReservation> reservationsMap, EnumMap<CurriculumRoles,Boolean> usedRoles) {
		
		OLATResource resource = row.getCurriculumElement().getResource();
		List<CurriculumRoles> memberships = membership == null ? List.of() : membership.getRoles();
		for(CurriculumRoles role:CurriculumRoles.values()) {
			ResourceReservation reservation = reservationsMap.get(new ResourceToRoleKey(role, resource));
			
			String id = "model-" + role + "-" + row.getKey();
			flc.remove(id);
			
			GroupMembershipStatus status = null;
			if(reservation != null) {
				row.addReservation(role, reservation);
				status = GroupMembershipStatus.reservation;
				usedRoles.put(role, Boolean.TRUE);
			} else if(memberships.contains(role)) {
				status = GroupMembershipStatus.active;
				usedRoles.put(role, Boolean.TRUE);
			} else  {
				GroupMembershipHistory lastHistory = lastHistoryPoint(role, history);
				if(lastHistory != null) {
					status = lastHistory.getStatus();
					usedRoles.put(role, Boolean.TRUE);
				}
			}
			
			FormLink link = null;
			if(status != null) {
				link = forgeLink(id, status);
			}
			if(link == null) {
				link = forgeAddLink(id);
			}
			link.setUserObject(new RoleCell(role, row));
			row.addButton(role, link);
		}
	}
	
	private GroupMembershipHistory lastHistoryPoint(CurriculumRoles role, CurriculumElementMembershipHistory history) {
		if(history == null) return null;
		
		List<GroupMembershipHistory> roleHistory = history.getHistory(role);
		if(roleHistory == null || roleHistory.isEmpty()) {
			return null;
		}
		if(roleHistory.size() == 1) {
			return roleHistory.get(0);
		}
		
		Collections.sort(roleHistory, new GroupMembershipHistoryComparator());
		return roleHistory.get(0);
	}

	private FormLink forgeLink(String id, GroupMembershipStatus status) {
		FormLink link;
		if(status == GroupMembershipStatus.reservation) {
			link = forgeLink(id, CMD_PENDING, "membership.pending",
					"o_gmembership_status_reservation", "o_membership_status_pending");
		} else if(status == GroupMembershipStatus.active) {
			link = forgeLink(id, CMD_ACTIVE, "membership.active",
					"o_gmembership_status_active", "o_membership_status_active");
		} else if(status == GroupMembershipStatus.cancel) {
			link = forgeLink(id, CMD_CHANGE, "membership.cancel",
					"o_gmembership_status_cancel", "o_membership_status_cancel");
		} else if(status == GroupMembershipStatus.cancelWithFee) {
			link = forgeLink(id, CMD_CHANGE, "membership.cancelWithFee",
					"o_gmembership_status_cancelwithfee", "o_membership_status_cancelwithfee");
		} else if(status == GroupMembershipStatus.declined) {
			link = forgeLink(id, CMD_CHANGE, "membership.declined",
					"o_gmembership_status_declined", "o_membership_status_declined");
		} else if(status == GroupMembershipStatus.resourceDeleted) {
			link = forgeLink(id, CMD_CHANGE, "membership.resourceDeleted",
					"o_gmembership_status_resourcedeleted", "o_membership_status_resourcedeleted");
		} else if(status == GroupMembershipStatus.finished) {
			link = forgeLink(id, CMD_CHANGE, "membership.finished",
					"o_gmembership_status_finished", "o_membership_status_finished");
		} else if(status == GroupMembershipStatus.removed) {
			link = forgeLink(id, CMD_CHANGE, "membership.removed",
					"o_gmembership_status_removed", "o_membership_status_removed");
		} else {
			link = null;
		}
		return link;
	}

	private FormLink forgeLink(String id, String cmd, String i18n, String boxCssClass, String iconCssClass) {
		FormLink link = uifactory.addFormLink(id, cmd, i18n, null, flc, Link.BUTTON_XSMALL);
		link.setCustomEnabledLinkCSS("o_labeled_light " + boxCssClass);
		link.setIconLeftCSS("o_icon o_icon-fw " + iconCssClass);
		link.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		return link;
	}
	
	private FormLink forgeAddLink(String id) {
		FormLink addLink = uifactory.addFormLink(id, CMD_ADD, "add", null, flc, Link.LINK);
		addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_plus");
		return addLink;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addMembershipCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setModification(addMembershipCtrl.getModification());
			}
			calloutCtrl.deactivate();
			cleanUp();
		} else if(changeMembershipCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setModification(changeMembershipCtrl.getModification());
			}
			calloutCtrl.deactivate();
			cleanUp();
		} else if(confirmMembershipCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT
					|| event instanceof AcceptMembershipEvent || event instanceof DeclineMembershipEvent) {
				setModification(confirmMembershipCtrl.getModification());
			}
			calloutCtrl.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmMembershipCtrl);
		removeAsListenerAndDispose(changeMembershipCtrl);
		removeAsListenerAndDispose(addMembershipCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		confirmMembershipCtrl = null;
		changeMembershipCtrl = null;
		addMembershipCtrl = null;
		calloutCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backButton == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(applyCustomNotificationButton == source
				|| applyWithoutNotificationButton == source) {
			doApply(ureq);
		} else if(resetButton == source) {
			doReset();
		} else if(source instanceof FormLink link) {
			if(CMD_ACTIVE.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doChangeMembership(ureq, link, cell.role(), cell.row());
			} else if(CMD_ADD.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doAddMembership(ureq, link, cell.role(), cell.row());
			} else if(CMD_PENDING.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doConfirmMembership(ureq, link, cell.role(), cell.row());
			} else if(CMD_CHANGE.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doChangeMembership(ureq, link, cell.role(), cell.row());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doApply(ureq);
	}
	
	private void doReset() {
		List<EditMemberCurriculumElementRow> rows = tableModel.getObjects();
		for(EditMemberCurriculumElementRow row:rows) {
			row.resetModification();
		}
		tableEl.reset(false, false, true);
	}
	
	private void doApply(UserRequest ureq) {
		List<EditMemberCurriculumElementRow> rows = tableModel.getObjects();
		List<MembershipModification> allModifications = new ArrayList<>();
		for(EditMemberCurriculumElementRow row:rows) {
			allModifications.addAll(row.getModifications());
		}
		for(MembershipModification modification:allModifications) {
			applyModification(modification);
		}
		
		loadModel();
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void applyModification(MembershipModification modification) {
		final CurriculumRoles role = modification.role();
		final GroupMembershipStatus nextStatus = modification.nextStatus();
		final CurriculumElement curriculumElement = modification.curriculumElement();
		
		if(nextStatus == GroupMembershipStatus.active) {
			curriculumService.removeMemberReservation(curriculumElement, member, role, null, null, null);
			curriculumService.addMember(curriculumElement, member, role,
					getIdentity(), modification.adminNote());
		} else if(nextStatus == GroupMembershipStatus.reservation) {
			Boolean confirmBy = modification.confirmationBy() == ConfirmationByEnum.PARTICIPANT ? Boolean.TRUE : Boolean.FALSE;
			curriculumService.addMemberReservation(curriculumElement, member, role, modification.confirmUntil(), confirmBy,
					getIdentity(), modification.adminNote());
		} else if(nextStatus == GroupMembershipStatus.cancel
				|| nextStatus == GroupMembershipStatus.cancelWithFee
				|| nextStatus == GroupMembershipStatus.removed
				|| nextStatus == GroupMembershipStatus.declined) {
			boolean removed = curriculumService.removeMemberReservation(curriculumElement, member, role, nextStatus,
					getIdentity(), modification.adminNote());
			removed |= curriculumService.removeMember(curriculumElement, member, role, nextStatus,
					getIdentity(), modification.adminNote());
			if(!removed) {
				curriculumService.addMemberHistory(curriculumElement, member, role, nextStatus,
						getIdentity(), modification.adminNote());
			}
		}
	}

	private void setModification(MembershipModification modification) {
		EditMemberCurriculumElementRow row = tableModel.getObject(modification.curriculumElement());
		row.addModification(modification.role(), modification);
		
		if(modification.toDescendants()) {
			int index = tableModel.getIndexOf(row);
			int numOfRows = tableModel.getRowCount();
			for(int i=index+1; i<numOfRows; i++) {
				EditMemberCurriculumElementRow obj = tableModel.getObject(i);
				if(tableModel.isParentOf(row, obj)) {
					obj.addModification(modification.role(), modification);
				}
			}
		}
		
		tableEl.reset(false, false, true);
	}
	
	private void doAddMembership(UserRequest ureq, FormLink link, CurriculumRoles role, EditMemberCurriculumElementRow row) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		addMembershipCtrl = new AddMembershipCalloutController(ureq, getWindowControl(), member, role, curriculumElement);
		listenTo(addMembershipCtrl);
		
		String title = translate("add.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doConfirmMembership(UserRequest ureq, FormLink link, CurriculumRoles role, EditMemberCurriculumElementRow row) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		ResourceReservation reservation = row.getReservation(role);
		if(reservation == null) {
			doChangeMembership(ureq, link, role, row);
		} else {
			confirmMembershipCtrl = new ConfirmMembershipCalloutController(ureq, getWindowControl(), member, role,
					curriculumElement, reservation);
			listenTo(confirmMembershipCtrl);
			
			String title = translate("confirm.membership");
			calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					confirmMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		}
	}
	
	private void doChangeMembership(UserRequest ureq, FormLink link, CurriculumRoles role, EditMemberCurriculumElementRow row) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		changeMembershipCtrl = new ChangeMembershipCalloutController(ureq, getWindowControl(), member, role, curriculumElement);
		listenTo(changeMembershipCtrl);
		
		String title = translate("change.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				changeMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private record RoleCell(CurriculumRoles role, EditMemberCurriculumElementRow row) {
		//
	}
}
