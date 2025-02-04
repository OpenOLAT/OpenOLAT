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
import java.util.Date;
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
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.DualNumberCellRenderer;
import org.olat.modules.curriculum.ui.member.AcceptDeclineMembershipsTableModel.AcceptDeclineCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.ui.OrderModification;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AcceptDeclineMembershipsController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private TextElement adminNoteEl;
	private FormLink applyCustomNotificationButton;
	private FormLink applyWithoutNotificationButton;
	private FlexiTableElement tableEl;
	private AcceptDeclineMembershipsTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private final Curriculum curriculum;
	private final String avatarMapperBaseURL;
	private final GroupMembershipStatus nextStatus;
	private List<ResourceReservation> reservations;
	private List<CurriculumElement> curriculumElements;
	private final CurriculumElement selectedCurriculumElement;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);
	
	private CloseableModalController cmc;
	private CustomizeNotificationController customizeNotificationsCtrl;

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public AcceptDeclineMembershipsController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, CurriculumElement selectedCurriculumElement, List<CurriculumElement> curriculumElements,
			List<ResourceReservation> reservations, GroupMembershipStatus nextStatus) {
		super(ureq, wControl, "accept_memberships", Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.curriculum = curriculum;
		this.nextStatus = nextStatus;
		this.reservations = reservations;
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
		adminNoteEl = uifactory.addTextAreaElement("admin.note", "admin.note", 2000, 4, 32, false, false, false, "", formLayout);
		initButtonsForm(formLayout, ureq);
		initTableForm(formLayout);
	}

	private void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AcceptDeclineCols.modifications,
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
		
		if(nextStatus == GroupMembershipStatus.active) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AcceptDeclineCols.accepted,
					new DualNumberCellRenderer(getTranslator())));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AcceptDeclineCols.declined,
					new DualNumberCellRenderer(getTranslator())));
		}
		
		tableModel = new AcceptDeclineMembershipsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	private void initButtonsForm(FormItemContainer formLayout, UserRequest ureq) {	
		uifactory.addFormSubmitButton("apply", formLayout);
		applyCustomNotificationButton = uifactory.addFormLink("apply.custom.notifications", formLayout, Link.LINK);
		DropdownItem moreMenu = uifactory.addDropdownMenu("action.more", null, null, formLayout, getTranslator());
		moreMenu.setCarretIconCSS("o_icon o_icon_caret");
		moreMenu.setOrientation(DropdownOrientation.normal);
		moreMenu.addElement(applyCustomNotificationButton);
		
		applyWithoutNotificationButton = uifactory.addFormLink("apply.without.notifications", formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof AcceptDeclineMembershipRow memberRow
				&& memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}
	
	private void loadModel() {
		final List<AcceptDeclineMembershipRow> rows = new ArrayList<>(reservations.size());
		final Map<Identity,AcceptDeclineMembershipRow> rowsMap = new HashMap<>();
		for(ResourceReservation reservation:reservations) {
			Identity identity = reservation.getIdentity();
			AcceptDeclineMembershipRow adRow = rowsMap.computeIfAbsent(identity, id -> {
				AcceptDeclineMembershipRow row = new AcceptDeclineMembershipRow(identity, userPropertyHandlers, getLocale());
				rows.add(row);
				return row;
			});
			adRow.addReservation(reservation);
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
				doApplyWithCustomNotifications(ureq, customTemplate);
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
		if(applyWithoutNotificationButton == source) {
			doApply(ureq, new MailPackage(false));
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(applyCustomNotificationButton == source) {
			doCustomizeNotifications(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					AcceptDeclineMembershipRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseMemberDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenMemberDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				AcceptDeclineMembershipRow row = tableModel.getObject(toggleEvent.getRowIndex());
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
		doApplyWithNotification(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
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
	
	private void doApplyWithNotification(UserRequest ureq) {
		MailerResult result = new MailerResult();
		MailTemplate template = CurriculumMailing.getDefaultMailTemplate(curriculum, null, getIdentity());
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doApply(ureq, mailing);
	}
	
	private void doApplyWithCustomNotifications(UserRequest ureq, MailTemplate template) {
		MailerResult result = new MailerResult();
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doApply(ureq, mailing);
	}
	
	private void doApply(UserRequest ureq, MailPackage mailing) {
		String adminNote = adminNoteEl.getValue();
		
		Map<OLATResource, CurriculumElement> resourceToCurriculumElements = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getResource, ce -> ce, (u, v) -> u));
		
		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		for(ResourceReservation reservation:reservations) {
			Identity member = reservation.getIdentity();
			CurriculumRoles role = ResourceToRoleKey.reservationToRole(reservation.getType());
			CurriculumElement curriculumElement = resourceToCurriculumElements.get(reservation.getResource());
			if(role != null && curriculumElement != null) {
				CurriculumElementMembershipChange change = CurriculumElementMembershipChange
						.valueOf(member, curriculumElement);
				change.setNextStatus(role, nextStatus);
				change.setAdminNote(role, adminNote);
				changes.add(change);
			}
		}
		
		curriculumService.updateCurriculumElementMemberships(getIdentity(), ureq.getUserSession().getRoles(), changes, mailing);
	}
	
	private final void doOpenMemberDetails(UserRequest ureq, AcceptDeclineMembershipRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		CurriculumRoles role = CurriculumRoles.participant;
		UserInfoProfileConfig profileConfig = createProfilConfig();
		List<CurriculumRoles> rolesToSee = List.of(role);
		boolean withOrders = (nextStatus == GroupMembershipStatus.declined);
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, rolesToSee, false, false, false, true, true,
				withOrders, false, false);
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculum, selectedCurriculumElement, curriculumElements, row.getIdentity(), config);
		listenTo(detailsCtrl);
		
		List<MembershipModification> modifications = buildAcceptDeclineModification(role,row.getReservations());
		detailsCtrl.setModifications(modifications);
		
		if(withOrders) {
			List<Order> ongoingOrders = acService.findOrders(row.getIdentity(), selectedCurriculumElement.getResource(),
					OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
			List<OrderModification> orderModifications = ongoingOrders.stream()
					.map(order -> new OrderModification(order.getKey(), OrderStatus.CANCELED))
					.toList();
			detailsCtrl.setOrderModifications(orderModifications);
		}
		
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	private final List<MembershipModification> buildAcceptDeclineModification(CurriculumRoles role, List<ResourceReservation> rowReservations) {
		List<MembershipModification> modifications = new ArrayList<>();
		Map<OLATResource, CurriculumElement> resourceToCurriculumElements = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getResource, ce -> ce, (u, v) -> u));
		
		for(ResourceReservation reservation:rowReservations) {
			CurriculumRoles reservationRole = ResourceToRoleKey.reservationToRole(reservation.getType());
			if(role.equals(reservationRole)) {
				Date confirmationUntil = reservation.getExpirationDate();
				CurriculumElement curriculumElement = resourceToCurriculumElements.get(reservation.getResource());
				MembershipModification modification = new MembershipModification(role, curriculumElement, nextStatus,
						null, null, confirmationUntil, false, null);
				modifications.add(modification);
			}
		}
		
		return modifications;
	}
	
	protected final void doCloseMemberDetails(AcceptDeclineMembershipRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private final UserInfoProfileConfig createProfilConfig() {
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		return profileConfig;
	}
}
