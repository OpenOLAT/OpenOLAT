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
package org.olat.modules.teams.ui;

import java.util.Date;

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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsGuestJoinController extends FormBasicController implements GenericEventListener {

	public static final String PARTICIPATION_IDENTIFIER = "evaluation-form-participation-identifier";
	
	private FormLink joinButton;

	private boolean readOnly = false;
	private final boolean allowedToMeet;
	private TeamsMeeting meeting;
	private OLATResourceable meetingOres;
	
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	public TeamsGuestJoinController(UserRequest ureq, WindowControl wControl, TeamsMeeting meeting) {
		super(ureq, wControl, "guest_join");
		this.meeting = meeting;
		allowedToMeet = isAllowedToMeet(ureq);
		initForm(ureq);
		updateButtonsAndStatus();

		if(meeting != null) {
			meetingOres = OresHelper.createOLATResourceableInstance(TeamsMeeting.class.getSimpleName(), meeting.getKey());
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
			layoutCont.contextPut("title", meeting.getSubject());
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
		
		boolean end = isEnded();
		joinButton = uifactory.addFormLink("meeting.join.button", formLayout, Link.BUTTON_LARGE);
		joinButton.setElementCssClass("o_sel_teams_guest_join");
		joinButton.setVisible(!end);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
	}
	
	private void reloadButtonsAndStatus() {
		meeting = teamsService.getMeeting(meeting);
		updateButtonsAndStatus();
		flc.setDirty(true);
	}
	
	private void updateButtonsAndStatus() {
		boolean isEnded = isEnded();
		boolean accessible = isAccessible();
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		flc.contextPut("notStarted", Boolean.TRUE);
		flc.contextPut("allowedToMeet", Boolean.valueOf(allowedToMeet));
		// only change from invisible to visible
		if(!joinButton.isVisible()) {
			joinButton.setVisible(allowedToMeet && accessible);
		}
		joinButton.setEnabled(allowedToMeet && accessible);
	
		if(allowedToMeet && accessible) {
			boolean running = teamsService.isMeetingRunning(meeting);
			if(!running) {
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

	}
	
	private boolean isAllowedToMeet(UserRequest ureq) {
		if(meeting == null) return false;

		UserSession usess = ureq.getUserSession();
		IdentityEnvironment identEnv = usess.getIdentityEnvironment();
		if(identEnv.getRoles() == null && identEnv.getIdentity() == null) {
			boolean externalUsersAllowed = StringHelper.containsNonWhitespace(meeting.getReadableIdentifier());
			if(meeting.getEntry() != null) {
				RepositoryEntry re = meeting.getEntry();
				externalUsersAllowed &= re.getEntryStatus() == RepositoryEntryStatusEnum.published;
			}
			return externalUsersAllowed;
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
					return courseTreeNode.isVisible();
				}
				return true;
			}
		} else if(meeting.getBusinessGroup() != null) {
			return businessGroupService.isIdentityInBusinessGroup(getIdentity(), meeting.getBusinessGroup());
		}
		return false;
	}
	
	private boolean isEnded() {
		return meeting != null && meeting.getEndDate() != null && new Date().after(meeting.getEndDate());
	}
	
	private boolean isAccessible() {
		if(meeting == null) return false;
		if(meeting.isPermanent()) {
			return true;
		}

		Date now = new Date();
		Date start = meeting.getStartDate();
		Date end = meeting.getEndWithFollowupTime();
		return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
	}

	@Override
	public void event(Event event) {
		if(event instanceof TeamsMeetingEvent) {
			TeamsMeetingEvent ace = (TeamsMeetingEvent)event;
			if(ace.getMeetingKey() != null && ace.getMeetingKey().equals(meeting.getKey())) {
				reloadButtonsAndStatus();
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doJoin(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(joinButton == source && validateFormLogic(ureq)) {
			doJoin(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doJoin(UserRequest ureq) {
		if(!allowedToMeet) return;
		
		TeamsErrors errors = new TeamsErrors();
		boolean guest = ureq.getUserSession().getRoles().isGuestOnly();
		Identity id = guest ? null : getIdentity();
		meeting = teamsService.joinMeeting(meeting, id, false, guest, errors);
		
		if(StringHelper.containsNonWhitespace(meeting.getOnlineMeetingJoinUrl())) {
			MediaResource resource = new RedirectMediaResource(meeting.getOnlineMeetingJoinUrl());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		} else if(errors.hasErrors()) {
			getWindowControl().setError(TeamsUIHelper.formatErrors(getTranslator(), errors));
			reloadButtonsAndStatus();
		} else {
			reloadButtonsAndStatus();
		}
	}
}
