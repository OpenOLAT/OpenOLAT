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
package org.olat.modules.bigbluebutton.ui;

import java.io.File;
import java.util.Date;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.BigBlueButtonCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.bigbluebutton.BigBlueButtonEditController;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.dispatcher.AuthenticatedDispatcher;
import org.olat.group.BusinessGroupService;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.manager.AvatarMapper;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.user.DisplayPortraitManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonGuestJoinController extends FormBasicController implements GenericEventListener {

	private TextElement nameEl;
	private TextElement passwordEl;
	private FormLink joinButton;
	private FormLink loginButton;
	private MultipleSelectionElement acknowledgeRecordingEl;

	private String avatarUrl;
	private boolean readOnly = false;
	private boolean authenticated = false;
	private boolean moderatorStartMeeting;
	private final MeetinSecurity allowedToMeet;
	private BigBlueButtonMeeting meeting;
	private OLATResourceable meetingOres;
	
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private DisplayPortraitManager displayPortraitManager;

	public BigBlueButtonGuestJoinController(UserRequest ureq, WindowControl wControl, BigBlueButtonMeeting meeting) {
		super(ureq, wControl, "guest_join");
		this.meeting = meeting;
		moderatorStartMeeting = isModeratorStartMeeting();
		allowedToMeet = isAllowedToMeet(ureq);
		UserSession usess = ureq.getUserSession();
		authenticated = getIdentity() != null && usess.getRoles() != null && !usess.getRoles().isGuestOnly();
		initForm(ureq);
		updateButtonsAndStatus();

		if(meeting != null) {
			meetingOres = OresHelper.createOLATResourceableInstance(BigBlueButtonMeeting.class.getSimpleName(), meeting.getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), meetingOres);
		}
	}

	@Override
	protected void doDispose() {
		if(meetingOres != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, meetingOres);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		if(formLayout instanceof FormLayoutContainer && meeting != null
				&& !Boolean.TRUE.equals(ureq.getUserSession().getEntry("meeting-" + meeting.getKey()))) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", meeting.getName());
			String descr = meeting.getDescription();
			if(StringHelper.containsNonWhitespace(descr)) {
				if(!StringHelper.isHtml(descr)) {
					descr = Formatter.escWithBR(descr).toString();
				}
				layoutCont.contextPut("description", descr);
			}
			if(meeting.getStartDate() != null) {
				String start = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getStartDate());
				layoutCont.contextPut("start", start);
			}
			if(meeting.getEndDate() != null) {
				String end = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getEndDate());
				layoutCont.contextPut("end", end);
			}
		}
		
		nameEl = uifactory.addTextElement("meeting.guest.pseudo", 128, "", formLayout);
		if(authenticated) {
			nameEl.setValue(getName());
			nameEl.setEnabled(false);
		}

		boolean end = isEnded();
		passwordEl = uifactory.addTextElement("meeting.guest.password", 64, "", formLayout);
		passwordEl.setVisible(!end && meeting != null && StringHelper.containsNonWhitespace(meeting.getPassword()));
		
		joinButton = uifactory.addFormLink("meeting.join.button", translate("meeting.join.button"), null, formLayout,
				Link.BUTTON_LARGE | Link.NONTRANSLATED);
		joinButton.setElementCssClass("o_sel_bbb_guest_join");
		joinButton.setVisible(!end);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		
		loginButton = uifactory.addFormLink("meeting.login", formLayout, Link.LINK);
		loginButton.setElementCssClass("o_sel_bbb_guest_login");
		loginButton.setVisible(!authenticated);
		
		KeyValues acknowledgeKeyValue = new KeyValues();
		acknowledgeKeyValue.add(KeyValues.entry("agree", translate("meeting.acknowledge.recording.agree")));
		acknowledgeRecordingEl = uifactory.addCheckboxesHorizontal("meeting.acknowledge.recording", null, formLayout,
				acknowledgeKeyValue.keys(), acknowledgeKeyValue.values());
		acknowledgeRecordingEl.setVisible(!end && BigBlueButtonUIHelper.isRecord(meeting));
	}
	
	private String getName() {
		User user = getIdentity().getUser();
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(user.getFirstName())) {
			sb.append(user.getFirstName());
		}
		if(StringHelper.containsNonWhitespace(user.getLastName())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(user.getLastName());
		}
		return sb.toString();
	}
	
	private void reloadButtonsAndStatus() {
		meeting = bigBlueButtonManager.getMeeting(meeting);
		updateButtonsAndStatus();
		flc.setDirty(true);
	}
	
	private void updateButtonsAndStatus() {
		boolean isEnded = isEnded();
		boolean accessible = isAccessible(allowedToMeet);
		boolean disabled = isDisabled();
		flc.contextPut("disabled", Boolean.valueOf(disabled));
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		flc.contextPut("notStarted", Boolean.TRUE);
		flc.contextPut("allowedToMeet", Boolean.valueOf(allowedToMeet.isAllowed()));
		// only change from invisible to visible
		if(!joinButton.isVisible()) {
			joinButton.setVisible(allowedToMeet.isAllowed() && accessible && !disabled);
		}
		joinButton.setEnabled(allowedToMeet.isAllowed() && accessible && !disabled);
	
		if(allowedToMeet.isAllowed() && accessible && !disabled) {
			boolean running = bigBlueButtonManager.isMeetingRunning(meeting);
			if(allowedToMeet.isModerator() || allowedToMeet.isAdmin()) {
				flc.contextPut("notStarted", Boolean.FALSE);
				if(!running && moderatorStartMeeting) {
					joinButton.setI18nKey(translate("meeting.start.button"));
				} else {
					joinButton.setI18nKey(translate("meeting.join.button"));
				}
			} else if(!running && moderatorStartMeeting) {
				flc.contextPut("notStarted", Boolean.TRUE);
				joinButton.setEnabled(false);
			} else {
				flc.contextPut("notStarted", Boolean.FALSE);
				joinButton.setEnabled(!readOnly);
			}
		} else if(isEnded) {
			flc.contextPut("notStarted", Boolean.FALSE);
		}
		// update button style to indicate that the user must now press to start
		joinButton.setPrimary(joinButton.isEnabled());
		acknowledgeRecordingEl.setVisible(joinButton.isEnabled() && BigBlueButtonUIHelper.isRecord(meeting));
	}
	
	private MeetinSecurity isAllowedToMeet(UserRequest ureq) {
		if(meeting == null) return new MeetinSecurity(false, false, false);

		UserSession usess = ureq.getUserSession();
		IdentityEnvironment identEnv = usess.getIdentityEnvironment();
		if(identEnv.getRoles() == null && identEnv.getIdentity() == null) {
			boolean externalUsersAllowed = StringHelper.containsNonWhitespace(meeting.getReadableIdentifier());
			if(meeting.getEntry() != null) {
				RepositoryEntry re = meeting.getEntry();
				externalUsersAllowed &= re.getEntryStatus() == RepositoryEntryStatusEnum.published;
			}
			return new MeetinSecurity(externalUsersAllowed, false, false);
		} else if(meeting.getEntry() != null) {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(getIdentity(), identEnv.getRoles(), meeting.getEntry());
			if(reSecurity.canLaunch()) {
				readOnly = reSecurity.isReadOnly();
				if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
					RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(meeting.getEntry().getKey());
					ICourse course = CourseFactory.loadCourse(entry);
					UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(identEnv, course.getCourseEnvironment());
					CourseTreeNode courseTreeNode = (CourseTreeNode)nodeAccessService.getCourseTreeModelBuilder(uce)
							.withFilter(AccessibleFilter.create())
							.build()
							.getNodeById(meeting.getSubIdent());
					return new MeetinSecurity(courseTreeNode.isVisible(), reSecurity.isAdministrator() || reSecurity.isOwner(), reSecurity.isCoach());
				}
				return new MeetinSecurity(true, reSecurity.isAdministrator() || reSecurity.isOwner(), reSecurity.isCoach());
			}
		} else if(meeting.getBusinessGroup() != null) {
			boolean member = businessGroupService.isIdentityInBusinessGroup(getIdentity(), meeting.getBusinessGroup());
			return new MeetinSecurity(member, false, false);
		}
		return new MeetinSecurity(false, false, false);
	}
	
	private boolean isModeratorStartMeeting() {
		if(meeting == null) return true;
		if(meeting.getEntry() != null) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(meeting.getEntry().getKey());
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(meeting.getSubIdent());
			if(courseNode instanceof BigBlueButtonCourseNode) {
				// check if the node exists and that it's a BigBlueButton node
				return courseNode.getModuleConfiguration()
						.getBooleanSafe(BigBlueButtonEditController.MODERATOR_START_MEETING, true);
			}
		}
		return true;
	}
	
	private boolean isEnded() {
		return meeting != null && meeting.getEndDate() != null && new Date().after(meeting.getEndDate());
	}
	
	private boolean isDisabled() {
		return meeting != null && meeting.getServer() != null && !meeting.getServer().isEnabled();
	}
	
	private boolean isAccessible(MeetinSecurity security) {
		if(meeting == null) return false;
		if(meeting.isPermanent()) {
			return bigBlueButtonModule.isPermanentMeetingEnabled();
		}

		Date now = new Date();
		Date start = (security.isAdmin() || security.isModerator()) ? meeting.getStartWithLeadTime() : meeting.getStartDate();
		Date end = meeting.getEndWithFollowupTime();
		return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
	}

	@Override
	public void event(Event event) {
		if(event instanceof BigBlueButtonEvent) {
			BigBlueButtonEvent ace = (BigBlueButtonEvent)event;
			if(ace.getMeetingKey() != null && ace.getMeetingKey().equals(meeting.getKey())) {
				reloadButtonsAndStatus();
			}
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		UserSession usess = ureq.getUserSession();
		if(usess != null && usess.isAuthenticated() && usess.getRoles() != null && !usess.getRoles().isGuestOnly()) {
			// name is not mandatory if logged in
		} else if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(nameEl.getValue().length() > 64) {
			nameEl.setErrorKey("form.error.toolong", new String[] { "64" });
			allOk &= false;
		}
		
		passwordEl.clearError();
		if(passwordEl.isVisible()) {
			if(!StringHelper.containsNonWhitespace(passwordEl.getValue())) {
				passwordEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(!passwordEl.getValue().equals(meeting.getPassword())) {
				passwordEl.setErrorKey("error.password", null);
				allOk &= false;
			}
		}
		
		acknowledgeRecordingEl.clearError();
		if(acknowledgeRecordingEl.isVisible()
				&& acknowledgeRecordingEl.isEnabled() && !acknowledgeRecordingEl.isAtLeastSelected(1)) {
			acknowledgeRecordingEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(!joinButton.isEnabled()) {
			if(!nameEl.hasError()) {// don't overwrite the validation error of name
				nameEl.setErrorKey("meeting.create.intro", null);
			}
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(joinButton.isEnabled()) {
			doJoin(ureq);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(joinButton == source && validateFormLogic(ureq)) {
			doJoin(ureq);
		} else if(loginButton == source) {
			doLogin(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doJoin(UserRequest ureq) {
		if(!allowedToMeet.isAllowed()) return;
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		String pseudo = nameEl.getValue();

		String aUrl = null;
		Identity identity = null;
		BigBlueButtonAttendeeRoles role;
		UserSession usess = ureq.getUserSession();
		if(getIdentity() == null || usess.getRoles() == null) {
			role = BigBlueButtonAttendeeRoles.external;
		} else if(usess.getRoles().isGuestOnly()) {
			role = BigBlueButtonAttendeeRoles.guest;
		} else {
			if(allowedToMeet.isAdmin() || allowedToMeet.isModerator()) {
				role = BigBlueButtonAttendeeRoles.moderator;
			} else {
				role = BigBlueButtonAttendeeRoles.viewer;
			}
			identity = getIdentity();
			aUrl = getAvatarUrl();
		}
		
		String url = bigBlueButtonManager.join(meeting, identity, pseudo, aUrl, role, null, errors);
		MediaResource resource = new RedirectMediaResource(url);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private String getAvatarUrl() {
		if(avatarUrl == null) {
			File portraitFile = displayPortraitManager.getBigPortrait(getIdentity());
			if(portraitFile != null) {
				String rnd = "r" + getIdentity().getKey() + CodeHelper.getRAMUniqueID();
				avatarUrl = Settings.createServerURI()
						+ registerCacheableMapper(null, rnd, new AvatarMapper(portraitFile), 5 * 60 * 60)
						+ "/" + portraitFile.getName();
			}
		}
		return avatarUrl;
	}
	
	private void doLogin(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		IdentityEnvironment identEnv = usess.getIdentityEnvironment();
		if(identEnv.getIdentity() == null && identEnv.getRoles() == null) {
			String url = Settings.getServerContextPathURI() + "/bigbluebutton/" + meeting.getReadableIdentifier();
			usess.putEntryInNonClearedStore(AuthenticatedDispatcher.AUTHDISPATCHER_REDIRECT_URL, url);
			usess.removeEntryFromNonClearedStore(AuthenticatedDispatcher.AUTHDISPATCHER_BUSINESSPATH);
			ureq.getDispatchResult().setResultingMediaResource(
					new RedirectMediaResource(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault()));
		} else if(identEnv.getRoles().isGuestOnly()) {
			AuthHelper.doLogout(ureq);
		}
	}
	
	private static class MeetinSecurity {
		
		private final boolean allowed;
		private final boolean admin;
		private final boolean moderator;
		
		public MeetinSecurity(boolean allowed,  boolean admin, boolean moderator) {
			this.allowed = allowed;
			this.admin = admin;
			this.moderator = moderator;
		}

		public boolean isAllowed() {
			return allowed;
		}

		public boolean isAdmin() {
			return admin;
		}

		public boolean isModerator() {
			return moderator;
		}	
	}
}
