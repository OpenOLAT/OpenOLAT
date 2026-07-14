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
package org.olat.modules.selectus.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PrivacyDisclaimerController extends FormBasicController {
	
	private static final String[] acceptKeys = new String[] { "accept" };
	
	private MultipleSelectionElement acceptCheckbox;

	private final String staffEmail;
	private final String staffPositionEmail;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PrivacyDisclaimerController(UserRequest ureq, WindowControl wControl, String staffPositionEmail) {
		super(ureq, wControl, "privacy_disclaimer");
		staffEmail = recruitingModule.getStaffMail();
		this.staffPositionEmail = staffPositionEmail;
		initForm(ureq);
	}
	
	public PrivacyDisclaimerController(UserRequest ureq, WindowControl wControl, Identity authenticatedIdentity) {
		super(ureq, wControl, "privacy_disclaimer");
		
		staffEmail = recruitingModule.getStaffMail();
		staffPositionEmail= recruitingService.getPrivacyDisclaimerEmail(authenticatedIdentity);
		
		initForm(ureq);
	}
	
	public boolean isAccepted() {
		return acceptCheckbox.isAtLeastSelected(1);
	}
	
	public String disableLegend() {
		flc.contextPut("showLegend", Boolean.FALSE);
		return translate("disclaimer.privacy.title");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("showLegend", Boolean.TRUE);
			
			String[] i18nArguments = new String[] {
					staffEmail, staffPositionEmail
			};
			layoutCont.contextPut("i18nArguments", i18nArguments);
		}
		
		String[] acceptValues = new String[] { translate("disclaimer.privacy.accept") };
		acceptCheckbox = uifactory.addCheckboxesVertical("accept", null, formLayout, acceptKeys, acceptValues, 1);
		acceptCheckbox.setEscapeHtml(false);
		acceptCheckbox.setMandatory(false);
		
		uifactory.addFormSubmitButton("dcl.accept", "disclaimer.privacy.ok", formLayout);
		uifactory.addFormCancelButton("disclaimer.nok", formLayout, ureq, getWindowControl());		
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acceptCheckbox.clearError();
		if(!acceptCheckbox.isAtLeastSelected(1)) {
			acceptCheckbox.setErrorKey("sr.error.disclaimer.checkbox");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
