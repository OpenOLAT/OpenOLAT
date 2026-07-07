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
package org.olat.modules.selectus.ui.mail;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackHelper;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailTemplateResetController extends FormBasicController {
	
	private FormLink deleteButton;
	
	private final String name;
	private Position position;
	private PositionMailTemplateRow templateRow;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionMailTemplateResetController(UserRequest ureq, WindowControl wControl,
			Position position, PositionMailTemplateRow templateRow, String name) {
		super(ureq, wControl, "confirm_reset", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.name = name;
		this.position = position;
		this.templateRow = templateRow;
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(StringHelper.containsNonWhitespace(templateRow.getLetterName())) {
				layoutCont.contextPut("msg", translate("confirm.reset.template.letter.text", new String[] { StringHelper.escapeHtml(name) }));
			} else {
				layoutCont.contextPut("msg", translate("confirm.reset.template.text", new String[] { StringHelper.escapeHtml(name) }));
			}
		}
		deleteButton = uifactory.addFormLink("reset", formLayout, Link.BUTTON);
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
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doDelete(UserRequest ureq) {
		
		position = recruitingService.getPosition(position.getKey());
		
		String before = auditService.toAuditXml(position);
		
		boolean update = true;
		Type type = templateRow.getType();
		if(type == Type.committeeReminder) {
			position.setCommitteeReminderMailTemplate(null);
			position.setCommitteeReminderMailLetter(null);
		} else if(type == Type.confirmationApplication) {
			position.setApplicationConfirmationMailTemplate(null);
			position.setApplicationConfirmationMailTemplateDe(null);
			position.setApplicationConfirmationMailTemplateFr(null);
			position.setApplicationConfirmationMailLetter(null);
		} else if(type == Type.confirmationApplicationWithRefereeManagement) {
			position.setApplicationConfirmationWithRefereeManagementMailTemplate(null);
			position.setApplicationConfirmationWithRefereeManagementMailTemplateDe(null);
			position.setApplicationConfirmationWithRefereeManagementMailTemplateFr(null);
			position.setApplicationConfirmationWithRefereeManagementMailLetter(null);
		} else if(type == Type.confirmationApplicationDuplicate) {
			position.setApplicationConfirmationDuplicateMailTemplate(null);
			position.setApplicationConfirmationDuplicateMailTemplateDe(null);
			position.setApplicationConfirmationDuplicateMailTemplateFr(null);
			position.setApplicationConfirmationDuplicateMailLetter(null);
		} else if(type == Type.expert) {
			position.setExpertRecommandationMailTemplate(null);
			position.setExpertRecommandationMailLetter(null);
		}  else if(type == Type.confirmationSubmissionExpert) {
			position.setExpertConfirmationSubmissionMailSubject(null);
			position.setExpertConfirmationSubmissionMailTemplate(null);
		} else if(type == Type.comparativeExpert) {
			position.setComparativeAssessmentExpertMailTemplate(null);
			position.setComparativeAssessmentExpertMailLetter(null);
		} else if(type == Type.confirmationSubmissionComparativeExpert) {
			position.setComparativeAssessmentExpertConfirmationSubmissionMailSubject(null);
			position.setComparativeAssessmentExpertConfirmationSubmissionMailTemplate(null);
		} else if(type == Type.referee) {
			position.setRefereeRecommandationMailTemplate(null);
			position.setRefereeRecommandationMailLetter(null);
		} else if(type == Type.confirmationSubmissionReferee) {
			position.setRefereeConfirmationSubmissionMailSubject(null);
			position.setRefereeConfirmationSubmissionMailTemplate(null);
		} else if(templateRow.getType() == Type.feedback) {
			String body = FeedbackHelper.getDefaultTemplateBodyHtml(position, salutationGenerator, getLocale());
			templateRow.getFeedbackConfiguration().setMailTemplate(body);
			templateRow.getFeedbackConfiguration().setMailLetter(null);
			feedbackService.updateApplicationsFeedbackConfiguration(templateRow.getFeedbackConfiguration());
		}  else if(templateRow.getMailTemplate() != null) {
			mailService.deleteTemplate(templateRow.getMailTemplate());
			update = false;
		}
		
		if(update) {
			position = recruitingService.savePosition(position);
		
			getLogger().info(Tracing.M_AUDIT, "Update position: {}", position.toStringFull());
			
			String after = auditService.toAuditXml(position);
			if(!before.equals(after)) {
				String messageI18n = "audit.log.position.change.configuration";
				String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
				auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
						messageI18n, messageArgs, getTranslator(), position, getIdentity());
			}
		}
		
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
