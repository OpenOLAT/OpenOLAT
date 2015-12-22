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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.Presence;
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
	
	private FormLink allEmailLink;
	
	private List<Member> ownerList;
	private List<Member> coachList;
	private List<Member> participantList;

	private final boolean canEmail;
	private final boolean showOwners;
	private final boolean showCoaches;
	private final boolean showParticipants;
	private final boolean chatEnabled;

	private MembersMailController mailCtrl;
	private ContactFormController emailController;
	private CloseableModalController cmc;
	
	private int count = 0;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private UserSessionManager sessionManager;

	public MembersCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, ModuleConfiguration config) {
		super(ureq, wControl, "members");

		courseEnv = userCourseEnv.getCourseEnvironment();
		avatarBaseURL = registerCacheableMapper(ureq, "avatars-members", new UserAvatarMapper(true));
		portraitManager = DisplayPortraitManager.getInstance();

		showOwners = config.getBooleanSafe(CONFIG_KEY_SHOWOWNER);
		showCoaches = config.getBooleanSafe(CONFIG_KEY_SHOWCOACHES);
		showParticipants = config.getBooleanSafe(CONFIG_KEY_SHOWPARTICIPANTS);
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		
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
			allEmailLink = uifactory.addFormLink("email", "members.email.title", null, formLayout, Link.BUTTON);
			allEmailLink.setIconLeftCSS("o_icon o_icon_mail");
		}

		Set<Long> duplicateCatcher = new HashSet<Long>();
		ownerList = initFormMemberList("owners", owners, duplicateCatcher, formLayout, canEmail);
		coachList = initFormMemberList("coaches", coaches, duplicateCatcher, formLayout, canEmail);
		participantList = initFormMemberList("participants", participants, duplicateCatcher, formLayout, canEmail);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("showOwners", showOwners);
			layoutCont.contextPut("hasOwners", new Boolean(!ownerList.isEmpty()));
			layoutCont.contextPut("showCoaches", showCoaches);
			layoutCont.contextPut("hasCoaches", new Boolean(!coachList.isEmpty()));
			layoutCont.contextPut("showParticipants", showParticipants);
			layoutCont.contextPut("hasParticipants", new Boolean(!participantList.isEmpty()));
		}
	}
	
	private List<Identity> getOwners() {
		RepositoryEntry courseRepositoryEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		return repositoryService.getMembers(courseRepositoryEntry, GroupRoles.owner.name());
	}
	
	private List<Member> initFormMemberList(String name, List<Identity> ids, Set<Long> duplicateCatcher, FormItemContainer formLayout, boolean withEmail) {
		String page = velocity_root + "/memberList.html";
		
		FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout(name, getTranslator(), page);
		formLayout.add(name, container);
		container.setRootForm(mainForm);

		List<Member> members = createMemberLinks(ids, duplicateCatcher, container, withEmail);
		container.contextPut("members", members);
		container.contextPut("avatarBaseURL", avatarBaseURL);
		return members;
	}
	
	protected List<Member> createMemberLinks(List<Identity> identities, Set<Long> duplicateCatcher, FormLayoutContainer formLayout, boolean withEmail) {
		List<Member> members = new ArrayList<>();
		for(Identity identity:identities) {
			if(duplicateCatcher.contains(identity.getKey())) continue;
			
			Member member = createMember(identity);
			members.add(member);
			
			String guiId = Integer.toString(++count);
			String fullname = StringHelper.escapeHtml(member.getFullName());
			
			FormLink idLink = uifactory.addFormLink("id_".concat(guiId), "id", fullname, null, formLayout, Link.NONTRANSLATED);
			
			idLink.setUserObject(member);
			formLayout.add(idLink.getComponent().getComponentName(), idLink);
			member.setIdLink(idLink);
			
			if(withEmail) {
				FormLink emailLink = uifactory.addFormLink("mail_".concat(guiId), "mail", "", null, formLayout, Link.NONTRANSLATED);
				emailLink.setUserObject(member);
				emailLink.setIconLeftCSS("o_icon o_icon_mail o_icon-lg");
				emailLink.setElementCssClass("o_mail");
				formLayout.add(emailLink.getComponent().getComponentName(), emailLink);
				member.setEmailLink(emailLink);
			}
			if(chatEnabled) {
				FormLink chatLink = uifactory.addFormLink("chat_".concat(guiId), "chat", "", null, formLayout, Link.NONTRANSLATED);
				chatLink.setUserObject(member);
				chatLink.setElementCssClass("o_chat");
				formLayout.add(chatLink.getComponent().getComponentName(), chatLink);
				member.setChatLink(chatLink);
			}
			
			duplicateCatcher.add(identity.getKey());
		}
		
		if(chatEnabled) {
			Long me = getIdentity().getKey();
			if(imModule.isOnlineStatusEnabled()) {
				Map<Long,Member> loadStatus = new HashMap<>();
				
				for(Member member:members) {
					if(member.getKey().equals(me)) {
						member.getChatLink().setVisible(false);
					} else if(sessionManager.isOnline(member.getKey())) {
						loadStatus.put(member.getKey(), member);
					} else {
						member.getChatLink().setIconLeftCSS("o_icon o_icon_status_unavailable");
					}
				}
				
				if(loadStatus.size() > 0) {
					List<Long> statusToLoadList = new ArrayList<>(loadStatus.keySet());
					Map<Long,String> statusMap = imService.getBuddyStatus(statusToLoadList);
					for(Long toLoad:statusToLoadList) {
						String status = statusMap.get(toLoad);
						Member member = loadStatus.get(toLoad);
						if(status == null || Presence.available.name().equals(status)) {
							member.getChatLink().setIconLeftCSS("o_icon o_icon_status_available");
						} else if(Presence.dnd.name().equals(status)) {
							member.getChatLink().setIconLeftCSS("o_icon o_icon_status_dnd");
						} else {
							member.getChatLink().setIconLeftCSS("o_icon o_icon_status_unavailable");
						}
					}
				}
			} else {
				for(Member member:members) {
					if(member.getKey().equals(me)) {
						member.getChatLink().setVisible(false);
					} else {
						member.getChatLink().setIconLeftCSS("o_icon o_icon_status_chat");
					}
				}
			}
		}
		
		return members;
	}
	
	private Member createMember(Identity identity) {
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
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == allEmailLink) {
			doEmail(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			Object uobject = link.getUserObject();
			if(uobject instanceof Member) {
				Member member = (Member)uobject;
				String cmd = link.getCmd();
				if("id".equals(cmd)) {
					doOpenHomePage(member, ureq);
				} else if("mail".equals(cmd)) {
					doSendEmailToMember(member, ureq);
				} else if("chat".equals(cmd)) {
					doOpenChat(member, ureq);
				}
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanUp();
		} else if (source == emailController) {
			cmc.deactivate();
			cleanUp();
		} else if(source == mailCtrl) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(emailController);
		removeAsListenerAndDispose(mailCtrl);
		removeAsListenerAndDispose(cmc);
		emailController = null;
		mailCtrl = null;
		cmc = null;
	}
	
	protected void doEmail(UserRequest ureq) {
		if(mailCtrl != null || cmc != null) return;
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(mailCtrl);
		
		mailCtrl = new MembersMailController(ureq, getWindowControl(), courseEnv, ownerList, coachList, participantList);
		listenTo(mailCtrl);
		
		String title = translate("members.email.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), mailCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		
		cmc.activate();	
	}
	
	protected void doOpenChat(Member member, UserRequest ureq) {
		Buddy buddy = imService.getBuddyById(member.getKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(ureq, buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}

	protected void doSendEmailToMember(Member member, UserRequest ureq) {
		ContactList memberList = new ContactList(translate("members.to", new String[]{ member.getFullName(), courseEnv.getCourseTitle() }));
		Identity identity = securityManager.loadIdentityByKey(member.getKey());
		memberList.add(identity);
		doSendEmailToMember(memberList, ureq);
	}

	protected void doSendEmailToMember(ContactList contactList, UserRequest ureq) {
		if (contactList.getEmailsAsStrings().size() > 0) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(emailController);
			
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			cmsg.addEmailTo(contactList);
			
			emailController = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
			listenTo(emailController);
			
			String title = translate("members.email.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), emailController.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	protected void doOpenHomePage(Member member, UserRequest ureq) {
		String url = "[HomePage:" + member.getKey() + "]";
		BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
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
