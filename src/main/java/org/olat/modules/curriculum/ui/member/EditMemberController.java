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
import java.util.EnumSet;
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
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.CurriculumUIFactory;
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

	private static final CurriculumRoles[] ROLES = CurriculumRoles.curriculumElementsRoles();
	private static final GroupMembershipStatus[] MODIFIABLE_STATUS = GroupMembershipStatus.statusWithNextStep();
	public static final int ROLES_OFFSET = 500;
	public static final int NOTES_OFFSET = 1000;

	private static final String CMD_ADD = "add";
	private static final String CMD_NOTE = "note";
	private static final String CMD_CHANGE = "change";
	private static final String CMD_ADD_ROLE = "add.role";
	
	private FormLink backButton;
	private FormLink resetButton;
	private FormLink applyCustomNotificationButton;
	private FormLink applyWithoutNotificationButton;
	private DropdownItem addRolesEl;
	private FlexiTableElement tableEl;
	private FlexiTableColumnModel columnsModel;
	private EditMemberCurriculumElementTableModel tableModel;
	
	private Identity member;
	private final Curriculum curriculum;
	private final UserInfoProfileConfig profileConfig;
	private final List<CurriculumElement> curriculumElements;
	private final EnumSet<CurriculumRoles> usedRoles = EnumSet.noneOf(CurriculumRoles.class);
	
	private CloseableModalController cmc;
	private NoteCalloutController noteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private AddMembershipCalloutController addMembershipCtrl;
	private ChangeMembershipCalloutController changeMembershipCtrl;
	private ConfirmMembershipCalloutController confirmMembershipCtrl;
	private CustomizeNotificationController customizeNotificationsCtrl;
	
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
		addRolesEl = uifactory.addDropdownMenu("assign.additional.roles", "assign.additional.roles", null, formLayout, getTranslator());
		for(CurriculumRoles role:CurriculumRoles.curriculumElementsRoles()) {
			String name = "role.".concat(role.name());
			FormLink addRoleButton = uifactory.addFormLink(name, CMD_ADD_ROLE, name, null, formLayout, Link.LINK);
			addRoleButton.setUserObject(role);
			addRolesEl.addElement(addRoleButton);
		}

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
		for(CurriculumRoles role:ROLES) {
			String i18nLabel = "role.".concat(role.name());
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(i18nLabel, role.ordinal() + ROLES_OFFSET, null, false, null, statusRenderer);
			col.setDefaultVisible(true);
			col.setAlwaysVisible(false);
			columnsModel.addFlexiColumnModel(col);
			
			DefaultFlexiColumnModel noteCol = new DefaultFlexiColumnModel(null, role.ordinal() + NOTES_OFFSET);
			noteCol.setDefaultVisible(true);
			noteCol.setAlwaysVisible(false);
			noteCol.setIconHeader("o_icon o_icon-fw");// Dummy icon
			noteCol.setHeaderLabel(i18nLabel);
			columnsModel.addFlexiColumnModel(noteCol);
		}
		
		String footerHeader = translate("table.footer.roles");
		tableModel = new EditMemberCurriculumElementTableModel(columnsModel, footerHeader);
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
		
		updateRolesColumnsVisibility();
		
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeLinks(EditMemberCurriculumElementRow row, CurriculumElementMembership membership, CurriculumElementMembershipHistory history,
			Map<ResourceToRoleKey,ResourceReservation> reservationsMap, EnumSet<CurriculumRoles> usedRoles) {
		
		OLATResource resource = row.getCurriculumElement().getResource();
		List<CurriculumRoles> memberships = membership == null ? List.of() : membership.getRoles();
		for(CurriculumRoles role:ROLES) {
			ResourceReservation reservation = reservationsMap.get(new ResourceToRoleKey(role, resource));
			
			String id = "model-" + role + "-" + row.getKey();
			flc.remove(id);
			
			GroupMembershipStatus status = null;
			GroupMembershipHistory lastHistory = lastHistoryPoint(role, history);
			if(reservation != null) {
				row.addReservation(role, reservation);
				status = GroupMembershipStatus.reservation;
				usedRoles.add(role);
			} else if(memberships.contains(role)) {
				status = GroupMembershipStatus.active;
				usedRoles.add(role);
			} else if(lastHistory != null) {
				status = lastHistory.getStatus();
				usedRoles.add(role);
			}
			row.setStatus(role, status);
			
			FormLink link = null;
			if(status == null || hasGroupMembershipStatus(status, MODIFIABLE_STATUS)) {
				if(status != null) {
					link = forgeLink(id, status);
				}
				if(link == null) {
					link = forgeAddLink(id);
				}
				link.setUserObject(new RoleCell(role, row, status));
				row.addButton(role, link);
			}
			if(lastHistory != null && status == lastHistory.getStatus() && StringHelper.containsNonWhitespace(lastHistory.getAdminNote())) {
				FormLink noteButton = forgeNoteLink(id);
				row.addNoteButton(role, noteButton);
				row.setAdminNote(lastHistory.getAdminNote());
				noteButton.setUserObject(new RoleCell(role, row, status));
			}
			
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
		if(status == null) return null;
		
		String labelCssClass = CurriculumUIFactory.getMembershipLabelCssClass(status);
		String iconCssClass = CurriculumUIFactory.getMembershipIconCssClass(status);
		String i18nKey = getMembershipI18nKey(status);
		return forgeLink(id, CMD_CHANGE, i18nKey, labelCssClass, iconCssClass);
	}

	private FormLink forgeLink(String id, String cmd, String i18n, String boxCssClass, String iconCssClass) {
		FormLink link = uifactory.addFormLink(id, cmd, i18n, null, flc, Link.BUTTON_XSMALL);
		link.setDomReplacementWrapperRequired(false);
		link.setCustomEnabledLinkCSS("o_labeled_light " + boxCssClass);
		link.setIconLeftCSS("o_icon o_icon-fw " + iconCssClass);
		link.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		return link;
	}
	
	private FormLink forgeNoteLink(String id) {
		FormLink noteLink = uifactory.addFormLink("note_" + id, CMD_NOTE, "", null, flc, Link.LINK | Link.NONTRANSLATED);
		noteLink.setDomReplacementWrapperRequired(false);
		noteLink.setIconLeftCSS("o_icon o_icon_notes");
		noteLink.setTitle(translate("note"));
		return noteLink;
	}
	
	private FormLink forgeAddLink(String id) {
		FormLink addLink = uifactory.addFormLink(id, CMD_ADD, "add", null, flc, Link.LINK);
		addLink.setDomReplacementWrapperRequired(false);
		addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_plus");
		return addLink;
	}
	
	public static final String getMembershipI18nKey(GroupMembershipStatus status) {
		return switch(status) {
			case reservation -> "membership.pending";
			case active -> "membership.active";
			case cancel -> "membership.cancel";
			case cancelWithFee -> "membership.cancelWithFee";
			case declined -> "membership.declined";
			case resourceDeleted -> "membership.resourceDeleted";
			case finished -> "membership.finished";
			case removed -> "membership.removed";
			default -> null;
		};
	}
	
	private void updateRolesColumnsVisibility() {
		// Update columns visibility
		for(CurriculumRoles role:ROLES) {
			FlexiColumnModel col = columnsModel.getColumnModelByIndex(role.ordinal() + ROLES_OFFSET);
			if(col instanceof DefaultFlexiColumnModel) {
				tableEl.setColumnModelVisible(col, usedRoles.contains(role));
			}
			FlexiColumnModel noteCol = columnsModel.getColumnModelByIndex(role.ordinal() + NOTES_OFFSET);
			if(noteCol instanceof DefaultFlexiColumnModel) {
				tableEl.setColumnModelVisible(noteCol, usedRoles.contains(role));
			}
		}
		
		for(FormItem item:addRolesEl.getFormItems()) {
			if(item instanceof FormLink link && link.getUserObject() instanceof CurriculumRoles role) {
				item.setVisible(!usedRoles.contains(role));
			}
		}
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
				setConfirmReservation(confirmMembershipCtrl.getModification());
			}
			calloutCtrl.deactivate();
			cleanUp();
		} else if(customizeNotificationsCtrl == source) {
			MailTemplate customTemplate = customizeNotificationsCtrl.getMailTemplate();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doApplyWithCustomNotifications(ureq, customTemplate);
			}
		} else if(calloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(customizeNotificationsCtrl);
		removeAsListenerAndDispose(confirmMembershipCtrl);
		removeAsListenerAndDispose(changeMembershipCtrl);
		removeAsListenerAndDispose(addMembershipCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		customizeNotificationsCtrl = null;
		confirmMembershipCtrl = null;
		changeMembershipCtrl = null;
		addMembershipCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backButton == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(applyWithoutNotificationButton == source) {
			doApply(ureq, new MailPackage(false));
		} else if(applyCustomNotificationButton == source) {
			doCustomizeNotifications(ureq);
		} else if(resetButton == source) {
			doReset();
		} else if(source instanceof FormLink link) {
			if(CMD_ADD_ROLE.equals(link.getCmd()) && link.getUserObject() instanceof CurriculumRoles role)  {
				doAddRole(role);
			} else if(CMD_ADD.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doAddMembership(ureq, link, cell.role(), cell.row());
			} else if(CMD_CHANGE.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doMembership(ureq, link, cell.role(), cell.row(), cell.status());
			} else if(CMD_NOTE.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doOpenNote(ureq, link, cell);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doApplyWithNotifications(ureq);
	}
	
	private void doCustomizeNotifications(UserRequest ureq) {
		MailTemplate template = CurriculumMailing.getDefaultMailTemplate(curriculum, null, getIdentity());
		customizeNotificationsCtrl = new CustomizeNotificationController(ureq, getWindowControl(), template);
		listenTo(customizeNotificationsCtrl);
		
		String title = translate("customize.notifications.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), customizeNotificationsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doApplyWithCustomNotifications(UserRequest ureq, MailTemplate template) {
		MailerResult result = new MailerResult();
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doApply(ureq, mailing);
	}
	
	private void doReset() {
		List<EditMemberCurriculumElementRow> rows = tableModel.getObjects();
		for(EditMemberCurriculumElementRow row:rows) {
			row.resetModification();
		}
		tableEl.reset(false, false, true);
	}
	
	private void doApplyWithNotifications(UserRequest ureq) {
		MailerResult result = new MailerResult();
		MailTemplate template = CurriculumMailing.getDefaultMailTemplate(curriculum, null, getIdentity());
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doApply(ureq, mailing);
	}

	private void doApply(UserRequest ureq, MailPackage mailPackage) {
		List<EditMemberCurriculumElementRow> rows = tableModel.getObjects();
		List<MembershipModification> allModifications = new ArrayList<>();
		for(EditMemberCurriculumElementRow row:rows) {
			allModifications.addAll(row.getModifications());
		}
		
		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		for(MembershipModification modification:allModifications) {
			changes.add(getModification(modification));
		}
		
		curriculumService.updateCurriculumElementMemberships(member, ureq.getUserSession().getRoles(), changes, mailPackage);
		
		loadModel();
		fireEvent(ureq, Event.CHANGED_EVENT);
		fireEvent(ureq, Event.CLOSE_EVENT);
	}
	
	private CurriculumElementMembershipChange getModification(MembershipModification modification) {
		final CurriculumRoles role = modification.role();
		final GroupMembershipStatus nextStatus = modification.nextStatus();
		final CurriculumElement curriculumElement = modification.curriculumElement();
		
		CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(member, curriculumElement);
		change.setNextStatus(role, nextStatus);
		change.setAdminNote(role, modification.adminNote());

		if(nextStatus == GroupMembershipStatus.reservation) {
			change.setConfirmation(modification.confirmation());
			change.setConfirmationBy(modification.confirmationBy());
			change.setConfirmUntil(modification.confirmUntil());
		}
		return change;
	}
	
	/**
	 * This modification can only be applied on current reservations.
	 * 
	 * @param modification The modification
	 */
	private void setConfirmReservation(MembershipModification modification) {
		EditMemberCurriculumElementRow row = tableModel.getObject(modification.curriculumElement());
		setModification(modification, row);
		
		if(modification.toDescendants()) {
			int index = tableModel.getIndexOf(row);
			int numOfRows = tableModel.getRowCount();
			for(int i=index+1; i<numOfRows; i++) {
				EditMemberCurriculumElementRow obj = tableModel.getObject(i);
				GroupMembershipStatus currentStatus = obj.getStatus(modification.role());
				if(tableModel.isParentOf(row, obj) && currentStatus == GroupMembershipStatus.reservation) {
					MembershipModification objModification = modification.copyFor(obj.getCurriculumElement());
					setModification(objModification, obj);
				}
			}
		}
		
		tableEl.reset(false, false, true);
	}

	private void setModification(MembershipModification modification) {
		EditMemberCurriculumElementRow row = tableModel.getObject(modification.curriculumElement());
		setModification(modification, row);
		
		if(modification.toDescendants()) {
			int index = tableModel.getIndexOf(row);
			int numOfRows = tableModel.getRowCount();
			for(int i=index+1; i<numOfRows; i++) {
				EditMemberCurriculumElementRow obj = tableModel.getObject(i);
				GroupMembershipStatus currentStatus = obj.getStatus(modification.role());
				if(tableModel.isParentOf(row, obj)
						&& GroupMembershipStatus.allowedAsNextStep(currentStatus, modification.nextStatus())) {
					MembershipModification objModification = modification.copyFor(obj.getCurriculumElement());
					setModification(objModification, obj);
				}
			}
		}
		
		tableEl.reset(false, false, true);
	}
	
	private void setModification(MembershipModification modification, EditMemberCurriculumElementRow row) {
		final CurriculumRoles role = modification.role();
		row.addModification(role, modification);

		final String adminNote = modification.adminNote();
		if(StringHelper.containsNonWhitespace(adminNote) && row.getNoteButton(role) == null) {
			String id = "model-" + role + "-" + row.getKey();
			FormLink noteButton = forgeNoteLink(id);
			row.addNoteButton(role, noteButton);
			noteButton.setUserObject(new RoleCell(role, row, modification.nextStatus()));
		}
	}
	
	private void doMembership(UserRequest ureq, FormLink link, CurriculumRoles role, EditMemberCurriculumElementRow row, GroupMembershipStatus status) {
		GroupMembershipStatus[] nextPossibleStatus = GroupMembershipStatus.possibleNextStatus(status);
		// Reservation -> confirm
		if(status == GroupMembershipStatus.reservation) {
			doConfirmMembership(ureq, link, role, row);
		// Possible steps active or reservation -> add membership callout (with or without confirmation)
		} else if(hasGroupMembershipStatus(GroupMembershipStatus.active, nextPossibleStatus)
				|| hasGroupMembershipStatus(GroupMembershipStatus.reservation, nextPossibleStatus)) {
			doAddMembership(ureq, link, role, row);
		} else if(nextPossibleStatus != null && nextPossibleStatus.length > 0) {
			doChangeMembership(ureq, link, role, row, nextPossibleStatus);
		}
	}
	
	private boolean hasGroupMembershipStatus(GroupMembershipStatus status, GroupMembershipStatus[] statusArr) {
		if(status != null && statusArr != null && statusArr.length > 0) {
			for(GroupMembershipStatus next:statusArr) {
				if(next == status) {
					return true;
				}
			}
		}
		return false;
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
			// Something wrong
			loadModel();
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
	
	private void doChangeMembership(UserRequest ureq, FormLink link, CurriculumRoles role,
			EditMemberCurriculumElementRow row, GroupMembershipStatus[] possibleStatus) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		changeMembershipCtrl = new ChangeMembershipCalloutController(ureq, getWindowControl(), member, role, curriculumElement, possibleStatus);
		listenTo(changeMembershipCtrl);
		
		String title = translate("change.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				changeMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doAddRole(CurriculumRoles role) {
		usedRoles.add(role);
		updateRolesColumnsVisibility();
	}
	
	private void doOpenNote(UserRequest ureq, FormLink link, RoleCell cell) {
		String adminNote = cell.row().getAdminNote();
		MembershipModification modification = cell.row().getModification(cell.role());		
		if(modification != null && StringHelper.containsNonWhitespace(modification.adminNote())) {
			adminNote = modification.adminNote();
		}
		
		StringBuilder sb = Formatter.stripTabsAndReturns(adminNote);
		String note = sb == null ? "" : sb.toString();
		noteCtrl = new NoteCalloutController(ureq, getWindowControl(), note);
		listenTo(noteCtrl);
		
		String title = translate("note");
		CalloutSettings settings = new CalloutSettings(title);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noteCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "", settings);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private record RoleCell(CurriculumRoles role, EditMemberCurriculumElementRow row, GroupMembershipStatus status) {
		//
	}
}
