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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.BadgeOrganization;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-06-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LinkedInOrganizationEditController extends FormBasicController {

	private final BadgeOrganization badgeOrganization;
	private IntegerElement organizationIdEl;
	private TextElement organizationNameEl;

	@Autowired
	OpenBadgesManager openBadgesManager;

	public LinkedInOrganizationEditController(UserRequest ureq, WindowControl wControl, BadgeOrganization badgeOrganization) {
		super(ureq, wControl);
		this.badgeOrganization = badgeOrganization;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int organizationId = 0;
		if (badgeOrganization != null) {
			organizationId = Integer.parseInt(badgeOrganization.getOrganizationKey());
		}
		organizationIdEl = uifactory.addIntegerElement("organizationId", "organization.id",
				organizationId, formLayout);
		organizationIdEl.setMinValueCheck(0, "error.integer.positive");
		organizationIdEl.setMandatory(true);

		String organizationName = "";
		if (badgeOrganization != null) {
			organizationName = badgeOrganization.getOrganizationValue();
		}
		organizationNameEl = uifactory.addTextElement("organizationName", "organization.name",
				255, organizationName, formLayout);
		organizationNameEl.setMandatory(true);

		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttons);
		uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttons);
	}


	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		organizationNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(organizationNameEl.getValue())) {
			organizationNameEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String organizationId = organizationIdEl.getValue();
		String organizationName = organizationNameEl.getValue();

		if (badgeOrganization != null) {
			badgeOrganization.setOrganizationKey(organizationId);
			badgeOrganization.setOrganizationValue(organizationName);
			openBadgesManager.updateBadgeOrganization(badgeOrganization);
		} else {
			openBadgesManager.addLinkedInOrganization(organizationId, organizationName);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
