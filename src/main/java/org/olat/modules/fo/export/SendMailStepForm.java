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
package org.olat.modules.fo.export;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailTemplateForm;
import org.olat.course.ICourse;
import org.olat.course.nodes.FOCourseNode;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 16.02.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class SendMailStepForm extends StepFormBasicController {

	protected static final String SENDMAIL = "sendmail";
	protected static final String RECIPIENTS = "recipients";
	protected static final String SENDER = "sender";
	protected static final String MAIL_TEMPLATE = "mailTemplate";
	protected static final String FORUM = "forum";
	public static final String START_COURSE = "startCourse";
	protected static final String PARENT_MESSAGE = "parentMessage";
	public static final String MESSAGE_TO_MOVE = "messageToMove";
	public static final String START_THREADTOP = "startMessage";
	protected static final String ICOURSE = "icourse";
	protected static final String COURSE = "course";
	protected static final String NEW_THREAD = "newThread";
	protected static final String COURSE_CHOSEN = "courseChosen";

	private MailTemplate mailTemplate;
	private MailTemplateForm templateForm;	
	
	private Message startMessage, parentMessage;
	
	@Autowired
	private ForumManager forumManager;

	
	public SendMailStepForm(UserRequest ureq, WindowControl wControl,
			Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(SendMailStepForm.class, getLocale(),
				Util.createPackageTranslator(RepositoryService.class, getLocale())));
		String comment = translate("forum.comment");
		String thread = translate("forum.thread");
		ICourse course = (ICourse)getFromRunContext(ICOURSE);		
		String courseTitle = course.getCourseTitle();
		RepositoryEntry startCourse = (RepositoryEntry)getFromRunContext(START_COURSE);
		String startCourseTitle = startCourse.getDisplayname();
		startMessage = (Message)getFromRunContext(MESSAGE_TO_MOVE);
		if (startMessage.getThreadtop() == null) {
			comment = thread;
		}
		String messageTitle = startMessage.getTitle();
		parentMessage = (Message)getFromRunContext(PARENT_MESSAGE);
		String parentMessageTitle = parentMessage != null ? parentMessage.getTitle() : startMessage.getTitle();
		FOCourseNode node = (FOCourseNode)getFromRunContext(FORUM);
		String targetForum = node.getShortTitle();
		
		String userName = getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null) + " "
				+ getIdentity().getUser().getProperty(UserConstants.LASTNAME, null);
		String email = getIdentity().getUser().getProperty(UserConstants.EMAIL, null);
		
		String[] subject = { comment, messageTitle };
		String[] body = { comment, messageTitle, startCourseTitle, courseTitle, targetForum, parentMessageTitle, userName, email };
		
		String subjectContent = translate("wizard.mail.subject", subject);
		String bodyContent = translate("wizard.mail.body", body);
		mailTemplate = createMailTemplate(subjectContent, bodyContent);		
				
		templateForm = new MailTemplateForm(ureq, wControl, mailTemplate, false, rootForm);
		initForm(ureq);
	}
	
	private MailTemplate createMailTemplate(String subject, String body) {		
		return new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// nothing to do
			}
		};
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(templateForm.getInitialFormItem());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (templateForm.sendMailSwitchEnabled()) {
			templateForm.updateTemplateFromForm(mailTemplate);
			addToRunContext(MAIL_TEMPLATE, mailTemplate);
			addToRunContext(SENDER, getIdentity());
			List<Identity> listOfIdentity = collectCreators();
			ListWrapper recipients = new ListWrapper(listOfIdentity);
			addToRunContext(RECIPIENTS, recipients);
			addToRunContext(SENDMAIL, Boolean.TRUE);
		} else {
			addToRunContext(SENDMAIL, Boolean.FALSE);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private List<Identity> collectCreators () {
		Set<Identity> setOfIdentity = new HashSet<>();
		if (startMessage.getThreadtop() == null) {
			List<Message> messages = forumManager.getTopMessageChildren(startMessage);
			// if  added to another thread, inform those creators and modifiers as well
			if (parentMessage != null && !parentMessage.equals(startMessage)) {
				List<Message> parentMessages = forumManager.getTopMessageChildren(parentMessage);
				messages.addAll(parentMessages);
			}
			// iterate all messages and extract distinct identities involved
			for (Message message : messages) {
				Identity creator = message.getCreator();
				if (creator != null) {
					setOfIdentity.add(creator);
				}
				Identity modifier = message.getModifier();
				if (modifier != null) {
					setOfIdentity.add(modifier);
				}
			}			
		} else {
			// only inform the message owner and possible modifier
			Identity creator = startMessage.getCreator(); 
			if (creator != null){
				setOfIdentity.add(creator);
			}
			Identity modifier = startMessage.getCreator(); 
			if (modifier != null){
				setOfIdentity.add(modifier);
			}
		}
		return setOfIdentity.stream().collect(Collectors.toList());
	}
	
}

