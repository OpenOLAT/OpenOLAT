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
package org.olat.course.nodes.members;

import static org.olat.course.nodes.members.MembersCourseNodeEditController.CONFIG_KEY_EMAIL_FUNCTION;
import static org.olat.course.nodes.members.MembersCourseNodeEditController.CONFIG_KEY_SHOWCOACHES;
import static org.olat.course.nodes.members.MembersCourseNodeEditController.CONFIG_KEY_SHOWOWNER;
import static org.olat.course.nodes.members.MembersCourseNodeEditController.CONFIG_KEY_SHOWPARTICIPANTS;
import static org.olat.course.nodes.members.MembersCourseNodeEditController.EMAIL_FUNCTION_ALL;
import static org.olat.course.nodes.members.MembersCourseNodeEditController.EMAIL_FUNCTION_COACH_ADMIN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * The run controller show the list of members of the course
 * 
 * <P>
 * Initial Date:  11 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersCourseNodeRunController extends FormBasicController {

	
	private final CourseEnvironment courseEnv;
	private final DisplayPortraitManager portraitManager;
	private final String avatarBaseURL;
	
	private FormLink ownersEmailLink;
	private FormLink coachesEmailLink;
	private FormLink participantsEmailLink;
	private List<FormLink> memberLinks = new ArrayList<FormLink>();
	private List<FormLink> emailLinks = new ArrayList<FormLink>();
	
	private List<FormLink> ownerLinks;
	private List<FormLink> coachesLinks;
	private List<FormLink> participantsLinks;

	private final boolean canEmail;
	private final boolean showOwners;
	private final boolean showCoaches;
	private final boolean showParticipants;

	private ContactFormController emailController;
	private CloseableModalController cmc;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;

	public MembersCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, ModuleConfiguration config) {
		super(ureq, wControl, "members");

		courseEnv = userCourseEnv.getCourseEnvironment();
		avatarBaseURL = registerCacheableMapper(ureq, "avatars-members", new UserAvatarMapper(true));
		portraitManager = DisplayPortraitManager.getInstance();

		showOwners = config.getBooleanSafe(CONFIG_KEY_SHOWOWNER);
		showCoaches = config.getBooleanSafe(CONFIG_KEY_SHOWCOACHES);
		showParticipants = config.getBooleanSafe(CONFIG_KEY_SHOWPARTICIPANTS);
		
		String emailFct = config.getStringValue(CONFIG_KEY_EMAIL_FUNCTION, EMAIL_FUNCTION_COACH_ADMIN);
		canEmail = EMAIL_FUNCTION_ALL.equals(emailFct) || userCourseEnv.isAdmin() || userCourseEnv.isCoach();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<Identity> owners;
		if(showOwners) {
			owners = getOwners();
		} else {
			owners = Collections.emptyList();
		}
		
		List<Identity> coaches = new ArrayList<>();
		if(showCoaches) {
			CourseGroupManager cgm = courseEnv.getCourseGroupManager();
			coaches.addAll(cgm.getCoachesFromBusinessGroups());
			coaches.addAll(cgm.getCoaches());
		}
		
		List<Identity> participants = new ArrayList<>();
		if(showParticipants) {
			CourseGroupManager cgm = courseEnv.getCourseGroupManager();
			participants.addAll(cgm.getParticipantsFromBusinessGroups());
			participants.addAll(cgm.getParticipants());
		}

		Comparator<Identity> idComparator = new IdentityComparator();
		Collections.sort(owners, idComparator);
		Collections.sort(coaches, idComparator);
		Collections.sort(participants, idComparator);
		
		if(canEmail) {
			ownersEmailLink = uifactory.addFormLink("owners-email", "members.email.title", null, formLayout, Link.BUTTON_XSMALL);
			ownersEmailLink.setIconLeftCSS("o_icon o_icon_mail");
			coachesEmailLink = uifactory.addFormLink("coaches-email", "members.email.title", null, formLayout, Link.BUTTON_XSMALL);
			coachesEmailLink.setIconLeftCSS("o_icon o_icon_mail");
			participantsEmailLink = uifactory.addFormLink("participants-email", "members.email.title", null, formLayout, Link.BUTTON_XSMALL);
			participantsEmailLink.setIconLeftCSS("o_icon o_icon_mail");
			
			formLayout.add("owners-email", ownersEmailLink);
			formLayout.add("coaches-email", coachesEmailLink);
			formLayout.add("participants-email", participantsEmailLink);
		}

		Set<Long> duplicateCatcher = new HashSet<Long>();
		ownerLinks = initFormMemberList("owners", owners, duplicateCatcher, formLayout, canEmail);
		coachesLinks = initFormMemberList("coaches", coaches, duplicateCatcher, formLayout, canEmail);
		participantsLinks = initFormMemberList("participants", participants, duplicateCatcher, formLayout, canEmail);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("showOwners", showOwners);
			layoutCont.contextPut("hasOwners", new Boolean(!ownerLinks.isEmpty()));
			layoutCont.contextPut("showCoaches", showCoaches);
			layoutCont.contextPut("hasCoaches", new Boolean(!coachesLinks.isEmpty()));
			layoutCont.contextPut("showParticipants", showParticipants);
			layoutCont.contextPut("hasParticipants", new Boolean(!participantsLinks.isEmpty()));
		}
	}
	
	private List<Identity> getOwners() {
		RepositoryEntry courseRepositoryEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		return repositoryService.getMembers(courseRepositoryEntry, GroupRoles.owner.name());
	}
	
	private List<FormLink> initFormMemberList(String name, List<Identity> ids, Set<Long> duplicateCatcher, FormItemContainer formLayout, boolean withEmail) {
		String page = velocity_root + "/memberList.html";
		
		FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout(name, getTranslator(), page);
		formLayout.add(name, container);
		container.setRootForm(mainForm);

		List<FormLink> links = createMemberLinks(ids, duplicateCatcher, container, withEmail);
		container.contextPut("memberLinks", links);
		container.contextPut("avatarBaseURL", avatarBaseURL);
		return links;
	}
	
	protected List<FormLink> createMemberLinks(List<Identity> identities, Set<Long> duplicateCatcher, FormLayoutContainer formLayout, boolean withEmail) {
		List<FormLink> idLinks = new ArrayList<FormLink>();
		for(Identity identity:identities) {
			if(duplicateCatcher.contains(identity.getKey())) continue;
			
			Member member = createMember(identity);
			String fullname = StringHelper.escapeHtml(member.getFullName());
			FormLink idLink = uifactory.addFormLink("id_" + identity.getKey(), fullname, null, formLayout, Link.NONTRANSLATED);
			idLink.setUserObject(member);
			idLinks.add(idLink);
			formLayout.add(idLink.getComponent().getComponentName(), idLink);
			memberLinks.add(idLink);
			
			if(withEmail) {
				FormLink emailLink = uifactory.addFormLink("mail_" + identity.getKey(), "", null, formLayout, Link.NONTRANSLATED);
				emailLink.setUserObject(member);
				emailLink.setIconLeftCSS("o_icon o_icon_mail o_icon-lg");
				emailLink.setElementCssClass("o_mail");
				formLayout.add(emailLink.getComponent().getComponentName(), emailLink);
				emailLinks.add(emailLink);
				member.setEmailLink(emailLink);
			}
			duplicateCatcher.add(identity.getKey());
		}
		return idLinks;
	}
	
	protected Member createMember(Identity identity) {
		User user = identity.getUser();
		String firstname = user.getProperty(UserConstants.FIRSTNAME, null);
		String lastname = user.getProperty(UserConstants.LASTNAME, null);
		MediaResource rsrc = portraitManager.getSmallPortraitResource(identity.getName());
		
		String portraitCssClass = null;
		String gender = identity.getUser().getProperty(UserConstants.GENDER, Locale.ENGLISH);
		if (gender.equalsIgnoreCase("male")) {
			portraitCssClass = DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS;
		} else if (gender.equalsIgnoreCase("female")) {
			portraitCssClass = DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS;
		} else {
			portraitCssClass = DisplayPortraitManager.DUMMY_BIG_CSS_CLASS;
		}
		String fullname = userManager.getUserDisplayName(identity);
		return new Member(identity.getKey(), firstname, lastname, fullname, rsrc != null, portraitCssClass);
	}
	
	@Override
	protected void doDispose() {
		memberLinks = emailLinks = ownerLinks = coachesLinks = participantsLinks = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(memberLinks.contains(source)) {
			FormLink memberLink = (FormLink)source;
			Member member = (Member)memberLink.getUserObject();
			openHomePage(member, ureq);
		} else if (emailLinks.contains(source)) {
			FormLink emailLink = (FormLink)source;
			Member member = (Member)emailLink.getUserObject();
			ContactList memberList = new ContactList(translate("members.to", new String[]{member.getFullName(), courseEnv.getCourseTitle()}));
			Identity identity = securityManager.loadIdentityByKey(member.getKey());
			memberList.add(identity);
			sendEmailToMember(memberList, ureq);
		} else if (source == ownersEmailLink) {
			ContactList ownerList = new ContactList(translate("owners.to", new String[]{ courseEnv.getCourseTitle() }));
			ownerList.addAllIdentites(getOwners());
			sendEmailToMember(ownerList, ureq);
		} else if (source == coachesEmailLink) {
			ContactList coachList = new ContactList(translate("coaches.to", new String[]{ courseEnv.getCourseTitle() }));
			Set<Long> sendToWhatYouSee = new HashSet<>();
			for(FormLink coachLink:coachesLinks) {
				Member member = (Member)coachLink.getUserObject();
				sendToWhatYouSee.add(member.getKey());
			}
			CourseGroupManager cgm = courseEnv.getCourseGroupManager();
			avoidInvisibleMember(cgm.getCoachesFromBusinessGroups(), coachList, sendToWhatYouSee);
			avoidInvisibleMember(cgm.getCoaches(), coachList, sendToWhatYouSee);
			sendEmailToMember(coachList, ureq);
		} else if (source == participantsEmailLink) {
			ContactList participantList = new ContactList(translate("participants.to", new String[]{ courseEnv.getCourseTitle() }));
			Set<Long> sendToWhatYouSee = new HashSet<>();
			for(FormLink participantLink:participantsLinks) {
				Member member = (Member)participantLink.getUserObject();
				sendToWhatYouSee.add(member.getKey());
			}
			CourseGroupManager cgm = courseEnv.getCourseGroupManager();
			avoidInvisibleMember(cgm.getParticipantsFromBusinessGroups(), participantList, sendToWhatYouSee);
			avoidInvisibleMember(cgm.getParticipants(), participantList, sendToWhatYouSee);
			sendEmailToMember(participantList, ureq);
		}
	}
	
	private void avoidInvisibleMember(List<Identity> members, ContactList contactList, Set<Long> sendToWhatYouSee) {
		for(Identity member:members) {
			if(sendToWhatYouSee.contains(member.getKey())) {
				contactList.add(member);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			removeAsListenerAndDispose(emailController);
			removeAsListenerAndDispose(cmc);
			emailController = null;
			cmc = null;
		} else if (source == emailController) {
			cmc.deactivate();
			removeAsListenerAndDispose(emailController);
			removeAsListenerAndDispose(cmc);
			emailController = null;
			cmc = null;
		}
		super.event(ureq, source, event);
	}

	protected void sendEmailToMember(ContactList contactList, UserRequest ureq) {
		if (contactList.getEmailsAsStrings().size() > 0) {
			removeAsListenerAndDispose(emailController);
			
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			cmsg.addEmailTo(contactList);
			
			emailController = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
			listenTo(emailController);
			
			removeAsListenerAndDispose(cmc);
			String title = translate("members.email.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), emailController.getInitialComponent(), true, title);
			listenTo(cmc);
			
			cmc.activate();			
		}
	}
	
	protected void openHomePage(Member member, UserRequest ureq) {
		String url = "[HomePage:" + member.getKey() + "]";
		BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
	
	public static class Member {
		private final String firstName;
		private final String lastName;
		private final String fullName;
		private final Long key;
		private boolean portrait;
		private String portraitCssClass;
		private FormLink emailLink;
		
		public Member(Long key, String firstName, String lastName, String fullName, boolean portrait, String portraitCssClass) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.fullName = fullName;
			this.key = key;
			this.portrait = portrait;
			this.portraitCssClass = portraitCssClass;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}
		
		public String getPortraitCssClass() {
			return portraitCssClass;
		}

		
		public boolean isPortraitAvailable() {
			return portrait; 
		}

		public FormLink getEmailLink() {
			return emailLink;
		}

		public void setEmailLink(FormLink emailLink) {
			this.emailLink = emailLink;
		}

		public String getFullName() {
			return fullName;
		}

		public Long getKey() {
			return key;
		}
		
		@Override
		public int hashCode() {
			return key.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof Member) {
				Member member = (Member)obj;
				return key != null && key.equals(member.key);
			}
			return false;
		}
	}
	
	public static class IdentityComparator implements Comparator<Identity> {

		@Override
		public int compare(Identity id1, Identity id2) {
			if(id1 == null) return -1;
			if(id2 == null) return 1;
			
			String l1 = id1.getUser().getProperty(UserConstants.LASTNAME, null);
			String l2 = id2.getUser().getProperty(UserConstants.LASTNAME, null);
			if(l1 == null) return -1;
			if(l2 == null) return 1;
			
			int result = l1.compareToIgnoreCase(l2);
			if(result == 0) {
				String f1 = id1.getUser().getProperty(UserConstants.FIRSTNAME, null);
				String f2 = id2.getUser().getProperty(UserConstants.FIRSTNAME, null);
				if(f1 == null) return -1;
				if(f2 == null) return 1;
				result = f1.compareToIgnoreCase(f2);
			}
			return result;
		}
	}
}
