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
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 12 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRemoveFeedbackMemberController extends FormBasicController {
	
	private FormLink deleteButton;

	private Position position;
	private Application application;
	private ApplicationFeedback feedback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ConfirmRemoveFeedbackMemberController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, ApplicationFeedback feedback) {
		super(ureq, wControl, "delete_feedback", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.feedback = feedback;
		this.position = position;
		this.application = application;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] args = new String[] {
			StringHelper.escapeHtml(RecruitingHelper.formatFullName(feedback.getIdentity())),		// 0
			StringHelper.escapeHtml(RecruitingHelper.formatFullName(application, getTranslator()))	// 1
		};
		String message = translate("confirm.remove.feedback.member", args);
		formLayout.contextPut("msg", message);

		deleteButton = uifactory.addFormLink("delete", "remove", null, formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			doDelete(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doDelete(UserRequest ureq) {
		String before = auditService.toAuditXml(feedback);
		String messageI18n = "audit.log.member.feedback.remove.member";
		String[] args = getLogArguments();
		auditService.auditFeedbackMemberLog(Action.remove, before, null, messageI18n, args, getTranslator(), position, application, null, getIdentity());
		
		feedbackService.deleteApplicationFeedback(feedback);
		dbInstance.commit();

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private String[] getLogArguments() {
		String appName = salutationGenerator.getTitleFullname(application, getLocale());
		String appId = application.getId() == null ? "" : application.getId().toString();
		String reviewer = RecruitingHelper.formatFullName(feedback.getIdentity());
		return new String[] { appName, appId, reviewer };
	}
}
