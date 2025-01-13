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

import java.util.Date;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.CurriculumManagerController;

/**
 * 
 * Initial date: 12 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeMembershipCalloutController extends FormBasicController {
	
	private static final GroupMembershipStatus[] DEFAULT_STATUS = {
			GroupMembershipStatus.active, GroupMembershipStatus.declined, GroupMembershipStatus.removed
	};
	
	private TextElement adminNoteEl;
	private SingleSelection applyToEl;
	private DateChooser confirmUntilEl;
	private SingleSelection nextStatusEl;
	private SingleSelection confirmationByEl;
	private SingleSelection confirmationTypeEl;
	
	private final Identity member;
	private final CurriculumRoles role;
	private final CurriculumElement curriculumElement;
	private final GroupMembershipStatus[] possibleStatus;
	
	private final boolean hasChildren;
	private final boolean confirmationPossible;
	private MembershipModification modification;

	public ChangeMembershipCalloutController(UserRequest ureq, WindowControl wControl,
			Identity member, CurriculumRoles role, CurriculumElement curriculumElement,
			GroupMembershipStatus[] possibleStatus, boolean hasChildren) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.role = role;
		this.member = member;
		this.hasChildren = hasChildren;
		this.possibleStatus = possibleStatus == null ? DEFAULT_STATUS : possibleStatus;
		this.confirmationPossible = (role == CurriculumRoles.participant);
		this.curriculumElement = curriculumElement;
		initForm(ureq);
		updateUI();
	}
	
	public MembershipModification getModification() {
		return modification;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues nextStatusPK = new SelectionValues();
		for(GroupMembershipStatus status:possibleStatus) {
			nextStatusPK.add(SelectionValues.entry(status.name(), translate("membership." + status.name())));
		}
		nextStatusEl = uifactory.addDropdownSingleselect("change.membership.to", formLayout, nextStatusPK.keys(), nextStatusPK.values());
		nextStatusEl.addActionListener(FormEvent.ONCHANGE);
		if(!nextStatusPK.isEmpty()) {
			nextStatusEl.select(nextStatusPK.keys()[0], true);
		}
		
		String suffix = Long.toString(CodeHelper.getRAMUniqueID());
		SelectionValues confirmationPK = new SelectionValues();
		confirmationPK.add(SelectionValues.entry(ConfirmationMembershipEnum.WITHOUT.name(), translate("confirmation.membership.without")));
		confirmationPK.add(SelectionValues.entry(ConfirmationMembershipEnum.WITH.name(), translate("confirmation.membership.with")));
		confirmationTypeEl = uifactory.addCardSingleSelectVertical("confirmation.membership", formLayout,
				confirmationPK.keys(), confirmationPK.values(), null, null);
		confirmationTypeEl.addActionListener(FormEvent.ONCLICK);
		confirmationTypeEl.select(ConfirmationMembershipEnum.WITHOUT.name(), true);
		confirmationTypeEl.setVisible(confirmationPossible);
		
		// confirmation by
		SelectionValues confirmationByPK = new SelectionValues();
		confirmationByPK.add(SelectionValues.entry(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), translate("confirmation.membership.by.admin")));
		confirmationByPK.add(SelectionValues.entry(ConfirmationByEnum.PARTICIPANT.name(), translate("confirmation.membership.by.participant")));
		confirmationByEl = uifactory.addRadiosVertical("confirmation.membership.by", formLayout,
				confirmationByPK.keys(), confirmationByPK.values());
		confirmationByEl.select(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), true);
		confirmationByEl.setVisible(confirmationPossible);
		
		// confirmation until
		confirmUntilEl = uifactory.addDateChooser("confirmation.until", "confirmation.until", null, formLayout);
		confirmUntilEl.setVisible(confirmationPossible);

		SelectionValues applyToPK = new SelectionValues();
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CONTAINED.name(), translate("apply.membership.to.contained")));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CURRENT.name(), translate("apply.membership.to.current")));
		applyToEl = uifactory.addRadiosVertical("apply.membership.to.".concat(suffix), "apply.membership.to", formLayout,
				applyToPK.keys(), applyToPK.values());
		applyToEl.select(ChangeApplyToEnum.CONTAINED.name(), true);
		applyToEl.setVisible(hasChildren);
		
		adminNoteEl = uifactory.addTextAreaElement("admin.note", "admin.note", 2000, 4, 32, false, false, false, "", formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("apply", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateUI() {
		GroupMembershipStatus selectedStatus = getNextStatus();
		boolean proposeConfirmation = selectedStatus == GroupMembershipStatus.reservation;
		confirmationTypeEl.setVisible(proposeConfirmation && confirmationPossible);
		if(!confirmationTypeEl.isOneSelected()) {
			confirmationTypeEl.select(ConfirmationMembershipEnum.WITHOUT.name(), true);
		}
		boolean withConfirmation = confirmationPossible
				&& confirmationTypeEl.isVisible() && confirmationTypeEl.isOneSelected()
				&& ConfirmationMembershipEnum.WITH.name().equals(confirmationTypeEl.getSelectedKey());
		confirmationByEl.setVisible(withConfirmation && confirmationPossible);
		confirmUntilEl.setVisible(withConfirmation && confirmationPossible);
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
	
	public GroupMembershipStatus getNextStatus() {
		return nextStatusEl.isOneSelected() ? GroupMembershipStatus.valueOf(nextStatusEl.getSelectedKey()) : null;
	}
	
	public ChangeApplyToEnum getApplyTo() {
		return applyToEl.isVisible() && applyToEl.isOneSelected()
				? ChangeApplyToEnum.valueOf(applyToEl.getSelectedKey()) : ChangeApplyToEnum.CONTAINED;
	}
	
	public String getAdminNotes() {
		return adminNoteEl.getValue();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(nextStatusEl == source || confirmationTypeEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean applyToDescendants = getApplyTo() == ChangeApplyToEnum.CONTAINED;
		
		GroupMembershipStatus nextStatus = GroupMembershipStatus.valueOf(nextStatusEl.getSelectedKey());
		
		ConfirmationMembershipEnum confirmation = confirmationTypeEl.isVisible() && confirmationTypeEl.isOneSelected()
				? ConfirmationMembershipEnum.valueOf(confirmationTypeEl.getSelectedKey())
				: ConfirmationMembershipEnum.WITHOUT;
		ConfirmationByEnum confirmationBy = confirmationByEl.isVisible()
				? ConfirmationByEnum.valueOf(confirmationByEl.getSelectedKey()) : null;
		Date confirmUntil = confirmUntilEl.isVisible() ? confirmUntilEl.getDate() : null;
		
		modification = new MembershipModification(role, curriculumElement, nextStatus,
				confirmation, confirmationBy, confirmUntil, applyToDescendants, adminNoteEl.getValue());
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		this.modification = null;
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
