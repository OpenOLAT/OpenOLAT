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
package org.olat.course.nodes.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.nodes.co.COToolRecipientsController.Config;
import org.olat.course.nodes.co.COToolRecipientsController.Recipients;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class COToolController extends BasicController {

	private static final Recipients[] PARTICIPANT_RECIPIENTS = new Recipients[] {Recipients.owners, Recipients.coaches};

	private final VelocityContainer mainVC;
	private final ContactFormController emailCtrl;
	private final COToolRecipientsController recipientCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final RepositoryEntry entry;

	@Autowired
	private RepositoryService repositoryService;

	public COToolController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		mainVC = createVelocityContainer("tool");
		
		Config recipientCtrlConfig = createRecipientCtrlConfigs(userCourseEnv);
		recipientCtrl = new COToolRecipientsController(ureq, wControl, recipientCtrlConfig);
		listenTo(recipientCtrl);
		mainVC.put("recipients", recipientCtrl.getInitialComponent());
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		// Empty contact lists are rejected by the constructor of the ContactMessage.
		// So we use a dummy list to have at least one recipient and set the real
		// contacts lists in a second step.
		ContactList dummyList = new ContactList("dummy");
		dummyList.add(getIdentity());
		cmsg.addEmailTo(dummyList);
		CourseMailTemplate template = new CourseMailTemplate(entry, getIdentity(), getLocale());
		String courseUrl = CourseMailTemplate.createCourseUrl(entry);
		String body = translate("tool.default.body", new String[] {courseUrl});
		template.setBodyTemplate(body);
		emailCtrl = new ContactFormController(ureq, getWindowControl(), false, false, false, cmsg, template);
		emailCtrl.setContactFormTitle(null);
		Set<Recipients> recipients = recipientCtrl.getSelectedRecipients();
		doSetReciepients(recipients);
		listenTo(emailCtrl);
		mainVC.put("email", emailCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	private Config createRecipientCtrlConfigs(UserCourseEnvironment userCourseEnv) {
		if (userCourseEnv.isAdmin() || userCourseEnv.isCoach()) {
			return new Config(true, Recipients.values());
		}
		return new Config(false, PARTICIPANT_RECIPIENTS);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == recipientCtrl && event == FormEvent.CHANGED_EVENT) {
			Set<Recipients> recipients = recipientCtrl.getSelectedRecipients();
			doSetReciepients(recipients);
		} else if (source == emailCtrl && (event == Event.DONE_EVENT || event == Event.FAILED_EVENT)) {
			recipientCtrl.setReadOnly();
			mainVC.setDirty(true);
		}
		super.event(ureq, source, event);
	}

	private void doSetReciepients(Set<Recipients> recipients) {
		List<ContactList> contactLists = getRecipientsLists(recipients);
		emailCtrl.setRecipientsLists(contactLists);
	}

	private List<ContactList> getRecipientsLists(Set<Recipients> recipients) {
		List<ContactList> contactLists = new ArrayList<>();
		if (recipients.contains(Recipients.participants)) {
			contactLists.add(getParticipantsContactList());
		}
		if (recipients.contains(Recipients.coaches)) {
			contactLists.add(getCoachesContactList());
		}
		if (recipients.contains(Recipients.owners)) {
			contactLists.add(getOwnersContactList());
		}
		return contactLists;
	}
	
	private ContactList getOwnersContactList() {
		ContactList cl = new ContactList(translate("tool.recipients.owners"));
		List<Identity> identities = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
		cl.addAllIdentites(identities);
		return cl;
	}
	
	private ContactList getCoachesContactList() {
		ContactList cl = new ContactList(translate("tool.recipients.coaches"));
		Collection<Identity> identities = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.coach.name());
		cl.addAllIdentites(identities);
		return cl;
	}
	
	private ContactList getParticipantsContactList() {
		ContactList cl = new ContactList(translate("tool.recipients.participants"));
		Collection<Identity> coachedIdentities = userCourseEnv.isAdmin()
				? repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.participant.name())
						.stream().distinct().collect(Collectors.toList())
				: repositoryService.getCoachedParticipants(getIdentity(), entry);
		cl.addAllIdentites(coachedIdentities);
		return cl;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
