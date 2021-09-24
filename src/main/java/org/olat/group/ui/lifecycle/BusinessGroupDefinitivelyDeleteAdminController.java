/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.group.ui.lifecycle;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupDefinitivelyDeleteAdminController extends FormBasicController {

	private SingleSelection enableDefinitivelyDeleteEl;
	private TextElement numberOfSoftDeleteDayDefinitivelyDeletionEl;

	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public BusinessGroupDefinitivelyDeleteAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.hard.delete.title");
		setFormDescription("admin.hard.delete.description");
		
		// day inactivity
		String daysBefore = Integer.toString(businessGroupModule.getNumberOfSoftDeleteDayBeforeDefinitivelyDelete());
		numberOfSoftDeleteDayDefinitivelyDeletionEl = uifactory.addTextElement("num.inactive.day.soft.deletion", "num.inactive.day.soft.deletion", 4, daysBefore, formLayout);
		initDays(numberOfSoftDeleteDayDefinitivelyDeletionEl, "num.inactive.day.soft.deletion.addon");
		
		SelectionValues modeValues = new SelectionValues();
		modeValues.add(new SelectionValue("auto", translate("mode.definitively.deletion.auto"), translate("mode.definitively.deletion.auto.desc")));
		modeValues.add(new SelectionValue("manual", translate("mode.definitively.deletion.manual"), translate("mode.definitively.deletion.manual.desc")));
		enableDefinitivelyDeleteEl = uifactory.addCardSingleSelectHorizontal("definitively.delete.mode", formLayout, modeValues.keys(), modeValues.values(), modeValues.descriptions(), null);
		enableDefinitivelyDeleteEl.addActionListener(FormEvent.ONCHANGE);
		
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupDefinitivelyDeleteEnabled();
		if(automaticEnabled) {
			enableDefinitivelyDeleteEl.select("auto", true);
		} else {
			enableDefinitivelyDeleteEl.select("manual", true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());	
	}
	
	private void initDays(TextElement textEl, String addOnKey) {
		textEl.setDisplaySize(6);
		textEl.setMaxLength(6);
		textEl.setElementCssClass("form-inline");
		textEl.setTextAddOn(addOnKey);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= BusinessGroupLifecycleUIHelper.validateInteger(numberOfSoftDeleteDayDefinitivelyDeletionEl, true);

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String mode = enableDefinitivelyDeleteEl.getSelectedKey();
		boolean automaticEnabled = "auto".equals(mode);
		businessGroupModule.setAutomaticGroupDefinitivelyDeleteEnabled(automaticEnabled ? "true" : "false");

		String daysBefore = numberOfSoftDeleteDayDefinitivelyDeletionEl.getValue();
		businessGroupModule.setNumberOfSoftDeleteDayBeforeDefinitivelyDelete(Integer.valueOf(daysBefore));
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
