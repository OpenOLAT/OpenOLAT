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
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupAdminProcessController extends FormBasicController {
	
	private FormLink inactiveStepLink;
	private FormLink softDeleteStepLink;
	private FormLink definitiveDeleteStepLink;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public BusinessGroupAdminProcessController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "process");
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		inactiveStepLink = uifactory.addFormLink("inactive.step", "inactive.step", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		inactiveStepLink.setElementCssClass("btn-arrow-right");
		softDeleteStepLink = uifactory.addFormLink("soft.step", "inactive.step", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		softDeleteStepLink.setElementCssClass("btn-arrow-right");
		definitiveDeleteStepLink = uifactory.addFormLink("definitive.step", "inactive.step", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		definitiveDeleteStepLink.setElementCssClass("btn-arrow-right");
	}

	protected void updateUI() {
		updateInactiveStep();
		updateSofDeleteStep();
		updateDefinitivelyDeleteStep();
	}
	
	private void updateInactiveStep() {
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupInactivationEnabled();
		int daysBeforeEmail = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		
		String mode = automaticEnabled ? translate("process.auto") : translate("process.manual");
		String mail;
		if(daysBeforeEmail <= 0) {
			mail = translate("process.without.email");
		} else {
			mail = translate("process.with.email", new String[] {Integer.toString(daysBeforeEmail) });
		}
		
		String days = translate("process.inactive.days",
				new String[] { Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeDeactivation()) });

		updateStep(inactiveStepLink, translate("process.inactive.title"), mode, mail, days);
	}
	
	private void updateSofDeleteStep() {
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupSoftDeleteEnabled();
		int dayBeforeEmail = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
		
		String mode = automaticEnabled ? translate("process.auto") : translate("process.manual");
		String mail;
		if(dayBeforeEmail <= 0) {
			mail = translate("process.without.email");
		} else {
			mail = translate("process.with.email", new String[] {Integer.toString(dayBeforeEmail) });
		}
		String days = translate("process.soft.delete.days",
				new String[] { Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete()) });
		
		updateStep(softDeleteStepLink, translate("process.soft.delete.title"), mode, mail, days);
	}
	
	private void updateDefinitivelyDeleteStep() {
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupDefinitivelyDeleteEnabled();
	
		String mode = automaticEnabled ? translate("process.auto") : translate("process.manual");
		String mail = translate("process.without.email");
		String days = translate("process.definitive.delete.days",
				new String[] { Integer.toString(businessGroupModule.getNumberOfSoftDeleteDayBeforeDefinitivelyDelete()) });
		
		updateStep(definitiveDeleteStepLink, translate("process.definitive.delete.title"), mode, mail, days);
	}
	
	private void updateStep(FormLink link, String title, String mode, String mail, String days) {
		StringBuilder sb = new StringBuilder();
		sb.append("<strong>").append(title).append("</strong><br>")
		  .append(mode).append(" - ").append(mail).append("<br>").append(days);
		link.getComponent().setCustomDisplayText(sb.toString());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
