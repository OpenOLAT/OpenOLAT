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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.event.EditMemberEvent;
import org.olat.repository.ui.list.ImplementationEvent;
import org.olat.repository.ui.list.ImplementationHeaderController;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.ui.OrderModification;
import org.olat.resource.accesscontrol.ui.OrdersController;
import org.olat.resource.accesscontrol.ui.OrdersSettings;
import org.olat.user.PortraitUser;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberDetailsController extends FormBasicController {

	private FormLink cancelButton;
	private FormLink acceptButton;
	private FormLink declineButton;
	private FormLink editMemberShipButton;
	
	private final Identity member;
	private final Curriculum curriculum;
	private final MemberDetailsConfig config;
	private final List<CurriculumElement> curriculumElements;
	private final CurriculumElement selectedCurriculumElement;
	
	private Object userObject;

	private CloseableModalController cmc;
	private final OrdersController ordersCtrl;
	private CancelMembershipsController cancelCtrl;
	private AcceptDeclineMembershipsController acceptCtrl;
	private MemberHistoryDetailsController historyDetailsCtrl;
	private final MemberRolesDetailsController rolesDetailsCtrl;

	@Autowired
	private ACService acService;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public MemberDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Curriculum curriculum, CurriculumElement selectedCurriculumElement, List<CurriculumElement> elements,
			Identity identity, MemberDetailsConfig config) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));

		this.config = config;
		this.member = identity;
		this.curriculum = curriculum;
		this.curriculumElements = elements;
		this.selectedCurriculumElement = selectedCurriculumElement;
		
		rolesDetailsCtrl = new MemberRolesDetailsController(ureq, getWindowControl(), rootForm,
				curriculum, selectedCurriculumElement, elements, member, config);
		listenTo(rolesDetailsCtrl);
		
		OrdersSettings settings = OrdersSettings.valueOf(config.withActivityColumns(),
				config.withOrdersDetails(), false, config.canEditOrder());
		ordersCtrl = new OrdersController(ureq, getWindowControl(), identity, selectedCurriculumElement.getResource(),
				settings, rootForm);
		listenTo(ordersCtrl);
		ordersCtrl.getInitialFormItem().setVisible(config.withOrders() && rolesDetailsCtrl.hasRoleParticipant());

		if(config.withHistory()) {
			historyDetailsCtrl = new MemberHistoryDetailsController(ureq, getWindowControl(), rootForm,
					selectedCurriculumElement, member);
			listenTo(historyDetailsCtrl);
		}
		
		initForm(ureq);
	}
	
	public List<MemberRolesDetailsRow> getRolesDetailsRows() {
		return rolesDetailsCtrl.getRolesDetailsRows();
	}
	
	public void setModifications(List<MembershipModification> modifications) {
		rolesDetailsCtrl.setModifications(modifications);
		ordersCtrl.getInitialFormItem().setVisible(config.withOrders() && rolesDetailsCtrl.hasRoleParticipant());
	}
	
	public void setOrderModifications(List<OrderModification> orderModifications) {
		if(ordersCtrl != null) {
			ordersCtrl.setModifications(orderModifications);
		}
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			if (config.profileConfig() != null) {
				// Profile
				PortraitUser memberPortraitUser = userPortraitService.createPortraitUser(getLocale(), member);
				MemberUserDetailsController profile = new MemberUserDetailsController(ureq, getWindowControl(), mainForm,
						member, config.profileConfig(), memberPortraitUser);
				listenTo(profile);
				layoutCont.put("profil", profile.getInitialComponent());

				String historyTitle = translate("details.history.title", "<small class='o_muted'>" + StringHelper.escapeHtml(selectedCurriculumElement.getDisplayName()) + "</small>" );
				layoutCont.contextPut("historyTitle", historyTitle);
			} else if (config.showImplementation() && selectedCurriculumElement != null){
				ImplementationHeaderController implementationHeaderController = new ImplementationHeaderController(ureq,
						getWindowControl(), selectedCurriculumElement);
				listenTo(implementationHeaderController);
				layoutCont.put("implementationDetails", implementationHeaderController.getInitialComponent());
			}
		}
		
		editMemberShipButton = uifactory.addFormLink("edit.member", formLayout, Link.BUTTON);
		editMemberShipButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editMemberShipButton.setVisible(config.withEdit());
		
		boolean showAcceptDeclineButtons = config.withAcceptDecline() && rolesDetailsCtrl.hasReservations();
		acceptButton = uifactory.addFormLink("accept", formLayout, Link.BUTTON);
		acceptButton.setIconLeftCSS("o_icon o_icon-fw o_icon_accepted");
		acceptButton.setVisible(showAcceptDeclineButtons);
		
		declineButton = uifactory.addFormLink("decline", formLayout, Link.BUTTON);
		declineButton.setIconLeftCSS("o_icon o_icon-fw o_icon_decline");
		declineButton.setVisible(showAcceptDeclineButtons);
		
		cancelButton = uifactory.addFormLink("cancel.booking", formLayout, Link.BUTTON);
		cancelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_circle_xmark");
		cancelButton.setVisible(showAcceptDeclineButtons && config.withOrders() && hasOngoingOrder());
	
		formLayout.add("roles", rolesDetailsCtrl.getInitialFormItem());
		if(ordersCtrl != null) {
			formLayout.add("orders", ordersCtrl.getInitialFormItem());
		}
		if(historyDetailsCtrl != null) {
			formLayout.add("history", historyDetailsCtrl.getInitialFormItem());
		}
	}
	
	private boolean hasOngoingOrder() {
		List<Order> ongoingOrders = acService.findOrders(member, selectedCurriculumElement.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		return !ongoingOrders.isEmpty();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(acceptCtrl == source || cancelCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		} else if (event instanceof ImplementationEvent) {
			fireEvent(ureq, event);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(acceptCtrl);
		removeAsListenerAndDispose(cancelCtrl);
		removeAsListenerAndDispose(cmc);
		acceptCtrl = null;
		cancelCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editMemberShipButton == source) {
			fireEvent(ureq, new EditMemberEvent(member));
		} else if(acceptButton == source) {
			doAccept(ureq);
		} else if(declineButton == source) {
			doDecline(ureq);
		} else if(cancelButton == source) {
			doCancel(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAccept(UserRequest ureq) {
		List<ResourceReservation> reservations = getPendingReservations();
		acceptCtrl = new AcceptDeclineMembershipsController(ureq, getWindowControl(),
				curriculum, selectedCurriculumElement, curriculumElements,
				reservations, GroupMembershipStatus.active);
		listenTo(acceptCtrl);
		
		String title = translate("accept.memberships");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), acceptCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDecline(UserRequest ureq) {
		List<ResourceReservation> reservations = getPendingReservations();
		acceptCtrl = new AcceptDeclineMembershipsController(ureq, getWindowControl(),
				curriculum, selectedCurriculumElement, curriculumElements,
				reservations, GroupMembershipStatus.declined);
		listenTo(acceptCtrl);
		
		String title = translate("decline.memberships");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), acceptCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCancel(UserRequest ureq) {
		List<ResourceReservation> reservations = getPendingReservations();
		cancelCtrl = new CancelMembershipsController(ureq, getWindowControl(),
				curriculum, selectedCurriculumElement, curriculumElements, List.of(member), reservations);
		listenTo(cancelCtrl);
		
		String title = translate("cancel.memberships");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), cancelCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private List<ResourceReservation> getPendingReservations() {
		final Set<OLATResource> resources = curriculumElements.stream()
				.map(CurriculumElement::getResource)
				.collect(Collectors.toSet());
		
		// Reservations
		List<ResourceReservation> reservations = acService.getReservations(member);
		return reservations.stream()
				.filter(reservation -> reservation.getResource() != null && resources.contains(reservation.getResource()))
				.toList();
	}
}
