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
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.ConfirmUpdateController;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIMoveEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIResizeEvent;
import org.olat.commons.calendar.ui.events.CalendarGUISelectEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.commons.services.tag.ui.component.FlexiTableTagFilter;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
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
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjAppointmentRef;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjCalendarFilter;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneRef;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjAppointmentDeleteConfirmationController.Cascade;
import org.olat.modules.project.ui.ProjCalendarDataModel.CalendarCols;
import org.olat.modules.project.ui.ProjNoteDataModel.NoteCols;
import org.olat.modules.project.ui.component.ProjAvatarComponent;
import org.olat.modules.project.ui.component.ProjAvatarComponent.Size;
import org.olat.modules.project.ui.event.AppointmentDeleteEvent;
import org.olat.modules.project.ui.event.AppointmentEditEvent;
import org.olat.modules.project.ui.event.MilestoneDeleteEvent;
import org.olat.modules.project.ui.event.MilestoneEditEvent;
import org.olat.modules.project.ui.event.MilestoneStatusEvent;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjCalendarAllController extends FormBasicController implements Activateable2 {
	
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_RECENTLY = "Recently";
	private static final String TAB_ID_NEW = "New";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String CMD_SELECT = "select";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private FormLink appointmentCreateLink;
	private FormLink milestoneCreateLink;
	private FormLink calLink;
	private FormLink tableLink;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRecently;
	private FlexiFiltersTab tabNew;
	private FlexiFiltersTab tabDeleted;
	private FullCalendarElement calendarEl;
	private FlexiTableElement tableEl;
	private ProjCalendarDataModel dataModel;
	
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtr;
	private ProjAppointmentEditController appointmentEditCtrl;
	private ProjAppointmentPreviewController appointmentPreviewCtrl;
	private ConfirmUpdateController appointmentEditAllCtr;
	private ConfirmUpdateController appointmentMoveAllCtr;
	private ProjAppointmentDeleteConfirmationController appointmentDeleteConfirmationCtrl;
	private ProjMilestoneEditController milestoneEditCtrl;
	private ProjMilestonePreviewController milestonePreviewCtrl;
	private ConfirmationController milestoneDeleteConfirmationCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private final ProjectBCFactory bcFactory;
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final Date lastVisitDate;
	private final MapperKey avatarMapperKey;
	private final String avatarUrl;
	private final Formatter formatter;
	private String appointmentReadWriteKalendarId;
	private String milestoneKalendarId;
	private Boolean calVisible = Boolean.TRUE;
	private int counter = 0;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private UserManager userManager;
	
	
	public ProjCalendarAllController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate,
			MapperKey avatarMapperKey) {
		super(ureq, wControl, "calendar_all");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.bcFactory = bcFactory;
		this.project = project;
		this.secCallback = secCallback;
		this.lastVisitDate = lastVisitDate;
		this.avatarMapperKey = avatarMapperKey;
		ProjProjectImageMapper projectImageMapper = new ProjProjectImageMapper(projectService);
		String projectMapperUrl = registerCacheableMapper(ureq, ProjProjectImageMapper.DEFAULT_ID, projectImageMapper,
				ProjProjectImageMapper.DEFAULT_EXPIRATION_TIME);
		this.avatarUrl = projectImageMapper.getImageUrl(projectMapperUrl, project, ProjProjectImageType.avatar);
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
		updateUI();
		loadModel(ureq, false);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("avatar", new ComponentWrapperElement(new ProjAvatarComponent("avatar", project, avatarUrl, Size.medium, false)));
		
		appointmentCreateLink = uifactory.addFormLink("appointment.create", formLayout, Link.BUTTON);
		appointmentCreateLink.setIconLeftCSS("o_icon o_icon_add");
		appointmentCreateLink.setVisible(secCallback.canCreateAppointments());
		
		milestoneCreateLink = uifactory.addFormLink("milestone.create", formLayout, Link.BUTTON);
		milestoneCreateLink.setIconLeftCSS("o_icon o_icon_add");
		milestoneCreateLink.setVisible(secCallback.canCreateMilestones());
		
		calLink = uifactory.addFormLink("cal.show", null, null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		calLink.setI18nKey("");
		calLink.setIconLeftCSS("o_icon o_icon o_icon_calendar");
		calLink.setAriaLabel(translate("calendar.show.calendar"));
		
		tableLink = uifactory.addFormLink("table.show", null, null, null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		tableLink.setI18nKey("");
		tableLink.setIconLeftCSS("o_icon o_icon o_icon_table");
		tableLink.setAriaLabel(translate("calendar.show.table"));
		
		calendarEl = new FullCalendarElement(ureq, "calendar", List.of(), getTranslator());
		formLayout.add("calendar", calendarEl);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CalendarCols.id));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.startDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.endDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.tags, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.involved));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CalendarCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.lastModifiedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CalendarCols.lastModifiedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CalendarCols.deletedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CalendarCols.deletedBy));
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CalendarCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		dataModel = new ProjCalendarDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "project-calendar-all");
		
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		filters.add(new FlexiTableTextFilter(translate("title"), ProjCalendarFilter.title.name(), true));
		
		if (secCallback.canViewAppointments() && secCallback.canViewMilestones()) {
			SelectionValues typeValues = new SelectionValues();
			typeValues.add(SelectionValues.entry(ProjAppointment.TYPE, translate("appointment")));
			typeValues.add(SelectionValues.entry(ProjMilestone.TYPE, translate("milestone")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("calendar.type"), ProjCalendarFilter.type.name(), typeValues, true));
		}
		
		filters.add(new FlexiTableDateRangeFilter(translate("filter.date.range"),
				ProjCalendarFilter.daterange.name(), true, false, translate("filter.date.range.from"),
				translate("filter.date.range.to"), getLocale()));
		
		List<TagInfo> tagInfos = projectService.getTagInfos(project, null);
		if (!tagInfos.isEmpty()) {
			filters.add(new FlexiTableTagFilter(translate("tags"), ProjCalendarFilter.tag.name(), tagInfos, true));
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(ProjectStatus.active.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.active)));
		statusValues.add(SelectionValues.entry(ProjectStatus.deleted.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.deleted)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("status"), ProjCalendarFilter.status.name(), statusValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(4);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjCalendarFilter.status, ProjectStatus.active.name())));
		tabs.add(tabAll);
		
		tabRecently = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RECENTLY,
				translate("tab.recently"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjCalendarFilter.status, ProjectStatus.active.name())));
		tabs.add(tabRecently);
		
		tabNew = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_NEW,
				translate("tab.new"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjCalendarFilter.status, ProjectStatus.active.name())));
		tabs.add(tabNew);
		
		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjCalendarFilter.status, ProjectStatus.deleted.name())));
		tabs.add(tabDeleted);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		doSelectFilterTab(tab);
		loadModel(ureq, true);
	}
	
	private void doSelectFilterTab(FlexiFiltersTab tab) {
		if (secCallback.canCreateNotes() && (tabDeleted == null || tabDeleted != tab)) {
			tableEl.setEmptyTableSettings("table.search.empty", null, "o_icon_proj_appointment", "appointment.create", "o_icon_add", false);
		} else {
			tableEl.setEmptyTableSettings("table.search.empty", null, "o_icon_proj_appointment");
		}
	}

	private void loadModel(UserRequest ureq, boolean sort) {
		List<KalendarRenderWrapper> calendarWrappers = calVisible? new ArrayList<>(3): List.of();
		List<ProjCalendarRow> rows = !calVisible? new ArrayList<>(): List.of();
		
		// Appointments
		if (secCallback.canViewAppointments()) {
			ProjAppointmentSearchParams appointmentSearchParams = new ProjAppointmentSearchParams();
			appointmentSearchParams.setProject(project);
			appointmentSearchParams.setDatesNull(Boolean.FALSE);
			if (calVisible) {
				appointmentSearchParams.setStatus(List.of(ProjectStatus.active));
			} else {
				applyFilters(appointmentSearchParams);
			}
			List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(appointmentSearchParams, ProjArtefactInfoParams.of(true, false, true));
			List<ProjAppointment> appointmentReadWrite = new ArrayList<>();
			List<ProjAppointment> appointmentReadOnly = new ArrayList<>();
			for (ProjAppointmentInfo appointmentInfo : appointmentInfos) {
				ProjAppointment appointment = appointmentInfo.getAppointment();
				if (secCallback.canEditAppointment(appointment)) {
					appointmentReadWrite.add(appointment);
				} else {
					appointmentReadOnly.add(appointment);
				}
			}
			
			Kalendar appointmentReadWriteKalendar = projectService.getAppointmentsKalendar(appointmentReadWrite);
			Kalendar appointmentReadOnlyKalendar = projectService.getAppointmentsKalendar(appointmentReadOnly);
			if (calVisible) {
				appointmentReadWriteKalendarId = appointmentReadWriteKalendar.getCalendarID();
				KalendarRenderWrapper appointmentReadWriteWrapper = new KalendarRenderWrapper(appointmentReadWriteKalendar,
						translate("appointment.calendar.name"), "project.appointments.rw" + project.getKey());
				appointmentReadWriteWrapper.setPrivateEventsVisible(true);
				appointmentReadWriteWrapper.setCssClass(ProjectUIFactory.COLOR_APPOINTMENT);
				appointmentReadWriteWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
				calendarWrappers.add(appointmentReadWriteWrapper);
				
				KalendarRenderWrapper appointmentReadOnlyWrapper = new KalendarRenderWrapper(appointmentReadOnlyKalendar,
						translate("appointment.calendar.name"), "project.appointments.ro" + project.getKey());
				appointmentReadOnlyWrapper.setPrivateEventsVisible(true);
				appointmentReadOnlyWrapper.setCssClass(ProjectUIFactory.COLOR_APPOINTMENT);
				appointmentReadOnlyWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
				calendarWrappers.add(appointmentReadOnlyWrapper);
			} else {
				Map<String, ProjAppointmentInfo> appointmentIdentToAppointment = appointmentInfos.stream()
						.collect(Collectors.toMap(info ->info.getAppointment().getIdentifier(), Function.identity()));
				
				List<KalendarEvent> readWriteEvents = calendarManager.getEvents(appointmentReadWriteKalendar,
						DateUtils.addYears(new Date(), -10), DateUtils.addYears(new Date(), 10), true);
				for (KalendarEvent event : readWriteEvents) {
					ProjCalendarRow row = createAppointmentRow(ureq, event, appointmentIdentToAppointment.get(event.getExternalId()), true);
					rows.add(row);
				}
				List<KalendarEvent> readOnlyEvents = calendarManager.getEvents(appointmentReadOnlyKalendar,
						DateUtils.addYears(new Date(), -10), DateUtils.addYears(new Date(), 10), true);
				for (KalendarEvent event : readOnlyEvents) {
					ProjCalendarRow row = createAppointmentRow(ureq, event, appointmentIdentToAppointment.get(event.getExternalId()), false);
					rows.add(row);
				}
				
				// Appointments without date
				appointmentSearchParams.setDatesNull(Boolean.TRUE);
				appointmentInfos = projectService.getAppointmentInfos(appointmentSearchParams, ProjArtefactInfoParams.ALL);
				
				for (ProjAppointmentInfo info : appointmentInfos) {
					ProjCalendarRow row = createAppointmentRow(ureq, null, info, secCallback.canEditAppointment(info.getAppointment()));
					rows.add(row);
				}
			}
		}
		
		// Milestones
		if (secCallback.canViewMilestones()) {
			ProjMilestoneSearchParams milestoneSearchParams = new ProjMilestoneSearchParams();
			milestoneSearchParams.setProject(project);
			if (calVisible) {
				milestoneSearchParams.setStatus(List.of(ProjectStatus.active));
			} else {
				applyFilters(milestoneSearchParams);
			}
			
			if (calVisible) {
				milestoneSearchParams.setDueDateNull(Boolean.FALSE);
				List<ProjMilestone> milestones = projectService.getMilestones(milestoneSearchParams);
				
				Kalendar milestoneKalendar = projectService.getMilestonesKalendar(milestones);
				milestoneKalendarId = milestoneKalendar.getCalendarID();
				KalendarRenderWrapper milestoneWrapper = new KalendarRenderWrapper(milestoneKalendar,
						translate("milestone.calendar.name"), "project.milestones." + project.getKey());
				milestoneWrapper.setPrivateEventsVisible(true);
				milestoneWrapper.setCssClass(ProjectUIFactory.COLOR_MILESTONE);
				int milestonesAccess = secCallback.canEditMilestones()
						? KalendarRenderWrapper.ACCESS_READ_WRITE
						: KalendarRenderWrapper.ACCESS_READ_ONLY;
				milestoneWrapper.setAccess(milestonesAccess);
				calendarWrappers.add(milestoneWrapper);
			} else {
				List<ProjMilestoneInfo> milestoneInfos = projectService.getMilestoneInfos(milestoneSearchParams, ProjArtefactInfoParams.of(true, false, true));
				for (ProjMilestoneInfo milestoneInfo : milestoneInfos) {
					ProjCalendarRow row = createMilestoneRow(ureq, milestoneInfo);
					rows.add(row);
				}
			}
		}
		
		if (calVisible) {
			calendarEl.setCalendars(calendarWrappers);
		} else {
			applyFilters(rows);
			if (sort) {
				sortTable();
			}
			dataModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
	
	private void applyFilters(ProjAppointmentSearchParams searchParams) {
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabNew) {
			searchParams.setCreatedAfter(lastVisitDate);
		} else {
			searchParams.setCreatedAfter(null);
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjCalendarFilter.status.name() == filter.getFilter()) {
				List<String> status = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ProjectStatus::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setStatus(null);
				}
			}
		}
	}
	
	private void applyFilters(ProjMilestoneSearchParams searchParams) {
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabNew) {
			searchParams.setCreatedAfter(lastVisitDate);
		} else {
			searchParams.setCreatedAfter(null);
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjCalendarFilter.status.name() == filter.getFilter()) {
				List<String> status = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ProjectStatus::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setStatus(null);
				}
			}
		}
	}
	
	private void applyFilters(List<ProjCalendarRow> rows) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjCalendarFilter.title.name().equals(filter.getFilter())) {
				String value = filter.getValue();
				if (StringHelper.containsNonWhitespace(value)) {
					String valueLowerCase = value.toLowerCase();
					rows.removeIf(row -> !row.getDisplayName().toLowerCase().contains(valueLowerCase));
				}
			}
			if (ProjCalendarFilter.type.name() == filter.getFilter()) {
				List<String> types = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (types != null && types.size() == 1) {
					String type = types.get(0);
					rows.removeIf(row -> !row.getType().equals(type));
				}
			}
			if (ProjCalendarFilter.daterange.name() == filter.getFilter()) {
				DateRange dateRange = ((FlexiTableDateRangeFilter)filter).getDateRange();
				if (dateRange != null) {
					Date filterStart = DateUtils.setTime(dateRange.getStart(), 0, 0, 0);
					if (filterStart != null) {
						rows.removeIf(row -> row.getStartDate() == null || !filterStart.before(row.getStartDate()));
					}
					Date filterEnd = DateUtils.setTime(dateRange.getEnd(), 23, 59, 59);
					if (filterEnd != null) {
						rows.removeIf(row -> row.getEndDate() == null || !filterEnd.after(row.getEndDate()));
					}
				}
			}
			if (ProjCalendarFilter.tag.name().equals(filter.getFilter())) {
				List<String> values = ((FlexiTableTagFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					Set<Long> selectedTagKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					rows.removeIf(row -> row.getTagKeys() == null || !row.getTagKeys().stream().anyMatch(key -> selectedTagKeys.contains(key)));
				}
			}
		}
	}
	
	private void sortTable() {
		if (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabRecently) {
			tableEl.sort(new SortKey(NoteCols.lastModifiedDate.name(), false));
		} else if (tableEl.getSelectedFilterTab() == tabAll || tableEl.getSelectedFilterTab() == tabDeleted) {
			tableEl.sort( new SortKey(NoteCols.displayName.name(), true));
		} else if (tableEl.getSelectedFilterTab() == tabNew) {
			tableEl.sort(new SortKey(NoteCols.creationDate.name(), false));
		}
	}
	
	private ProjCalendarRow createAppointmentRow(UserRequest ureq, KalendarEvent event, ProjAppointmentInfo info, boolean canEdit) {
		ProjAppointment appointment = info.getAppointment();
		ProjCalendarRow row = new ProjCalendarRow(appointment, event);
		
		row.setTranslatedType(translate("appointment"));
		row.setDisplayName(ProjectUIFactory.getDisplayName(getTranslator(), appointment));
		
		String modifiedDate = formatter.formatDateRelative(appointment.getArtefact().getContentModifiedDate());
		String modifiedBy = userManager.getUserDisplayName(appointment.getArtefact().getContentModifiedBy().getKey());
		row.setContentModifiedByName(modifiedBy);
		String modified = translate("date.by", modifiedDate, modifiedBy);
		row.setModified(modified);
		
		row.setTagKeys(info.getTags().stream().map(Tag::getKey).collect(Collectors.toSet()));
		row.setFormattedTags(TagUIFactory.getFormattedTags(getLocale(), info.getTags()));
		
		if (event != null) {
			if (event.isAllDayEvent()) {
				row.setStartDate(DateUtils.setTime(event.getBegin(), 0, 0, 0));
				row.setEndDate(DateUtils.setTime(event.getEnd(), 23, 59, 59));
			} else {
				row.setStartDate(event.getBegin());
				row.setEndDate(event.getEnd());
			}
		} else {
			if (appointment.isAllDay() && appointment.getStartDate() != null) {
				row.setStartDate(DateUtils.setTime(appointment.getStartDate(), 0, 0, 0));
				row.setEndDate(DateUtils.setTime(appointment.getStartDate(), 23, 59, 59));
			} else {
				row.setStartDate(appointment.getStartDate());
				row.setEndDate(appointment.getEndDate());
			}
		}
		
		if (!calVisible) {
			row.setMemberKeys(info.getMembers().stream().map(Identity::getKey).collect(Collectors.toSet()));
			row.setUserPortraits(createUsersPortraits(ureq, info.getMembers()));
		}
		
		if (canEdit) {
			forgeToolsLink(row);
		}
		
		return row;
	}
	
	private ProjCalendarRow createMilestoneRow(UserRequest ureq, ProjMilestoneInfo info) {
		ProjMilestone milestone = info.getMilestone();
		ProjCalendarRow row = new ProjCalendarRow(milestone);
		
		row.setTranslatedType(translate("milestone"));
		row.setDisplayName(ProjectUIFactory.getDisplayName(getTranslator(), milestone));
		
		String modifiedDate = formatter.formatDateRelative(milestone.getArtefact().getContentModifiedDate());
		String modifiedBy = userManager.getUserDisplayName(milestone.getArtefact().getContentModifiedBy().getKey());
		row.setContentModifiedByName(modifiedBy);
		String modified = translate("date.by", modifiedDate, modifiedBy);
		row.setModified(modified);
		
		if (row.getDeletedBy() != null) {
			row.setDeletedByName(userManager.getUserDisplayName(row.getDeletedBy().getKey()));
		}
		
		row.setTagKeys(info.getTags().stream().map(Tag::getKey).collect(Collectors.toSet()));
		row.setFormattedTags(TagUIFactory.getFormattedTags(getLocale(), info.getTags()));
		
		if (milestone.getDueDate() != null) {
			row.setEndDate(DateUtils.setTime(milestone.getDueDate(), 23, 59, 59));
		}
		
		if (!calVisible) {
			row.setMemberKeys(info.getMembers().stream().map(Identity::getKey).collect(Collectors.toSet()));
			row.setUserPortraits(createUsersPortraits(ureq, info.getMembers()));
		}
		
		if (secCallback.canEditMilestone(milestone)) {
			forgeToolsLink(row);
		}
		
		return row;
	}
	
	private UsersPortraitsComponent createUsersPortraits(UserRequest ureq, Set<Identity> members) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(new ArrayList<>(members));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + counter++, flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("involved"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(10);
		usersPortraitCmp.setUsers(portraitUsers);
		return usersPortraitCmp;
	}
	
	private void forgeToolsLink(ProjCalendarRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	private void updateUI() {
		calendarEl.setVisible(calVisible);
		tableEl.setVisible(!calVisible);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			calVisible = Boolean.FALSE;
			updateUI();
			ContextEntry entry = entries.get(0);
			String type = entry.getOLATResourceable().getResourceableTypeName();
			FlexiFiltersTab tab = tableEl.getFilterTabById(type);
			if (tab != null) {
				selectFilterTab(ureq, tab);
			} else {
				selectFilterTab(ureq, tabAll);
				if (ProjectBCFactory.TYPE_APPOINTMENT.equals(type) && secCallback.canViewAppointments()) {
					Long key = entry.getOLATResourceable().getResourceableId();
					activate(ureq, ProjAppointment.TYPE, key);
				} else if (ProjectBCFactory.TYPE_MILESTONE.equals(type) && secCallback.canViewMilestones()) {
					if (secCallback.canViewMilestones()) {
						Long key = entry.getOLATResourceable().getResourceableId();
						activate(ureq, ProjMilestone.TYPE, key);
					}
				}
			}
		}
	}
	
	private void activate(UserRequest ureq, String type, Long key) {
		ProjCalendarRow row = dataModel.getObjectByKey(type, key);
		if (row != null) {
			int index = dataModel.getObjects().indexOf(row);
			if (index >= 1 && tableEl.getPageSize() > 1) {
				int page = index / tableEl.getPageSize();
				tableEl.setPage(page);
			}
			doOpenPreview(ureq, row);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == appointmentPreviewCtrl) {
			if (event instanceof AppointmentEditEvent aEvent) {
				closeCalloutOrCmc();
				cleanUp();
				
				doEditAppointment(ureq, aEvent.getAppointment(), aEvent.getKalendarEvent());
			} else if (event instanceof AppointmentDeleteEvent aEvent) {
				closeCalloutOrCmc();
				cleanUp();
				
				doConfirmDeleteAppointment(ureq, aEvent.getAppointment(), aEvent.getKalendarEvent());
			} else if (event == Event.DONE_EVENT) {
				closeCalloutOrCmc();
				cleanUp();
			}
		} else if (source == appointmentEditAllCtr) {
			if (event instanceof CalendarGUIUpdateEvent calEvent) {
				KalendarRecurEvent kalendarEvent = appointmentEditAllCtr.getKalendarEvent();
				org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent.Cascade cascade = calEvent.getCascade();
				cmc.deactivate();
				cleanUp();
				doEditRecurringAppointment(ureq, kalendarEvent, cascade);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == appointmentMoveAllCtr) {
			if (event instanceof CalendarGUIUpdateEvent calEvent) {
				doMoveRecurringAppointment(ureq, appointmentMoveAllCtr.getKalendarEvent(),
						calEvent.getCascade(), appointmentMoveAllCtr.getDayDelta(),
						appointmentMoveAllCtr.getMinuteDelta(), appointmentMoveAllCtr.getChangeBegin());
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentDeleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				Object userObject = appointmentDeleteConfirmationCtrl.getUserObject();
				if (userObject instanceof KalendarEvent kalendarEvent)
				doDeleteAppointment(ureq, kalendarEvent, appointmentDeleteConfirmationCtrl.getCascade());
			}
			cmc.deactivate();
			cleanUp();
		} else if (milestoneEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == milestonePreviewCtrl) {
			if (event instanceof MilestoneEditEvent mEvent) {
				closeCalloutOrCmc();
				cleanUp();
				
				doEditMilestone(ureq, mEvent.getMilestone());
			} else if (event instanceof MilestoneStatusEvent mEvent) {
				closeCalloutOrCmc();
				cleanUp();
				
				doAcomplishMilestone(mEvent.getMilestone());
			} else if (event instanceof MilestoneDeleteEvent eEvent) {
				closeCalloutOrCmc();
				cleanUp();
				
				doConfirmDeleteMilestone(ureq, eEvent.getMilestone());
			} else if (event == Event.DONE_EVENT) {
				closeCalloutOrCmc();
				cleanUp();
			}
		} else if (milestoneDeleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				Object userObject = milestoneDeleteConfirmationCtrl.getUserObject();
				if (userObject instanceof ProjMilestoneRef milestone)
				doDeleteMilestone(ureq, milestone);
			}
			cmc.deactivate();
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if(cmc == source) {
			loadModel(ureq, false);
			cleanUp();
		} else if(calloutCtr == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void closeCalloutOrCmc() {
		if (calloutCtr != null) {
			calloutCtr.deactivate();
		}
		if (cmc != null) {
			cmc.deactivate();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appointmentDeleteConfirmationCtrl);
		removeAsListenerAndDispose(appointmentPreviewCtrl);
		removeAsListenerAndDispose(appointmentEditAllCtr);
		removeAsListenerAndDispose(appointmentMoveAllCtr);
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(milestoneDeleteConfirmationCtrl);
		removeAsListenerAndDispose(milestonePreviewCtrl);
		removeAsListenerAndDispose(milestoneEditCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		appointmentDeleteConfirmationCtrl = null;
		appointmentPreviewCtrl = null;
		appointmentEditAllCtr = null;
		appointmentMoveAllCtr = null;
		appointmentEditCtrl = null;
		milestoneDeleteConfirmationCtrl = null;
		milestonePreviewCtrl = null;
		milestoneEditCtrl = null;
		toolsCalloutCtrl = null;
		calloutCtr = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == appointmentCreateLink) {
			doCreateAppointment(ureq, new Date());
		} else if (source == milestoneCreateLink) {
			doCreateMilestone(ureq);
		} else if (source == calLink) {
			calVisible = Boolean.TRUE;
			updateUI();
			loadModel(ureq, false);
		} else if (source == tableLink) {
			calVisible = Boolean.FALSE;
			updateUI();
			loadModel(ureq, false);
		} else if (source == calendarEl) {
			if (event instanceof CalendarGUIAddEvent caEvent) {
				doCreateAppointment(ureq, DateUtils.copyTime(caEvent.getStartDate(), new Date()));
			} else if (event instanceof CalendarGUISelectEvent) {
				CalendarGUISelectEvent selectEvent = (CalendarGUISelectEvent)event;
				if (selectEvent.getKalendarEvent() != null) {
					doOpenPreviewCallout(ureq, selectEvent.getKalendarEvent(), selectEvent.getTargetDomId());
				}
			} else if (event instanceof CalendarGUIMoveEvent) {
				CalendarGUIMoveEvent moveEvent = (CalendarGUIMoveEvent)event;
				doMove(ureq, moveEvent.getKalendarEvent(), moveEvent.getDayDelta(),
							moveEvent.getMinuteDelta(), moveEvent.getAllDay(), true);
			} else if (event instanceof CalendarGUIResizeEvent) {
				CalendarGUIResizeEvent resizeEvent = (CalendarGUIResizeEvent)event;
				if (appointmentReadWriteKalendarId.equals(resizeEvent.getKalendarRenderWrapper().getKalendar().getCalendarID())) {
					doMoveAppointment(ureq, resizeEvent.getKalendarEvent(), 0l, resizeEvent.getMinuteDelta(),
							resizeEvent.getAllDay(), false);
				}
			}
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ProjCalendarRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doOpenPreview(ureq, row);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq, false);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				doSelectFilterTab(((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq, true);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreateAppointment(ureq, new Date());
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ProjCalendarRow row) {
				doOpenTools(ureq, row, link);
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenPreviewCallout(UserRequest ureq, KalendarEvent kalendarEvent, String targetDomId) {
		if (kalendarEvent.getCalendar() != null) {
			if (appointmentReadWriteKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doOpenPreviewAppointmentCallout(ureq, kalendarEvent, targetDomId);
			} else if (milestoneKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doOpenPreviewMilestoneCallout(ureq, kalendarEvent, targetDomId);
			}
		}
	}
	
	private void doOpenPreviewAppointmentCallout(UserRequest ureq, KalendarEvent kalendarEvent, String targetDomId) {
		if (calloutCtr != null && appointmentEditCtrl != null) return;
		
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(appointmentEditCtrl);
		
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setIdentifiers(List.of(kalendarEvent.getExternalId()));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams,
				ProjArtefactInfoParams.of(true, false, false));
		if (appointmentInfos.isEmpty()) {
			return;
		}
		ProjAppointmentInfo appointmentInfo = appointmentInfos.get(0);
		
		appointmentPreviewCtrl = new ProjAppointmentPreviewController(ureq, getWindowControl(), bcFactory, secCallback,
				appointmentInfo, kalendarEvent);
		listenTo(appointmentPreviewCtrl);
		
		calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(),
				appointmentPreviewCtrl.getInitialComponent(), targetDomId, null, true, "");
		listenTo(calloutCtr);
		calloutCtr.activate();
	}
	
	private void doOpenPreviewMilestoneCallout(UserRequest ureq, KalendarEvent kalendarEvent, String targetDomId) {
		if (calloutCtr != null && milestoneEditCtrl != null) return;
		
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(milestoneEditCtrl);
		
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setIdentifiers(List.of(kalendarEvent.getExternalId()));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjMilestoneInfo> milestonesInfos = projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.TAGS);
		if (milestonesInfos.isEmpty()) {
			return;
		}
		ProjMilestoneInfo milestoneInfo = milestonesInfos.get(0);
		
		milestonePreviewCtrl = new ProjMilestonePreviewController(ureq, getWindowControl(), secCallback,
				milestoneInfo);
		listenTo(milestonePreviewCtrl);
		
		calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(),
				milestonePreviewCtrl.getInitialComponent(), targetDomId, null, true, "");
		listenTo(calloutCtr);
		calloutCtr.activate();
	}
	
	private void doOpenPreview(UserRequest ureq, ProjCalendarRow row) {
		if (ProjAppointment.TYPE.equals(row.getType())) {
			if (cmc != null && appointmentEditCtrl != null) return;
			
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(appointmentEditCtrl);
			
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setAppointments(List.of(() -> row.getKey()));
			searchParams.setStatus(List.of(ProjectStatus.active));
			List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams,
					ProjArtefactInfoParams.of(true, false, false));
			if (appointmentInfos.isEmpty()) {
				return;
			}
			ProjAppointmentInfo appointmentInfo = appointmentInfos.get(0);
			
			appointmentPreviewCtrl = new ProjAppointmentPreviewController(ureq, getWindowControl(), bcFactory,
					secCallback, appointmentInfo, row.getKalendarEvent());
			listenTo(appointmentPreviewCtrl);
			
			String title = translate("appointment");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentPreviewCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
			
		} else if (ProjMilestone.TYPE.equals(row.getType())) {
			if (cmc != null && milestoneEditCtrl != null) return;
			
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(milestoneEditCtrl);
			
			ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
			searchParams.setMilestones(List.of(() -> row.getKey()));
			searchParams.setStatus(List.of(ProjectStatus.active));
			List<ProjMilestoneInfo> milestonesInfos = projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.TAGS);
			if (milestonesInfos.isEmpty()) {
				return;
			}
			ProjMilestoneInfo milestoneInfo = milestonesInfos.get(0);
			
			milestonePreviewCtrl = new ProjMilestonePreviewController(ureq, getWindowControl(), secCallback,
					milestoneInfo);
			listenTo(milestonePreviewCtrl);
			
			String title = translate("milestone");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), milestonePreviewCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
		
	}

	private void doCreateAppointment(UserRequest ureq, Date initialStartDate) {
		if (guardModalController(appointmentEditCtrl)) return;
		
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), bcFactory, project,
				Set.of(getIdentity()), false, initialStartDate);
		listenTo(appointmentEditCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateMilestone(UserRequest ureq) {
		if (guardModalController(milestoneEditCtrl)) return;
		
		milestoneEditCtrl = new ProjMilestoneEditController(ureq, getWindowControl(), bcFactory, project);
		listenTo(milestoneEditCtrl);
		
		String title = translate("milestone.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), milestoneEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditAppointment(UserRequest ureq, ProjAppointment appointment, KalendarEvent kalendarEvent) {
		if (kalendarEvent instanceof KalendarRecurEvent recurEvent
				&& !StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			appointmentEditAllCtr = new ConfirmUpdateController(ureq, getWindowControl(), recurEvent);
			listenTo(appointmentEditAllCtr);

			String title = translate("appointment.edit");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					appointmentEditAllCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else {
			doEditAppointment(ureq, appointment.getIdentifier());
		}
	}
	
	private void doEditRecurringAppointment(UserRequest ureq, KalendarRecurEvent kalendarRecurEvent,
		org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent.Cascade cascade) {
		switch(cascade) {
			case all: {
				doEditAppointment(ureq, kalendarRecurEvent.getExternalId());
				break;
			}
			case once: {
				KalendarEvent occurenceEvent = calendarManager.createKalendarEventRecurringOccurence(kalendarRecurEvent);
				ProjAppointment appointment = projectService.createAppointmentOcurrence(getIdentity(),bcFactory, kalendarRecurEvent.getExternalId(),
						occurenceEvent.getRecurrenceID(), occurenceEvent.getBegin(), occurenceEvent.getEnd());
				doEditAppointment(ureq, appointment.getIdentifier());
				break;
			}
		}
	}
	
	private void doEditAppointment(UserRequest ureq, String externalId) {
		if (guardModalController(appointmentEditCtrl)) return;
		
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setIdentifiers(List.of(externalId));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams, ProjArtefactInfoParams.MEMBERS);
		if (appointmentInfos.isEmpty()) {
			return;
		}
		ProjAppointmentInfo appointmentInfo = appointmentInfos.get(0);
		
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), bcFactory,
				appointmentInfo.getAppointment(), appointmentInfo.getMembers(), false);
		listenTo(appointmentEditCtrl);

		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentEditCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMove(UserRequest ureq, KalendarEvent kalendarEvent, Long days, Long minutes, Boolean allDay,
			boolean changeStartDate) {
		if (kalendarEvent.getCalendar() != null) {
			if (appointmentReadWriteKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doMoveAppointment(ureq, kalendarEvent, days, minutes, allDay, changeStartDate);
			} else if (milestoneKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doMoveMilestone(ureq, kalendarEvent, days);
			}
		}
	}
	
	private void doMoveAppointment(UserRequest ureq, KalendarEvent kalendarEvent, Long days, Long minutes,
			Boolean allDay, boolean changeStartDate) {
		if (kalendarEvent instanceof KalendarRecurEvent recurEvent
				&& !StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			appointmentMoveAllCtr = new ConfirmUpdateController(ureq, getWindowControl(), recurEvent, days, minutes,
					allDay, changeStartDate);
			listenTo(appointmentMoveAllCtr);

			String title = translate("cal.edit.update");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					appointmentMoveAllCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if (kalendarEvent != null) {
			projectService.moveAppointment(getIdentity(), bcFactory, kalendarEvent.getExternalId(), days, minutes, changeStartDate);
			loadModel(ureq, false);
		} else {
			loadModel(ureq, false);
		}
	}
	
	private void doMoveRecurringAppointment(UserRequest ureq, KalendarRecurEvent kalendarRecurEvent,
			org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent.Cascade cascade, Long days, Long minutes,
			boolean moveStartDate) {
		switch(cascade) {
			case all: {
				projectService.moveAppointment(getIdentity(), bcFactory, kalendarRecurEvent.getExternalId(), days, minutes, moveStartDate);
				break;
			}
			case once: {
				KalendarEvent occurenceEvent = calendarManager.createKalendarEventRecurringOccurence(kalendarRecurEvent);
				projectService.createMovedAppointmentOcurrence(getIdentity(), bcFactory, kalendarRecurEvent.getExternalId(),
						occurenceEvent.getRecurrenceID(), occurenceEvent.getBegin(), occurenceEvent.getEnd(), days,
						minutes, moveStartDate);
				break;
			}
		}
		loadModel(ureq, false);
	}
	
	private void doEditMilestone(UserRequest ureq, ProjMilestoneRef milestone) {
		if(guardModalController(milestoneEditCtrl)) return;
		
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setMilestones(List.of(milestone));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjMilestone> milestones = projectService.getMilestones(searchParams);
		if (milestones.isEmpty()) {
			return;
		}
		
		milestoneEditCtrl = new ProjMilestoneEditController(ureq, getWindowControl(), bcFactory, milestones.get(0));
		listenTo(milestoneEditCtrl);

		String title = translate("milestone.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				milestoneEditCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAcomplishMilestone(ProjMilestoneRef milestone) {
		projectService.updateMilestoneStatus(getIdentity(), bcFactory, milestone, ProjMilestoneStatus.achieved);
	}
	
	private void doMoveMilestone(UserRequest ureq, KalendarEvent kalendarEvent, Long days) {
		projectService.moveMilestone(getIdentity(), bcFactory, kalendarEvent.getExternalId(), days);
		loadModel(ureq, false);
	}
	
	private void doConfirmDeleteAppointment(UserRequest ureq, ProjAppointmentRef appointmentRef, KalendarEvent kalendarEvent) {
		if (guardModalController(appointmentDeleteConfirmationCtrl)) return;
		
		ProjAppointment appointment = projectService.getAppointment(appointmentRef);
		if (appointment == null || ProjectStatus.deleted == appointment.getArtefact().getStatus()) {
			return;
		}
		
		appointmentDeleteConfirmationCtrl = new ProjAppointmentDeleteConfirmationController(ureq, getWindowControl(),
				translate("appointment.delete.confirmation.message", ProjectUIFactory.getDisplayName(getTranslator(), appointment)),
				translate("appointment.delete.confirmation.confirm"),
				translate("appointment.delete.confirmation.button"),
				StringHelper.containsNonWhitespace(appointment.getRecurrenceRule()));
		appointmentDeleteConfirmationCtrl.setUserObject(kalendarEvent);
		listenTo(appointmentDeleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentDeleteConfirmationCtrl.getInitialComponent(),
				true, translate("appointment.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteAppointment(UserRequest ureq, KalendarEvent kalendarEvent, Cascade cascade) {
		switch(cascade) {
			case all: 
				projectService.deleteAppointmentSoftly(getIdentity(), bcFactory, kalendarEvent.getExternalId(), kalendarEvent.getOccurenceDate());
				break;
			case single: {
				projectService.addAppointmentExclusion(getIdentity(), bcFactory, kalendarEvent.getExternalId(), kalendarEvent.getBegin(), true);
				break;
			}
			case future: {
				projectService.addAppointmentExclusion(getIdentity(), bcFactory, kalendarEvent.getExternalId(), kalendarEvent.getBegin(), false);
				break;
			}
		}
		loadModel(ureq, false);
	}
	
	private void doConfirmDeleteMilestone(UserRequest ureq, ProjMilestoneRef milestoneRef) {
		if (guardModalController(milestoneDeleteConfirmationCtrl)) return;
		
		ProjMilestone milestone = projectService.getMilestone(milestoneRef);
		if (milestone == null || ProjectStatus.deleted == milestone.getArtefact().getStatus()) {
			return;
		}
		
		milestoneDeleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("milestone.delete.confirmation.message", StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(getTranslator(), milestone))),
				translate("milestone.delete.confirmation.confirm"),
				translate("milestone.delete.confirmation.button"), true);
		milestoneDeleteConfirmationCtrl.setUserObject(milestone);
		listenTo(milestoneDeleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), milestoneDeleteConfirmationCtrl.getInitialComponent(),
				true, translate("milestone.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteMilestone(UserRequest ureq, ProjMilestoneRef milestone) {
		projectService.deleteMilestoneSoftly(getIdentity(), milestone);
		loadModel(ureq, false);
	}
	
	private void doOpenTools(UserRequest ureq, ProjCalendarRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final ProjCalendarRow row;
		private ProjAppointment appointment;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ProjCalendarRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("calendar_tools");
			
			if (ProjAppointment.TYPE.equals(row.getType())) {
				appointment = projectService.getAppointment(() -> row.getKey());
				if (appointment != null) {
					if (secCallback.canEditAppointment(appointment)) {
						addLink("appointment.edit", CMD_EDIT, "o_icon o_icon-fw o_icon_edit");
					}
					
					if (secCallback.canDeleteAppointment(appointment, getIdentity())) {
						addLink("delete", CMD_DELETE, "o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
					}
				}
			} else if (ProjMilestone.TYPE.equals(row.getType())) {
				ProjMilestone milestone = projectService.getMilestone(() -> row.getKey());
				if (milestone != null) {
					if (secCallback.canEditMilestone(milestone)) {
						addLink("milestone.edit", CMD_EDIT, "o_icon o_icon-fw o_icon_edit");
					}
					
					if (secCallback.canDeleteMilestone(milestone, getIdentity())) {
						addLink("delete", CMD_DELETE, "o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
					}
				}
			}
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					if (ProjAppointment.TYPE.equals(row.getType())) {
						doEditAppointment(ureq, appointment, row.getKalendarEvent());
					} else if (ProjMilestone.TYPE.equals(row.getType())) {
						doEditMilestone(ureq, () -> row.getKey());
					}
				} else if(CMD_DELETE.equals(cmd)) {
					if (ProjAppointment.TYPE.equals(row.getType())) {
						doConfirmDeleteAppointment(ureq, () -> row.getKey(), row.getKalendarEvent());
					} else if (ProjMilestone.TYPE.equals(row.getType())) {
						doConfirmDeleteMilestone(ureq, () -> row.getKey());
					}
				}
			}
		}
	}
	
}
