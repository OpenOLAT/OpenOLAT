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

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.date.DateElement;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemList;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.member.MemberSearchConfig;
import org.olat.course.member.MemberSearchController;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationResult;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.appointments.ui.AppointmentCreateController.AppointmentInputType;
import org.olat.modules.appointments.ui.AppointmentDataModel.AppointmentCols;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AppointmentListController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_FUTURE = "Future";
	private static final String TAB_ID_PARTICIPATED = "Participated";
	private static final String TAB_ID_FULLY_BOOKED = "FullyBooked";
	private static final String FILTER_KEY_PAST = "past";
	private static final String FILTER_KEY_FUTURE = "future";
	private static final String FILTER_KEY_PARTICIPATED = "participated";
	private static final String FILTER_KEY_OCCUPANCY_FREE_SEATS_AVAILABLE = "occupancy.free.seats.available";
	private static final String FILTER_KEY_OCCUPANCY_FULLY_BOOKED = "occupancy.fully.booked";
	private static final String CMD_MORE = "more";
	private static final String CMD_SHOW_PARTICIPANTS = "show.participants";
	private static final String CMD_SELECT = "select";
	private static final String CMD_ADD_USER = "add";
	private static final String CMD_REMOVE = "remove";
	private static final String CMD_EXPORT = "export";
	private static final String CMD_CONFIRM = "confirm";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_RECORDING = "recording";
	private static final long PARTICIPANTS_RENDER_LIMIT = 3;
	
	private FormLink backLink;
	private DropdownItem addAppointmentsDropdown;
	private FormLink addStartDurationAppointmentsLink;
	private FormLink addStartEndAppointmentsLink;
	private FormLink addRecurringAppointmentLink;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabFuture;
	private FlexiFiltersTab tabParticipated;
	private FlexiFiltersTab tabFullyBooked;
	private FlexiTableElement tableEl;
	private AppointmentDataModel dataModel;
	
	private CloseableModalController cmc;
	private TopicHeaderController headerCtrl;
	private DialogBoxController confirmParticipationCrtl;
	private FindingConfirmationController findingConfirmationCtrl;
	private AppointmentEditController appointmentEditCtrl;
	private AppointmentCreateController addAppointmentsCtrl;
	private MemberSearchController userSearchCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ParticipationRemoveController removeCtrl;
	private AppointmentDeleteController appointmentDeleteCtrl;
	private AppointmentSelectController appointmentSelectCtrl;

	protected Topic topic;
	protected final AppointmentsSecurityCallback secCallback;
	protected final List<Organizer> organizers;
	private final RepositoryEntry courseEntry;
	private final Set<Appointment> showAllParticipations = new HashSet<>();
	
	@Autowired
	protected AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	protected UserManager userManager;
	
	protected AppointmentListController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl, "appointments_list");
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.topic = topic;
		this.secCallback = secCallback;
		this.organizers = appointmentsService.getOrganizers(topic);
		this.courseEntry = topic.getEntry();
		initForm(ureq);
		updateModel();
	}
	
	protected abstract boolean canSelect();
	
	protected abstract boolean canEdit();
	
	protected abstract boolean canEmailToOrganizers();

	protected abstract boolean isParticipationVisible();
	
	protected abstract List<String> getFilters();
	
	protected abstract List<String> getDefaultFilters();
	
	protected abstract String getPersistedPreferencesId();
	
	protected abstract List<AppointmentRow> loadModel();
	
	protected void setAddAppointmentVisible(boolean visible) {
		if (addAppointmentsDropdown != null) {
			addAppointmentsDropdown.setVisible(visible);
		}
	}
	
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
		headerCtrl = new TopicHeaderController(ureq, getWindowControl(), topic, canEmailToOrganizers());
		listenTo(headerCtrl);
		flc.put("header", headerCtrl.getInitialComponent());
		
		// Buttons
		if (canEdit()) {
			FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
			topButtons.setRootForm(mainForm);
			formLayout.add("topButtons", topButtons);
			topButtons.setElementCssClass("o_button_group o_button_group_right");
			
			if (secCallback.canEditTopic(organizers)) {
				addAppointmentsDropdown = uifactory.addDropdownMenu("add.appointment", "add.appointment", topButtons, getTranslator());
				addAppointmentsDropdown.setOrientation(DropdownOrientation.right);
				
				addStartDurationAppointmentsLink = uifactory.addFormLink("add.appointment.start.duration", formLayout, Link.LINK);
				addAppointmentsDropdown.addElement(addStartDurationAppointmentsLink);
				addStartEndAppointmentsLink = uifactory.addFormLink("add.appointment.start.end", formLayout, Link.LINK);
				addAppointmentsDropdown.addElement(addStartEndAppointmentsLink);
				addRecurringAppointmentLink = uifactory.addFormLink("add.appointment.recurring", formLayout, Link.LINK);
				addAppointmentsDropdown.addElement(addRecurringAppointmentLink);
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
		DefaultFlexiColumnModel deadlineModel = new DefaultFlexiColumnModel(AppointmentCols.deadline);
		deadlineModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(deadlineModel);
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.participants));
		if (appointmentsService.isBigBlueButtonEnabled()) {
			DefaultFlexiColumnModel recordingsModel = new DefaultFlexiColumnModel(AppointmentCols.recordings);
			recordingsModel.setExportable(false);
			columnsModel.addFlexiColumnModel(recordingsModel);
		}
		if (canSelect()) {
			DefaultFlexiColumnModel selectModel = new DefaultFlexiColumnModel(AppointmentCols.select);
			selectModel.setExportable(false);
			columnsModel.addFlexiColumnModel(selectModel);
		}
		if (canEdit()) {
			DefaultFlexiColumnModel confirmModel = new DefaultFlexiColumnModel(AppointmentCols.confirm);
			confirmModel.setExportable(false);
			columnsModel.addFlexiColumnModel(confirmModel);
			DefaultFlexiColumnModel commandsModel = new DefaultFlexiColumnModel(AppointmentCols.commands);
			commandsModel.setIconHeader(FlexiTableDataModelFactory.getColumnModelMoreMenuIconCSS());
			commandsModel.setExportable(false);
			columnsModel.addFlexiColumnModel(commandsModel);
		}
		
		dataModel = new AppointmentDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, getPersistedPreferencesId());
		tableEl.setEmptyTableMessageKey("table.empty.appointments");

		tableEl.setElementCssClass("o_appointments o_list");
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("appointment_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		initFilters();
		initFilterTabs(ureq);
		initSorters();
	}
	
	private void initFilters() {
		List<String> filterConfig = getFilters();
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		if (filterConfig.contains(AppointmentDataModel.FILTER_FUTURE)) {
			SelectionValues futureValues = new SelectionValues();
			futureValues.add(SelectionValues.entry(FILTER_KEY_PAST, translate("filter.past")));
			futureValues.add(SelectionValues.entry(FILTER_KEY_FUTURE, translate("filter.future")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.future"), AppointmentDataModel.FILTER_FUTURE, futureValues, true));
		}
		
		if (filterConfig.contains(AppointmentDataModel.FILTER_STATUS)) {
			SelectionValues values = new SelectionValues();
			values.add(SelectionValues.entry(Appointment.Status.planned.name(), translate("appointment.status.planned")));
			values.add(SelectionValues.entry(Appointment.Status.confirmed.name(), translate("appointment.status.confirmed")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("appointment.status"), AppointmentDataModel.FILTER_STATUS, values, true));
		}
		
		if (filterConfig.contains(AppointmentDataModel.FILTER_PARTICIPATED)) {
			SelectionValues participatedValues = new SelectionValues();
			participatedValues.add(SelectionValues.entry(FILTER_KEY_PARTICIPATED, translate("filter.participated")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.participated"), AppointmentDataModel.FILTER_PARTICIPATED, participatedValues, true));
		}
		
		if (filterConfig.contains(AppointmentDataModel.FILTER_OCCUPANCY_STATUS) && Type.finding != topic.getType()) {
			SelectionValues occupancyValues = new SelectionValues();
			occupancyValues.add(SelectionValues.entry(FILTER_KEY_OCCUPANCY_FREE_SEATS_AVAILABLE, translate("filter.free.seats.available")));
			occupancyValues.add(SelectionValues.entry(FILTER_KEY_OCCUPANCY_FULLY_BOOKED, translate("filter.fully.booked")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.occupancy.status"), AppointmentDataModel.FILTER_OCCUPANCY_STATUS, occupancyValues, true));
		}
		
		if (!filters.isEmpty()) {
			tableEl.setFilters(true, filters, false, false);
		}
	}
	
	private void initFilterTabs(UserRequest ureq) {
		List<String> filterConfig = getFilters();
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		if (filterConfig.contains(AppointmentDataModel.FILTER_FUTURE)) {
			tabFuture = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_FUTURE,
					translate("filter.future"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(AppointmentDataModel.FILTER_FUTURE, FILTER_KEY_FUTURE)));
			tabs.add(tabFuture);
		}
		
		if (filterConfig.contains(AppointmentDataModel.FILTER_PARTICIPATED)) {
			tabParticipated = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_PARTICIPATED,
					translate("filter.participated"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(AppointmentDataModel.FILTER_PARTICIPATED, FILTER_KEY_PARTICIPATED)));
			tabs.add(tabParticipated);
		}
		
		if (filterConfig.contains(AppointmentDataModel.FILTER_OCCUPANCY_STATUS) && Type.finding != topic.getType()) {
			tabFullyBooked = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_FULLY_BOOKED,
					translate("filter.fully.booked"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(AppointmentDataModel.FILTER_OCCUPANCY_STATUS, FILTER_KEY_OCCUPANCY_FULLY_BOOKED))
			);
			tabs.add(tabFullyBooked);
		}
		
		tableEl.setFilterTabs(true, tabs);
		if (filterConfig.contains(AppointmentDataModel.FILTER_FUTURE)) {
			tableEl.setSelectedFilterTab(ureq, tabFuture);
		} else {
			tableEl.setSelectedFilterTab(ureq, tabAll);
		}
	}	
	private void initSorters() {
		List<FlexiTableSort> sorters = new ArrayList<>(2);
		sorters.add(new FlexiTableSort(translate(AppointmentCols.start.i18nHeaderKey()), AppointmentCols.start.name()));
		sorters.add(new FlexiTableSort(translate(AppointmentCols.participants.i18nHeaderKey()), AppointmentCols.participants.name()));
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(AppointmentCols.start.name(), true));
		tableEl.setSortSettings(options);
	}

	private void updateModel() {
		List<AppointmentRow> rows = loadModel();
		applyFilter(rows);
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	private void applyFilter(List<AppointmentRow> rows) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		for (FlexiTableFilter filter : filters) {
			if (AppointmentDataModel.FILTER_STATUS.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && values.size() == 1) {
					Status status = Appointment.Status.valueOf(values.get(0));
					rows.removeIf(row -> status != row.getAppointment().getStatus());
				}
			}
			
			if (AppointmentDataModel.FILTER_FUTURE.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && values.size() == 1) {
					if (values.get(0).equalsIgnoreCase(FILTER_KEY_FUTURE)) {
						Date now = new Date();
						rows.removeIf(row -> !AppointmentsUIFactory.isEndInFuture(row.getAppointment(), now));
						
					} else if (values.get(0).equalsIgnoreCase(FILTER_KEY_PAST)) {
						Date now = new Date();
						rows.removeIf(row -> !AppointmentsUIFactory.isEndInPast(row.getAppointment(), now));
					}
				}
			}
			
			if (AppointmentDataModel.FILTER_PARTICIPATED.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_KEY_PARTICIPATED)) {
					rows.removeIf(row -> row.getParticipation() == null);
				}
			}
			
			if (AppointmentDataModel.FILTER_OCCUPANCY_STATUS.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && values.size() == 1) {
					if (values.get(0).equalsIgnoreCase(FILTER_KEY_OCCUPANCY_FREE_SEATS_AVAILABLE)) {
						rows.removeIf(AppointmentRow::isFullyBooked);
					} else if (values.get(0).equalsIgnoreCase(FILTER_KEY_OCCUPANCY_FULLY_BOOKED)) {
						rows.removeIf(row -> !row.isFullyBooked());
					}
				}
			}
		}
	}

	protected void forgeAppointmentView(AppointmentRow row, Appointment appointment, boolean selected) {
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
		forgeEnrollmentDeadline(row, appointment, selected);
		row.setLocation(AppointmentsUIFactory.getDisplayLocation(getTranslator(), appointment));
		row.setDetails(appointment.getDetails());
		row.setEscapedDetails(StringHelper.escapeHtml(row.getDetails()));
		forgeDayElement(row, appointment.getStart());
	}

	private void forgeEnrollmentDeadline(AppointmentRow row, Appointment appointment, boolean selected) {
		if (!appointment.isUseEnrollmentDeadline()) {
			return;
		}
		if (!canSelect()) {
			return;
		}
		if (selected) {
			return;
		}
		Long enrollmentDeadlineMinutes = appointment.getEnrollmentDeadlineMinutes();
		if (enrollmentDeadlineMinutes == null) {
			return;
		}
		Date enrollmentDeadline = DateUtils.addMinutes(appointment.getStart(), -enrollmentDeadlineMinutes.intValue());
		Date now  = new Date();
		boolean pastDeadline = now.after(enrollmentDeadline);
		if (DateUtils.isSameDay(enrollmentDeadline, new Date())) {
			String timeShort = Formatter.getInstance(getLocale()).formatTimeShort(enrollmentDeadline);
			row.setEnrollmentDeadline(translate("appointment.select.until.today", timeShort));
			row.setEnrollmentDeadlineCSS(pastDeadline ? "o_past_deadline" : "o_today");
			return;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrow = cal.getTime();
		if (DateUtils.isSameDay(enrollmentDeadline, tomorrow)) {
			String timeShort = Formatter.getInstance(getLocale()).formatTimeShort(enrollmentDeadline);
			row.setEnrollmentDeadline(translate("appointment.select.until.tomorrow", timeShort));
			row.setEnrollmentDeadlineCSS("o_after_today");
			return;
		}
		
		String dateTime = Formatter.getInstance(getLocale()).formatDateAndTime(enrollmentDeadline);
		row.setEnrollmentDeadline(translate("appointment.select.until", dateTime));
		row.setEnrollmentDeadlineCSS(pastDeadline ? "o_past_deadline" : "o_after_today");
	}

	protected void forgeDayElement(AppointmentRow row, Date date) {
		DateElement dayEl = DateComponentFactory.createDateElementWithYear("day_" + row.getKey(), date);
		row.setDayEl(dayEl);
	}

	protected void forgeParticipants(AppointmentRow row, List<Participation> participations) {
		row.setParticipations(participations);
		
		long limit = showAllParticipations.contains(row.getAppointment())? Long.MAX_VALUE: PARTICIPANTS_RENDER_LIMIT;
		List<String> participants = participations.stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.limit(limit)
				.collect(Collectors.toList());
		row.setParticipants(participants);
		
		String numOfParticipations = "<span class=\"o_nowrap\">" + translate("appointment.participations", String.valueOf(participations.size())) + "</span>";
		if (isParticipationVisible() && !participants.isEmpty()) {
			FormLink participationsEl = uifactory.addFormLink("participations_" + row.getKey(), CMD_SHOW_PARTICIPANTS,
					numOfParticipations, null, null, Link.NONTRANSLATED);
			participationsEl.setUserObject(row);
			row.setParticipationsEl(participationsEl);
		} else {
			StaticTextElement participationsEl = uifactory.addStaticTextElement("participations_" + row.getKey(), null,
					numOfParticipations, null);
			participationsEl.setStaticFormElement(false);
			row.setParticipationsEl(participationsEl);
		}
		
		if (participations.size() > PARTICIPANTS_RENDER_LIMIT) {
			String name = "more_" + row.getKey();
			Link showMoreLink = LinkFactory.createCustomLink(name, CMD_MORE, "", Link.LINK + Link.NONTRANSLATED, null, this);
			
			long hiddenParticipations = participations.size() - PARTICIPANTS_RENDER_LIMIT;
			String displayText = showAllParticipations.contains(row.getAppointment())
					? translate("show.less")
					: translate("show.more", new String[] { String.valueOf(hiddenParticipations)} );
			showMoreLink.setCustomDisplayText(displayText);
			showMoreLink.setUserObject(row);
			row.setShowMoreLink(showMoreLink);
			flc.getFormItemComponent().put(name, showMoreLink);
		}
	}
	
	protected void forgeSelectionLink(AppointmentRow row, boolean selected) {
		String i18n = selected? "appointment.selected": "appointment.select";
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, i18n, null, null, Link.BUTTON);
		link.setUserObject(row);
		link.setElementCssClass("o_sel_appointment_select");
		if (!selected) {
			link.setIconLeftCSS("o_icon o_icon_lg o_icon_selected");
			link.getComponent().setElementCssClass("btn-primary o_sel_appointment_select");
		}
		row.setSelectLink(link);
	}
	
	protected void forgeConfirmLink(AppointmentRow row, boolean confirmable) {
		String i18nKey = confirmable? "confirm": "unconfirm";
		FormLink link = uifactory.addFormLink("confirm_" + row.getKey(), CMD_CONFIRM, i18nKey, null, null, Link.BUTTON);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon_lg o_icon_selected");
		if (confirmable) {
			link.setElementCssClass("o_button_confirm");
		}
		row.setConfirmLink(link);
	}
	
	private DropdownItem getOrCreateCommandDroppdown(AppointmentRow row) {
		DropdownItem dropdown = row.getCommandDropdown();
		if (dropdown == null) {
			dropdown = uifactory.addDropdownMenuMore("cmd_" + row.getKey(), null, getTranslator());
			dropdown.setUserObject(row);
			row.setCommandDropdown(dropdown);
		}
		return dropdown;
	}
	
	protected void forgeAddUserLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("add_" + row.getKey(), CMD_ADD_USER, "add.user", null, flc, Link.LINK);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		getOrCreateCommandDroppdown(row).addElement(link);
	}

	protected void forgeRemoveUserLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("remove_" + row.getKey(), CMD_REMOVE, "remove.user", null, flc, Link.LINK);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_remove_member");
		link.setUserObject(row);
		getOrCreateCommandDroppdown(row).addElement(link);
	}
	
	protected void forgeExportUserLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("export_" + row.getKey(), CMD_EXPORT, "export.participations", null, flc, Link.LINK);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		getOrCreateCommandDroppdown(row).addElement(link);
	}
	
	protected void forgeDeleteLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("delete_" + row.getKey(), CMD_DELETE, "delete", null, flc, Link.LINK);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
		getOrCreateCommandDroppdown(row).addElement(link);
	}
	
	protected void forgeEditLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("edit_" + row.getKey(), CMD_EDIT, "edit.appointment", null, flc, Link.LINK);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		link.setUserObject(row);
		getOrCreateCommandDroppdown(row).addElement(link);
	}

	protected void forgeRecordingReferencesLinks(AppointmentRow row, List<BigBlueButtonRecordingReference> recordingReferences) {
		if (recordingReferences.isEmpty()) return;
		
		recordingReferences.sort((r1, r2) -> r1.getStartDate().compareTo(r2.getStartDate()));
		FormItemList recordingLinks = new FormItemList(recordingReferences.size());
		for (int i = 0; i < recordingReferences.size(); i++) {
			BigBlueButtonRecordingReference recording = recordingReferences.get(i);
			FormLink link = uifactory.addFormLink("rec_" + recording.getRecordingId(), CMD_RECORDING, null, null, flc, Link.NONTRANSLATED);
			String name = translate("recording");
			if (recordingReferences.size() > 1) {
				name = name + " " + (i+1);
			}
			name = name + "  ";
			link.setI18nKey(name);
			link.setIconLeftCSS("o_icon o_icon_lg o_vc_icon");
			link.setNewWindow(true, true, false);
			link.setUserObject(recording);
			recordingLinks.add(link);
		}
		row.setRecordingLinks(recordingLinks);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if (CMD_MORE.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doToggleShowMoreParticipations(row);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == addStartDurationAppointmentsLink) {
			doAddStartDurationAppointments(ureq);
		} else if (source == addStartEndAppointmentsLink) {
			doAddStartEndAppointments(ureq);
		} else if (source == addRecurringAppointmentLink) {
			doAddRecurringAppointment(ureq);
		} else if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				updateModel();
			} else if (event instanceof FlexiTableFilterTabEvent) {
				updateModel();
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_SELECT.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doToggleParticipation(ureq, row);
			} else if (CMD_EDIT.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doEditAppointment(ureq, row.getAppointment());
			} else if (CMD_DELETE.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doConfirmDeletion(ureq, row.getAppointment());
			} else if (CMD_CONFIRM.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doConfirm(ureq, row.getAppointment());
			} else if (CMD_SHOW_PARTICIPANTS.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doOpenParticipations(ureq, row, link);
			} else if (CMD_ADD_USER.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doSelectUser(ureq, row.getAppointment());
			} else if (CMD_REMOVE.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doRemove(ureq, row.getAppointment());
			} else if (CMD_EXPORT.equals(cmd) && link.getUserObject() instanceof AppointmentRow row) {
				doExportParticipations(ureq, row.getAppointment());
			} else if (CMD_RECORDING.equals(cmd) && link.getUserObject() instanceof BigBlueButtonRecordingReference) {
				BigBlueButtonRecordingReference recordingReference = (BigBlueButtonRecordingReference)link.getUserObject();
				doOpenRecording(ureq, recordingReference);
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmParticipationCrtl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				Appointment appointment = (Appointment)confirmParticipationCrtl.getUserObject();
				doCreateParticipation(ureq, appointment);
				updateModel();
			}
		} else if (findingConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (addAppointmentsCtrl == source) {
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
		} else if (appointmentSelectCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doCreateParticipation(appointmentSelectCtrl.getAppointment(), appointmentSelectCtrl.getComment());
				updateModel();
			} else if (event == Event.CANCELLED_EVENT) {
				doCreateParticipation(appointmentSelectCtrl.getAppointment(), null);
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (userSearchCtrl == source) {
			Appointment appointment = (Appointment)userSearchCtrl.getUserObject();
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddUser(appointment, toAdd);
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddUser(appointment, multiEvent.getChosenIdentities());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (removeCtrl == source) {
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
		removeAsListenerAndDispose(findingConfirmationCtrl);
		removeAsListenerAndDispose(appointmentDeleteCtrl);
		removeAsListenerAndDispose(appointmentSelectCtrl);
		removeAsListenerAndDispose(addAppointmentsCtrl);
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(removeCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		findingConfirmationCtrl = null;
		appointmentDeleteCtrl = null;
		appointmentSelectCtrl = null;
		addAppointmentsCtrl = null;
		appointmentEditCtrl = null;
		userSearchCtrl = null;
		removeCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doToggleShowMoreParticipations(AppointmentRow row) {
		Appointment appointment = row.getAppointment();
		if (showAllParticipations.contains(appointment)) {
			showAllParticipations.remove(appointment);
		} else {
			showAllParticipations.add(appointment);
		}
		updateModel();
	}
	
	private void doToggleParticipation(UserRequest ureq, AppointmentRow row) {
		if (row.getParticipation() == null) {
			if (topic.isAutoConfirmation()) {
				doSelfConfirmParticipation(ureq, row);
			} else {
				doCreateParticipation(ureq, row.getAppointment());
			}
		} else {
			appointmentsService.deleteParticipation(row.getParticipation());
		}
		updateModel();
	}

	private void doSelfConfirmParticipation(UserRequest ureq, AppointmentRow row) {
		String formatedDate;
		if (StringHelper.containsNonWhitespace(row.getTime())) {
			formatedDate = row.getDate() + ", " + row.getTime();
		} else if (StringHelper.containsNonWhitespace(row.getDateLong())) {
			formatedDate = row.getDateLong();
		} else {
			formatedDate = row.getDate() + ", " + translate("full.day.lower");
		}
		
		String title = translate("confirm.participation.self.title");
		String text = topic.isMultiParticipation()
				? translate("confirm.participation.self.multi", new String[] { formatedDate })
				: translate("confirm.participation.self", new String[] { formatedDate });
		confirmParticipationCrtl = activateYesNoDialog(ureq, title, text, confirmParticipationCrtl);
		confirmParticipationCrtl.setUserObject(row.getAppointment());
	}

	private void doCreateParticipation(UserRequest ureq, Appointment appointment) {
		if (secCallback.isParticipantCanComment()) {
			doShowCommentDialog(ureq, appointment);
		} else {
			doCreateParticipation(appointment, null);
		}
	}

	private void doCreateParticipation(Appointment appointment, String comment) {
		ParticipationResult participationResult = appointmentsService.createParticipations(appointment,
				singletonList(getIdentity()), getIdentity(), topic.isMultiParticipation(), topic.isAutoConfirmation(),
				true, secCallback.isSendParticipationNotificationToOrganizers(), comment);
		if (ParticipationResult.Status.ok != participationResult.getStatus()) {
			showWarning("participation.not.created");
		}
	}
	
	private void doShowCommentDialog(UserRequest ureq, Appointment appointment) {
		String title = translate("comment.for", appointment.getTopic().getTitle());
		appointmentSelectCtrl = new AppointmentSelectController(ureq, getWindowControl(), appointment);
		listenTo(appointmentSelectCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				appointmentSelectCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddStartDurationAppointments(UserRequest ureq) {
		addAppointmentsCtrl = new AppointmentCreateController(ureq, getWindowControl(), topic, AppointmentInputType.startDuration);
		listenTo(addAppointmentsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addAppointmentsCtrl.getInitialComponent(), true,
				translate("add.appointment.individual"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddStartEndAppointments(UserRequest ureq) {
		addAppointmentsCtrl = new AppointmentCreateController(ureq, getWindowControl(), topic, AppointmentInputType.startEnd);
		listenTo(addAppointmentsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addAppointmentsCtrl.getInitialComponent(), true,
				translate("add.appointment.individual"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddRecurringAppointment(UserRequest ureq) {
		addAppointmentsCtrl = new AppointmentCreateController(ureq, getWindowControl(), topic, AppointmentInputType.recurring);
		listenTo(addAppointmentsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addAppointmentsCtrl.getInitialComponent(), true,
				translate("add.appointment.recurring"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditAppointment(UserRequest ureq, Appointment appointment) {
		appointmentEditCtrl = new AppointmentEditController(ureq, getWindowControl(), appointment);
		listenTo(appointmentEditCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentEditCtrl.getInitialComponent(), true,
				translate("edit.appointment.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeletion(UserRequest ureq, Appointment appointment) {
		appointmentDeleteCtrl = new AppointmentDeleteController(ureq, getWindowControl(), appointment);
		listenTo(appointmentDeleteCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentDeleteCtrl.getInitialComponent(),
				true, translate("confirm.appointment.delete.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirm(UserRequest ureq, Appointment appointment) {
		if (Status.planned == appointment.getStatus()) {
			if (Type.finding == topic.getType()) {
				doConfirmFinding(ureq, appointment);
			} else {
				appointmentsService.confirmAppointment(appointment);
			}
		} else {
			appointmentsService.unconfirmAppointment(appointment);
		}
		updateModel();
	}

	private void doConfirmFinding(UserRequest ureq, Appointment appointment) {
		findingConfirmationCtrl = new FindingConfirmationController(ureq, getWindowControl(), appointment, 
				secCallback.isSendParticipationNotificationToOrganizers());
		listenTo(findingConfirmationCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), findingConfirmationCtrl.getInitialComponent(), true,
				translate("edit.appointment.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doSelectUser(UserRequest ureq, Appointment appointment) {
		RepositoryEntry entry = repositoryService.loadBy(courseEntry);
		MemberSearchConfig config = MemberSearchConfig.defaultConfig(entry, secCallback.searchMemberAs(),
				"appointment-list-identitity-v1.0");
		userSearchCtrl = new MemberSearchController(ureq, getWindowControl(), config);
		userSearchCtrl.setUserObject(appointment);
		listenTo(userSearchCtrl);
		
		String title = translate("add.user.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddUser(Appointment appointment, List<Identity> identities) {
		ParticipationResult result = appointmentsService.createParticipations(appointment, identities, getIdentity(),
				topic.isMultiParticipation(), topic.isAutoConfirmation(), false, secCallback.isSendParticipationNotificationToOrganizers(), null);
		if (ParticipationResult.Status.appointmentFull == result.getStatus()) {
			showWarning("error.not.as.many.participations.left");
		} else if (ParticipationResult.Status.ok != result.getStatus()) {
			showWarning("participations.not.created");
		}
		updateModel();
	}
	
	private void doRemove(UserRequest ureq, Appointment appointment) {
		removeCtrl = new ParticipationRemoveController(ureq, getWindowControl(), appointment);
		listenTo(removeCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeCtrl.getInitialComponent(),
				true, translate("remove.user.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doExportParticipations(UserRequest ureq, Appointment appointment) {
		ParticipationSearchParams searchParams = new ParticipationSearchParams();
		searchParams.setAppointment(appointment);
		ExcelExport export = new ExcelExport(ureq, searchParams, getExportName(topic));
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
	}

	private String getExportName(Topic topic) {
		return new StringBuilder()
				.append(translate("export.participations.file.prefix"))
				.append("_")
				.append(topic.getTitle())
				.append("_")
				.append(Formatter.formatDatetimeFilesystemSave(new Date()))
				.toString();
	}

	private void doOpenRecording(UserRequest ureq, BigBlueButtonRecordingReference recordingReference) {
		String url = appointmentsService.getBBBRecordingUrl(ureq.getUserSession(), recordingReference);
		if(StringHelper.containsNonWhitespace(url)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.recording.not.found");
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(1);
		if (rowObject instanceof AppointmentRow) {
			AppointmentRow appointmentRow = (AppointmentRow)rowObject;
			if (appointmentRow.getDayEl() != null) {
				cmps.add(appointmentRow.getDayEl().getComponent());
			}
			if (appointmentRow.getShowMoreLink() != null) {
				cmps.add(appointmentRow.getShowMoreLink());
			}
		}
		return cmps;
	}
	
	private void doOpenParticipations(UserRequest ureq, AppointmentRow row, FormLink link) {
		removeAsListenerAndDispose(calloutCtrl);
		
		List<String> participants = row.getParticipations().stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
		
		VelocityContainer participationsCont = createVelocityContainer("appointment_participations");
		participationsCont.contextPut("participants", participants);
		
		String title = translate("appointment.participations", String.valueOf(participants.size()));
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				participationsCont, link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
}
