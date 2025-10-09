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

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.event.AcceptMembershipEvent;
import org.olat.modules.curriculum.ui.event.DeclineMembershipEvent;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 12 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmMembershipCalloutController extends FormBasicController {
	
	private FormLink acceptButton;
	private FormLink declineButton;
	private TextElement adminNoteEl;
	private SingleSelection applyToEl;
	
	private final Identity member;
	private final CurriculumRoles role;
	private final ResourceReservation reservation;
	private final CurriculumElement curriculumElement;
	private MembershipModification modification;

	public ConfirmMembershipCalloutController(UserRequest ureq, WindowControl wControl,
			Identity member, CurriculumRoles role, CurriculumElement curriculumElement, ResourceReservation reservation) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.role = role;
		this.member = member;
		this.reservation = reservation;
		this.curriculumElement = curriculumElement;
		initForm(ureq);
	}
	
	public MembershipModification getModification() {
		return modification;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String confirmedBy = "Hello world";
		boolean confirmationByParticipant = reservation != null
				&& reservation.getConfirmableBy() == ConfirmationByEnum.PARTICIPANT;
		if(confirmationByParticipant) {
			confirmedBy = translate("confirmation.by.participant");
		} else {
			confirmedBy = translate("confirmation.by.administrator");
		}
		uifactory.addStaticTextElement("confirmation.by", "confirmation.by", confirmedBy, formLayout);
		
		SelectionValues applyToPK = new SelectionValues();
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CONTAINED.name(), translate("apply.membership.to.contained")));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CURRENT.name(), translate("apply.membership.to.current")));
		applyToEl = uifactory.addRadiosVertical("apply.membership.to", "apply.membership.to", formLayout, applyToPK.keys(), applyToPK.values());
		
		adminNoteEl = uifactory.addTextAreaElement("admin.note", "admin.note", 2000, 4, 32, false, false, false, "", formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		acceptButton = uifactory.addFormLink("accept", buttonsCont, Link.BUTTON);
		acceptButton.setIconLeftCSS("o_icon o_icon_check");
		acceptButton.setVisible(!confirmationByParticipant);
		declineButton = uifactory.addFormLink("decline", buttonsCont, Link.BUTTON);
		declineButton.setIconLeftCSS("o_icon o_icon_decline");
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public CurriculumRoles getRole() {
		return role;
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	public Identity getMember() {
		return member;
	}
	
	public ChangeApplyToEnum getApplyTo() {
		return applyToEl.isOneSelected()
				? ChangeApplyToEnum.valueOf(applyToEl.getSelectedKey()) : ChangeApplyToEnum.CONTAINED;
	}
	
	public String getAdminNotes() {
		return adminNoteEl.getValue();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(acceptButton == source) {
			doAccept(ureq);
		} else if(declineButton == source) {
			doDecline(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		this.modification = null;
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doAccept(UserRequest ureq) {
		boolean applyToDescendants = getApplyTo() == ChangeApplyToEnum.CONTAINED;
		GroupMembershipStatus nextStatus = GroupMembershipStatus.active;
		modification = new MembershipModification(role, curriculumElement, nextStatus,
				null, null, null, applyToDescendants, adminNoteEl.getValue());
		fireEvent(ureq, new AcceptMembershipEvent());
	}
	
	private void doDecline(UserRequest ureq) {
		boolean applyToDescendants = getApplyTo() == ChangeApplyToEnum.CONTAINED;
		GroupMembershipStatus nextStatus = GroupMembershipStatus.declined;
		modification = new MembershipModification(role, curriculumElement, nextStatus,
				null, null, null, applyToDescendants, adminNoteEl.getValue());
		fireEvent(ureq, new DeclineMembershipEvent());
	}
}
