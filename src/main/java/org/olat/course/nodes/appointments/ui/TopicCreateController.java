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
package org.olat.course.nodes.appointments.ui;

import static org.olat.core.gui.components.util.KeyValues.VALUE_ASC;
import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
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
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.AppointmentsSecurityCallback;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Organizer;
import org.olat.course.nodes.appointments.Topic;
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
	
	private static final String CMD_REMOVE = "remove";
	private static final String CMD_ADD = "add";
	
	private TextElement titleEl;
	private TextElement descriptionEl;
	private MultipleSelectionElement organizerEl;
	private TextElement locationEl;
	private TextElement maxParticipationsEl;
	private FormLayoutContainer appointmentsCont;
	
	private RepositoryEntry entry;
	private String subIdent;
	private AppointmentsSecurityCallback secCallback;
	private Topic topic;
	private List<Organizer> organizers;
	private List<Identity> coaches;
	private List<AppointmentWrapper> appointmentWrappers;
	private int counter = 0;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;

	public TopicCreateController(UserRequest ureq, WindowControl wControl, AppointmentsSecurityCallback secCallback,
			RepositoryEntry entry, String subIdent) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.entry = entry;
		this.subIdent = subIdent;
		
		organizers = new ArrayList<>(0);
		coaches = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.coach.name());
		
		initForm(ureq);
	}
	
	public Topic getTopic() {
		return topic;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Topic
		String title = topic == null ? "" : topic.getTitle();
		titleEl = uifactory.addTextElement("topic.title", "topic.title", 128, title, formLayout);
		titleEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(title)) {
			titleEl.setFocus(true);
		}
		
		String description = topic == null ? "" : topic.getDescription();
		descriptionEl = uifactory.addTextAreaElement("topic.description", "topic.description", 2000, 4, 72, false,
				false, description, formLayout);
		
		// Organizer
		KeyValues coachesKV = new KeyValues();
		for (Identity coach : coaches) {
			coachesKV.add(entry(coach.getKey().toString(), userManager.getUserDisplayName(coach.getKey())));
		}
		coachesKV.sort(VALUE_ASC);
		organizerEl = uifactory.addCheckboxesVertical("coaches", formLayout, coachesKV.keys(), coachesKV.values(), 2);
		for (Organizer organizer : organizers) {
			Long organizerKey = organizer.getIdentity().getKey();
			if (coaches.stream().anyMatch(coach -> organizerKey.equals(coach.getKey()))) {
				organizerEl.select(organizerKey.toString(), true);
			}
		}
		organizerEl.setVisible(!coaches.isEmpty());
		
		// Appointments
		locationEl = uifactory.addTextElement("appointment.location", 128, null, formLayout);
		
		maxParticipationsEl = uifactory.addTextElement("appointment.max.participations", 5, null, formLayout);
		
		appointmentsCont = FormLayoutContainer.createCustomFormLayout("appointmentsCont", getTranslator(), velocity_root + "/appointments_create.html");
		formLayout.add(appointmentsCont);
		appointmentsCont.setRootForm(mainForm);
		appointmentsCont.setLabel("appointments", null);
		
		appointmentWrappers = new ArrayList<>();
		doCreateAppointmentWrapper(null);
		appointmentsCont.contextPut("appointments", appointmentWrappers);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof DateChooser) {
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
		if (StringHelper.containsNonWhitespace(maxParticipationsValue)) {
			try {
				int value = Integer.parseInt(maxParticipationsValue);
				if (value < 1) {
					maxParticipationsEl.setErrorKey("error.positiv.number", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				maxParticipationsEl.setErrorKey("error.positiv.number", null);
				allOk &= false;
			}
		}
		
		for (AppointmentWrapper wrapper : appointmentWrappers) {
			DateChooser startEl = wrapper.getStartEl();
			DateChooser endEl = wrapper.getEndEl();
			startEl.clearError();
			endEl.clearError();
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
		
		return allOk;
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
		doSaveAppointments();
	}

	private void doSaveTopic() {
		topic = appointmentsService.createTopic(entry, subIdent);
		Identity organizer = secCallback.getDefaultOrganizer();
		if (organizer != null) {
			appointmentsService.createOrganizer(topic, organizer);
		}
		
		String title = titleEl.getValue();
		topic.setTitle(title);
		
		String description = descriptionEl.getValue();
		topic.setDescription(description);
		
		topic = appointmentsService.updateTopic(topic);
	}

	private void doSaveOrganizers() {
		Collection<String> selectedOrganizerKeys = organizerEl.getSelectedKeys();
		
		// delete unselected
		List<Organizer> organizersToDelete = organizers.stream()
				.filter(organizer -> !selectedOrganizerKeys.contains(organizer.getIdentity().getKey().toString()))
				.collect(Collectors.toList());
		appointmentsService.deleteOrganizers(topic, organizersToDelete);
		
		// create newly selected
		Set<String> currentOrganizerKeys = organizers.stream()
				.map(o -> o.getIdentity().getKey().toString())
				.collect(Collectors.toSet());
		selectedOrganizerKeys.removeAll(currentOrganizerKeys);
		coaches.stream()
				.filter(coach -> selectedOrganizerKeys.contains(coach.getKey().toString()))
				.forEach(coach -> appointmentsService.createOrganizer(topic, coach));
	}

	private void doSaveAppointments() {
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
				
				String maxParticipationsValue = maxParticipationsEl.getValue();
				Integer maxParticipations = StringHelper.containsNonWhitespace(maxParticipationsValue)
						? Integer.valueOf(maxParticipationsValue)
						: null;
				appointment.setMaxParticipations(maxParticipations);
				
				appointmentsService.saveAppointment(appointment);
			}
		}
	}
	
	private void doCreateAppointmentWrapper(AppointmentWrapper after) {
		AppointmentWrapper wrapper = new AppointmentWrapper();
		
		DateChooser startEl = uifactory.addDateChooser("start_" + counter++, null, appointmentsCont);
		startEl.setDateChooserTimeEnabled(true);
		startEl.setUserObject(wrapper);
		startEl.addActionListener(FormEvent.ONCHANGE);
		wrapper.setStartEl(startEl);
		
		DateChooser endEl = uifactory.addDateChooser("end_" + counter++, null, appointmentsCont);
		endEl.setDateChooserTimeEnabled(true);
		wrapper.setEndEl(endEl);
		
		FormLink addEl = uifactory.addFormLink("add_" + counter++, CMD_ADD, "", null, appointmentsCont, Link.NONTRANSLATED + Link.BUTTON);
		addEl.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		addEl.setUserObject(wrapper);
		wrapper.setAddEl(addEl);
		
		FormLink removeEl = uifactory.addFormLink("remove_" + counter++, CMD_REMOVE, "", null, appointmentsCont, Link.NONTRANSLATED + Link.BUTTON);
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
