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

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.util.DateUtils.toDate;
import static org.olat.core.util.DateUtils.toLocalDateTime;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.AppointmentInput;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.DuplicationContext;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class DuplicateTopic2StepController extends StepFormBasicController {
	
	private static final String KEY_NONE = "none";
	private static final String KEY_PERIOD = "period";
	private static final String KEY_FIRST = "first";
	
	private static final Comparator<Appointment> START_END_COMPARATOR = 
			Comparator.comparing(Appointment::getStart)
			.thenComparing(Appointment::getEnd)
			.thenComparing(Appointment::getKey);
	
	private SingleSelection moveEl;
	private FormLayoutContainer periodCont;
	private TextElement periodDaysEl;
	private TextElement periodHoursEl;
	private TextElement periodMinutesEl;
	private DateChooser firstEl;
	private FormLayoutContainer meetingValidationCont;
	private FlexiTableElement tableEl;
	private AppointmentInputDataModel dataModel;

	private final DuplicationContext context;
	private final TopicLight topic;
	private final List<Appointment> sourceAppointments;
	private final boolean meetings;
	private final Date currentFirstStart;
	private long moveDays;
	private long moveHours;
	private long moveMinutes;
	
	@Autowired
	private AppointmentsService appointmentsService;

	public DuplicateTopic2StepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, Topic sourceTopic) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		
		context = DuplicateTopicCallback.getDuplicationContext(runContext);
		topic = context.getTopic();
		
		AppointmentSearchParams aParams = new AppointmentSearchParams();
		aParams.setTopic(sourceTopic);
		aParams.setFetchMeetings(true);
		sourceAppointments = appointmentsService.getAppointments(aParams).stream()
				.sorted(START_END_COMPARATOR)
				.collect(Collectors.toList());
		
		meetings = appointmentsService.isBigBlueButtonEnabled() && hasBBBMeeting(sourceAppointments)
				|| (appointmentsService.isTeamsEnabled() && hasTeamsMeetings(sourceAppointments));
		
		currentFirstStart = sourceAppointments.isEmpty() ? new Date(): sourceAppointments.get(0).getStart();
		
		initForm(ureq);
		loadModel();
		updateUI();
		tableEl.selectAll();
	}

	private boolean hasBBBMeeting(List<Appointment> appointments) {
		return appointments.stream().anyMatch(a -> a.getBBBMeeting() != null);
	}

	private boolean hasTeamsMeetings(List<Appointment> appointments) {
		return appointments.stream().anyMatch(a -> a.getTeamsMeeting() != null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer configCont = FormLayoutContainer.createDefaultFormLayout("configs", getTranslator());
		configCont.setRootForm(mainForm);
		formLayout.add(configCont);
		
		SelectionValues moveKV = new SelectionValues();
		moveKV.add(entry(KEY_NONE, translate("duplicate.move.none")));
		moveKV.add(entry(KEY_PERIOD, translate("duplicate.move.period")));
		moveKV.add(entry(KEY_FIRST, translate("duplicate.move.first")));
		moveEl = uifactory.addRadiosHorizontal("duplicate.move", configCont, moveKV.keys(), moveKV.values());
		moveEl.addActionListener(FormEvent.ONCHANGE);
		moveEl.select(KEY_NONE, true);
		
		periodCont = FormLayoutContainer.createCustomFormLayout("period", getTranslator(), velocity_root + "/time_period.html");
		((AbstractComponent)periodCont.getComponent()).setDomReplacementWrapperRequired(false);
		periodCont.setLabel("duplicate.period",null);
		periodCont.setRootForm(mainForm);
		configCont.add(periodCont);
		
		periodDaysEl = uifactory.addTextElement("duplicate.period.d", 3, "", periodCont);
		periodDaysEl.setDisplaySize(3);
		((AbstractComponent)periodDaysEl.getComponent()).setDomReplacementWrapperRequired(false);
		periodDaysEl.addActionListener(FormEvent.ONCHANGE);
		
		periodHoursEl = uifactory.addTextElement("duplicate.period.h", 3, "", periodCont);
		((AbstractComponent)periodHoursEl.getComponent()).setDomReplacementWrapperRequired(false);
		periodHoursEl.setDisplaySize(3);
		periodHoursEl.addActionListener(FormEvent.ONCHANGE);
		
		periodMinutesEl = uifactory.addTextElement("duplicate.period.m", 3, "", periodCont);
		((AbstractComponent)periodMinutesEl.getComponent()).setDomReplacementWrapperRequired(false);
		periodMinutesEl.setDisplaySize(3);
		periodMinutesEl.addActionListener(FormEvent.ONCHANGE);
		
		firstEl = uifactory.addDateChooser("duplicate.first", new Date(), configCont);
		firstEl.setDateChooserTimeEnabled(true);
		firstEl.addActionListener(FormEvent.ONCHANGE);
		
		meetingValidationCont = FormLayoutContainer.createHorizontalFormLayout("meeting.validation.error", getTranslator());
		meetingValidationCont.setElementCssClass("o_appointments_duplicate");
		meetingValidationCont.setFormWarning(translate("error.meetings.validation"));
		meetingValidationCont.setRootForm(mainForm);
		formLayout.add(meetingValidationCont);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.location));
		DefaultFlexiColumnModel detailsModel = new DefaultFlexiColumnModel(Cols.details);
		detailsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(detailsModel);
		if (Type.finding != topic.getType()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.maxParticipations));
		}
		if (meetings) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.meeting, new MeetingValidationRenderer(getTranslator())));
		}
		
		dataModel = new AppointmentInputDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "appointments-duplicate");
		tableEl.setEmptyTableMessageKey("table.empty.appointments");
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}
	
	private void loadModel() {
		boolean meetingValidationFailures = false;
		List<AppointmentInput> rows = new ArrayList<>(sourceAppointments.size());
		for (Appointment appointment : sourceAppointments) {
			Date start = toDate(toLocalDateTime(appointment.getStart()).plusDays(moveDays).plusHours(moveHours).plusMinutes(moveMinutes));
			Date end = toDate(toLocalDateTime(appointment.getEnd()).plusDays(moveDays).plusHours(moveHours).plusMinutes(moveMinutes));
			Boolean meetingValidation = validateMeeting(appointment, start, end);
			AppointmentInput row = new AppointmentInput(appointment, start, end, meetingValidation);
			rows.add(row);
			
			if (meetingValidation != null && !meetingValidation.booleanValue()) {
				meetingValidationFailures = true;
			}
		}
		meetingValidationCont.setVisible(meetingValidationFailures);
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private Boolean validateMeeting(Appointment appointment, Date start, Date end) {
		if (appointment.getBBBMeeting() != null) {
			return validateBBBMeeting(appointment.getBBBMeeting(), start, end);
		} else if (appointment.getTeamsMeeting() != null) {
			return Boolean.TRUE;
		}
		return null;
	}

	private Boolean validateBBBMeeting(BigBlueButtonMeeting meeting, Date start, Date end) {
		// Only validation of the slot. All other restrictions should still be ok.
		boolean valid = BigBlueButtonUIHelper.validateSlot(null, meeting.getTemplate(), start, end, meeting.getLeadTime(), meeting.getFollowupTime());
		return Boolean.valueOf(valid);
	}
	
	private void updateUI() {
		Set<Integer> multiSelectedIndex = tableEl.getMultiSelectedIndex();
		periodCont.clearError();

		boolean none = moveEl.isOneSelected() && moveEl.getSelectedKey().equals(KEY_NONE);
		if (none) {
			moveDays = 0;
			moveHours = 0;
			moveMinutes = 0;
		}
		
		boolean period = moveEl.isOneSelected() && moveEl.getSelectedKey().equals(KEY_PERIOD);
		periodCont.setVisible(period);
		if (period && validatePeriod()) {
			moveDays = 0;
			moveHours = 0;
			moveMinutes = 0;
			if (StringHelper.containsNonWhitespace(periodDaysEl.getValue())) {
				moveDays = Long.parseLong(periodDaysEl.getValue());
			}
			if (StringHelper.containsNonWhitespace(periodHoursEl.getValue())) {
				moveHours = Long.parseLong(periodHoursEl.getValue());
			}
			if (StringHelper.containsNonWhitespace(periodMinutesEl.getValue())) {
				moveMinutes += Long.parseLong(periodMinutesEl.getValue());
			}
		}
		
		
		boolean firstDate = moveEl.isOneSelected() && moveEl.getSelectedKey().equals(KEY_FIRST);
		firstEl.setVisible(firstDate);
		if (firstDate && firstEl.getDate() != null) {
			LocalDateTime currentFirstDateTime = toLocalDateTime(currentFirstStart);
			LocalDateTime firstDateTime = toLocalDateTime(firstEl.getDate());
			moveDays = ChronoUnit.DAYS.between(currentFirstDateTime.toLocalDate(), firstDateTime.toLocalDate());
			moveHours = 0;
			moveMinutes = ChronoUnit.MINUTES.between(currentFirstDateTime.toLocalTime(), firstDateTime.toLocalTime());
		}
		
		loadModel();
		tableEl.setMultiSelectedIndex(multiSelectedIndex);
	}
	
	private boolean validatePeriod() {
		boolean allOk = true;
		
		int min = 0;
		int max = 1000;
		allOk &= validatePeriod(periodDaysEl, min, max);
		allOk &= validatePeriod(periodHoursEl, min, max);
		allOk &= validatePeriod(periodMinutesEl, min, max);
		
		if (!allOk) {
			periodCont.setErrorKey("error.period.number", new String[] { String.valueOf(min), String.valueOf(max)} );
		}
		return allOk;
	}

	private boolean validatePeriod(TextElement element, int min, int max) {
		if (StringHelper.containsNonWhitespace(element.getValue())) {
			try {
				int value = Integer.parseInt(element.getValue());
				if (min > value || max < value) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == moveEl) {
			updateUI();
		} else if (source == periodDaysEl) {
			updateUI();
		} else if (source == periodHoursEl) {
			updateUI();
		} else if (source == periodMinutesEl) {
			updateUI();
		} else if (source == firstEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean ollOk = super.validateFormLogic(ureq);
		
		periodCont.clearError();
		boolean period = moveEl.isOneSelected() && moveEl.getSelectedKey().equals(KEY_PERIOD);
		if (period) {
			ollOk &= validatePeriod();
		}
		
		return ollOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<AppointmentInput> selectedAppointments = tableEl.getMultiSelectedIndex().stream()
					.map(index -> dataModel.getObject(index.intValue()))
					.collect(Collectors.toList());
		context.setAppointments(selectedAppointments);
		
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

	private final class AppointmentInputDataModel extends DefaultFlexiTableDataModel<AppointmentInput> {
		
		private final Translator translator;
		
		public AppointmentInputDataModel(FlexiTableColumnModel columnsModel, Translator translator) {
			super(columnsModel);
			this.translator = translator;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			AppointmentInput appointment = getObject(row);
			return getValueAt(appointment, col);
		}

		public Object getValueAt(AppointmentInput row, int col) {
			switch(Cols.values()[col]) {
				case start: return row.getStart();
				case end: return row.getEnd();
				case location: return AppointmentsUIFactory.getDisplayLocation(translator, row.getAppointment());
				case details: return row.getAppointment().getDetails();
				case maxParticipations: return row.getAppointment().getMaxParticipations();
				case meeting: return row;
				default: return null;
			}
		}
	}
		
	private enum Cols implements FlexiColumnDef {
		start("appointment.start"),
		end("appointment.end"),
		location("appointment.location"),
		details("appointment.details"),
		maxParticipations("appointment.max.participations"),
		meeting("appointment.meeting");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
