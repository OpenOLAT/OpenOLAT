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

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.TeamsDispatcher;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsRecordingsPublishedRoles;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.manager.MicrosoftGraphDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.ACService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTeamsMeetingController extends FormBasicController {

	private static final String ON_KEY = "on";
	private static final String OFF_KEY = "off";
	private static final String[] onKeys = new String[] { ON_KEY };

	private FormLink openCalLink;
	private TextElement subjectEl;
	private TextElement descriptionEl;
	private TextElement mainPresenterEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private SingleSelection participantsOpenEl;
	private SingleSelection presentersEl;
	private MultipleSelectionElement guestEl;
	private TextElement externalLinkEl;
	private FormToggle recordingEl;
	private SpacerElement recordingSpacer;
	private SingleSelection recordingStartEl;
	private MultipleSelectionElement publishRecordingsEl;
	
	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;
	
	private final Mode mode;
	private final boolean editable;
	private TeamsMeeting meeting;
	
	private Object userObject;

	private CloseableModalController cmc;
	private TeamsMeetingsCalendarController calendarCtr;
	
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ACService acService;
	@Autowired
	private TeamsService teamsService;
	
	public EditTeamsMeetingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdentifier, BusinessGroup group, Mode mode) {
		super(ureq, wControl);
		this.mode = mode;
		this.entry = entry;
		this.group = group;
		this.subIdent = subIdentifier;
		editable = true;
		
		initForm(ureq);
		updateRecordingsOption();
	}
	
	public EditTeamsMeetingController(UserRequest ureq, WindowControl wControl,
			TeamsMeeting meeting) {
		super(ureq, wControl);
		mode = meeting.isPermanent() ? Mode.permanent : Mode.dates;
		this.entry = meeting.getEntry();
		this.subIdent = meeting.getSubIdent();
		this.group = meeting.getBusinessGroup();
		this.meeting = meeting;
		editable = TeamsUIHelper.isEditable(meeting);
		
		initForm(ureq);
		updateRecordingsOption();
	}
	
	public TeamsMeeting getMeeting() {
		return meeting;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	public void removeDates() {
		startDateEl.setVisible(false);
		endDateEl.setVisible(false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_teams_edit_meeting");
		
		String subject = meeting == null ? "" : meeting.getSubject();
		subjectEl = uifactory.addTextElement("meeting.subject", "meeting.subject", 128, subject, formLayout);
		subjectEl.setElementCssClass("o_sel_teams_edit_meeting_subject");
		subjectEl.setMandatory(true);
		subjectEl.setEnabled(editable);
		if(editable && !StringHelper.containsNonWhitespace(subject)) {
			subjectEl.setFocus(true);
		}
		
		Identity creator = meeting == null ? getIdentity() : meeting.getCreator();
		if(creator != null) {
			String creatorFullName = userManager.getUserDisplayName(creator);
			uifactory.addStaticTextElement("meeting.creator", creatorFullName, formLayout);
		}
		
		String description = meeting == null ? "" : meeting.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);
		descriptionEl.setEnabled(editable);
		
		String presenter = meeting == null ? userManager.getUserDisplayName(getIdentity()) : meeting.getMainPresenter();
		mainPresenterEl = uifactory.addTextElement("meeting.main.presenter", "meeting.main.presenter", 128, presenter, formLayout);
		mainPresenterEl.setElementCssClass("o_sel_bbb_edit_meeting_presenter");
		mainPresenterEl.setEnabled(editable);
		
		String[] guestValues = new String[] { translate("meeting.guest.on") };
		guestEl = uifactory.addCheckboxesHorizontal("meeting.guest", formLayout, onKeys, guestValues);
		guestEl.setVisible(entry != null && entry.isPublicVisible() && acService.isGuestAccessible(entry, false));
		guestEl.select(onKeys[0], meeting != null && meeting.isGuest());
		guestEl.setEnabled(editable);
		
		String externalLink = meeting == null ? CodeHelper.getForeverUniqueID() + "" : meeting.getReadableIdentifier();
		externalLinkEl = uifactory.addTextElement("meeting.external.users", 64, externalLink, formLayout);
		externalLinkEl.setPlaceholderKey("meeting.external.users.empty", null);
		externalLinkEl.setHelpTextKey("meeting.external.users.help", null);
		externalLinkEl.addActionListener(FormEvent.ONCHANGE);
		if (externalLink != null) {
			externalLinkEl.setExampleKey("noTransOnlyParam", new String[] {TeamsDispatcher.getMeetingUrl(externalLink)});			
		}
		
		openCalLink = uifactory.addFormLink("calendar.open", formLayout);
		openCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
		
		if(mode == Mode.dates) {
			Date startDate = meeting == null ? new Date() : meeting.getStartDate();
			startDateEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
			startDateEl.setMandatory(true);
			startDateEl.setDateChooserTimeEnabled(true);
			startDateEl.setValidDateCheck("form.error.date");
			startDateEl.setEnabled(editable);
			
			String leadtime = meeting == null ? null : Long.toString(meeting.getLeadTime());
			leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
			leadTimeEl.setEnabled(editable);
			leadTimeEl.setExampleKey("meeting.leadTime.explain", null);
			
			Date endDate = meeting == null ? null : meeting.getEndDate();
			if (endDate == null && startDate != null) {
				// set meeting time default to 1 hour
				Calendar calendar = Calendar.getInstance();
			    calendar.setTime(startDate);
			    calendar.add(Calendar.HOUR_OF_DAY, 1);
			    endDate = calendar.getTime();
			}
			endDateEl = uifactory.addDateChooser("meeting.end", "meeting.end", endDate, formLayout);
			endDateEl.setMandatory(true);
			endDateEl.setDefaultValue(startDateEl);
			endDateEl.setDateChooserTimeEnabled(true);
			endDateEl.setValidDateCheck("form.error.date");
			endDateEl.setEnabled(editable);
			
			String followup = meeting == null ? null : Long.toString(meeting.getFollowupTime());
			followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
			followupTimeEl.setEnabled(editable);
		}
		
		recordingSpacer = uifactory.addSpacerElement("spacer-recording-1", formLayout, false);
		
		recordingEl = uifactory.addToggleButton("meeting.recording", "meeting.recording", translate("on"), translate("off"), formLayout);
		boolean recordingEnabled = meeting == null
				? teamsModule.isRecordingsDefaultEnabled()	
				: meeting.isRecord();
		recordingEl.toggle(recordingEnabled);
		recordingEl.setEnabled(editable);
		
		SelectionValues startPK = new SelectionValues();
		startPK.add(SelectionValues.entry(ON_KEY, translate("teams.recordings.auto.start.automatically")));
		startPK.add(SelectionValues.entry(OFF_KEY, translate("teams.recordings.auto.start.manually")));
		recordingStartEl = uifactory.addRadiosVertical("meeting.recording.start", formLayout, startPK.keys(), startPK.values());
		recordingStartEl.setEnabled(editable);
		String startOption;
		if(meeting == null) {
			startOption = teamsModule.isRecordingsAutoStartEnabled() ? ON_KEY : OFF_KEY;
		} else {
			startOption = meeting.isRecordAutoStart() ? ON_KEY : OFF_KEY;
		}
		recordingStartEl.select(startOption, true);
		
		SelectionValues publishingDefaultKV = new SelectionValues();
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.coach.name(), translate("teams.recordings.publish.to.coach")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.participant.name(), translate("teams.recordings.publish.to.participant")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.all.name(), translate("teams.recordings.publish.to.all")));
		publishingDefaultKV.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.guest.name(), translate("teams.recordings.publish.to.guest")));
		publishRecordingsEl = uifactory.addCheckboxesVertical("meeting.recording.publishing", formLayout, publishingDefaultKV.keys(), publishingDefaultKV.values(), 1);
		publishRecordingsEl.setHelpText(translate("teams.recordings.publishing.hint"));
		publishRecordingsEl.setEnabled(editable);
		initPublishRecordingsElement();
		
		uifactory.addSpacerElement("spacer-opening-2", formLayout, false);

		SelectionValues openPK = new SelectionValues();
		openPK.add(SelectionValues.entry(OFF_KEY, translate("meeting.participants.open.off.title"),
				translate("meeting.participants.open.off", new String[] {teamsModule.getTenantOrganisation() }), null, null, true));
		openPK.add(SelectionValues.entry(ON_KEY, translate("meeting.participants.open.on.title"),
				translate("meeting.participants.open.on", new String[] {teamsModule.getTenantOrganisation() }), null, null, true));
		participantsOpenEl = uifactory.addCardSingleSelectHorizontal("meeting.participants.open", "meeting.participants.open", formLayout, openPK);
		participantsOpenEl.setEnabled(editable);
		participantsOpenEl.setHelpTextKey("meeting.participants.open.hint", null);
		participantsOpenEl.addActionListener(FormEvent.ONCHANGE);
		participantsOpenEl.setVisible(StringHelper.containsNonWhitespace(teamsModule.getProducerId()));
		if(meeting != null && meeting.isParticipantsCanOpen()) {
			participantsOpenEl.select(ON_KEY, true);
		} else {
			participantsOpenEl.select(OFF_KEY, true);
		}

		SelectionValues presentersKeyValues = getPresenters(participantsOpenEl.isOneSelected() && ON_KEY.equals(participantsOpenEl.getSelectedKey()));
		presentersEl = uifactory.addDropdownSingleselect("meeting.presenters", formLayout, presentersKeyValues.keys(), presentersKeyValues.values());
		presentersEl.setMandatory(true);
		presentersEl.setEnabled(editable);
		
		TeamsUIHelper.setDefaults(presentersEl, meeting);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		if(editable) {
			uifactory.addFormSubmitButton("save", buttonLayout);
		}
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private void initPublishRecordingsElement() {
		TeamsRecordingsPublishedRoles[] publishingRoles = meeting == null
				? teamsModule.getRecordingsDefaultPublicationSettings()
				: meeting.getRecordingsPublishingEnum();
		for(TeamsRecordingsPublishedRoles publishedRole:publishingRoles) {
			publishRecordingsEl.select(publishedRole.name(), true);
		}
	}
	
	private SelectionValues getPresenters(boolean attendee) {
		SelectionValues presentersKeyValues = new SelectionValues();
		addPresenter(presentersKeyValues, attendee, OnlineMeetingPresenters.RoleIsPresenter, "meeting.presenters.role");
		addPresenter(presentersKeyValues, attendee, OnlineMeetingPresenters.Organization, "meeting.presenters.organization");
		addPresenter(presentersKeyValues, attendee, OnlineMeetingPresenters.Everyone, "meeting.presenters.everyone");
		return presentersKeyValues;
	}
	
	private void addPresenter(SelectionValues presentersKeyValues, boolean attendee, OnlineMeetingPresenters presenter, String i18nKey) {
		if(!attendee || MicrosoftGraphDAO.ALLOWED_PRESENTERS_FOR_ATTENDEE.contains(presenter)) {
			presentersKeyValues.add(SelectionValues.entry(presenter.name(), translate(i18nKey)));	
		}
	}

	private void updateAccessOption() {
		boolean attendeeMode = participantsOpenEl.isOneSelected() && ON_KEY.equals(participantsOpenEl.getSelectedKey());
		
		String presentersKey = presentersEl.getSelectedKey();
		SelectionValues presentersKeyValues = getPresenters(attendeeMode);
		presentersEl.setKeysAndValues(presentersKeyValues.keys(), presentersKeyValues.values(), null);
		if(presentersKeyValues.containsKey(presentersKey)) {
			presentersEl.select(presentersKey, true);
		} else if(attendeeMode && presentersKeyValues.containsKey(OnlineMeetingPresenters.Everyone.name())) {
			presentersEl.select(OnlineMeetingPresenters.Everyone.name(), true);
		} else {
			presentersEl.select(presentersKeyValues.keys()[0], true);
		}
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
		participantsOpenEl.setEnabled(!enabled && editable);
		if(enabled && participantsOpenEl.isOneSelected() && ON_KEY.equals(participantsOpenEl.getSelectedKey())) {
			participantsOpenEl.select(OFF_KEY, true);
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTextElement(subjectEl, 255, true);
		allOk &= validateTextElement(descriptionEl, 4000, false);
		if(mode == Mode.dates && startDateEl.isVisible()) {
			allOk &= TeamsUIHelper.validateDates(startDateEl, endDateEl);
		}

		allOk &= TeamsUIHelper.validateReadableIdentifier(externalLinkEl, meeting);
		
		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength, boolean mandatory) {
		boolean allOk = true;

		el.clearError();
		String val = el.getValue();
		if(!StringHelper.containsNonWhitespace(val) && mandatory) {
			el.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			el.setErrorKey("input.toolong", Integer.toString(maxLength));
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calendarCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calendarCtr);
		removeAsListenerAndDispose(cmc);
		calendarCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (openCalLink == source) {
			doOpenCalendar(ureq);
		} else if(participantsOpenEl == source) {
			updateAccessOption();
		} else if(recordingEl == source) {
			updateRecordingsOption();
		}
	}

	@Override
	public void formOK(UserRequest ureq) {
		Date startDate = null;
		Date endDate = null;
		if(mode == Mode.dates) {
			startDate = startDateEl.getDate();
			endDate = endDateEl.getDate();
		}
		
		if(meeting == null) {
			meeting = teamsService.createMeeting(subjectEl.getValue(),
					startDate, endDate, entry, subIdent, group, getIdentity());
		} else {
			meeting.setSubject(subjectEl.getValue());
			meeting.setStartDate(startDate);
			meeting.setEndDate(endDate);
		}

		if(mode == Mode.dates) {
			long leadTime = TeamsUIHelper.getLongOrZero(leadTimeEl);
			meeting.setLeadTime(leadTime);
			long followupTime = TeamsUIHelper.getLongOrZero(followupTimeEl);
			meeting.setFollowupTime(followupTime);
		} else {
			meeting.setLeadTime(0l);
			meeting.setFollowupTime(0l);
		}
		
		if(externalLinkEl.isVisible() && StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
			meeting.setReadableIdentifier(externalLinkEl.getValue());
		} else {
			meeting.setReadableIdentifier(null);
		}
		
		boolean guests = guestEl.isVisible() && guestEl.isAtLeastSelected(1);
		meeting.setGuest(guests);
		
		meeting.setPermanent(mode == Mode.permanent);
		meeting.setDescription(descriptionEl.getValue());
		meeting.setMainPresenter(mainPresenterEl.getValue());
		meeting.setAllowedPresenters(presentersEl.getSelectedKey());
		meeting.setParticipantsCanOpen(participantsOpenEl.isOneSelected()
				&& ON_KEY.equals(participantsOpenEl.getSelectedKey()));
		
		if(recordingEl.isOn()) {
			meeting.setRecord(true);
			meeting.setRecordAutoStart(recordingStartEl.isOneSelected()
					&& ON_KEY.equals(recordingStartEl.getSelectedKey()));
		
			if(publishRecordingsEl.isVisible()) {
				meeting.setRecordingsPublishingEnum(TeamsRecordingsPublishedRoles.toArray(publishRecordingsEl.getSelectedKeys()));
			} else {
				meeting.setRecordingsPublishingEnum(null);
			}
		} else {
			meeting.setRecord(false);
			meeting.setRecordAutoStart(false);
			meeting.setRecordingsPublishingEnum(null);
		}
		
		meeting = teamsService.updateMeeting(meeting, true);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calendarCtr);
		removeAsListenerAndDispose(cmc);

		// open calendar controller in modal. Not very nice to have stacked modal, but
		// still better than having no overview at all
		calendarCtr = new TeamsMeetingsCalendarController(ureq, getWindowControl());
		listenTo(calendarCtr);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), calendarCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}
	
	public enum Mode {
		permanent,
		dates
	}
}
