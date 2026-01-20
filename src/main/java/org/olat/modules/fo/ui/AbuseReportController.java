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
package org.olat.modules.fo.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for reporting inappropriate forum messages.
 * 
 * Initial date: January 2026<br>
 * @author OpenOLAT Community
 */
public class AbuseReportController extends FormBasicController {
	
	private TextElement reasonEl;
	
	private final Message message;
	
	@Autowired
	private ForumManager forumManager;
	
	public AbuseReportController(UserRequest ureq, WindowControl wControl, Message message) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		this.message = message;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("msg.report.abuse.reason");
		
		reasonEl = uifactory.addTextAreaElement("reason", null, 2000, 5, 60, false, false, "", formLayout);
		reasonEl.setPlaceholderKey("msg.report.abuse.reason.placeholder", null);
		reasonEl.setMandatory(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("msg.report.abuse.submit", buttonLayout);
		uifactory.addFormCancelButton("msg.report.abuse.cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		reasonEl.clearError();
		if (!StringHelper.containsNonWhitespace(reasonEl.getValue())) {
			reasonEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String reason = reasonEl.getValue();
		
		// Check if user has already reported this message
		if (forumManager.hasUserReportedMessage(message.getKey(), getIdentity().getKey())) {
			showWarning("msg.report.abuse.already.reported");
			fireEvent(ureq, Event.CANCELLED_EVENT);
			return;
		}
		
		// Create the abuse report
		try {
			forumManager.createAbuseReport(message, getIdentity(), reason);
			showInfo("msg.report.abuse.success");
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (Exception e) {
			logError("Error creating abuse report", e);
			showError("msg.report.abuse.error");
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
