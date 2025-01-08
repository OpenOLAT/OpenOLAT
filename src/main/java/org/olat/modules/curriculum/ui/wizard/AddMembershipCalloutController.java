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
package org.olat.modules.curriculum.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.member.ChangeApplyToEnum;
import org.olat.modules.curriculum.ui.member.MembershipModification;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMembershipCalloutController extends FormBasicController {
	
	private TextElement adminNoteEl;
	private SingleSelection applyToEl;
	
	private MembershipModification modification;
	
	private final boolean hasChildren;
	private final CurriculumRoles role;
	private final CurriculumElement curriculumElement;
	
	public AddMembershipCalloutController(UserRequest ureq, WindowControl wControl,
			CurriculumRoles role, CurriculumElement curriculumElement, boolean hasChildren) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.role = role;
		this.hasChildren = hasChildren;
		this.curriculumElement = curriculumElement;
		initForm(ureq);
	}
	
	public MembershipModification getModification() {
		return modification;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String suffix = Long.toString(CodeHelper.getRAMUniqueID());
		// apply to
		SelectionValues applyToPK = new SelectionValues();
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CONTAINED.name(), translate("apply.membership.to.contained")));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CURRENT.name(), translate("apply.membership.to.current")));
		applyToEl = uifactory.addRadiosVertical("apply.membership.to." + suffix, "apply.membership.to", formLayout, applyToPK.keys(), applyToPK.values());
		applyToEl.select(ChangeApplyToEnum.CONTAINED.name(), true);
		applyToEl.setVisible(hasChildren);
		
		adminNoteEl = uifactory.addTextAreaElement("admin.note." + suffix, "admin.note", 2000, 4, 32, false, false, false, "", formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("add", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String adminNote = adminNoteEl.getValue();
		boolean applyToDescendants = applyToEl.isVisible() && applyToEl.isOneSelected()
				&& ChangeApplyToEnum.CONTAINED.name().equals(applyToEl.getSelectedKey());

		modification = new MembershipModification(role, curriculumElement, null,
				null, null, null, applyToDescendants, adminNote);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		this.modification = null;
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
