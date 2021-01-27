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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
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
import org.olat.modules.teams.TeamsService;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.LobbyBypassScope;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTeamsMeetingController extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };

	private FormLink openCalLink;
	private TextElement subjectEl;
	private TextElement descriptionEl;
	private TextElement mainPresenterEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private MultipleSelectionElement participantsOpenEl;
	private SingleSelection accessLevelEl;
	private SingleSelection presentersEl;
	private MultipleSelectionElement annoncementEl;
	private SingleSelection lobbyEl;
	private MultipleSelectionElement guestEl;
	private TextElement externalLinkEl;
	
	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;
	
	private final Mode mode;
	private final boolean editable;
	private final boolean editableGraph;
	private final boolean meetingExtendedOptionsEnabled;
	private TeamsMeeting meeting;

	private CloseableModalController cmc;
	private TeamsMeetingsCalendarController calendarCtr;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private TeamsModule teamsModule;
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
		editableGraph = true;
		meetingExtendedOptionsEnabled = teamsModule.isOnlineMeetingExtendedOptionsEnabled();
		
		initForm(ureq);
	}
	
	public EditTeamsMeetingController(UserRequest ureq, WindowControl wControl,
			TeamsMeeting meeting) {
		super(ureq, wControl);
		mode = meeting.isPermanent() ? Mode.permanent : Mode.dates;
		this.entry = meeting.getEntry();
		this.subIdent = meeting.getSubIdent();
		this.group = meeting.getBusinessGroup();
		this.meeting = meeting;
		editable = TeamsUIHelper.isEditable(meeting, ureq);
		editableGraph = TeamsUIHelper.isEditableGraph(meeting);
		meetingExtendedOptionsEnabled = teamsModule.isOnlineMeetingExtendedOptionsEnabled();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
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
		guestEl.setVisible(entry != null && entry.isGuests());
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
			endDateEl.setEnabled(editable);
			
			String followup = meeting == null ? null : Long.toString(meeting.getFollowupTime());
			followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
			followupTimeEl.setEnabled(editable);
		}

		String[] onOpenValues = new String[] { "" };
		participantsOpenEl = uifactory.addCheckboxesHorizontal("meeting.participants.open", formLayout, onKeys, onOpenValues);
		participantsOpenEl.setHelpTextKey("meeting.participants.open.hint", null);
		if(meeting != null && meeting.isParticipantsCanOpen()) {
			participantsOpenEl.select("on", true);
		}
		
		KeyValues accessKeyValues = new KeyValues();
		String organisation = teamsModule.getTenantOrganisation();
		accessKeyValues.add(KeyValues.entry(AccessLevel.EVERYONE.name(), translate("meeting.accesslevel.everyone")));
		accessKeyValues.add(KeyValues.entry(AccessLevel.SAME_ENTERPRISE.name(),
				translate("meeting.accesslevel.same.enterprise", new String[] { organisation })));
		accessKeyValues.add(KeyValues.entry(AccessLevel.SAME_ENTERPRISE_AND_FEDERATED.name(),
				translate("meeting.accesslevel.same.enterprise.federated", new String[] { organisation })));
		accessLevelEl = uifactory.addDropdownSingleselect("meeting.accesslevel", formLayout, accessKeyValues.keys(), accessKeyValues.values());
		accessLevelEl.setMandatory(true);
		accessLevelEl.setEnabled(editable && editableGraph);

		KeyValues presentersKeyValues = new KeyValues();
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name(), translate("meeting.presenters.role")));
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ORGANIZATION.name(), translate("meeting.presenters.organization")));
		presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.EVERYONE.name(), translate("meeting.presenters.everyone")));
		presentersEl = uifactory.addDropdownSingleselect("meeting.presenters", formLayout, presentersKeyValues.keys(), presentersKeyValues.values());
		presentersEl.setMandatory(true);
		presentersEl.setEnabled(editable);
		
		String[] onValues = new String[] { translate("meeting.annoncement.on") };
		annoncementEl = uifactory.addCheckboxesHorizontal("meeting.annoncement", formLayout, onKeys, onValues);
		annoncementEl.setEnabled(editable && editableGraph);
		annoncementEl.setVisible(meetingExtendedOptionsEnabled);
		if(meeting != null && meeting.isEntryExitAnnouncement()) {
			annoncementEl.select(onKeys[0], true);
		}
		
		KeyValues lobbyKeyValues = new KeyValues();
		lobbyKeyValues.add(KeyValues.entry(LobbyBypassScope.EVERYONE.name(), translate("meeting.lobby.bypass.everyone")));
		lobbyKeyValues.add(KeyValues.entry(LobbyBypassScope.ORGANIZATION.name(),
				translate("meeting.lobby.bypass.organization", new String[] { organisation })));
		lobbyKeyValues.add(KeyValues.entry(LobbyBypassScope.ORGANIZATION_AND_FEDERATED.name(),
				translate("meeting.lobby.bypass.same.enterprise.federated", new String[] { organisation })));
		lobbyKeyValues.add(KeyValues.entry(LobbyBypassScope.ORGANIZER.name(), translate("meeting.lobby.bypass.organizer")));
		lobbyEl = uifactory.addDropdownSingleselect("meeting.lobby.bypass", formLayout, lobbyKeyValues.keys(), lobbyKeyValues.values());
		lobbyEl.setMandatory(true);
		lobbyEl.setEnabled(editable);

		TeamsUIHelper.setDefaults(accessLevelEl, presentersEl, lobbyEl, meeting, meetingExtendedOptionsEnabled);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		if(editable) {
			uifactory.addFormSubmitButton("save", buttonLayout);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTextElement(subjectEl, 255, true);
		allOk &= validateTextElement(descriptionEl, 4000, false);
		if(mode == Mode.dates) {
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
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
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
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
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
		meeting.setAccessLevel(accessLevelEl.getSelectedKey());
		meeting.setAllowedPresenters(presentersEl.getSelectedKey());
		meeting.setEntryExitAnnouncement(annoncementEl.isAtLeastSelected(1));
		meeting.setLobbyBypassScope(lobbyEl.getSelectedKey());
		meeting.setParticipantsCanOpen(participantsOpenEl.isAtLeastSelected(1));
		
		meeting = teamsService.updateMeeting(meeting);
		
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
		cmc = new CloseableModalController(getWindowControl(), "close", calendarCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}
	
	public enum Mode {
		permanent,
		dates
	}
}
