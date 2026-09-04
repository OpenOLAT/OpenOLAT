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
package org.olat.modules.teams.ui.recurring;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsRecordingsPublishedRoles;
import org.olat.modules.teams.ui.EditTeamsMeetingController;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 11 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingConfigurationController extends StepFormBasicController {

	private static final String ON_KEY = "on";
	private static final String OFF_KEY = "off";
	private static final String[] onKeys = new String[] { ON_KEY };
	
	private TextElement nameEl;
	private TextElement descriptionEl;
	private TextElement mainPresenterEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private DateChooser startTimeEl;
	private DateChooser endTimeEl;
	private DateChooser endRecurringDateEl;
	private DateChooser startRecurringDateEl;
	private SingleSelection presentersEl;
	private MultipleSelectionElement participantsOpenEl;
	private FormToggle recordingEl;
	private SpacerElement recordingSpacer;
	private SingleSelection recordingStartEl;
	private MultipleSelectionElement publishRecordingsEl;
	
	private TeamsRecurringMeetingsContext meetingsContext;
	
	@Autowired
	private TeamsModule teamsModule;
	
	public TeamsMeetingConfigurationController(UserRequest ureq, WindowControl wControl,
			TeamsRecurringMeetingsContext meetingsContext, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(EditTeamsMeetingController.class, getLocale()));

		this.meetingsContext = meetingsContext;
		
		initForm(ureq);
		updateRecordingsOption();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_teams_recurring_form");
		
		String name = meetingsContext.getName();
		nameEl = uifactory.addTextElement("meeting.subject", "meeting.subject", 128, name, formLayout);
		nameEl.setElementCssClass("o_sel_teams_recurring_meeting_name");
		nameEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(name)) {
			nameEl.setFocus(true);
		}
		
		String description = meetingsContext.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);

		String mainPresenter = meetingsContext.getMainPresenter();
		mainPresenterEl = uifactory.addTextElement("meeting.main.presenter", "meeting.main.presenter", 128, mainPresenter, formLayout);
		
		startRecurringDateEl = uifactory.addDateChooser("meeting.recurring.start", "meeting.recurring.start", null, formLayout);
		startRecurringDateEl.setElementCssClass("o_sel_teams_recurring_meeting_start");
		startRecurringDateEl.setMandatory(true);
		
		endRecurringDateEl = uifactory.addDateChooser("meeting.recurring.end", "meeting.recurring.end", null, formLayout);
		endRecurringDateEl.setElementCssClass("o_sel_teams_recurring_meeting_end");
		endRecurringDateEl.setMandatory(true);
		
		Date startDate = new Date();
		startTimeEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
		startTimeEl.setMandatory(true);
		startTimeEl.setTimeOnly(true);
		startTimeEl.setValidDateCheck("form.error.date");
		
		String leadtime = Long.toString(meetingsContext.getLeadTime());
		leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
		
		Date endDate = null;
		if (endDate == null && startDate != null) {
			// set meeting time default to 1 hour
			Calendar calendar = Calendar.getInstance();
		    calendar.setTime(startDate);
		    calendar.add(Calendar.HOUR_OF_DAY, 1);
		    endDate = calendar.getTime();
		}
		endTimeEl = uifactory.addDateChooser("meeting.end", "meeting.end", endDate, formLayout);
		endTimeEl.setMandatory(true);
		endTimeEl.setDefaultValue(startTimeEl);
		endTimeEl.setTimeOnly(true);
		endTimeEl.setValidDateCheck("form.error.date");
		
		String followup = Long.toString(meetingsContext.getFollowupTime());
		followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
		
		recordingSpacer = uifactory.addSpacerElement("spacer-recording-1", formLayout, false);
		
		recordingEl = uifactory.addToggleButton("meeting.recording", "meeting.recording", translate("on"), translate("off"), formLayout);
		boolean recordingEnabled = teamsModule.isRecordingsDefaultEnabled();
		recordingEl.toggle(recordingEnabled);
		
		SelectionValues startPK = new SelectionValues();
		startPK.add(SelectionValues.entry(ON_KEY, translate("teams.recordings.auto.start.automatically")));
		startPK.add(SelectionValues.entry(OFF_KEY, translate("teams.recordings.auto.start.manually")));
		recordingStartEl = uifactory.addRadiosVertical("meeting.recording.start", formLayout, startPK.keys(), startPK.values());
		String startOption = teamsModule.isRecordingsAutoStartEnabled() ? ON_KEY : OFF_KEY;
		recordingStartEl.select(startOption, true);
		
		SelectionValues publishingDefaultKV = new SelectionValues();
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.coach.name(), translate("teams.recordings.publish.to.coach")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.participant.name(), translate("teams.recordings.publish.to.participant")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.all.name(), translate("teams.recordings.publish.to.all")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.guest.name(), translate("teams.recordings.publish.to.guest")));
		publishRecordingsEl = uifactory.addCheckboxesVertical("meeting.recording.publishing", formLayout, publishingDefaultKV.keys(), publishingDefaultKV.values(), 1);
		publishRecordingsEl.setHelpText(translate("teams.recordings.publishing.hint"));
		TeamsRecordingsPublishedRoles[] publishingRoles = teamsModule.getRecordingsDefaultPublicationSettings();
		for(TeamsRecordingsPublishedRoles publishedRole:publishingRoles) {
			publishRecordingsEl.select(publishedRole.name(), true);
		}
		
		uifactory.addSpacerElement("spacer-opening-2", formLayout, false);
		
		String[] onOpenValues = new String[] { translate("meeting.participants.open.on",
				new String[] {teamsModule.getTenantOrganisation() }) };
		participantsOpenEl = uifactory.addCheckboxesHorizontal("meeting.participants.open", formLayout, onKeys, onOpenValues);
		participantsOpenEl.setHelpTextKey("meeting.participants.open.hint", null);
		participantsOpenEl.setVisible(StringHelper.containsNonWhitespace(teamsModule.getProducerId()));
		
		SelectionValues presentersKeyValues = new SelectionValues();
		presentersKeyValues.add(SelectionValues.entry(OnlineMeetingPresenters.RoleIsPresenter.name(), translate("meeting.presenters.role")));
		presentersKeyValues.add(SelectionValues.entry(OnlineMeetingPresenters.Organization.name(), translate("meeting.presenters.organization")));
		presentersKeyValues.add(SelectionValues.entry(OnlineMeetingPresenters.Everyone.name(), translate("meeting.presenters.everyone")));
		presentersEl = uifactory.addDropdownSingleselect("meeting.presenters", formLayout, presentersKeyValues.keys(), presentersKeyValues.values());
		presentersEl.setMandatory(true);
		if(meetingsContext.getAllowedPresenters() != null && presentersKeyValues.containsKey(meetingsContext.getAllowedPresenters())) {
			presentersEl.select(meetingsContext.getAllowedPresenters(), true);
		} else {
			presentersEl.select(OnlineMeetingPresenters.RoleIsPresenter.name(), true);
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		startRecurringDateEl.clearError();
		if(startRecurringDateEl.getDate() == null) {
			startRecurringDateEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		endRecurringDateEl.clearError();
		if(endRecurringDateEl.getDate() == null) {
			endRecurringDateEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		if(startRecurringDateEl.getDate() != null && endRecurringDateEl.getDate() != null
				&& endRecurringDateEl.getDate().before(startRecurringDateEl.getDate())) {
			endRecurringDateEl.setErrorKey("error.start.after.end");
			allOk &= false;
		}

		startTimeEl.clearError();
		if(startTimeEl.getDate() == null) {
			startTimeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!validateFormItem(ureq, startTimeEl)) {
			allOk &= false;
		}
		
		endTimeEl.clearError();
		if(endTimeEl.getDate() == null) {
			endTimeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!validateFormItem(ureq, endTimeEl)) {
			allOk &= false;
		}
		
		if(startTimeEl.getDate() != null && endTimeEl.getDate() != null) {
			long start = startTimeEl.getDate().getTime();
			long end = endTimeEl.getDate().getTime();
			if(start > end) {
				endTimeEl.setErrorKey("error.start.after.end");
				allOk &= false;
			}
		}
		
		if(allOk) {
			Date firstDate = getFirstDateTime();
			if(firstDate != null && firstDate.before(new Date())) {
				startRecurringDateEl.setErrorKey("error.first.date.in.past");
				allOk &= false;
			}
		}
		
		allOk &= validateTime(leadTimeEl, 15l);
		allOk &= validateTime(followupTimeEl, 15l);
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (nameEl.getValue().contains("&")) {
			nameEl.setErrorKey("form.invalidchar.noamp");
			allOk &= false;
		}
		return allOk;
	}
	
	private Date getFirstDateTime() {
		if(startRecurringDateEl.getDate() != null && startTimeEl.getDate() != null) {
			return TeamsRecurringMeetingsContext
					.transferTime(startRecurringDateEl.getDate(), startTimeEl.getDate());
		}
		return null;
	}
	
	private boolean validateTime(TextElement el, long maxValue) {
		boolean allOk = true;
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			if(!StringHelper.isLong(el.getValue())) {
				el.setErrorKey("form.error.nointeger");
				allOk &= false;
			} else if(Long.parseLong(el.getValue()) > maxValue) {
				el.setErrorKey("error.too.long.time", Long.toString(maxValue));
				allOk &= false;
			}
		}
		return allOk;
	}
	
	public long getLeadTime() {
		long leadTime = 0;
		if(leadTimeEl.isVisible() && StringHelper.isLong(leadTimeEl.getValue())) {
			leadTime = Long.valueOf(leadTimeEl.getValue());
		}
		return leadTime;
	}
	
	private long getFollowupTime() {
		long followupTime = 0;
		if(followupTimeEl.isVisible() && StringHelper.isLong(followupTimeEl.getValue())) {
			followupTime = Long.valueOf(followupTimeEl.getValue());
		}
		return followupTime;
	}
	
	private void updateRecordingsOption() {
		boolean recordingsEnabled = teamsModule.isRecordingsEnabled();
		recordingEl.setVisible(recordingsEnabled);
		recordingSpacer.setVisible(recordingsEnabled);
		
		boolean enabled = recordingsEnabled && recordingEl.isOn();
		recordingStartEl.setVisible(enabled);
		boolean previousPublishRecordings = publishRecordingsEl.isVisible();
		publishRecordingsEl.setVisible(enabled);
		if(!previousPublishRecordings && publishRecordingsEl.isVisible()) {
			initPublishRecordingsElement();
		}
		
		// If recordings are enabled, the organizer must open the meeting
		participantsOpenEl.setEnabled(!enabled);
		if(enabled && participantsOpenEl.isAtLeastSelected(1)) {
			participantsOpenEl.uncheckAll();
		}
	}
	
	private void initPublishRecordingsElement() {
		TeamsRecordingsPublishedRoles[] publishingRoles = teamsModule.getRecordingsDefaultPublicationSettings();
		for(TeamsRecordingsPublishedRoles publishedRole:publishingRoles) {
			publishRecordingsEl.select(publishedRole.name(), true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(recordingEl == source) {
			updateRecordingsOption();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		meetingsContext.setName(nameEl.getValue());
		meetingsContext.setDescription(descriptionEl.getValue());
		meetingsContext.setMainPresenter(mainPresenterEl.getValue());

		Date startDate = startTimeEl.getDate();
		meetingsContext.setStartTime(startDate);
		Date endDate = endTimeEl.getDate();
		meetingsContext.setEndTime(endDate);
		long leadTime = getLeadTime();
		meetingsContext.setLeadTime(leadTime);
		long followupTime = getFollowupTime();
		meetingsContext.setFollowupTime(followupTime);
		
		meetingsContext.setStartRecurringDate(startRecurringDateEl.getDate());
		meetingsContext.setEndRecurringDate(endRecurringDateEl.getDate());
		
		meetingsContext.setAllowedPresenters(presentersEl.getSelectedKey());
		meetingsContext.setParticipantsCanOpen(participantsOpenEl.isAtLeastSelected(1));
		
		if(recordingEl.isOn()) {
			meetingsContext.setRecord(true);
			meetingsContext.setRecordAutoStart(recordingStartEl.isOneSelected()
					&& ON_KEY.equals(recordingStartEl.getSelectedKey()));
		
			if(publishRecordingsEl.isVisible()) {
				meetingsContext.setRecordingsPublishing(TeamsRecordingsPublishedRoles.toArray(publishRecordingsEl.getSelectedKeys()));
			} else {
				meetingsContext.setRecordingsPublishing(null);
			}
		} else {
			meetingsContext.setRecord(false);
			meetingsContext.setRecordAutoStart(false);
			meetingsContext.setRecordingsPublishing(null);
		}
		
		meetingsContext.generateMeetings();

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
