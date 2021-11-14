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
package org.olat.user.ui.absenceleave;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.user.AbsenceLeaveService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateAbsenceLeaveController extends FormBasicController {
	
	private static final String[] restrictKeys = new String[] { "restrict" };
	
	private DateChooser datesEl;
	private MultipleSelectionElement restrictEl;
	
	private final Identity absentIdentity;
	private final OLATResourceable resource;
	private final String subIdent;
	
	@Autowired
	private AbsenceLeaveService absenceLeaveService;
	
	public CreateAbsenceLeaveController(UserRequest ureq, WindowControl wControl, Identity absentIdentity) {
		super(ureq, wControl);
		this.absentIdentity = absentIdentity;
		resource = null;
		subIdent = null;
		initForm(ureq);
	}
	
	public CreateAbsenceLeaveController(UserRequest ureq, WindowControl wControl, Identity absentIdentity,
			OLATResourceable resource, String subIdent) {
		super(ureq, wControl);
		this.absentIdentity = absentIdentity;
		this.resource = resource;
		this.subIdent = subIdent;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		datesEl = uifactory.addDateChooser("dates", "absence.dates", null, formLayout);
		datesEl.setSecondDate(true);
		datesEl.setSeparator("absence.dates.separator");
		
		String[] restrictValues = new String[] { "" };
		restrictEl = uifactory.addCheckboxesHorizontal("resource", "absence.restrict.resource", formLayout, restrictKeys, restrictValues);
		restrictEl.setVisible(resource != null);
		if(resource != null) {
			restrictEl.select(restrictKeys[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		datesEl.clearError();
		if((datesEl.getDate() != null && datesEl.getSecondDate() == null)
				|| (datesEl.getDate() == null && datesEl.getSecondDate() != null)) {
			datesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(resource != null && restrictEl.isAtLeastSelected(1)) {
			absenceLeaveService.getOrCreateAbsenceLeave(absentIdentity,
					datesEl.getDate(), datesEl.getSecondDate(), resource, subIdent);
		} else {
			absenceLeaveService.getOrCreateAbsenceLeave(absentIdentity,
					datesEl.getDate(), datesEl.getSecondDate(), null, null);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
