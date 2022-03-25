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
package org.olat.commons.memberlist.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.memberlist.manager.MembersExportManager;
import org.olat.commons.memberlist.model.CurriculumMemberInfos;
import org.olat.commons.memberlist.ui.MembersAvatarDisplayRunController.IdentityComparator;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.nodes.members.Member;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.ui.main.MemberRow;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date: 29.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class MembersListDisplayRunController extends BasicController {
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private Link printLink;
	private Link allEmailLink;
	private Link downloadLink;
	
	private FormBasicController mailCtrl;
	private ContactFormController emailController;
	private CloseableModalController cmc;
	
	private List<Member> ownerList;
	private List<Member> coachList;
	private List<Member> participantList;
	private List<Member> waitingtList;
	
	private List<Identity> owners;
	private List<Identity> coaches;
	private List<Identity> participants;
	private List<Identity> waiting;
	private Map<Long,CurriculumMemberInfos> curriculumInfos;

	private final boolean showOwners;
	private final boolean showCoaches;
	private final boolean showParticipants;
	private final boolean showWaiting;
	private final boolean deduplicateList;
	
	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment userCourseEnv;
	private final BusinessGroup businessGroup;
	private final RepositoryEntry repoEntry;
	
	private MembersTableController ownersTableCtrl;
	private MembersTableController coachesTableCtrl;
	private MembersTableController participantsTableCtrl;
	private MembersTableController waitingTableCtrl;
	
	private Map<Long,BusinessGroupMembership> groupmemberships;
	private	Map<Long,Date> recentLaunches;
	private	Map<Long,Date> initialLaunches;

	private final VelocityContainer mainVC;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MembersExportManager exportManager;
	@Autowired
	private DisplayPortraitManager portraitManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;

	public MembersListDisplayRunController(UserRequest ureq, WindowControl wControl, Translator translator, UserCourseEnvironment userCourseEnv, BusinessGroup businessGroup,
			List<Identity> owners, List<Identity> coaches, List<Identity> participants, List<Identity> waiting, Map<Long,CurriculumMemberInfos> curriculumInfos,
			boolean canEmail, boolean canDownload, boolean deduplicateList, boolean showOwners, boolean showCoaches, boolean showParticipants, boolean showWaiting, boolean editable) {
		super(ureq, wControl);
		Translator fallback = userManager.getPropertyHandlerTranslator(getTranslator());		
		setTranslator(Util.createPackageTranslator(translator, fallback, getLocale()));		
		
		mainVC = createVelocityContainer("membersTable");
		
		this.userCourseEnv = userCourseEnv;
		courseEnv = userCourseEnv == null ? null : userCourseEnv.getCourseEnvironment();
		this.businessGroup = businessGroup;
		this.curriculumInfos = curriculumInfos;
		repoEntry = courseEnv != null ? courseEnv.getCourseGroupManager().getCourseEntry() : null;

		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(MembersDisplayRunController.USER_PROPS_LIST_ID, isAdministrativeUser);
		
		// lists
		this.owners = owners;
		this.coaches = coaches;
		this.participants = participants;
		this.waiting = waiting;
		// flags
		this.showOwners = showOwners;
		this.showCoaches = showCoaches;
		this.showParticipants = showParticipants;
		this.showWaiting = showWaiting;
		this.deduplicateList = deduplicateList;
		
		if(canEmail) {
			allEmailLink = LinkFactory.createLink(null, "email", "email.all", "members.email.title", getTranslator(), mainVC, this, Link.BUTTON);
			allEmailLink.setIconLeftCSS("o_icon o_icon_mail");
			allEmailLink.setElementCssClass("o_sel_cmembers_email_all");
		}
		
		Identity ownId = getIdentity();
		if (editable && (isManager(ureq) || owners.contains(ownId) || coaches.contains(ownId) 
				|| (canDownload && !waiting.contains(ownId)))) {
			downloadLink = LinkFactory.createLink(null, "download", "download", "members.download", getTranslator(), mainVC, this, Link.BUTTON);
			downloadLink.setIconLeftCSS("o_icon o_icon_download");
			printLink = LinkFactory.createButton("print", mainVC, this);
			printLink.setIconLeftCSS("o_icon o_icon_print o_icon-lg");
			printLink.setPopup(new LinkPopupSettings(700, 500, "print-members"));
		}
		
		Comparator<Identity> idComparator = new IdentityComparator();
		Collections.sort(owners, idComparator);
		ownerList = convertIdentitiesToMembers(owners);
		Collections.sort(coaches, idComparator);
		coachList = convertIdentitiesToMembers(coaches);
		Collections.sort(participants, idComparator);
		participantList = convertIdentitiesToMembers(participants);
		Collections.sort(waiting, idComparator);
		waitingtList = convertIdentitiesToMembers(waiting);
		
		Set<MemberRow> duplicateCatcher = new HashSet<>();
		boolean userLastTimeVisible = cacheGroupMemberships(ureq);
		
		if (showOwners && !owners.isEmpty()) {
			ownersTableCtrl = new MembersTableController(ureq, wControl, owners, duplicateCatcher, recentLaunches, initialLaunches, curriculumInfos,
					userPropertyHandlers, groupmemberships,	repoEntry, businessGroup, courseEnv,
					deduplicateList, getTranslator(), editable, canEmail, userLastTimeVisible);
			listenTo(ownersTableCtrl);
			mainVC.put("ownerList", ownersTableCtrl.getInitialComponent());
		}
		if (showCoaches && !coaches.isEmpty()) {
			coachesTableCtrl = new MembersTableController(ureq, wControl, coaches, duplicateCatcher, recentLaunches, initialLaunches, curriculumInfos,
					userPropertyHandlers, groupmemberships, repoEntry, businessGroup, courseEnv,
					deduplicateList, getTranslator(), editable, canEmail, userLastTimeVisible);
			listenTo(coachesTableCtrl);
			mainVC.put("coachList", coachesTableCtrl.getInitialComponent());
		}
		if (showParticipants && !participants.isEmpty()) {
			participantsTableCtrl = new MembersTableController(ureq, wControl, participants, duplicateCatcher, recentLaunches, initialLaunches, curriculumInfos,
					userPropertyHandlers, groupmemberships,	repoEntry, businessGroup, courseEnv,
					deduplicateList, getTranslator(), editable, canEmail, userLastTimeVisible);
			listenTo(participantsTableCtrl);
			mainVC.put("participantList", participantsTableCtrl.getInitialComponent());
		}
		if (showWaiting && !waiting.isEmpty()) {
			waitingTableCtrl = new MembersTableController(ureq, wControl, waiting, duplicateCatcher, recentLaunches, initialLaunches, curriculumInfos,
					userPropertyHandlers, groupmemberships, repoEntry, businessGroup, courseEnv,
					deduplicateList, getTranslator(), editable, canEmail, userLastTimeVisible);
			listenTo(waitingTableCtrl);
			mainVC.put("waitingList", waitingTableCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);		
	}
	
	private boolean isManager(UserRequest ureq) {
		if(businessGroup != null) {
			Roles roles = ureq.getUserSession().getRoles();
			return roles.isAdministrator() || roles.isGroupManager();
		}
		return userCourseEnv != null && userCourseEnv.isAdmin();
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == printLink) {
			doPrint(ureq);
		} else if(source == allEmailLink) {
			doEmail(ureq);
		} else if (source == downloadLink) {
			doExport(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanUp();
		} else if (source == emailController || source == mailCtrl) {
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
	
	private boolean cacheGroupMemberships (UserRequest ureq) {
		boolean isUserLastVisitVisible = securityModule.isUserLastVisitVisible(ureq.getUserSession().getRoles());
		if (isUserLastVisitVisible) {
			if (businessGroup != null) {
				List<BusinessGroup> groups = new ArrayList<>(); 
				groups.add(businessGroup);
				groupmemberships = exportManager.getGroupMembershipMap(groups);
			}
			if (repoEntry != null) {
				initialLaunches = userInfosMgr.getInitialLaunchDates(repoEntry.getOlatResource().getResourceableId());
				recentLaunches = userInfosMgr.getRecentLaunchDates(repoEntry.getOlatResource());			
			}
		}
		return isUserLastVisitVisible;
	}
	
	private void doEmail(UserRequest ureq) {
		if(mailCtrl != null || cmc != null) return;
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(mailCtrl);
		mailCtrl = new MembersMailController(ureq, getWindowControl(), getTranslator(), courseEnv,
				ownerList, coachList, participantList, waitingtList, createBodyTemplate());
		listenTo(mailCtrl);
		
		String title = translate("members.email.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), mailCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		
		cmc.activate();	
	}
	
	private String createBodyTemplate() {
		if (userCourseEnv == null) {
			String groupName = businessGroup.getName();
			// Build REST URL to business group, use hack via group manager to access
			StringBuilder groupLink = new StringBuilder();
			groupLink.append(Settings.getServerContextPathURI())
				.append("/url/BusinessGroup/").append(businessGroup.getKey());
			return translate("email.body.template", new String[]{groupName, groupLink.toString()});	
		} else {
			String courseName = courseEnv.getCourseTitle();
			// Build REST URL to course element, use hack via group manager to access repo entry
			StringBuilder courseLink = new StringBuilder();
			RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
			courseLink.append(Settings.getServerContextPathURI())
				.append("/url/RepositoryEntry/").append(entry.getKey());
			// the ext-ref and location are not in default mail template, but used by some instances
			return translate("email.body.template", courseName, courseLink.toString(), entry.getExternalRef(), entry.getLocation());		
		}
	}
	
	private void doExport(UserRequest ureq) {
		MediaResource resource = exportManager.getXlsMediaResource(showOwners, showCoaches, showParticipants, showWaiting, 
				owners, coaches, participants, waiting, curriculumInfos, getTranslator(), userPropertyHandlers, repoEntry, businessGroup);
		
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doPrint(UserRequest ureq) {
		final String title = businessGroup != null ? businessGroup.getName() : courseEnv.getCourseTitle();
		
		ControllerCreator printControllerCreator = (lureq, lwControl) -> {
			lwControl.getWindowBackOffice().getChiefController().addBodyCssClass("o_cmembers_print");
			return new MembersPrintController(lureq, lwControl, getTranslator(), owners, coaches,
					participants, waiting, curriculumInfos, showOwners, showCoaches, showParticipants, showWaiting, deduplicateList, title);
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr, true);
	}
	
	private List<Member> convertIdentitiesToMembers(List<Identity> identities) {
		List<Member> memberList = new ArrayList<>();
		for (Identity identity : identities) {
			Member member = createMember(identity);
			memberList.add(member);
		}
		return memberList;
	}
	
	private Member createMember(Identity identity) {		
		boolean hasPortrait = portraitManager.hasPortrait(identity);

		String portraitCssClass;
		String gender = identity.getUser().getProperty(UserConstants.GENDER, Locale.ENGLISH);
		if ("male".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS;
		} else if ("female".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS;
		} else {
			portraitCssClass = DisplayPortraitManager.DUMMY_BIG_CSS_CLASS;
		}
		String fullname = userManager.getUserDisplayName(identity);
		return new Member(identity, fullname, null, userPropertyHandlers, getLocale(), hasPortrait, portraitCssClass);
	}
}
