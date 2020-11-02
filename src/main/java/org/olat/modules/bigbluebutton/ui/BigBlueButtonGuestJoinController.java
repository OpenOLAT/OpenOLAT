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

import java.util.Date;

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
import org.olat.course.nodes.BigBlueButtonCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.bigbluebutton.BigBlueButtonEditController;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonGuestJoinController extends FormBasicController implements GenericEventListener {

	public static final String PARTICIPATION_IDENTIFIER = "evaluation-form-participation-identifier";
	
	private TextElement nameEl;
	private FormLink joinButton;
	private MultipleSelectionElement acknowledgeRecordingEl;

	private boolean readOnly = false;
	private boolean moderatorStartMeeting;
	private final boolean allowedToMeet;
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

	public BigBlueButtonGuestJoinController(UserRequest ureq, WindowControl wControl, BigBlueButtonMeeting meeting) {
		super(ureq, wControl, "guest_join");
		this.meeting = meeting;
		moderatorStartMeeting = isModeratorStartMeeting();
		allowedToMeet = isAllowedToMeet(ureq);
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
		
		boolean end = isEnded();
		joinButton = uifactory.addFormLink("meeting.join.button", formLayout, Link.BUTTON_LARGE);
		joinButton.setElementCssClass("o_sel_bbb_guest_join");
		joinButton.setVisible(!end);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		
		KeyValues acknowledgeKeyValue = new KeyValues();
		acknowledgeKeyValue.add(KeyValues.entry("agree", translate("meeting.acknowledge.recording.agree")));
		acknowledgeRecordingEl = uifactory.addCheckboxesHorizontal("meeting.acknowledge.recording", null, formLayout,
				acknowledgeKeyValue.keys(), acknowledgeKeyValue.values());
		acknowledgeRecordingEl.setVisible(!end && BigBlueButtonUIHelper.isRecord(meeting));
	}
	
	private void reloadButtonsAndStatus() {
		meeting = bigBlueButtonManager.getMeeting(meeting);
		updateButtonsAndStatus();
		flc.setDirty(true);
	}
	
	private void updateButtonsAndStatus() {
		boolean isEnded = isEnded();
		boolean accessible = isAccessible();
		boolean disabled = isDisabled();
		flc.contextPut("disabled", Boolean.valueOf(disabled));
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		flc.contextPut("notStarted", Boolean.TRUE);
		flc.contextPut("allowedToMeet", Boolean.valueOf(allowedToMeet));
		// only change from invisible to visible
		if(!joinButton.isVisible()) {
			joinButton.setVisible(allowedToMeet && accessible && !disabled);
		}
		joinButton.setEnabled(allowedToMeet && accessible && !disabled);
	
		if(allowedToMeet && accessible && !disabled) {
			boolean running = bigBlueButtonManager.isMeetingRunning(meeting);
			if(!running && moderatorStartMeeting) {
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
	
	private boolean isAccessible() {
		if(meeting == null) return false;
		if(meeting.isPermanent()) {
			return bigBlueButtonModule.isPermanentMeetingEnabled();
		}

		Date now = new Date();
		Date start = meeting.getStartDate();
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
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(nameEl.getValue().length() > 64) {
			nameEl.setErrorKey("form.error.toolong", new String[] { "64" });
			allOk &= false;
		}
		
		acknowledgeRecordingEl.clearError();
		if(acknowledgeRecordingEl.isVisible()
				&& acknowledgeRecordingEl.isEnabled() && !acknowledgeRecordingEl.isAtLeastSelected(1)) {
			acknowledgeRecordingEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
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
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		String pseudo = nameEl.getValue();

		BigBlueButtonAttendeeRoles role;
		UserSession usess = ureq.getUserSession();
		if(getIdentity() == null || usess.getRoles() == null) {
			role = BigBlueButtonAttendeeRoles.external;
		} else {
			role = BigBlueButtonAttendeeRoles.guest;
		}
		String url = bigBlueButtonManager.join(meeting, null, pseudo, role, null, errors);
		MediaResource resource = new RedirectMediaResource(url);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}
