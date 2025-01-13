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
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.event.EditMemberEvent;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberDetailsController extends FormBasicController {

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
	private AcceptDeclineMembershipsController acceptCtrl;
	private MemberHistoryDetailsController historyDetailsCtrl;
	private final MemberRolesDetailsController rolesDetailsCtrl;

	@Autowired
	private ACService acService;
	@Autowired
	private UserInfoService userInfoService;
	
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
	}

	public void setVisibleRoles(List<CurriculumRoles> roles) {
		rolesDetailsCtrl.setVisibleRoles(roles);
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
			// Profile
			UserInfoProfile memberConfig = userInfoService.createProfile(member);
			MemberUserDetailsController profile = new MemberUserDetailsController(ureq, getWindowControl(), mainForm,
					member, config.profileConfig(), memberConfig);
			listenTo(profile);
			layoutCont.put("profil", profile.getInitialComponent());
		}
		
		editMemberShipButton = uifactory.addFormLink("edit.member", formLayout, Link.BUTTON);
		editMemberShipButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editMemberShipButton.setVisible(config.withEdit());
		
		boolean showAcceptDeclineButtons = config.withAcceptDecline() && rolesDetailsCtrl.hasReservations();
		acceptButton = uifactory.addFormLink("accept", formLayout, Link.BUTTON);
		acceptButton.setIconLeftCSS("o_icon o_icon-fw o_icon_check");
		acceptButton.setVisible(showAcceptDeclineButtons);
		
		declineButton = uifactory.addFormLink("decline", formLayout, Link.BUTTON);
		declineButton.setIconLeftCSS("o_icon o_icon-fw o_icon_decline");
		declineButton.setVisible(showAcceptDeclineButtons);
	
		formLayout.add("roles", rolesDetailsCtrl.getInitialFormItem());
		if(historyDetailsCtrl != null) {
			formLayout.add("history", historyDetailsCtrl.getInitialFormItem());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(acceptCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(acceptCtrl);
		removeAsListenerAndDispose(cmc);
		acceptCtrl = null;
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
