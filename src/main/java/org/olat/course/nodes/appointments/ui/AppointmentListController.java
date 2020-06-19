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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.date.DateElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
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
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.Appointment.Status;
import org.olat.course.nodes.appointments.AppointmentsSecurityCallback;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Organizer;
import org.olat.course.nodes.appointments.ParticipationResult;
import org.olat.course.nodes.appointments.Topic;
import org.olat.course.nodes.appointments.Topic.Type;
import org.olat.course.nodes.appointments.ui.AppointmentDataModel.AppointmentCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AppointmentListController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final String CMD_SELECT = "select";
	private static final String CMD_REBOOK = "rebook";
	private static final String CMD_CONFIRM = "confirm";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_EDIT = "edit";
	
	private FormLink backLink;
	private FormLink addAppointmentLink;
	private FlexiTableElement tableEl;
	private AppointmentDataModel dataModel;
	
	private CloseableModalController cmc;
	private TopicHeaderController headerCtrl;
	private DialogBoxController confirmParticipationCrtl;
	private AppointmentEditController appointmentEditCtrl;
	private RebookController rebookCtrl;
	private AppointmentDeleteController appointmentDeleteCtrl;

	protected Topic topic;
	protected final AppointmentsSecurityCallback secCallback;
	
	@Autowired
	protected AppointmentsService appointmentsService;
	@Autowired
	protected UserManager userManager;
	
	protected AppointmentListController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl, "appointments_list");
		this.topic = topic;
		this.secCallback = secCallback;

		initForm(ureq);
		updateModel();
	}
	
	protected abstract boolean canSelect();
	
	protected abstract boolean canEdit();
	
	protected abstract String getTableCssClass();
	
	protected abstract List<String> getFilters();
	
	protected abstract List<String>  getDefaultFilters();
	
	protected abstract String getPersistedPreferencesId();
	
	protected abstract List<AppointmentRow> loadModel();
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Back
		FormLayoutContainer backButtons = FormLayoutContainer.createButtonLayout("backButtons", getTranslator());
		backButtons.setRootForm(mainForm);
		formLayout.add("backButtons", backButtons);
		backButtons.setElementCssClass("o_button_group o_button_group_left");
		
		backLink = uifactory.addFormLink("backLink", "back", "back", "", backButtons, Link.LINK_BACK);
		backLink.setElementCssClass("o_back");
		
		// Header
		headerCtrl = new TopicHeaderController(ureq, getWindowControl(), topic, false);
		listenTo(headerCtrl);
		flc.put("header", headerCtrl.getInitialComponent());
		
		// Buttons
		if (canEdit()) {
			FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
			topButtons.setRootForm(mainForm);
			formLayout.add("topButtons", topButtons);
			topButtons.setElementCssClass("o_button_group o_button_group_right");
			
			List<Organizer> organizers = appointmentsService.getOrganizers(topic);
			if (secCallback.canEditAppointment(organizers)) {
				addAppointmentLink = uifactory.addFormLink("add.appointment", topButtons, Link.BUTTON);
				addAppointmentLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			}
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
		if (Type.finding != topic.getType()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.maxParticipations));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.freeParticipations));
		}
		DefaultFlexiColumnModel numberOfParticipationsModel = new DefaultFlexiColumnModel(AppointmentCols.numberOfParticipations);
		numberOfParticipationsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(numberOfParticipationsModel);
		DefaultFlexiColumnModel participantsModel = new DefaultFlexiColumnModel(AppointmentCols.participants);
		participantsModel.setCellRenderer(new ParticipationsRenderer());
		participantsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(participantsModel);
		if (canSelect()) {
			DefaultFlexiColumnModel selectModel = new DefaultFlexiColumnModel(AppointmentCols.select);
			selectModel.setExportable(false);
			columnsModel.addFlexiColumnModel(selectModel);
		}
		if (canEdit()) {
			if (Type.finding != topic.getType()) {
				DefaultFlexiColumnModel rebookModel = new DefaultFlexiColumnModel(AppointmentCols.rebook);
				rebookModel.setExportable(false);
				columnsModel.addFlexiColumnModel(rebookModel);
			}
			DefaultFlexiColumnModel confirmModel = new DefaultFlexiColumnModel(AppointmentCols.confirm);
			confirmModel.setExportable(false);
			columnsModel.addFlexiColumnModel(confirmModel);
			DefaultFlexiColumnModel deleteModel = new DefaultFlexiColumnModel(AppointmentCols.delete);
			deleteModel.setExportable(false);
			columnsModel.addFlexiColumnModel(deleteModel);
			DefaultFlexiColumnModel editModel = new DefaultFlexiColumnModel(AppointmentCols.edit);
			editModel.setExportable(false);
			columnsModel.addFlexiColumnModel(editModel);
		}
		
		dataModel = new AppointmentDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, getPersistedPreferencesId());
		tableEl.setEmtpyTableMessageKey("table.empty.appointments");

		tableEl.setElementCssClass("o_appointments o_list " + getTableCssClass());
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("appointment_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		initFilters();
		initSorters();
	}

	protected void initFilters() {
		List<String> filters = getFilters();
		if (filters != null && !filters.isEmpty()) {
			List<FlexiTableFilter> tableFilters = new ArrayList<>(3);
			List<String> defaultFilters = getDefaultFilters();
			List<FlexiTableFilter> selectedFilters = new ArrayList<>(defaultFilters.size());
			if (filters.contains(AppointmentDataModel.FILTER_PARTICIPATED)) {
				FlexiTableFilter filter = new FlexiTableFilter(translate("filter.participated"), AppointmentDataModel.FILTER_PARTICIPATED, false);
				tableFilters.add(filter);
				if (defaultFilters.contains(AppointmentDataModel.FILTER_PARTICIPATED)) {
					selectedFilters.add(filter);
				}
			}
			if (filters.contains(AppointmentDataModel.FILTER_FUTURE)) {
				FlexiTableFilter filter = new FlexiTableFilter(translate("filter.future"), AppointmentDataModel.FILTER_FUTURE, false);
				tableFilters.add(filter);
				if (defaultFilters.contains(AppointmentDataModel.FILTER_FUTURE)) {
					selectedFilters.add(filter);
				}
			}
			tableFilters.add(FlexiTableFilter.SPACER);
			FlexiTableFilter filter = new FlexiTableFilter(translate("filter.all"), AppointmentDataModel.FILTER_ALL, true);
			tableFilters.add(filter);
			if (defaultFilters.contains(AppointmentDataModel.FILTER_ALL)) {
				selectedFilters.add(filter);
			}
			tableEl.setFilters("Filters", tableFilters, true);
			tableEl.setSelectedFilters(selectedFilters);
		}
	}
	
	private void initSorters() {
		List<FlexiTableSort> sorters = new ArrayList<>(8);
		sorters.add(new FlexiTableSort(translate(AppointmentCols.start.i18nHeaderKey()), AppointmentCols.start.name()));
		sorters.add(new FlexiTableSort(translate(AppointmentCols.numberOfParticipations.i18nHeaderKey()), AppointmentCols.numberOfParticipations.name()));
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(AppointmentCols.start.name(), true));
		tableEl.setSortSettings(options);
	}

	private void updateModel() {
		List<AppointmentRow> rows = loadModel();
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	protected void forgeAppointmentView(AppointmentRow row, Appointment appointment) {
		Locale locale = getLocale();
		Date begin = appointment.getStart();
		Date end = appointment.getEnd();
		String date = null;
		String dateLong = null;
		String dateShort1 = null;
		String dateShort2 = null;
		String time = null;

		boolean sameDay = DateUtils.isSameDay(begin, end);
		boolean sameTime = DateUtils.isSameTime(begin, end);
		String startDate = StringHelper.formatLocaleDateFull(begin.getTime(), locale);
		String startTime = StringHelper.formatLocaleTime(begin.getTime(), locale);
		String endDate = StringHelper.formatLocaleDateFull(end.getTime(), locale);
		String endTime = StringHelper.formatLocaleTime(end.getTime(), locale);
		if (sameDay) {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(startDate);
			date = dateSb.toString();
			StringBuilder timeSb = new StringBuilder();
			if (sameTime) {
				timeSb.append(translate("full.day"));
			} else {
				timeSb.append(startTime);
				timeSb.append(" - ");
				timeSb.append(endTime);
			}
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
		forgeDayElement(row, appointment.getStart());
	}
	
	protected void forgeDayElement(AppointmentRow row, Date date) {
		DateElement dayEl = DateComponentFactory.createDateElementWithYear("day_" + row.getKey(), date);
		row.setDayEl(dayEl);
	}
	
	protected void forgeSelectionLink(AppointmentRow row, boolean selected, boolean enabled) {
		String i18n = selected? "appointment.selected": "appointment.select";
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, i18n, null, null, Link.LINK);
		link.setUserObject(row);
		if (selected) {
			link.setIconLeftCSS("o_icon o_icon_lg o_icon_selected");
		} else {
			link.setIconLeftCSS("o_icon o_icon_lg o_icon_unselected");
		}
		link.setEnabled(enabled);
		row.setSelectLink(link);
	}

	protected void forgeRebookLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("rebook_" + row.getKey(), CMD_REBOOK, "rebook", null, null, Link.LINK);
		link.setUserObject(row);
		row.setRebookLink(link);
	}
	
	protected void forgeConfirmLink(AppointmentRow row, boolean confirmable) {
		String i18nKey = confirmable? "confirm": "unconfirm";
		FormLink link = uifactory.addFormLink("confirm_" + row.getKey(), CMD_CONFIRM, i18nKey, null, null, Link.LINK);
		link.setUserObject(row);
		row.setConfirmLink(link);
	}
	
	protected void forgeDeleteLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("delete_" + row.getKey(), CMD_DELETE, "delete", null, null, Link.LINK);
		link.setUserObject(row);
		row.setDeleteLink(link);
	}
	
	protected void forgeEditLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("edit_" + row.getKey(), CMD_EDIT, "edit", null, null, Link.LINK);
		link.setUserObject(row);
		row.setEditLink(link);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == addAppointmentLink) {
			doAddAppointment(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_SELECT.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doToggleParticipation(ureq, row);
			} else if (CMD_EDIT.equals(cmd)) {
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
		if (source == confirmParticipationCrtl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				Appointment appointment = (Appointment)confirmParticipationCrtl.getUserObject();
				doCreateParticipation(appointment);
				updateModel();
			}
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
	
	private void doToggleParticipation(UserRequest ureq, AppointmentRow row) {
		if (row.getParticipation() == null) {
			if (topic.isAutoConfirmation()) {
				doSelfConfirmParticipation(ureq, row.getAppointment());
			} else {
				doCreateParticipation(row.getAppointment());
			}
		} else {
			appointmentsService.deleteParticipation(row.getParticipation());
		}
		updateModel();
	}

	private void doSelfConfirmParticipation(UserRequest ureq, Appointment appointment) {
		String title = translate("confirm.participation.self.title");
		String text = translate("confirm.participation.self");
		confirmParticipationCrtl = activateYesNoDialog(ureq, title, text, confirmParticipationCrtl);
		confirmParticipationCrtl.setUserObject(appointment);
	}

	private void doCreateParticipation(Appointment appointment) {
		ParticipationResult participationResult = appointmentsService.createParticipation(appointment, getIdentity(),
				topic.isMultiParticipation(), topic.isAutoConfirmation());
		if (ParticipationResult.Status.ok != participationResult.getStatus()) {
			showWarning("participation.not.created");
		}
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
		appointmentDeleteCtrl = new AppointmentDeleteController(ureq, getWindowControl(), appointment);
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
		rebookCtrl = new RebookController(ureq, getWindowControl(), appointment);
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
		List<Component> cmps = new ArrayList<>(1);
		if (rowObject instanceof AppointmentRow) {
			AppointmentRow appointmentRow = (AppointmentRow)rowObject;
			if (appointmentRow.getDayEl() != null) {
				cmps.add(appointmentRow.getDayEl().getComponent());
			}
		}
		return cmps;
	}

}
