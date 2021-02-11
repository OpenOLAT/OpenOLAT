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
package org.olat.modules.appointments.ui;

import static java.util.Arrays.asList;
import static org.olat.core.gui.components.util.KeyValues.entry;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.getSelectedTemplate;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.isWebcamLayoutAvailable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentRef;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingsCalendarController;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.modules.teams.TeamsDispatcher;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.ui.EditTeamsMeetingController;
import org.olat.modules.teams.ui.TeamsMeetingsCalendarController;
import org.olat.modules.teams.ui.TeamsUIHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 * Initial date: 15 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentEditController extends FormBasicController {
	
	private static final String[] KEYS_YES_NO = new String[] { "yes", "no" };
	private static final String KEY_NO = "no";
	private static final String KEY_BIGBLUEBUTTON = "bbb";
	private static final String KEY_TEAMS = "teams";
	
	private DateChooser startEl;
	private DateChooser endEl;
	private TextElement locationEl;
	private TextElement maxParticipationsEl;
	private TextElement detailsEl;
	private SpacerElement meetingSpacer;
	private SingleSelection meetingEl;
	// BigBlueButton
	private TextElement bbbExternalLinkEl;
	private FormLink bbbOpenCalLink;
	private TextElement bbbLeadTimeEl;
	private TextElement bbbFollowupTimeEl;
	private TextElement welcomeEl;
	private SingleSelection templateEl;
	private SingleSelection recordEl;
	private SingleSelection layoutEl;
	// Teams
	private StaticTextElement teamsCreatorEl;
	private FormLink teamsOpenCalLink;
	private TextElement teamsLeadTimeEl;
	private TextElement teamsFollowupTimeEl;
	private SingleSelection presentersEl;
	private TextElement teamsExternalLinkEl;
	
	private BigBlueButtonMeetingsCalendarController bbbCalendarCtr;
	private TeamsMeetingsCalendarController teamsCalendarCtr;
	private CloseableModalController cmc;
	
	private Topic topic;
	private Appointment appointment;
	private final boolean hasParticipations;
	private final boolean bbbEditable;
	private BigBlueButtonMeeting bbbMeeting;
	private List<BigBlueButtonMeetingTemplate> templates;
	private TeamsMeeting teamsMeeting;
	private final boolean teamsEditable;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;

	public AppointmentEditController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(EditTeamsMeetingController.class, getLocale(), getTranslator()));
		this.topic = topic;
		this.hasParticipations = false;
		this.bbbEditable = true;
		this.bbbMeeting = null;
		this.teamsMeeting = null;
		this.teamsEditable = true;
		initForm(ureq);
		updateUI();
	}

	public AppointmentEditController(UserRequest ureq, WindowControl wControl, AppointmentRef appointmentRef) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(EditTeamsMeetingController.class, getLocale(), getTranslator()));
		
		AppointmentSearchParams appointmentParams = new AppointmentSearchParams();
		appointmentParams.setAppointment(appointmentRef);
		appointmentParams.setFetchTopic(true);
		appointmentParams.setFetchEntry(true);
		appointmentParams.setFetchMeetings(true);
		this.appointment = appointmentsService.getAppointments(appointmentParams).get(0);
		this.topic = appointment.getTopic();

		this.bbbMeeting = appointment.getBBBMeeting();
		this.bbbEditable = bbbMeeting == null || bbbMeeting.getServer() == null;
		
		this.teamsMeeting = appointment.getTeamsMeeting();
		teamsEditable = TeamsUIHelper.isEditable(teamsMeeting);
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(appointmentRef);
		this.hasParticipations = appointmentsService.getParticipationCount(params).longValue() > 0;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Date start = appointment != null? appointment.getStart(): null;
		startEl = uifactory.addDateChooser("appointment.start", start, formLayout);
		startEl.setDateChooserTimeEnabled(true);
		startEl.setMandatory(true);
		startEl.setEnabled(!hasParticipations);
		startEl.addActionListener(FormEvent.ONCHANGE);
		
		Date end = appointment != null? appointment.getEnd(): null;
		endEl = uifactory.addDateChooser("appointment.end", end, formLayout);
		endEl.setDateChooserTimeEnabled(true);
		endEl.setMandatory(true);
		endEl.setEnabled(!hasParticipations);
		
		String location = appointment != null? appointment.getLocation(): null;
		locationEl = uifactory.addTextElement("appointment.location", 128, location, formLayout);
		
		String maxParticipations = appointment != null && appointment.getMaxParticipations() != null
				? appointment.getMaxParticipations().toString()
				: null;
		maxParticipationsEl = uifactory.addTextElement("appointment.max.participations", 5, maxParticipations,
				formLayout);
		maxParticipationsEl.setVisible(Type.finding != topic.getType());
		
		String details = appointment == null ? "" : appointment.getDetails();
		detailsEl = uifactory.addTextAreaElement("appointment.details", "appointment.details", 2000, 4, 72, false,
				false, details, formLayout);
		
		if (appointmentsService.isBigBlueButtonEnabled() || appointmentsService.isTeamsEnabled()) {
			meetingSpacer = uifactory.addSpacerElement("meeting.spacer", formLayout, false);
			
			KeyValues meetingKV = new KeyValues();
			meetingKV.add(entry(KEY_NO, translate("appointment.meeting.no")));
			if (appointmentsService.isBigBlueButtonEnabled()) {
				meetingKV.add(entry(KEY_BIGBLUEBUTTON, translate("appointment.meeting.bigbluebutton")));
			}
			if (appointmentsService.isTeamsEnabled()) {
				meetingKV.add(entry(KEY_TEAMS, translate("appointment.meeting.teams")));
			}
			meetingEl = uifactory.addRadiosHorizontal("appointment.meeting", "appointment.meeting", formLayout,
					meetingKV.keys(), meetingKV.values());
			meetingEl.addActionListener(FormEvent.ONCHANGE);
			if (bbbMeeting != null && Arrays.asList(meetingEl.getKeys()).contains(KEY_BIGBLUEBUTTON)) {
				meetingEl.select(KEY_BIGBLUEBUTTON, true);
			} else if (teamsMeeting != null && Arrays.asList(meetingEl.getKeys()).contains(KEY_TEAMS)) {
				meetingEl.select(KEY_TEAMS, true);
			} else {
				meetingEl.select(KEY_NO, bbbMeeting == null && teamsMeeting == null);
			}
		}

		if (appointmentsService.isBigBlueButtonEnabled()) {
			String welcome = bbbMeeting == null ? "" : bbbMeeting.getWelcome();
			welcomeEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.welcome", "meeting.welcome", welcome, 8, 60, formLayout, getWindowControl());
			welcomeEl.setEnabled(bbbEditable);
			
			KeyValues templatesKV = new KeyValues();
			Long selectedTemplateKey = bbbMeeting == null || bbbMeeting.getTemplate() == null ? null : bbbMeeting.getTemplate().getKey();
			templates = appointmentsService.getBigBlueButtonTemplates(() -> topic.getEntry().getKey(), getIdentity(), ureq.getUserSession().getRoles(), selectedTemplateKey);
			templates.forEach(template -> templatesKV.add(KeyValues.entry(template.getKey().toString(), template.getName())));
			templatesKV.sort(KeyValues.VALUE_ASC);
			templateEl = uifactory.addDropdownSingleselect("meeting.template", "meeting.template", formLayout,
					templatesKV.keys(), templatesKV.values());
			templateEl.addActionListener(FormEvent.ONCHANGE);
			templateEl.setEnabled(bbbEditable);
			if (templateEl.getKeys().length > 0) {
				if (selectedTemplateKey != null && asList(templateEl.getKeys()).contains(selectedTemplateKey.toString())) {
					templateEl.select(selectedTemplateKey.toString(), true);
				}
				if (!templateEl.isOneSelected()) {
					templateEl.select(templateEl.getKeys()[0], true);
				}
			}
		
			KeyValues layoutKeyValues = new KeyValues();
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
			if(isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates))) {
				layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.webcam.name(), translate("layout.webcam")));
			}
			
			String[] yesNoValues = new String[] { translate("yes"), translate("no")  };
			recordEl = uifactory.addRadiosVertical("meeting.record", formLayout, KEYS_YES_NO, yesNoValues);
			recordEl.setEnabled(bbbEditable);
			if(BigBlueButtonUIHelper.isRecord(bbbMeeting)) {
				recordEl.select(KEYS_YES_NO[0], true);
			} else {
				recordEl.select(KEYS_YES_NO[1], true);
			}
			
			layoutEl = uifactory.addDropdownSingleselect("meeting.layout", "meeting.layout", formLayout,
					layoutKeyValues.keys(), layoutKeyValues.values());
			layoutEl.setEnabled(bbbEditable);
			boolean layoutSelected = false;
			String selectedLayout = bbbMeeting == null ? BigBlueButtonMeetingLayoutEnum.standard.name() : bbbMeeting.getMeetingLayout().name();
			for(String layoutKey:layoutKeyValues.keys()) {
				if(layoutKey.equals(selectedLayout)) {
					layoutEl.select(layoutKey, true);
					layoutSelected = true;
				}
			}
			if(!layoutSelected) {
				layoutEl.select(BigBlueButtonMeetingLayoutEnum.standard.name(), true);
			}
			layoutEl.setVisible(layoutEl.getKeys().length > 1);
			
			String externalLink = bbbMeeting == null ? CodeHelper.getForeverUniqueID() + "" : bbbMeeting.getReadableIdentifier();
			bbbExternalLinkEl = uifactory.addTextElement("meeting.external.users", 64, externalLink, formLayout);
			bbbExternalLinkEl.setPlaceholderKey("meeting.external.users.empty", null);
			bbbExternalLinkEl.setHelpTextKey("meeting.external.users.help", null);
			bbbExternalLinkEl.addActionListener(FormEvent.ONCHANGE);
			if (externalLink != null) {
				bbbExternalLinkEl.setExampleKey("noTransOnlyParam", new String[] {BigBlueButtonDispatcher.getMeetingUrl(externalLink)});
			}
			
			bbbOpenCalLink = uifactory.addFormLink("calendar.open", formLayout);
			bbbOpenCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, bbbExternalLinkEl, null, null, null, recordEl, templates);
			
			String leadtime = bbbMeeting == null ? null : Long.toString(bbbMeeting.getLeadTime());
			bbbLeadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
			bbbLeadTimeEl.setEnabled(bbbEditable);
			bbbLeadTimeEl.setExampleKey("meeting.leadTime.explain", null);
			
			String followup = bbbMeeting == null ? null : Long.toString(bbbMeeting.getFollowupTime());
			bbbFollowupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
			bbbFollowupTimeEl.setEnabled(bbbEditable);
		}
		
		if (appointmentsService.isTeamsEnabled()) {
			
			Identity creator = teamsMeeting == null ? getIdentity() : teamsMeeting.getCreator();
			if(creator != null) {
				String creatorFullName = userManager.getUserDisplayName(creator);
				teamsCreatorEl = uifactory.addStaticTextElement("meeting.creator.teams", "meeting.creator", creatorFullName, formLayout);
			}
			
			String externalLink = teamsMeeting == null ? CodeHelper.getForeverUniqueID() + "" : teamsMeeting.getReadableIdentifier();
			teamsExternalLinkEl = uifactory.addTextElement("meeting.external.users.teams", "meeting.external.users", 64, externalLink, formLayout);
			teamsExternalLinkEl.setPlaceholderKey("meeting.external.users.empty", null);
			teamsExternalLinkEl.setHelpTextKey("meeting.external.users.help", null);
			teamsExternalLinkEl.addActionListener(FormEvent.ONCHANGE);
			if (externalLink != null) {
				teamsExternalLinkEl.setExampleKey("noTransOnlyParam", new String[] {TeamsDispatcher.getMeetingUrl(externalLink)});
			}
			
			teamsOpenCalLink = uifactory.addFormLink("calendar.open.teams", "calendar.open", null, formLayout, Link.LINK);
			teamsOpenCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
			
			String leadtime = teamsMeeting == null ? null : Long.toString(teamsMeeting.getLeadTime());
			teamsLeadTimeEl = uifactory.addTextElement("meeting.leadTime.teams", "meeting.leadTime", 8, leadtime, formLayout);
			teamsLeadTimeEl.setEnabled(teamsEditable);
			teamsLeadTimeEl.setExampleKey("meeting.leadTime.explain", null);
			
			String followup = teamsMeeting == null ? null : Long.toString(teamsMeeting.getFollowupTime());
			teamsFollowupTimeEl = uifactory.addTextElement("meeting.followupTime.teams", "meeting.followupTime", 8, followup, formLayout);
			teamsFollowupTimeEl.setEnabled(teamsEditable);

			KeyValues presentersKeyValues = new KeyValues();
			presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name(), translate("meeting.presenters.role")));
			presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.ORGANIZATION.name(), translate("meeting.presenters.organization")));
			presentersKeyValues.add(KeyValues.entry(OnlineMeetingPresenters.EVERYONE.name(), translate("meeting.presenters.everyone")));
			presentersEl = uifactory.addDropdownSingleselect("meeting.presenters", formLayout, presentersKeyValues.keys(), presentersKeyValues.values());
			presentersEl.setMandatory(true);
			presentersEl.setEnabled(teamsEditable);

			TeamsUIHelper.setDefaults(presentersEl, teamsMeeting);
		}
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		String changeI18n = appointment == null? "add.appointment.button": "edit.appointment.button";
		uifactory.addFormSubmitButton(changeI18n, buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	private void updateUI() {
		if (templateEl != null) {
			boolean bbbMeeting = meetingEl.isOneSelected() && meetingEl.getSelectedKey().equals(KEY_BIGBLUEBUTTON);
			meetingSpacer.setVisible(bbbMeeting);
			templateEl.setVisible(bbbMeeting);
			bbbExternalLinkEl.setVisible(bbbMeeting);
			bbbOpenCalLink.setVisible(bbbMeeting);
			bbbLeadTimeEl.setVisible(bbbMeeting);
			bbbFollowupTimeEl.setVisible(bbbMeeting);
			welcomeEl.setVisible(bbbMeeting);
			templateEl.setVisible(bbbMeeting);
			recordEl.setVisible(bbbMeeting && BigBlueButtonUIHelper.isRecord(getSelectedTemplate(templateEl, templates)));
			layoutEl.setVisible(bbbMeeting);
		}
		
		if (presentersEl != null) {
			boolean teamsMeeting = meetingEl.isOneSelected() && meetingEl.getSelectedKey().equals(KEY_TEAMS);
			teamsCreatorEl.setVisible(teamsMeeting);
			teamsExternalLinkEl.setVisible(teamsMeeting);
			teamsOpenCalLink.setVisible(teamsMeeting);
			teamsLeadTimeEl.setVisible(teamsMeeting);
			teamsFollowupTimeEl.setVisible(teamsMeeting);
			presentersEl.setVisible(teamsMeeting);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (bbbCalendarCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if (teamsCalendarCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(teamsCalendarCtr);
		removeAsListenerAndDispose(bbbCalendarCtr);
		removeAsListenerAndDispose(cmc);
		teamsCalendarCtr = null;
		bbbCalendarCtr = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == startEl) {
			if (startEl.getDate() != null && endEl.getDate() == null) {
				endEl.setDate(startEl.getDate());
			}
		} else if (source == meetingEl) {
			updateUI();
		} else if (templateEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, bbbExternalLinkEl, null, null, null, recordEl, templates);
			boolean webcamAvailable = isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates));
			BigBlueButtonUIHelper.updateLayoutSelection(layoutEl, getTranslator(), webcamAvailable);
		} else if (bbbOpenCalLink == source) {
			doOpenBBBCalendar(ureq);
		} else if (bbbExternalLinkEl == source) {
			BigBlueButtonUIHelper.validateReadableIdentifier(bbbExternalLinkEl, bbbMeeting);
		} else if (teamsOpenCalLink == source) {
			doOpenTeamsCalendar(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		startEl.clearError();
		endEl.clearError();
		if (startEl.getDate() == null) {
			startEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		if (endEl.getDate() == null) {
			endEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (startEl.getDate() != null && endEl.getDate() != null) {
			Date start = startEl.getDate();
			Date end = endEl.getDate();
			if(end.before(start)) {
				endEl.setErrorKey("error.start.after.end", null);
				allOk &= false;
			}
		}
		
		maxParticipationsEl.clearError();
		String maxParticipationsValue = maxParticipationsEl.getValue();
		Integer maxParticipants = null;
		Long particiationCount = null;
		if (StringHelper.containsNonWhitespace(maxParticipationsValue)) {
			try {
				maxParticipants = Integer.valueOf(maxParticipationsValue);
				if (maxParticipants.intValue() < 1) {
					maxParticipationsEl.setErrorKey("error.positiv.number", null);
					allOk &= false;
				} else if (appointment != null) {
					particiationCount = getParticipationCount();
					if (particiationCount.doubleValue() > maxParticipants.intValue()) {
						maxParticipationsEl.setErrorKey("error.too.much.participations",
								new String[] { particiationCount.toString() });
						allOk &= false;
					}
				}
			} catch (NumberFormatException e) {
				maxParticipationsEl.setErrorKey("error.positiv.number", null);
				allOk &= false;
			}
		}
		
		if (bbbEditable && templateEl != null && templateEl.isVisible()) {
			allOk &= BigBlueButtonUIHelper.validateReadableIdentifier(bbbExternalLinkEl, bbbMeeting);
			
			allOk &= BigBlueButtonUIHelper.validateTime(bbbLeadTimeEl, 15l);
			allOk &= BigBlueButtonUIHelper.validateTime(bbbFollowupTimeEl, 15l);
			
			templateEl.clearError();
			if(!templateEl.isOneSelected()) {
				templateEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			// dates ok
			if(allOk) {
				BigBlueButtonMeetingTemplate template = BigBlueButtonUIHelper.getSelectedTemplate(templateEl, templates);
				
				if (!maxParticipationsEl.hasError()){
					if (maxParticipants == null) {
						particiationCount = particiationCount != null? particiationCount: getParticipationCount();
						if (particiationCount.intValue() > template.getMaxParticipants().intValue()) {
							templateEl.setErrorKey("error.participations.count.greater.room", new String[] {particiationCount.toString()});
							allOk &= false;
						} else {
							maxParticipationsEl.setValue(template.getMaxParticipants().toString());
						}
					} else if (maxParticipants.intValue() > template.getMaxParticipants().intValue()) {
						maxParticipationsEl.setErrorKey("error.participations.max.greater.room", new String[] {template.getMaxParticipants().toString()});
						allOk &= false;
					}
				}
				
				allOk &= BigBlueButtonUIHelper.validateDuration(startEl, bbbLeadTimeEl, endEl, bbbFollowupTimeEl, template);
				allOk &= BigBlueButtonUIHelper.validateSlot(startEl, bbbLeadTimeEl, endEl, bbbFollowupTimeEl, bbbMeeting, template);
			}
		}
		
		if (teamsEditable && meetingEl!= null && meetingEl.isOneSelected() && meetingEl.getSelectedKey().equals(KEY_TEAMS)) {
			allOk &= TeamsUIHelper.validateReadableIdentifier(teamsExternalLinkEl, teamsMeeting);
		}
		
		return allOk;
	}

	private Long getParticipationCount() {
		if (appointment == null) return Long.valueOf(0);
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointments(Collections.singletonList(appointment));
		return appointmentsService.getParticipationCount(params);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (appointment == null) {
			appointment = appointmentsService.createUnsavedAppointment(topic);
		}
		
		Date start = startEl.getDate();
		appointment.setStart(start);
		
		Date end = endEl.getDate();
		appointment.setEnd(end);
		
		String location = locationEl.getValue();
		appointment.setLocation(location);
		
		String details = detailsEl.getValue();
		appointment.setDetails(details);
		
		String maxParticipationsValue = maxParticipationsEl.getValue();
		Integer maxParticipations = StringHelper.containsNonWhitespace(maxParticipationsValue)
				? Integer.valueOf(maxParticipationsValue)
				: null;
		appointment.setMaxParticipations(maxParticipations);
		
		if (meetingEl != null && meetingEl.isOneSelected() && meetingEl.getSelectedKey().equals(KEY_BIGBLUEBUTTON)) {
			if (bbbMeeting == null) {
				appointment = appointmentsService.addBBBMeeting(appointment, getIdentity());
				bbbMeeting = appointment.getBBBMeeting();
				
				String mainPresenters = appointmentsService.getFormattedOrganizers(topic);
				bbbMeeting.setMainPresenter(mainPresenters);
			}
			
			bbbMeeting.setName(topic.getTitle());
			bbbMeeting.setDescription(topic.getDescription());
			bbbMeeting.setWelcome(welcomeEl.getValue());
			BigBlueButtonMeetingTemplate template = getSelectedTemplate(templateEl, templates);
			bbbMeeting.setTemplate(template);
			
			if(template != null && template.isExternalUsersAllowed()
					&& bbbExternalLinkEl.isVisible() && StringHelper.containsNonWhitespace(bbbExternalLinkEl.getValue())) {
				bbbMeeting.setReadableIdentifier(bbbExternalLinkEl.getValue());
			} else {
				bbbMeeting.setReadableIdentifier(null);
			}
			
			bbbMeeting.setPermanent(false);
		
			Date startDate = startEl.getDate();
			bbbMeeting.setStartDate(startDate);
			Date endDate = endEl.getDate();
			bbbMeeting.setEndDate(endDate);
			long leadTime = BigBlueButtonUIHelper.getLongOrZero(bbbLeadTimeEl);
			bbbMeeting.setLeadTime(leadTime);
			long followupTime = BigBlueButtonUIHelper.getLongOrZero(bbbFollowupTimeEl);
			bbbMeeting.setFollowupTime(followupTime);
			
			if(layoutEl.isVisible() && layoutEl.isOneSelected()) {
				BigBlueButtonMeetingLayoutEnum layout = BigBlueButtonMeetingLayoutEnum.secureValueOf(layoutEl.getSelectedKey());
				bbbMeeting.setMeetingLayout(layout);
			} else {
				bbbMeeting.setMeetingLayout(BigBlueButtonMeetingLayoutEnum.standard);
			}
			
			if(recordEl.isVisible() && recordEl.isOneSelected()) {
				bbbMeeting.setRecord(Boolean.valueOf(KEYS_YES_NO[0].equals(recordEl.getSelectedKey())));
			} else {
				bbbMeeting.setRecord(null);
			}
			
			appointment = appointmentsService.removeTeamsMeeting(appointment);
			
		} else if (meetingEl != null && meetingEl.isOneSelected() && meetingEl.getSelectedKey().equals(KEY_TEAMS)) {
			if (teamsMeeting == null) {
				appointment = appointmentsService.addTeamsMeeting(appointment, getIdentity());
				teamsMeeting = appointment.getTeamsMeeting();
			}
			
			teamsMeeting.setSubject(topic.getTitle());
			teamsMeeting.setDescription(topic.getDescription());
			
			Date startDate = startEl.getDate();
			teamsMeeting.setStartDate(startDate);
			Date endDate = endEl.getDate();
			teamsMeeting.setEndDate(endDate);
			long leadTime = TeamsUIHelper.getLongOrZero(teamsLeadTimeEl);
			teamsMeeting.setLeadTime(leadTime);
			long followupTime = TeamsUIHelper.getLongOrZero(teamsFollowupTimeEl);
			teamsMeeting.setFollowupTime(followupTime);
			
			if (teamsExternalLinkEl.isVisible() && StringHelper.containsNonWhitespace(teamsExternalLinkEl.getValue())) {
				teamsMeeting.setReadableIdentifier(teamsExternalLinkEl.getValue());
			} else {
				teamsMeeting.setReadableIdentifier(null);
			}
			
			teamsMeeting.setPermanent(false);
			teamsMeeting.setAllowedPresenters(presentersEl.getSelectedKey());
			
			appointment = appointmentsService.removeBBBMeeting(appointment);
		} else {
			appointment = appointmentsService.removeBBBMeeting(appointment);
			appointment = appointmentsService.removeTeamsMeeting(appointment);
		}
		
		appointment = appointmentsService.saveAppointment(appointment);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doOpenBBBCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(bbbCalendarCtr);
		removeAsListenerAndDispose(cmc);

		bbbCalendarCtr = new BigBlueButtonMeetingsCalendarController(ureq, getWindowControl());
		listenTo(bbbCalendarCtr);
		cmc = new CloseableModalController(getWindowControl(), "close", bbbCalendarCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenTeamsCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(teamsCalendarCtr);
		removeAsListenerAndDispose(cmc);

		teamsCalendarCtr = new TeamsMeetingsCalendarController(ureq, getWindowControl());
		listenTo(teamsCalendarCtr);
		cmc = new CloseableModalController(getWindowControl(), "close", teamsCalendarCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}

	@Override
	protected void doDispose() {
		//
	}

}
