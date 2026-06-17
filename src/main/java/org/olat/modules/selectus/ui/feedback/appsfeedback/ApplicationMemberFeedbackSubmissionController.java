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

import java.util.Date;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
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
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 4 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationMemberFeedbackSubmissionController extends FormBasicController {
	
	private TextElement feedbackEl;
	
	private final Position position;
	private final Application application;
	private ApplicationFeedback feedback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ApplicationMemberFeedbackSubmissionController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, ApplicationFeedback feedback) {
		super(ureq, wControl, "feedback_submission", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		this.feedback = feedback;
		initForm(ureq);
	}
	
	public ApplicationFeedback getFeedback() {
		return feedback;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String text = feedback.getComment();
		feedbackEl = uifactory.addTextAreaElement("apps.feedback.area", 8, 60, text, formLayout);

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Action action;
		String before;
		String messageI18n;
		if(StringHelper.containsNonWhitespace(feedback.getComment())) {
			action = Action.update;
			before = auditService.toAuditXml(feedback);
			messageI18n = "audit.log.member.feedback.update";
		} else {
			action = Action.add;
			before = null;
			messageI18n = "audit.log.member.feedback.add";
		}
		
		feedback.setComment(feedbackEl.getValue());
		feedback.setCommentDate(new Date());
		feedback.setReferenceStatus(ReferenceStatus.submitted);
		feedback = feedbackService.updateApplicationFeedback(feedback);
		dbInstance.commit();
		
		String after = auditService.toAuditXml(feedback);
		String[] args = getLogArguments();
		Identity doer = getIdentity() instanceof TransientIdentity ? null : getIdentity();
		auditService.auditFeedbackLog(action, before, after, messageI18n, args, getTranslator(), position, application, feedback, doer);
		
		getWindowControl().setInfo(translate("apps.feedback.area.info.confirmation"));
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private String[] getLogArguments() {
		String appName = salutationGenerator.getTitleFullname(application, getLocale());
		String appId = application.getId() == null ? "" : application.getId().toString();
		String reviewer = RecruitingHelper.formatFullName(getIdentity());
		return new String[] { appName, appId, reviewer };
	}
}
