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

import static org.olat.core.gui.components.util.KeyValues.entry;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.getSelectedTemplate;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.isWebcamLayoutAvailable;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Topic;
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
 * Initial date: 26 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RecurringAppointmentsController extends FormBasicController {
	
	private static final String KEY_ON = "on";
	private static final String[] KEYS_ON = new String[] { KEY_ON };
	private static final String[] KEYS_YES_NO = new String[] { "yes", "no" };
	
	private TextElement locationEl;
	private TextElement maxParticipationsEl;
	private DateChooser recurringFirstEl;
	private MultipleSelectionElement recurringDaysOfWeekEl;
	private DateChooser recurringLastEl;
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

	private final Topic topic;
	private List<BigBlueButtonMeetingTemplate> templates;
	
	@Autowired
	private AppointmentsService appointmentsService;

	public RecurringAppointmentsController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.topic = topic;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		locationEl = uifactory.addTextElement("appointment.location", 128, null, formLayout);
		locationEl.setHelpTextKey("appointment.init.value", null);
		
		maxParticipationsEl = uifactory.addTextElement("appointment.max.participations", 5, null, formLayout);
		maxParticipationsEl.setHelpTextKey("appointment.init.value", null);
		
		recurringFirstEl = uifactory.addDateChooser("appointments.recurring.first", null, formLayout);
		recurringFirstEl.setDateChooserTimeEnabled(true);
		recurringFirstEl.setSecondDate(true);
		recurringFirstEl.setSameDay(true);
		recurringFirstEl.setMandatory(true);
		
		DayOfWeek[] dayOfWeeks = DayOfWeek.values();
		KeyValues dayOfWeekKV = new KeyValues();
		for (int i = 0; i < dayOfWeeks.length; i++) {
			dayOfWeekKV.add(entry(dayOfWeeks[i].name(), dayOfWeeks[i].getDisplayName(TextStyle.FULL_STANDALONE, getLocale())));
		}
		recurringDaysOfWeekEl = uifactory.addCheckboxesHorizontal("appointments.recurring.days.of.week", formLayout,
				dayOfWeekKV.keys(), dayOfWeekKV.values());
		recurringDaysOfWeekEl.setMandatory(true);
		
		recurringLastEl = uifactory.addDateChooser("appointments.recurring.last", null, formLayout);
		recurringLastEl.setMandatory(true);
		
		if (appointmentsService.isBigBlueButtonEnabled()) {
			bbbSpacer = uifactory.addSpacerElement("bbb.spacer", formLayout, false);
			
			String[] onValues = TranslatorHelper.translateAll(getTranslator(), KEYS_ON);
			bbbRoomEl = uifactory.addCheckboxesHorizontal("appointment.bbb.room", "appointment.bbb.room", formLayout, KEYS_ON, onValues);
			bbbRoomEl.addActionListener(FormEvent.ONCHANGE);
			
			welcomeEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.welcome", "meeting.welcome", "", 8, 60, formLayout, getWindowControl());
			
			KeyValues templatesKV = new KeyValues();
			templates = appointmentsService.getBigBlueButtonTemplates(() -> topic.getEntry().getKey(), getIdentity(), ureq.getUserSession().getRoles(), null);
			templates.forEach(template -> templatesKV.add(KeyValues.entry(template.getKey().toString(), template.getName())));
			templatesKV.sort(KeyValues.VALUE_ASC);
			templateEl = uifactory.addDropdownSingleselect("meeting.template", "meeting.template", formLayout,
					templatesKV.keys(), templatesKV.values());
			templateEl.addActionListener(FormEvent.ONCHANGE);
			templateEl.select(templateEl.getKeys()[0], true);
			
			String[] yesNoValues = new String[] { translate("yes"), translate("no")  };
			recordEl = uifactory.addRadiosVertical("meeting.record", formLayout, KEYS_YES_NO, yesNoValues);
			recordEl.select(KEYS_YES_NO[0], true);
		
			KeyValues layoutKeyValues = new KeyValues();
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
			if(isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates))) {
				layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.webcam.name(), translate("layout.webcam")));
			}
			layoutEl = uifactory.addDropdownSingleselect("meeting.layout", "meeting.layout", formLayout,
					layoutKeyValues.keys(), layoutKeyValues.values());
			boolean layoutSelected = false;
			if(!layoutSelected) {
				layoutEl.select(BigBlueButtonMeetingLayoutEnum.standard.name(), true);
			}
			layoutEl.setVisible(layoutEl.getKeys().length > 1);
			
			String externalLink = CodeHelper.getForeverUniqueID() + "";
			externalLinkEl = uifactory.addTextElement("meeting.external.users", 64, externalLink, formLayout);
			externalLinkEl.setPlaceholderKey("meeting.external.users.empty", null);
			externalLinkEl.setHelpTextKey("meeting.external.users.help", null);
			externalLinkEl.addActionListener(FormEvent.ONCHANGE);
			externalLinkEl.setExampleKey("noTransOnlyParam", new String[] {BigBlueButtonDispatcher.getMeetingUrl(externalLink)});
			
			openCalLink = uifactory.addFormLink("calendar.open", formLayout);
			openCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, null, recordEl, templates);
			
			leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, null, formLayout);
			leadTimeEl.setExampleKey("meeting.leadTime.explain", null);
			
			followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, null, formLayout);
		}
		
		// Buttons
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
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
			recordEl.setVisible(bbbRoom && BigBlueButtonUIHelper.isRecord(getSelectedTemplate(templateEl, templates)));
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
		if (source == bbbRoomEl) {
			updateUI();
		} else if (templateEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, null, recordEl, templates);
			boolean webcamAvailable = isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates));
			BigBlueButtonUIHelper.updateLayoutSelection(layoutEl, getTranslator(), webcamAvailable);
		} else if (openCalLink == source) {
			doOpenCalendar(ureq);
		} else if (externalLinkEl == source) {
			BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, null);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		maxParticipationsEl.clearError();
		String maxParticipationsValue = maxParticipationsEl.getValue();
		Integer maxParticipants = null;
		if (maxParticipationsEl.isVisible() && StringHelper.containsNonWhitespace(maxParticipationsValue)) {
			try {
				maxParticipants = Integer.parseInt(maxParticipationsValue);
				if (maxParticipants.intValue() < 1) {
					maxParticipationsEl.setErrorKey("error.positiv.number", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				maxParticipationsEl.setErrorKey("error.positiv.number", null);
				allOk &= false;
			}
		}
		
		recurringFirstEl.clearError();
		recurringDaysOfWeekEl.clearError();
		recurringLastEl.clearError();
		if (recurringFirstEl.getDate() == null || recurringFirstEl.getSecondDate() == null) {
			recurringFirstEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (recurringFirstEl.getDate().after(recurringFirstEl.getSecondDate())) {
			recurringFirstEl.setErrorKey("error.start.after.end", null);
			allOk &= false;
		}
		
		if (!recurringDaysOfWeekEl.isAtLeastSelected(1)) {
			recurringDaysOfWeekEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (recurringLastEl.getDate() == null) {
			recurringLastEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (recurringFirstEl.getDate() != null && recurringLastEl.getDate() != null
				&& recurringFirstEl.getDate().after(recurringLastEl.getDate())) {
			recurringLastEl.setErrorKey("error.first.after.start", null);
			allOk &= false;
		}
		
		if (templateEl != null && templateEl.isVisible()) {
			allOk &= BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, null);
			
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
						maxParticipationsEl.setValue(template.getMaxParticipants().toString());
					} else if (maxParticipants.intValue() > template.getMaxParticipants().intValue()) {
						maxParticipationsEl.setErrorKey("error.participations.max.greater.room", new String[] {template.getMaxParticipants().toString()});
						allOk &= false;
					}
				}
				
				allOk &= BigBlueButtonUIHelper.validateDuration(recurringFirstEl, leadTimeEl, followupTimeEl, template);
				if (!recurringFirstEl.hasError() && !validateRecurringSlot(template)) {
						recurringFirstEl.setErrorKey("server.overloaded", new String[] { null });
						allOk &= false;
					}
			}
		}
		
		return allOk;
	}

	private boolean validateRecurringSlot(BigBlueButtonMeetingTemplate template) {
		long leadTime = BigBlueButtonUIHelper.getLongOrZero(leadTimeEl);
		long followupTime = BigBlueButtonUIHelper.getLongOrZero(followupTimeEl);
		
		Date firstStart = recurringFirstEl.getDate();
		Date firstEnd = recurringFirstEl.getSecondDate();
		
		Date last = recurringLastEl.getDate();
		last = DateUtils.setTime(last, 23, 59, 59);
		
		Collection<DayOfWeek> daysOfWeek = recurringDaysOfWeekEl.getSelectedKeys().stream()
				.map(DayOfWeek::valueOf)
				.collect(Collectors.toList());
		
		List<Date> starts = DateUtils.getDaysInRange(firstStart, last, daysOfWeek);
		for (Date start : starts) {
			Date end = DateUtils.copyTime(start, firstEnd);
			boolean valid = BigBlueButtonUIHelper.validateSlot(null, template, start, end, leadTime, followupTime);
			if (!valid) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSaveReccuringAppointments();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void doSaveReccuringAppointments() {
		Date firstStart = recurringFirstEl.getDate();
		Date firstEnd = recurringFirstEl.getSecondDate();
		
		Date last = recurringLastEl.getDate();
		last = DateUtils.setTime(last, 23, 59, 59);
		
		Collection<DayOfWeek> daysOfWeek = recurringDaysOfWeekEl.getSelectedKeys().stream()
				.map(DayOfWeek::valueOf)
				.collect(Collectors.toList());
		
		List<Date> starts = DateUtils.getDaysInRange(firstStart, last, daysOfWeek);
		for (Date start : starts) {
			Appointment appointment = appointmentsService.createUnsavedAppointment(topic);
			
			appointment.setStart(start);
			
			Date end = DateUtils.copyTime(start, firstEnd);
			appointment.setEnd(end);
			
			String location = locationEl.getValue();
			appointment.setLocation(location);
			
			if (maxParticipationsEl.isVisible()) {
				String maxParticipationsValue = maxParticipationsEl.getValue();
				Integer maxParticipations = StringHelper.containsNonWhitespace(maxParticipationsValue)
						? Integer.valueOf(maxParticipationsValue)
						: null;
				appointment.setMaxParticipations(maxParticipations);
			}
			
			if (bbbRoomEl != null && bbbRoomEl.isAtLeastSelected(1)) {
				appointment = appointmentsService.addMeeting(appointment, getIdentity());
				BigBlueButtonMeeting meeting = appointment.getMeeting();
				
				String mainPresenters = appointmentsService.getMainPresenters(topic);
				meeting.setMainPresenter(mainPresenters);
				
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
			
				meeting.setStartDate(appointment.getStart());
				meeting.setEndDate(appointment.getEnd());
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
			} 
			
			appointmentsService.saveAppointment(appointment);
		}
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
