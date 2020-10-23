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

import static org.olat.core.gui.components.util.KeyValues.VALUE_ASC;
import static org.olat.core.gui.components.util.KeyValues.entry;
import static org.olat.core.util.ArrayHelper.emptyStrings;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.getSelectedTemplate;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.isWebcamLayoutAvailable;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.Topic.Type;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingsCalendarController;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicCreateController extends FormBasicController {
	
	private static final String KEY_ON = "on";
	private static final String[] KEYS_ON = new String[] { KEY_ON };
	private static final String KEY_MULTI_PARTICIPATION = "multi.participation";
	private static final String KEY_COACH_CONFIRMATION = "coach.confirmation";
	private static final String CMD_REMOVE = "remove";
	private static final String CMD_ADD = "add";
	
	private TextElement titleEl;
	private TextElement descriptionEl;
	private SingleSelection typeEl;
	private MultipleSelectionElement configurationEl;
	private MultipleSelectionElement organizerEl;
	private TextElement locationEl;
	private TextElement maxParticipationsEl;
	private MultipleSelectionElement recurringEl;
	private FormLayoutContainer singleCont;
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
	private SingleSelection layoutEl;
	
	private BigBlueButtonMeetingsCalendarController calCtr;
	private CloseableModalController cmc;
	
	private RepositoryEntry entry;
	private String subIdent;
	private Topic topic;
	private List<Identity> coaches;
	private List<BigBlueButtonMeetingTemplate> templates;
	private List<AppointmentWrapper> appointmentWrappers;
	private boolean multiParticipationsSelected = true;
	private boolean coachConfirmationSelected = true;
	private int counter = 0;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;

	public TopicCreateController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			String subIdent) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.subIdent = subIdent;
		
		coaches = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.coach.name());
		
		initForm(ureq);
		updateUI();
	}
	
	public Topic getTopic() {
		return topic;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Topic
		titleEl = uifactory.addTextElement("topic.title", "topic.title", 128, null, formLayout);
		titleEl.setMandatory(true);
		
		descriptionEl = uifactory.addTextAreaElement("topic.description", "topic.description", 2000, 3, 72, false,
				false, null, formLayout);
		
		// Configs
		KeyValues typeKV = new KeyValues();
		typeKV.add(entry(Topic.Type.enrollment.name(), translate("topic.type.enrollment")));
		typeKV.add(entry(Topic.Type.finding.name(), translate("topic.type.finding")));
		typeEl = uifactory.addRadiosHorizontal("topic.type", formLayout, typeKV.keys(), typeKV.values());
		typeEl.select(Topic.Type.enrollment.name(), true);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		
		configurationEl = uifactory.addCheckboxesVertical("topic.configuration", formLayout, emptyStrings(),
				emptyStrings(), 1);
		configurationEl.addActionListener(FormEvent.ONCHANGE);
		
		// Organizer
		KeyValues coachesKV = new KeyValues();
		for (Identity coach : coaches) {
			coachesKV.add(entry(coach.getKey().toString(), userManager.getUserDisplayName(coach.getKey())));
		}
		coachesKV.sort(VALUE_ASC);
		organizerEl = uifactory.addCheckboxesDropdown("organizer", "organizer", formLayout, coachesKV.keys(), coachesKV.values());
		organizerEl.setVisible(!coaches.isEmpty());
		
		if (organizerEl.isVisible()) {
			String defaultOrganizerKey = getIdentity().getKey().toString();
			if (organizerEl.getKeys().contains(defaultOrganizerKey)) {
					organizerEl.select(defaultOrganizerKey, true);
			}
		}
		
		// Appointments
		locationEl = uifactory.addTextElement("appointment.location", 128, null, formLayout);
		locationEl.setHelpTextKey("appointment.init.value", null);
		
		maxParticipationsEl = uifactory.addTextElement("appointment.max.participations", 5, null, formLayout);
		maxParticipationsEl.setHelpTextKey("appointment.init.value", null);
		
		recurringEl = uifactory.addCheckboxesHorizontal("appointments.recurring", formLayout,
				new String[] { "xx" }, new String[] { null });
		recurringEl.addActionListener(FormEvent.ONCHANGE);
		
		// Single appointments
		singleCont = FormLayoutContainer.createCustomFormLayout("singleCont", getTranslator(), velocity_root + "/appointments_single.html");
		formLayout.add(singleCont);
		singleCont.setRootForm(mainForm);
		singleCont.setLabel("appointments", null);
		
		appointmentWrappers = new ArrayList<>();
		doCreateAppointmentWrapper(null);
		singleCont.contextPut("appointments", appointmentWrappers);
		
		// Reccuring appointments
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
			templates = appointmentsService.getBigBlueButtonTemplates(entry, getIdentity(), ureq.getUserSession().getRoles(), null);
			templates.forEach(template -> templatesKV.add(KeyValues.entry(template.getKey().toString(), template.getName())));
			templatesKV.sort(KeyValues.VALUE_ASC);
			templateEl = uifactory.addDropdownSingleselect("meeting.template", "meeting.template", formLayout,
					templatesKV.keys(), templatesKV.values());
			templateEl.addActionListener(FormEvent.ONCHANGE);
			templateEl.select(templateEl.getKeys()[0], true);
		
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
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, templates);
			
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
		boolean enrollment = typeEl.isOneSelected() && Type.valueOf(typeEl.getSelectedKey()) != Type.finding;
		
		KeyValues configKV = new KeyValues();
		configKV.add(entry(KEY_MULTI_PARTICIPATION, translate("topic.multi.participation")));
		if (enrollment) {
			configKV.add(entry(KEY_COACH_CONFIRMATION, translate("topic.coach.confirmation")));
		}
		configurationEl.setKeysAndValues(configKV.keys(), configKV.values());
		configurationEl.select(KEY_MULTI_PARTICIPATION, multiParticipationsSelected);
		configurationEl.select(KEY_COACH_CONFIRMATION, coachConfirmationSelected);

		maxParticipationsEl.setVisible(enrollment);
		
		boolean recurring = recurringEl.isAtLeastSelected(1);
		singleCont.setVisible(!recurring);
		recurringFirstEl.setVisible(recurring);
		recurringDaysOfWeekEl.setVisible(recurring);
		recurringLastEl.setVisible(recurring);
		
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
		if (source == typeEl) {
			updateUI();
		} else if (source == configurationEl) {
			Collection<String> configKeys = configurationEl.getSelectedKeys();
			multiParticipationsSelected = configKeys.contains(KEY_MULTI_PARTICIPATION);
			coachConfirmationSelected = configKeys.contains(KEY_COACH_CONFIRMATION);
		} else if (source == recurringEl) {
			updateUI();
		} else if (source == bbbRoomEl) {
			updateUI();
		} else if (templateEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, templates);
			boolean webcamAvailable = isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates));
			BigBlueButtonUIHelper.updateLayoutSelection(layoutEl, getTranslator(), webcamAvailable);
		} else if (openCalLink == source) {
			doOpenCalendar(ureq);
		} else if (externalLinkEl == source) {
			BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, null);
		} else if (source instanceof DateChooser) {
			DateChooser dateChooser = (DateChooser)source;
			AppointmentWrapper wrapper = (AppointmentWrapper)dateChooser.getUserObject();
			doInitEndDate(wrapper);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_ADD.equals(cmd)) {
				AppointmentWrapper wrapper = (AppointmentWrapper)link.getUserObject();
				doCreateAppointmentWrapper(wrapper);
			} else if (CMD_REMOVE.equals(cmd)) {
				AppointmentWrapper wrapper = (AppointmentWrapper)link.getUserObject();
				doRemoveAppointmentWrapper(wrapper);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
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
		
		boolean recurring = recurringEl.isAtLeastSelected(1);
			
		for (AppointmentWrapper wrapper : appointmentWrappers) {
			DateChooser startEl = wrapper.getStartEl();
			DateChooser endEl = wrapper.getEndEl();
			startEl.clearError();
			endEl.clearError();
			if (!recurring) {
				if (startEl.getDate() == null && endEl.getDate() != null) {
					startEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}
				if (endEl.getDate() == null && startEl.getDate() != null) {
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
			}
		}
		
		recurringFirstEl.clearError();
		recurringDaysOfWeekEl.clearError();
		recurringLastEl.clearError();
		if (recurring) {
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
		}
		
		boolean bbbOk = true;
		if (templateEl != null && templateEl.isVisible()) {
			bbbOk &= BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, null);
			
			bbbOk &= BigBlueButtonUIHelper.validateTime(leadTimeEl, 15l);
			bbbOk &= BigBlueButtonUIHelper.validateTime(followupTimeEl, 15l);
			
			templateEl.clearError();
			if(!templateEl.isOneSelected()) {
				templateEl.setErrorKey("form.legende.mandatory", null);
				bbbOk &= false;
			}
			
			// dates ok
			if(bbbOk) {
				BigBlueButtonMeetingTemplate template = BigBlueButtonUIHelper.getSelectedTemplate(templateEl, templates);
				if (!maxParticipationsEl.hasError()){
					if (maxParticipants == null) {
						maxParticipationsEl.setValue(template.getMaxParticipants().toString());
					} else if (maxParticipants.intValue() > template.getMaxParticipants().intValue()) {
						maxParticipationsEl.setErrorKey("error.participations.max.greater.room", new String[] {template.getMaxParticipants().toString()});
						bbbOk &= false;
					}
				}
				
				if (recurring) {
					allOk &= BigBlueButtonUIHelper.validateDuration(recurringFirstEl, leadTimeEl, followupTimeEl, template);
					if (!recurringFirstEl.hasError() && !validateRecurringSlot(template)) {
						recurringFirstEl.setErrorKey("server.overloaded", new String[] { null });
						bbbOk &= false;
					}
				} else {
					for (AppointmentWrapper wrapper : appointmentWrappers) {
						DateChooser startEl = wrapper.getStartEl();
						DateChooser endEl = wrapper.getEndEl();
						startEl.clearError();
						endEl.clearError();
						if (!BigBlueButtonUIHelper.validateDuration(startEl, leadTimeEl, endEl, followupTimeEl, template)) {
							bbbOk &= false;
						}
						if (!BigBlueButtonUIHelper.validateSlot(startEl, leadTimeEl, endEl, followupTimeEl, null, template)) {
							bbbOk &= false;
						}
					}
				}
			}
		}
		if (!bbbOk) {
			allOk &= false;
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
		doSave();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSave() {
		doSaveTopic();
		doSaveOrganizers();
		
		boolean reccuring = recurringEl.isAtLeastSelected(1);
		if (reccuring) {
			doSaveReccuringAppointments();
		} else {
			doSaveSingleAppointments();
		}
	}

	private void doSaveTopic() {
		topic = appointmentsService.createTopic(entry, subIdent);
		
		String title = titleEl.getValue();
		topic.setTitle(title);
		
		String description = descriptionEl.getValue();
		topic.setDescription(description);

		Type type = typeEl.isOneSelected() ? Type.valueOf(typeEl.getSelectedKey()) : Type.enrollment;
		topic.setType(type);
		
		Collection<String> configKeys = configurationEl.getSelectedKeys();
		boolean multiParticipation = configKeys.contains(KEY_MULTI_PARTICIPATION);
		topic.setMultiParticipation(multiParticipation);
		
		boolean autoConfirmation = Type.finding == type
				? false
				: !configKeys.contains(KEY_COACH_CONFIRMATION);
		topic.setAutoConfirmation(autoConfirmation);
		
		topic = appointmentsService.updateTopic(topic);
	}

	private void doSaveOrganizers() {
		Collection<String> selectedOrganizerKeys = organizerEl.getSelectedKeys();
		List<Identity> selectedOrganizers = coaches.stream()
				.filter(i -> selectedOrganizerKeys.contains(i.getKey().toString()))
				.collect(Collectors.toList());
		appointmentsService.updateOrganizers(topic, selectedOrganizers);
	}

	private void doSaveSingleAppointments() {
		for (AppointmentWrapper wrapper : appointmentWrappers) {
			DateChooser startEl = wrapper.getStartEl();
			DateChooser endEl = wrapper.getEndEl();
			if (startEl.getDate() != null && endEl.getDate() != null) {
				Appointment appointment = appointmentsService.createUnsavedAppointment(topic);
				
				Date start = startEl.getDate();
				appointment.setStart(start);
				
				Date end = endEl.getDate();
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
				
				appointment = addMeeting(appointment);
				appointmentsService.saveAppointment(appointment);
			}
		}
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
			
			appointment = addMeeting(appointment);
			appointmentsService.saveAppointment(appointment);
		}
	}

	private Appointment addMeeting(Appointment appointment) {
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
		}
		return appointment;
	}
	
	private void doCreateAppointmentWrapper(AppointmentWrapper after) {
		AppointmentWrapper wrapper = new AppointmentWrapper();
		
		DateChooser startEl = uifactory.addDateChooser("start_" + counter++, null, singleCont);
		startEl.setDateChooserTimeEnabled(true);
		startEl.setUserObject(wrapper);
		startEl.addActionListener(FormEvent.ONCHANGE);
		wrapper.setStartEl(startEl);
		
		DateChooser endEl = uifactory.addDateChooser("end_" + counter++, null, singleCont);
		endEl.setDateChooserTimeEnabled(true);
		wrapper.setEndEl(endEl);
		
		FormLink addEl = uifactory.addFormLink("add_" + counter++, CMD_ADD, "", null, singleCont, Link.NONTRANSLATED + Link.BUTTON);
		addEl.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		addEl.setUserObject(wrapper);
		wrapper.setAddEl(addEl);
		
		FormLink removeEl = uifactory.addFormLink("remove_" + counter++, CMD_REMOVE, "", null, singleCont, Link.NONTRANSLATED + Link.BUTTON);
		removeEl.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
		removeEl.setUserObject(wrapper);
		wrapper.setRemoveEl(removeEl);
		
		if (after == null) {
			appointmentWrappers.add(wrapper);
		} else {
			int index = appointmentWrappers.indexOf(after) + 1;
			appointmentWrappers.add(index, wrapper);
		}
		showHideRemoveButtons();
	}

	private void doRemoveAppointmentWrapper(AppointmentWrapper wrapper) {
		appointmentWrappers.remove(wrapper);
		showHideRemoveButtons();
	}

	private void showHideRemoveButtons() {
		boolean enabled = appointmentWrappers.size() != 1;
		appointmentWrappers.stream()
				.forEach(wrapper -> wrapper.getRemoveEl().setEnabled(enabled));
	}

	private void doInitEndDate(AppointmentWrapper wrapper) {
		DateChooser startEl = wrapper.getStartEl();
		DateChooser endEl = wrapper.getEndEl();
		if (startEl.getDate() != null && endEl.getDate() == null) {
			endEl.setDate(startEl.getDate());
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
	
	public static final class AppointmentWrapper {
		
		private DateChooser startEl;
		private DateChooser endEl;
		private FormLink addEl;
		private FormLink removeEl;

		public String getStartElName() {
			return startEl != null? startEl.getComponent().getComponentName(): null;
		}
		
		public DateChooser getStartEl() {
			return startEl;
		}
		
		public void setStartEl(DateChooser startEl) {
			this.startEl = startEl;
		}
		
		public String getEndElName() {
			return endEl != null? endEl.getComponent().getComponentName(): null;
		}
		
		public DateChooser getEndEl() {
			return endEl;
		}
		
		public void setEndEl(DateChooser endEl) {
			this.endEl = endEl;
		}
		
		public String getRemoveElName() {
			return removeEl != null? removeEl.getComponent().getComponentName(): null;
		}

		public FormLink getRemoveEl() {
			return removeEl;
		}

		public void setRemoveEl(FormLink removeEl) {
			this.removeEl = removeEl;
		}
		
		public String getAddElName() {
			return addEl != null? addEl.getComponent().getComponentName(): null;
		}

		public FormLink getAddEl() {
			return addEl;
		}

		public void setAddEl(FormLink addEl) {
			this.addEl = addEl;
		}
		
	}

}
