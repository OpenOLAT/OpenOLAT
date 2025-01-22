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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
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
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.member.RemoveMembershipsTableModel.RemoveMembershipCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RemoveMembershipsController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FormLink removeButton;
	private TextElement adminNoteEl;
	private SingleSelection applyToEl;
	private FlexiTableElement tableEl;
	private final VelocityContainer detailsVC;
	private RemoveMembershipsTableModel tableModel;
	private FormLink removeCustomNotificationButton;
	private FormLink removeWithoutNotificationButton;

	private List<Identity> members;
	private final Curriculum curriculum;
	private final String avatarMapperBaseURL;
	private final GroupMembershipStatus nextStatus;
	private List<CurriculumElement> curriculumElements;
	private final CurriculumElement selectedCurriculumElement;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);

	private CloseableModalController cmc;
	private CustomizeNotificationController customizeNotificationsCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	
	public RemoveMembershipsController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, CurriculumElement selectedCurriculumElement, List<CurriculumElement> curriculumElements,
			List<Identity> members, GroupMembershipStatus nextStatus) {
		super(ureq, wControl, "remove_memberships", Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.members = members;
		this.curriculum = curriculum;
		this.nextStatus = nextStatus;
		this.curriculumElements = curriculumElements;
		this.selectedCurriculumElement = selectedCurriculumElement;

		detailsVC = createVelocityContainer("member_details");
		
		avatarMapperBaseURL = registerCacheableMapper(ureq, "res-cur-avatars", avatarMapper);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AbstractMembersController.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// apply to
		SelectionValues applyToPK = new SelectionValues();
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CONTAINED.name(), translate("apply.membership.to.contained")));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CURRENT.name(), translate("apply.membership.to.current")));
		applyToEl = uifactory.addRadiosVertical("apply.membership.to.delete", "remove.membership.from", formLayout, applyToPK.keys(), applyToPK.values());
		applyToEl.select(ChangeApplyToEnum.CONTAINED.name(), true);
		
		adminNoteEl = uifactory.addTextAreaElement("admin.note.delete", "admin.note", 2000, 4, 32, false, false, false, "", formLayout);
		initButtonsForm(formLayout, ureq);
		initTableForm(formLayout);
	}

	private void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RemoveMembershipCols.modifications,
				new ModificationCellRenderer(getTranslator())));
		
		int colIndex = AbstractMembersController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			String name = userPropertyHandler.getName();
			String action = UserConstants.NICKNAME.equals(name) || UserConstants.FIRSTNAME.equals(name) || UserConstants.LASTNAME.equals(name)
					? TOGGLE_DETAILS_CMD : null;
			boolean visible = userManager.isMandatoryUserProperty(AbstractMembersController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, action, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RemoveMembershipCols.removed));
		
		tableModel = new RemoveMembershipsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	private void initButtonsForm(FormItemContainer formLayout, UserRequest ureq) {	
		removeButton = uifactory.addFormLink("remove", formLayout, Link.LINK);
		removeButton.setElementCssClass("btn btn-default btn-danger");
		removeButton.setIconLeftCSS("o_icon o_icon-fw o_membership_status_removed");
		
		removeCustomNotificationButton = uifactory.addFormLink("remove.custom.notifications", formLayout, Link.LINK);
		DropdownItem moreMenu = uifactory.addDropdownMenu("action.more", null, null, formLayout, getTranslator());
		moreMenu.setCarretIconCSS("o_icon o_icon_caret");
		moreMenu.setOrientation(DropdownOrientation.normal);
		moreMenu.addElement(removeCustomNotificationButton);
		
		removeWithoutNotificationButton = uifactory.addFormLink("remove.without.notifications", formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof RemoveMembershipRow memberRow
				&& memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}
	
	private void loadModel() {
		final List<RemoveMembershipRow> rows = new ArrayList<>(members.size());
		List<CurriculumElementMembership> memberships = curriculumService
				.getCurriculumElementMemberships(curriculumElements, members);
		
		final Map<Long,RemoveMembershipRow> rowsMap = new HashMap<>();
		for(Identity member:members) {
			RemoveMembershipRow row = new RemoveMembershipRow(member, userPropertyHandlers, getLocale());
			rowsMap.put(member.getKey(), row);
			rows.add(row);
		}
		
		for(CurriculumElementMembership membership:memberships) {
			RemoveMembershipRow row = rowsMap.get(membership.getIdentityKey());
			if(row != null) {
				row.addMembership(membership);
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(customizeNotificationsCtrl == source) {
			MailTemplate customTemplate = customizeNotificationsCtrl.getMailTemplate();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doRemoveWithCustomNotifications(ureq, customTemplate);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(customizeNotificationsCtrl);
		removeAsListenerAndDispose(cmc);
		customizeNotificationsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(removeWithoutNotificationButton == source) {
			doRemove(ureq, new MailPackage(false));
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(removeButton == source) {
			doRemoveWithNotification(ureq);
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(removeCustomNotificationButton == source) {
			doCustomizeNotifications(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					RemoveMembershipRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseMemberDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenMemberDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				RemoveMembershipRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenMemberDetails(ureq, row);
				} else {
					doCloseMemberDetails(row);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
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
	
	private void doRemoveWithCustomNotifications(UserRequest ureq, MailTemplate template) {
		MailerResult result = new MailerResult();
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doRemove(ureq, mailing);
	}
	
	private void doRemoveWithNotification(UserRequest ureq) {
		MailerResult result = new MailerResult();
		MailTemplate template = CurriculumMailing.getDefaultMailTemplate(curriculum, null, getIdentity());
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doRemove(ureq, mailing);
	}
	
	private void doRemove(UserRequest ureq, MailPackage mailing) {
		String adminNote = adminNoteEl.getValue();
		
		Map<OLATResource, CurriculumElement> resourceToCurriculumElements = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getResource, ce -> ce, (u, v) -> u));
		Map<Long, CurriculumElement> keyToCurriculumElements = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getKey, ce -> ce, (u, v) -> u));
		
		List<RemoveMembershipRow> rows = tableModel.getObjects();
		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		for(RemoveMembershipRow row:rows) {
			Identity identity = row.getIdentity();
			for(ResourceReservation reservation:row.getReservations()) {
				CurriculumRoles role = ResourceToRoleKey.reservationToRole(reservation.getType());
				CurriculumElement curriculumElement = resourceToCurriculumElements.get(reservation.getResource());
				CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(identity, curriculumElement);
				change.setNextStatus(role, nextStatus);
				change.setAdminNote(role, adminNote);
				changes.add(change);
			}
			
			for(CurriculumElementMembership membership:row.getMemberships()) {
				CurriculumElement curriculumElement = keyToCurriculumElements.get(membership.getCurriculumElementKey());
				if(curriculumElement != null) {
					List<CurriculumRoles> roles = membership.getRoles();
					for(CurriculumRoles role:roles) {
						CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(identity, curriculumElement);
						change.setNextStatus(role, nextStatus);
						change.setAdminNote(role, adminNote);
						changes.add(change);
					}
				}
			}
		}
		
		curriculumService.updateCurriculumElementMemberships(getIdentity(), ureq.getUserSession().getRoles(), changes, mailing);
	}

	private final void doOpenMemberDetails(UserRequest ureq, RemoveMembershipRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}

		UserInfoProfileConfig profileConfig = createProfilConfig();
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, null, false, false, false, true, false,
				true, false, false);
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculum, selectedCurriculumElement, curriculumElements, row.getIdentity(), config);
		listenTo(detailsCtrl);
		
		Map<OLATResource, CurriculumElement> resourceToCurriculumElements = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getResource, ce -> ce, (u, v) -> u));
		Map<Long, CurriculumElement> keyToCurriculumElements = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getKey, ce -> ce, (u, v) -> u));
		
		List<MembershipModification> modifications = new ArrayList<>();
		for(ResourceReservation reservation:row.getReservations()) {
			CurriculumRoles role = ResourceToRoleKey.reservationToRole(reservation.getType());
			CurriculumElement curriculumElement = resourceToCurriculumElements.get(reservation.getResource());
			MembershipModification modification = new MembershipModification(role, curriculumElement, nextStatus,
					null, null, null, false, null);
			modifications.add(modification);
		}
		
		for(CurriculumElementMembership membership:row.getMemberships()) {
			CurriculumElement curriculumElement = keyToCurriculumElements.get(membership.getCurriculumElementKey());
			if(curriculumElement != null) {
				List<CurriculumRoles> roles = membership.getRoles();
				for(CurriculumRoles role:roles) {
					MembershipModification modification = new MembershipModification(role, curriculumElement, nextStatus,
							null, null, null, false, null);
					modifications.add(modification);
				}
			}
		}
		
		detailsCtrl.setModifications(modifications);
		
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	protected final void doCloseMemberDetails(RemoveMembershipRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private final UserInfoProfileConfig createProfilConfig() {
		UserInfoProfileConfig profileConfig = userInfoService.createProfileConfig();
		profileConfig.setChatEnabled(true);
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		return profileConfig;
	}
}
