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

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.Appointment.Status;
import org.olat.course.nodes.appointments.AppointmentSearchParams;
import org.olat.course.nodes.appointments.AppointmentsSecurityCallback;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Organizer;
import org.olat.course.nodes.appointments.Participation;
import org.olat.course.nodes.appointments.ParticipationSearchParams;
import org.olat.course.nodes.appointments.Topic;
import org.olat.course.nodes.appointments.ui.AppointmentDataModel.AppointmentCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicCoachController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final String CMD_REBOOK = "rebook";
	private static final String CMD_CONFIRM = "confirm";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_EDIT = "edit";
	
	private FormLink deleteTopicLink;
	private FormLink editTopicLink;
	private FormLink editOrganizerLink;
	private FormLink addAppointmentLink;
	private FlexiTableElement tableEl;
	private AppointmentDataModel dataModel;
	
	private CloseableModalController cmc;
	private TopicEditController topicEditCtrl;
	private OrganizersEditController organizersEditCtrl;
	private AppointmentEditController appointmentEditCtrl;
	private RebookController rebookCtrl;
	private DialogBoxController confirmDeleteTopicCrtl;
	private AppointmentDeleteController appointmentDeleteCtrl;

	private Topic topic;
	private final AppointmentsSecurityCallback secCallback;
	private final Configuration config;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;
	
	public TopicCoachController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback, Configuration config) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.topic = topic;
		this.secCallback = secCallback;
		this.config = config;

		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Buttons
		FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
		topButtons.setRootForm(mainForm);
		formLayout.add("topButtons", topButtons);
		topButtons.setElementCssClass("o_button_group o_button_group_right");
		List<Organizer> organizers = appointmentsService.getOrganizers(topic);
		if (secCallback.canEditTopic(organizers)) {
			deleteTopicLink = uifactory.addFormLink("delete.topic", topButtons, Link.BUTTON);
			deleteTopicLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
			editTopicLink = uifactory.addFormLink("edit.topic", topButtons, Link.BUTTON);
			editTopicLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
			editOrganizerLink = uifactory.addFormLink("edit.organizer", topButtons, Link.BUTTON);
			editOrganizerLink.setIconLeftCSS("o_icon o_icon-lg o_icon_coach");
		}
		if (secCallback.canEditAppointment(organizers)) {
			addAppointmentLink = uifactory.addFormLink("add.appointment", topButtons, Link.BUTTON);
			addAppointmentLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}
		
		// Table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			DefaultFlexiColumnModel idModel = new DefaultFlexiColumnModel(AppointmentCols.id);
			idModel.setDefaultVisible(false);
			columnsModel.addFlexiColumnModel(idModel);
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.status, new AppointmentStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.location));
		DefaultFlexiColumnModel detailsModel = new DefaultFlexiColumnModel(AppointmentCols.details);
		detailsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(detailsModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.maxParticipations));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.freeParticipations));
		DefaultFlexiColumnModel participantsModel = new DefaultFlexiColumnModel(AppointmentCols.participants);
		participantsModel.setCellRenderer(new ParticipationsRenderer());
		participantsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(participantsModel);
		DefaultFlexiColumnModel rebookModel = new DefaultFlexiColumnModel(AppointmentCols.rebook);
		rebookModel.setExportable(false);
		columnsModel.addFlexiColumnModel(rebookModel);
		DefaultFlexiColumnModel confirmModel = new DefaultFlexiColumnModel(AppointmentCols.confirm);
		confirmModel.setExportable(false);
		columnsModel.addFlexiColumnModel(confirmModel);
		DefaultFlexiColumnModel deleteModel = new DefaultFlexiColumnModel(AppointmentCols.delete);
		deleteModel.setExportable(false);
		columnsModel.addFlexiColumnModel(deleteModel);
		DefaultFlexiColumnModel editModel = new DefaultFlexiColumnModel(AppointmentCols.edit);
		editModel.setExportable(false);
		columnsModel.addFlexiColumnModel(editModel);
		
		dataModel = new AppointmentDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "appointments");
		tableEl.setEmtpyTableMessageKey("table.empty.appointments");

		tableEl.setElementCssClass("o_appointments o_selection_run o_coach");
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("appointment_row_coach");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
	}

	private void updateModel() {
		AppointmentSearchParams searchParams = new AppointmentSearchParams();
		searchParams.setTopic(topic);
		searchParams.setFetchTopic(true);
		List<Appointment> appointments = appointmentsService.getAppointments(searchParams);
		ParticipationSearchParams pParams = new ParticipationSearchParams();
		pParams.setAppointments(appointments);
		Map<Long, List<Participation>> appointmentKeyToParticipations = appointmentsService
				.getParticipations(pParams).stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		
		appointments.sort((a1, a2) -> a1.getStart().compareTo(a2.getStart()));
		
		List<AppointmentRow> rows = new ArrayList<>(appointments.size());
		for (Appointment appointment : appointments) {
			List<Participation> participations = appointmentKeyToParticipations.getOrDefault(appointment.getKey(), emptyList());
			AppointmentRow row = createRow(appointment, participations);
			if (row != null) {
				rows.add(row);
			}
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AppointmentRow createRow(Appointment appointment, List<Participation> participations) {
		AppointmentRow row = new AppointmentRow(appointment);

		Locale locale = getLocale();
		Date begin = appointment.getStart();
		Date end = appointment.getEnd();
		String date = null;
		String dateLong = null;
		String dateShort1 = null;
		String dateShort2 = null;
		String time = null;

		boolean sameDay = DateUtils.isSameDay(begin, end);
		String startDate = StringHelper.formatLocaleDateFull(begin.getTime(), locale);
		String startTime = StringHelper.formatLocaleTime(begin.getTime(), locale);
		String endDate = StringHelper.formatLocaleDateFull(end.getTime(), locale);
		String endTime = StringHelper.formatLocaleTime(end.getTime(), locale);
		if (sameDay) {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(startDate);
			date = dateSb.toString();
			StringBuilder timeSb = new StringBuilder();
			timeSb.append(startTime);
			timeSb.append(" - ");
			timeSb.append(endTime);
			time = timeSb.toString();
		} else {
			StringBuilder dateSbLong = new StringBuilder();
			dateSbLong.append(startDate);
			dateSbLong.append(" ");
			dateSbLong.append(startTime);
			dateSbLong.append(" - ");
			dateSbLong.append(endDate);
			dateSbLong.append(" ");
			dateSbLong.append(endTime);
			dateLong = dateSbLong.toString();
			StringBuilder dateSbShort1 = new StringBuilder();
			dateSbShort1.append(startDate);
			dateSbShort1.append(" ");
			dateSbShort1.append(startTime);
			dateSbShort1.append(" -");
			dateShort1 = dateSbShort1.toString();
			StringBuilder dateSbShort2 = new StringBuilder();
			dateSbShort2.append(endDate);
			dateSbShort2.append(" ");
			dateSbShort2.append(endTime);
			dateShort2 = dateSbShort2.toString();
		}

		row.setDate(date);
		row.setDateLong(dateLong);
		row.setDateShort1(dateShort1);
		row.setDateShort2(dateShort2);
		row.setTime(time);
		row.setLocation(appointment.getLocation());
		row.setDetails(appointment.getDetails());
		
		List<String> participants = participations.stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
		row.setParticipants(participants);

		if (Appointment.Status.planned == appointment.getStatus()) {
			Integer maxParticipations = appointment.getMaxParticipations();
			Integer freeParticipations = maxParticipations != null ? maxParticipations.intValue() - participations.size()
					: null;
			row.setMaxParticipations(maxParticipations);
			row.setFreeParticipations(freeParticipations);
		}
		
		row.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
		row.setStatusCSS("o_ap_status_" + appointment.getStatus().name());

		if (participations.size() > 0) {
			forgeRebookLink(row);
		}
		if (config.isConfirmation()) {
			boolean confirmable = Appointment.Status.planned == appointment.getStatus()
					&& participations.size() > 0;
			boolean unconfirmable = Appointment.Status.confirmed == appointment.getStatus();
			if (confirmable || unconfirmable) {
				forgeConfirmLink(row, confirmable);
			}
		}
		forgeDeleteLink(row);
		forgeEditLink(row);

		return row;
	}

	private void forgeRebookLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("rebook_" + row.getKey(), CMD_REBOOK, "rebook", null, null, Link.LINK);
		link.setUserObject(row);
		row.setRebookLink(link);
	}
	
	private void forgeConfirmLink(AppointmentRow row, boolean confirmable) {
		String i18nKey = confirmable? "confirm": "unconfirm";
		FormLink link = uifactory.addFormLink("confirm_" + row.getKey(), CMD_CONFIRM, i18nKey, null, null, Link.LINK);
		link.setUserObject(row);
		row.setConfirmLink(link);
	}
	
	private void forgeDeleteLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("delete_" + row.getKey(), CMD_DELETE, "delete", null, null, Link.LINK);
		link.setUserObject(row);
		row.setDeleteLink(link);
	}
	
	private void forgeEditLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("edit_" + row.getKey(), CMD_EDIT, "edit", null, null, Link.LINK);
		link.setUserObject(row);
		row.setEditLink(link);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addAppointmentLink) {
			doAddAppointment(ureq);
		} else if (source == deleteTopicLink) {
			doConfirmDeleteTopic(ureq);
		} else if (source == editTopicLink) {
			doEditTopic(ureq);
		} else if (source == editOrganizerLink) {
			doEditOrganizer(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_EDIT.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doEditAppointment(ureq, row.getAppointment());
			} else if (CMD_DELETE.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doConfirmDeletion(ureq, row.getAppointment());
			} else if (CMD_CONFIRM.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doConfirm(row.getAppointment());
			} else if (CMD_REBOOK.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doRebook(ureq, row.getAppointment());
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (topicEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				topic = topicEditCtrl.getTopic();
				fireEvent(ureq, new TopicChangedEvent(topic));
			}
			cmc.deactivate();
			cleanUp();
		} else if (organizersEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, new OrganizersChangedEvent(topic));
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentDeleteCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		}  else if (rebookCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == confirmDeleteTopicCrtl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doDeleteTopic(ureq);
			}
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appointmentDeleteCtrl);
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(rebookCtrl);
		removeAsListenerAndDispose(cmc);
		appointmentDeleteCtrl = null;
		appointmentEditCtrl = null;
		rebookCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doConfirmDeleteTopic(UserRequest ureq) {
		String title = translate("confirm.topic.delete.title");
		String text = translate("confirm.topic.delete");
		confirmDeleteTopicCrtl = activateYesNoDialog(ureq, title, text, confirmDeleteTopicCrtl);
	}

	private void doDeleteTopic(UserRequest ureq) {
		appointmentsService.deleteTopic(topic);
		fireEvent(ureq, Event.BACK_EVENT);
	}

	private void doEditTopic(UserRequest ureq) {
		topicEditCtrl = new TopicEditController(ureq, getWindowControl(), topic, secCallback);
		listenTo(topicEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", topicEditCtrl.getInitialComponent(), true,
				translate("edit.topic"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditOrganizer(UserRequest ureq) {
		organizersEditCtrl = new OrganizersEditController(ureq, getWindowControl(), topic);
		listenTo(organizersEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", organizersEditCtrl.getInitialComponent(), true,
				translate("edit.organizer"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddAppointment(UserRequest ureq) {
		appointmentEditCtrl = new AppointmentEditController(ureq, getWindowControl(), topic);
		listenTo(appointmentEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", appointmentEditCtrl.getInitialComponent(), true,
				translate("add.appointment.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditAppointment(UserRequest ureq, Appointment appointment) {
		appointmentEditCtrl = new AppointmentEditController(ureq, getWindowControl(), appointment);
		listenTo(appointmentEditCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", appointmentEditCtrl.getInitialComponent(), true,
				translate("edit.appointment.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeletion(UserRequest ureq, Appointment appointment) {
		appointmentDeleteCtrl = new AppointmentDeleteController(ureq, getWindowControl(), appointment, config);
		listenTo(appointmentDeleteCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", appointmentDeleteCtrl.getInitialComponent(),
				true, translate("confirm.appointment.delete.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirm(Appointment appointment) {
		if (Status.planned == appointment.getStatus()) {
			appointmentsService.confirmAppointment(appointment);
		} else {
			appointmentsService.unconfirmAppointment(appointment);
		}
		updateModel();
	}
	
	private void doRebook(UserRequest ureq, Appointment appointment) {
		rebookCtrl = new RebookController(ureq, getWindowControl(), appointment, config);
		listenTo(rebookCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", rebookCtrl.getInitialComponent(),
				true, translate("rebook.title"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

}
