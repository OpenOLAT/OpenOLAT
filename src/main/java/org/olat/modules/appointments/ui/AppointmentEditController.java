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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.Topic.Type;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingsCalendarController;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentEditController extends FormBasicController {
	
	private static final String KEY_ON = "on";
	private static final String[] KEYS_ON = new String[] { KEY_ON };
	private static final String[] KEYS_YES_NO = new String[] { "yes", "no" };
	
	private DateChooser startEl;
	private DateChooser endEl;
	private TextElement locationEl;
	private TextElement maxParticipationsEl;
	private TextElement detailsEl;
	private SpacerElement bbbSpacer;
	private MultipleSelectionElement bbbRoomEl;
	private TextElement externalLinkEl;
	private FormLink openCalLink;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private TextElement welcomeEl;
	private SingleSelection templateEl;
	private SingleSelection recordEl;
	private SingleSelection layoutEl;
	
	private BigBlueButtonMeetingsCalendarController calCtr;
	private CloseableModalController cmc;
	
	private Topic topic;
	private Appointment appointment;
	private final boolean hasParticipations;
	private final boolean bbbEditable;
	private BigBlueButtonMeeting meeting;
	private List<BigBlueButtonMeetingTemplate> templates;
	
	@Autowired
	private AppointmentsService appointmentsService;

	public AppointmentEditController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.topic = topic;
		this.hasParticipations = false;
		this.bbbEditable = true;
		this.meeting = null;
		initForm(ureq);
		updateUI();
	}

	public AppointmentEditController(UserRequest ureq, WindowControl wControl, Appointment appointment) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		
		this.topic = appointment.getTopic();
		if (appointment.getMeeting() == null) {
			this.appointment = appointment;
		} else {
			AppointmentSearchParams params = new AppointmentSearchParams();
			params.setAppointment(appointment);
			params.setFetchTopic(true);
			params.setFetchMeetings(true);
			this.appointment = appointmentsService.getAppointments(params).get(0);
			this.meeting = this.appointment.getMeeting();
		}
		
		this.bbbEditable = meeting == null || meeting.getServer() == null;
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(appointment);
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

		if (appointmentsService.isBigBlueButtonEnabled()) {
			bbbSpacer = uifactory.addSpacerElement("bbb.spacer", formLayout, false);
			
			String[] onValues = TranslatorHelper.translateAll(getTranslator(), KEYS_ON);
			bbbRoomEl = uifactory.addCheckboxesHorizontal("appointment.bbb.room", "appointment.bbb.room", formLayout, KEYS_ON, onValues);
			bbbRoomEl.addActionListener(FormEvent.ONCHANGE);
			bbbRoomEl.select(KEY_ON, meeting != null);
			
			String welcome = meeting == null ? "" : meeting.getWelcome();
			welcomeEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.welcome", "meeting.welcome", welcome, 8, 60, formLayout, getWindowControl());
			welcomeEl.setEnabled(bbbEditable);
			
			KeyValues templatesKV = new KeyValues();
			Long selectedTemplateKey = meeting == null || meeting.getTemplate() == null ? null : meeting.getTemplate().getKey();
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
			if(BigBlueButtonUIHelper.isRecord(meeting)) {
				recordEl.select(KEYS_YES_NO[0], true);
			} else {
				recordEl.select(KEYS_YES_NO[1], true);
			}
			
			layoutEl = uifactory.addDropdownSingleselect("meeting.layout", "meeting.layout", formLayout,
					layoutKeyValues.keys(), layoutKeyValues.values());
			layoutEl.setEnabled(bbbEditable);
			boolean layoutSelected = false;
			String selectedLayout = meeting == null ? BigBlueButtonMeetingLayoutEnum.standard.name() : meeting.getMeetingLayout().name();
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
			
			String externalLink = meeting == null ? CodeHelper.getForeverUniqueID() + "" : meeting.getReadableIdentifier();
			externalLinkEl = uifactory.addTextElement("meeting.external.users", 64, externalLink, formLayout);
			externalLinkEl.setPlaceholderKey("meeting.external.users.empty", null);
			externalLinkEl.setHelpTextKey("meeting.external.users.help", null);
			externalLinkEl.addActionListener(FormEvent.ONCHANGE);
			if (externalLink != null) {
				externalLinkEl.setExampleKey("noTransOnlyParam", new String[] {BigBlueButtonDispatcher.getMeetingUrl(externalLink)});			
			}
			
			openCalLink = uifactory.addFormLink("calendar.open", formLayout);
			openCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, recordEl, templates);
			
			String leadtime = meeting == null ? null : Long.toString(meeting.getLeadTime());
			leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
			leadTimeEl.setEnabled(bbbEditable);
			leadTimeEl.setExampleKey("meeting.leadTime.explain", null);
			
			String followup = meeting == null ? null : Long.toString(meeting.getFollowupTime());
			followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
			followupTimeEl.setEnabled(bbbEditable);
		}
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		String changeI18n = appointment == null? "add.appointment.button": "edit.appointment.button";
		uifactory.addFormSubmitButton(changeI18n, buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	private void updateUI() {
		if (bbbRoomEl != null) {
			boolean bbbRoom = bbbRoomEl.isAtLeastSelected(1);
			bbbSpacer.setVisible(bbbRoom);
			templateEl.setVisible(bbbRoom);
			externalLinkEl.setVisible(bbbRoom);
			openCalLink.setVisible(bbbRoom);
			leadTimeEl.setVisible(bbbRoom);
			followupTimeEl.setVisible(bbbRoom);
			welcomeEl.setVisible(bbbRoom);
			templateEl.setVisible(bbbRoom);
			layoutEl.setVisible(bbbRoom);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (calCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calCtr);
		removeAsListenerAndDispose(cmc);
		calCtr = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == startEl) {
			if (startEl.getDate() != null && endEl.getDate() == null) {
				endEl.setDate(startEl.getDate());
			}
		} else if (source == bbbRoomEl) {
			updateUI();
		} else if (templateEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, recordEl, templates);
			boolean webcamAvailable = isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates));
			BigBlueButtonUIHelper.updateLayoutSelection(layoutEl, getTranslator(), webcamAvailable);
		} else if (openCalLink == source) {
			doOpenCalendar(ureq);
		} else if (externalLinkEl == source) {
			BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, meeting);
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
			allOk &= BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, meeting);
			
			allOk &= BigBlueButtonUIHelper.validateTime(leadTimeEl, 15l);
			allOk &= BigBlueButtonUIHelper.validateTime(followupTimeEl, 15l);
			
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
				
				allOk &= BigBlueButtonUIHelper.validateDuration(startEl, leadTimeEl, endEl, followupTimeEl, template);
				allOk &= BigBlueButtonUIHelper.validateSlot(startEl, leadTimeEl, endEl, followupTimeEl, meeting, template);
			}
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
		
		if (bbbRoomEl != null && bbbRoomEl.isAtLeastSelected(1)) {
			if (meeting == null) {
				appointment = appointmentsService.addMeeting(appointment, getIdentity());
				meeting = appointment.getMeeting();
				
				String mainPresenters = appointmentsService.getMainPresenters(topic);
				meeting.setMainPresenter(mainPresenters);
			}
			
			meeting.setName(topic.getTitle());
			meeting.setDescription(topic.getDescription());
			meeting.setWelcome(welcomeEl.getValue());
			BigBlueButtonMeetingTemplate template = getSelectedTemplate(templateEl, templates);
			meeting.setTemplate(template);
			
			if(template != null && template.isExternalUsersAllowed()
					&& externalLinkEl.isVisible() && StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
				meeting.setReadableIdentifier(externalLinkEl.getValue());
			} else {
				meeting.setReadableIdentifier(null);
			}
			
			meeting.setPermanent(false);
		
			Date startDate = startEl.getDate();
			meeting.setStartDate(startDate);
			Date endDate = endEl.getDate();
			meeting.setEndDate(endDate);
			long leadTime = BigBlueButtonUIHelper.getLongOrZero(leadTimeEl);
			meeting.setLeadTime(leadTime);
			long followupTime = BigBlueButtonUIHelper.getLongOrZero(followupTimeEl);
			meeting.setFollowupTime(followupTime);
			
			if(layoutEl.isVisible() && layoutEl.isOneSelected()) {
				BigBlueButtonMeetingLayoutEnum layout = BigBlueButtonMeetingLayoutEnum.secureValueOf(layoutEl.getSelectedKey());
				meeting.setMeetingLayout(layout);
			} else {
				meeting.setMeetingLayout(BigBlueButtonMeetingLayoutEnum.standard);
			}
			
			if(recordEl.isVisible() && recordEl.isOneSelected()) {
				meeting.setRecord(Boolean.valueOf(KEYS_YES_NO[0].equals(recordEl.getSelectedKey())));
			} else {
				meeting.setRecord(null);
			}
		} else {
			appointment = appointmentsService.removeMeeting(appointment);
		}
		
		appointment = appointmentsService.saveAppointment(appointment);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calCtr);
		removeAsListenerAndDispose(cmc);

		calCtr = new BigBlueButtonMeetingsCalendarController(ureq, getWindowControl());
		listenTo(calCtr);
		cmc = new CloseableModalController(getWindowControl(), "close", calCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}

	@Override
	protected void doDispose() {
		//
	}

}
