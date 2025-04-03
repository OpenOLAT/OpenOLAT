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
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.DualNumberCellRenderer;
import org.olat.modules.curriculum.ui.member.CancelMembershipsTableModel.CancelCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.ui.OrderModification;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Cancel ongoing orders.
 * 
 * Initial date: 20 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CancelMembershipsController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private TextElement adminNoteEl;
	private FormLink applyCustomNotificationButton;
	private FormLink applyWithoutNotificationButton;
	private FlexiTableElement tableEl;
	private CancelMembershipsTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private final Curriculum curriculum;
	private final List<Identity> identities;
	private final String avatarMapperBaseURL;
	private final CurriculumRoles roleToModify;
	private final List<ResourceReservation> reservations;
	private List<CurriculumElement> curriculumElements;
	private final CurriculumElement selectedCurriculumElement;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper();
	
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
	
	public CancelMembershipsController(UserRequest ureq, WindowControl wControl, Curriculum curriculum,
			CurriculumElement selectedCurriculumElement, List<CurriculumElement> curriculumElements,
			List<Identity> identities, List<ResourceReservation> reservations) {
		super(ureq, wControl, "accept_memberships", Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.curriculum = curriculum;
		this.identities = identities;
		this.reservations = reservations;
		this.curriculumElements = curriculumElements;
		this.selectedCurriculumElement = selectedCurriculumElement;
		roleToModify = CurriculumRoles.participant;

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
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CancelCols.modifications,
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
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CancelCols.cancelled,
				new DualNumberCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CancelCols.cancellationFee));
		
		tableModel = new CancelMembershipsTableModel(columnsModel, curriculumElements, getLocale());
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
		if(rowObject instanceof CancelMembershipRow memberRow
				&& memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}
	
	private void loadModel() {
		final Map<Long,CancelMembershipRow> rowsMap = new HashMap<>();
		final List<CancelMembershipRow> rows = new ArrayList<>(identities.size());
		final List<Order> ongoingOrders = acService.findOrders(selectedCurriculumElement.getResource(),
				OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		final Map<Long,CurriculumElement> elementsToKeys = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getKey, el -> el, (u, v) -> u));
		final List<CurriculumElementMembership> memberships = curriculumService
				.getCurriculumElementMemberships(curriculumElements, identities);

		for(Identity identity:identities) {
			CancelMembershipRow row = new CancelMembershipRow(identity, userPropertyHandlers, getLocale());
			rows.add(row);
			rowsMap.put(identity.getKey(), row);
		}
		
		// Map the reservations
		for(ResourceReservation reservation:reservations) {
			CancelMembershipRow row = rowsMap.get(reservation.getIdentity().getKey());
			if(row != null && roleToModify == ResourceToRoleKey.reservationToRole(reservation.getType())) {
				row.addReservation(reservation);
			}
		}
		
		// Map of orders
		for(Order order:ongoingOrders) {
			CancelMembershipRow row = rowsMap.get(order.getDelivery().getKey());
			if(row != null) {
				row.addOrder(order);
			}
		}
		
		// Memberships of the curriculum elements
		for(CurriculumElementMembership membership:memberships) {
			CancelMembershipRow row = rowsMap.get(membership.getIdentityKey());
			CurriculumElement curriculumElement = elementsToKeys.get(membership.getCurriculumElementKey());
			if(row != null && curriculumElement != null && containsRole(membership.getRoles())) {
				row.addMemberships(curriculumElement);
			}
		}
		
		// Calculate cancellation fees
		for(CancelMembershipRow row:rows) {
			Price cancellationFee = acService.getCancellationFee(selectedCurriculumElement.getResource(),
					selectedCurriculumElement.getBeginDate(), row.getOngoingOrders());
			row.setCancellationFee(cancellationFee);	
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private boolean containsRole(List<CurriculumRoles> roles) {
		if(roles == null || roles.isEmpty()) return false;
		for(CurriculumRoles role:roles) {
			if(roleToModify == role) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(customizeNotificationsCtrl == source) {
			MailTemplate customTemplate = customizeNotificationsCtrl.getMailTemplate();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doApplyWithCustomNotifications(customTemplate);
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
			doCancelMemberships(new MailPackage(false));
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(applyCustomNotificationButton == source) {
			doCustomizeNotifications(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					CancelMembershipRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseMemberDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenMemberDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				CancelMembershipRow row = tableModel.getObject(toggleEvent.getRowIndex());
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
		doApplyWithNotification();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doCustomizeNotifications(UserRequest ureq) {
		Map<Long,Price> cancellationFees = getCancellationFees();
		MailTemplate template = CurriculumMailing.getMembershipCancelledTemplate(curriculum, selectedCurriculumElement,
				cancellationFees, getIdentity());
		customizeNotificationsCtrl = new CustomizeNotificationController(ureq, getWindowControl(), template);
		listenTo(customizeNotificationsCtrl);
		
		String title = translate("customize.notifications.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), customizeNotificationsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doApplyWithNotification() {
		MailerResult result = new MailerResult();
		Map<Long,Price> cancellationFees = getCancellationFees();
		MailTemplate template = CurriculumMailing.getMembershipCancelledTemplate(curriculum, selectedCurriculumElement,
				cancellationFees, getIdentity());
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doCancelMemberships(mailing);
	}
	
	private void doApplyWithCustomNotifications(MailTemplate template) {
		MailerResult result = new MailerResult();
		MailPackage mailing = new MailPackage(template, result, (MailContext)null, template != null);
		doCancelMemberships(mailing);
	}
	
	private Map<Long,Price> getCancellationFees() {
		Map<Long,Price> fees = new HashMap<>();
		
		List<CancelMembershipRow> rows = tableModel.getObjects();
		for(CancelMembershipRow row:rows) {
			Price fee = row.getCancellationFee();
			if(fee != null) {
				fees.put(row.getIdentityKey(), fee);
			}
		}
		
		return fees;
	}
	
	private void doCancelMemberships(MailPackage mailing) {
		String adminNote = adminNoteEl.getValue();

		List<CancelMembershipRow> rows = tableModel.getObjects();
		for(CancelMembershipRow row:rows) {
			List<Order> ordersToCancel = row.getOngoingOrders();
			for(Order order:ordersToCancel) {
				acService.cancelOrder(order, getIdentity(), adminNote, mailing);
			}
		}
	}
	
	private final void doOpenMemberDetails(UserRequest ureq, CancelMembershipRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		CurriculumRoles role = CurriculumRoles.participant;
		UserInfoProfileConfig profileConfig = createProfilConfig();
		List<CurriculumRoles> rolesToSee = List.of(role);
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, rolesToSee, false, false, false, true, true,
				true, false, false);
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculum, selectedCurriculumElement, curriculumElements, row.getIdentity(), config);
		listenTo(detailsCtrl);
		
		List<MembershipModification> modifications = buildCancelModification(role, row.getOngoingOrders(), row.getReservations());
		detailsCtrl.setModifications(modifications);
		
		List<Order> ongoingOrders = acService.findOrders(row.getIdentity(), selectedCurriculumElement.getResource(),
				OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		List<OrderModification> orderModifications = ongoingOrders.stream()
				.map(order -> new OrderModification(order.getKey(), OrderStatus.CANCELED,
						acService.getCancellationFee(selectedCurriculumElement.getResource(), selectedCurriculumElement.getBeginDate(), List.of(order))))
				.toList();
		detailsCtrl.setOrderModifications(orderModifications);
		
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	private final List<MembershipModification> buildCancelModification(CurriculumRoles role, List<Order> orders, List<ResourceReservation> rowReservations) {
		List<MembershipModification> modifications = new ArrayList<>();
		Map<OLATResource, ResourceReservation> resourceToCurriculumElements = rowReservations.stream()
				.collect(Collectors.toMap(ResourceReservation::getResource, ce -> ce, (u, v) -> u));
		
		Price cancellationFee = acService.getCancellationFee(selectedCurriculumElement.getResource(),
				selectedCurriculumElement.getBeginDate(), orders);
		GroupMembershipStatus nextStatus = cancellationFee == null ? GroupMembershipStatus.cancel : GroupMembershipStatus.cancelWithFee;
		
		for(CurriculumElement curriculumElement:curriculumElements) {
			ResourceReservation reservation = resourceToCurriculumElements.get(curriculumElement.getResource());
			
			Date confirmationUntil = reservation == null ? null : reservation.getExpirationDate();
			ConfirmationByEnum confirmation = ConfirmationByEnum.valueOf(reservation);
			
			MembershipModification modification = new MembershipModification(role, curriculumElement, nextStatus,
					null, confirmation, confirmationUntil, false, null);
			modifications.add(modification);
		}
		return modifications;
	}
	
	protected final void doCloseMemberDetails(CancelMembershipRow row) {
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
