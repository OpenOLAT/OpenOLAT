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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.ExternalLinkItem;
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
	
	private ExternalLinkItem inactiveStepLink;
	private ExternalLinkItem softDeleteStepLink;
	private ExternalLinkItem definitiveDeleteStepLink;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public BusinessGroupAdminProcessController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "process");
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		inactiveStepLink = uifactory.addExternalLink("inactive.step", "javascript:o_scrollToElement('#inactive');", "_self", formLayout);
		inactiveStepLink.setElementCssClass("btn btn-default btn-arrow-right");
		softDeleteStepLink = uifactory.addExternalLink("soft.step", "javascript:o_scrollToElement('#soft');", "_self", formLayout);
		softDeleteStepLink.setElementCssClass("btn btn-default btn-arrow-right");
		definitiveDeleteStepLink = uifactory.addExternalLink("definitive.step", "javascript:o_scrollToElement('#delete');", "_self", formLayout);
		definitiveDeleteStepLink.setElementCssClass("btn btn-default btn-arrow-right");
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
			mail = translate("process.with.email", Integer.toString(daysBeforeEmail));
		}
		
		String days = translate("process.inactive.days",
				Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeDeactivation()));

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
			mail = translate("process.with.email", Integer.toString(dayBeforeEmail));
		}
		String days = translate("process.soft.delete.days",
				Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete()));
		
		updateStep(softDeleteStepLink, translate("process.soft.delete.title"), mode, mail, days);
	}
	
	private void updateDefinitivelyDeleteStep() {
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupDefinitivelyDeleteEnabled();
	
		String mode = automaticEnabled ? translate("process.auto") : translate("process.manual");
		String mail = translate("process.without.email");
		String days = translate("process.definitive.delete.days",
				Integer.toString(businessGroupModule.getNumberOfSoftDeleteDayBeforeDefinitivelyDelete()));
		
		updateStep(definitiveDeleteStepLink, translate("process.definitive.delete.title"), mode, mail, days);
	}
	
	private void updateStep(ExternalLinkItem link, String title, String mode, String mail, String days) {
		StringBuilder sb = new StringBuilder();
		sb.append("<strong>").append(title).append("</strong><br>")
		  .append(mode).append(" - ").append(mail).append("<br>").append(days);
		link.setName(sb.toString());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
