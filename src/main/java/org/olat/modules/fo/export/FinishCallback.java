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

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.ICourse;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;

/**
 * Initial Date: 28.02.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class FinishCallback implements StepRunnerCallback {
	
	private static final Logger log = Tracing.createLoggerFor(FinishCallback.class);
	
	private final ForumManager forumManager;
	private final MailManager mailService;
	
	public FinishCallback() {
		forumManager = CoreSpringFactory.getImpl(ForumManager.class);
		mailService = CoreSpringFactory.getImpl(MailManager.class);		
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		FOCourseNode targetNode = (FOCourseNode)runContext.get(SendMailStepForm.FORUM);
		ICourse course = (ICourse)runContext.get(SendMailStepForm.ICOURSE);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		Forum chosenforum = targetNode.loadOrCreateForum(courseEnv);
		Message msgToMove = (Message)runContext.get(SendMailStepForm.MESSAGE_TO_MOVE);
		msgToMove = forumManager.getMessageById(msgToMove.getKey());
		Message parentMessage = (Message)runContext.get(SendMailStepForm.PARENT_MESSAGE);	
		if (parentMessage!= null) {
			parentMessage = forumManager.getMessageById(parentMessage.getKey());
		}
		if (msgToMove.getParentKey() == null && msgToMove.getThreadtop() == null) {
			forumManager.createOrAppendThreadInAnotherForum(msgToMove, chosenforum, parentMessage, ureq.getIdentity());
		} else {
			forumManager.moveMessageToAnotherForum(msgToMove, chosenforum, parentMessage, ureq.getIdentity());
		}
		DBFactory.getInstance().commit();//commit before sending event
		if (((Boolean)runContext.get(SendMailStepForm.SENDMAIL)).booleanValue()) {
			sendMail(ureq, wControl, runContext);
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private boolean sendMail(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		boolean success = false;
		try {
			ContactList contacts = new ContactList("Forum");
			ListWrapper recipients = (ListWrapper)runContext.get(SendMailStepForm.RECIPIENTS);
			contacts.addAllIdentites(recipients.getListOfIdentity());			
			MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			Identity sender = (Identity)runContext.get(SendMailStepForm.SENDER);
			bundle.setFromId(sender != null ? sender : ureq.getIdentity());
			bundle.setContactLists(new ArrayList<>(Arrays.asList(contacts)));
			MailTemplate mailTemplate = (MailTemplate)runContext.get(SendMailStepForm.MAIL_TEMPLATE);
			bundle.setContent(mailTemplate.getSubjectTemplate(), mailTemplate.getBodyTemplate());

			MailerResult result = mailService.sendMessage(bundle);
			return success = result.isSuccessful();

		} catch (Exception e) {
			log.error("", e);
			return success;
		}
	}
	

}
