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
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.teams.TeamsDispatcher;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.model.TeamsErrors;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.extensions.User;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingController extends FormBasicController implements GenericEventListener {
	
	private FormLink joinButton;
	
	private TeamsMeeting meeting;
	private final boolean readOnly;
	private final boolean moderator;
	private final boolean administrator;
	private final OLATResourceable meetingOres;
	private final User graphUser;
	
	@Autowired
	private TeamsService teamsService;
	
	public TeamsMeetingController(UserRequest ureq, WindowControl wControl, TeamsMeeting meeting,
			boolean administrator, boolean moderator, boolean readOnly) {
		super(ureq, wControl, "meeting");
		this.readOnly = readOnly;
		this.meeting = meeting;
		this.moderator = moderator;
		this.administrator = administrator;
		graphUser = teamsService.lookupUser(getIdentity());
		meetingOres = OresHelper.createOLATResourceableInstance(TeamsMeeting.class.getSimpleName(), meeting.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), meetingOres);
		
		initForm(ureq);
		updateButtonsAndStatus();
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, meetingOres);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean ended = isEnded();
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			initFormInformations(layoutCont);
		}
		
		joinButton = uifactory.addFormLink("meeting.join.button", translate("meeting.join.button"), null,
				formLayout, Link.BUTTON | Link.NONTRANSLATED);
		joinButton.setElementCssClass("o_sel_teams_join");
		joinButton.setNewWindow(true, true, true);
		joinButton.setVisible(!ended);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
	}
	
	private void initFormInformations(FormLayoutContainer layoutCont) {
		layoutCont.contextPut("subject", meeting.getSubject());
		if(meeting.getStartDate() != null) {
			String start = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getStartDate());
			layoutCont.contextPut("start", start);
		}
		if(meeting.getEndDate() != null) {
			String end = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getEndDate());
			layoutCont.contextPut("end", end);
		}
		
		String descr = meeting.getDescription();
		if(StringHelper.containsNonWhitespace(descr)) {
			if(!StringHelper.isHtml(descr)) {
				descr = Formatter.escWithBR(descr).toString();
			}
			layoutCont.contextPut("description", descr);
		}
		
		if((administrator || moderator) && StringHelper.containsNonWhitespace(meeting.getReadableIdentifier())) {
			String url = TeamsDispatcher.getMeetingUrl(meeting.getReadableIdentifier());
			layoutCont.contextPut("externalUrl", url);
		}
		
		if(graphUser != null && StringHelper.containsNonWhitespace(graphUser.displayName)) {
			layoutCont.contextPut("asUser", translate("as.user", new String[] { graphUser.displayName } ));
		} else {
			layoutCont.contextPut("asUser", translate("as.user.guest"));
		}
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
		Date start = (administrator || moderator) ? meeting.getStartWithLeadTime() : meeting.getStartDate();
		Date end = meeting.getEndWithFollowupTime();
		return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
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
		joinButton.setEnabled(!readOnly && accessible);
		
		boolean running = teamsService.isMeetingRunning(meeting);
		if(moderator || administrator) {
			flc.contextPut("notStarted", Boolean.FALSE);
			if(!running) {
				joinButton.setI18nKey(translate("meeting.start.button"));
			} else {
				joinButton.setI18nKey(translate("meeting.join.button"));
			}
		} else if(!running) {
			flc.contextPut("notStarted", Boolean.TRUE);
			joinButton.setEnabled(false);
		} else {
			flc.contextPut("notStarted", Boolean.FALSE);
			joinButton.setEnabled(!readOnly);
		}

		joinButton.setPrimary(joinButton.isEnabled());
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
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(joinButton == source) {
			doJoin(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doJoin(UserRequest ureq) {
		TeamsErrors errors = new TeamsErrors();
		meeting = teamsService.joinMeeting(meeting, getIdentity(), (administrator || moderator), errors);
		if(meeting == null) {
			showWarning("warning.no.meeting");
			fireEvent(ureq, Event.BACK_EVENT);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		} else if(errors.hasErrors()) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			getWindowControl().setError(TeamsUIHelper.formatErrors(getTranslator(), errors));
			return;
		}
		
		String joinUrl = meeting.getOnlineMeetingJoinUrl();
		if(StringHelper.containsNonWhitespace(joinUrl)) {
			TeamsMeetingEvent event = new TeamsMeetingEvent(meeting.getKey(), getIdentity().getKey());
        	CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, meetingOres);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(joinUrl));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.no.access");
		}
	}
}
