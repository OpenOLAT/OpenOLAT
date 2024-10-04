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

import static org.olat.modules.project.ui.ProjectUIFactory.templateSuffix;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableReduceEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.ActionTarget;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjTimelineActivityRowsFactory.ActivityRowData;
import org.olat.modules.project.ui.ProjTimelineActivityRowsFactory.ProjTimelineUIFactory;
import org.olat.modules.project.ui.ProjTimelineDataModel.TimelineCols;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTimelineController extends FormBasicController
		implements ProjTimelineUIFactory, FlexiTableComponentDelegate {
	
	private static final String TAB_ID_MY = "My";
	private static final String TAB_ID_ALL = "All";
	private static final String FILTER_TYPE = "type";
	private static final String FILTER_USER = "user";
	private static final String CMD_RANGE = "range";
	private static final String CMD_MORE = "more";
	private static final String CMD_ARTEFACT = "artefact";
	
	private FlexiFiltersTab tabMy;
	private FlexiFiltersTab tabAll;
	private List<FormLink> rangeLinks = new ArrayList<>();
	private FormLink moreLink;
	private FlexiTableElement tableEl;
	private ProjTimelineDataModel dataModel;
	
	private final List<ProjTimelineRow> allRows = new ArrayList<>();
	private final List<ProjTimelineRow> artefactRows = new ArrayList<>();
	private final List<ProjTimelineRow> activityTodayRows = new ArrayList<>();
	private final List<ProjTimelineRow> activityEarlierRows = new ArrayList<>();
	private final DateRange todayDateRange;
	private Date offsetDate;

	private final ProjectBCFactory bcFactory;
	private final ProjProject project;
	private final List<Identity> members;
	private final MapperKey avatarMapperKey;
	private final Formatter formatter;
	private final ProjTimelineActivityRowsFactory activityRowsFactory;
	private int counter;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private CalendarManager calendarManager;

	public ProjTimelineController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjProject project, List<Identity> members, MapperKey avatarMapperKey) {
		super(ureq, wControl, "timeline");
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		this.bcFactory = bcFactory;
		this.project = project;
		this.members = members;
		this.avatarMapperKey = avatarMapperKey;
		this.formatter = Formatter.getInstance(getLocale());
		this.activityRowsFactory = new ProjTimelineActivityRowsFactory(getTranslator(), formatter, userManager, this);
		
		Date today = DateUtils.setTime(new Date(), 0, 0, 1);
		this.todayDateRange = new DateRange(today, DateUtils.addDays(today, 1));
		
		initForm(ureq);
		loadModel(ureq, true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.message));
		
		dataModel = new ProjTimelineDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 2000, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setSearchEnabled(true);

		tableEl.setCssDelegate(new ProjTimelineListCssDelegate());
		// If you ever want to enable the classic renderer, ensure that the range row links are not displayed.
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("timeline_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		moreLink = uifactory.addFormLink("more", CMD_MORE, "timeline.show.more", null, flc, Link.BUTTON);
		
		initFilterTabs(ureq);
		initFilters();
	}
	
	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabMy = FlexiFiltersTabFactory.tab(
				TAB_ID_MY,
				translate("timeline.tab.my"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabMy);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		SelectionValues typeValues = new SelectionValues();
		typeValues.add(SelectionValues.entry(ActionTarget.project.name(), translate(templateSuffix("timeline.filter.type.project", project))));
		typeValues.add(SelectionValues.entry(ActionTarget.file.name(), translate("timeline.filter.type.file")));
		typeValues.add(SelectionValues.entry(ActionTarget.toDo.name(), translate("timeline.filter.type.todo")));
		typeValues.add(SelectionValues.entry(ActionTarget.note.name(), translate("timeline.filter.type.note")));
		typeValues.add(SelectionValues.entry(ActionTarget.appointment.name(), translate("timeline.filter.type.appointment")));
		typeValues.add(SelectionValues.entry(ActionTarget.milestone.name(), translate("timeline.filter.type.milestone")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("timeline.filter.type"), FILTER_TYPE, typeValues, true));
		
		SelectionValues userValues = new SelectionValues();
		members.stream()
				.filter(member -> {
					if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabMy) {
						return !member.getKey().equals(getIdentity().getKey());
					}
					return true;
				})
				.forEach(member -> userValues.add(SelectionValues.entry(
						member.getKey().toString(),
						StringHelper.escapeHtml(userManager.getUserDisplayName(member)))));
		userValues.sort(SelectionValues.VALUE_ASC);
		if (!userValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("timeline.filter.user"), FILTER_USER, userValues, true));
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	public void reload(UserRequest ureq) {
		loadModel(ureq, false);
	}

	private void loadModel(UserRequest ureq, boolean resetRanges) {
		if (resetRanges) {
			offsetDate = DateUtils.setTime(new Date(), 0, 0, 0);
			resetRangeLinks();
			moreLink.setVisible(true);
			activityEarlierRows.clear();
			allRows.clear();
		}
		
		loadArtefactRows();
		loadActivityTodayRows(ureq);
		fillAllRows();
		addRows(false);
	}
	
	private void fillAllRows() {
		allRows.clear();
		allRows.addAll(artefactRows);
		allRows.addAll(activityTodayRows);
		allRows.addAll(activityEarlierRows);
		allRows.sort((a1, a2) -> a2.getDate().compareTo(a1.getDate()));
	}

	private void addRows(boolean showLastRange) {
		TimelineRowFilter filter = new TimelineRowFilter();
		List<ProjTimelineRow> rows = new ArrayList<>();
		
		int allRowIndex = 0;
		for (FormLink rangeLink : rangeLinks) {
			if (rangeLink.getUserObject() instanceof RangeUserObject rangeUserObject) {
				addRangeLinkRow(rows, rangeLink);
				Date from = rangeUserObject.getDateRange().getFrom();
				Date to = rangeUserObject.getDateRange().getTo();
				
				// The last range has to be open after reload
				if (showLastRange && offsetDate.before(to) && (offsetDate.after(from) || offsetDate.equals(from))) {
					rangeUserObject.setShow(true);
					updateRangeIcon(rangeLink, rangeUserObject);
				}
				
				if (!allRows.isEmpty()) {
					boolean hasNext = true;
					ProjTimelineRow row = allRows.get(allRowIndex);
					while (hasNext && row.getDate().after(from) && row.getDate().after(offsetDate)) {
						if (filter.test(row)) {
							rangeUserObject.setRowsAvailable(true);
							if (rangeUserObject.isShow()) {
								rows.add(row);
							}
						}
						if (allRowIndex < allRows.size()-1) {
							row = allRows.get(++allRowIndex);
						} else {
							hasNext = false;
						}
					}
				}
			}
		}
		
		rows.sort((r1, r2) -> r2.getDate().compareTo(r1.getDate()));
		adjustEmptyRanges(rows);
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void adjustEmptyRanges(List<ProjTimelineRow> rows) {
		List<ProjTimelineRow> emptyRangeRows = new ArrayList<>(1);
		for (int i = 0; i < rows.size(); i++) {
			ProjTimelineRow row = rows.get(i);
			if (row.getRangeLink() != null && row.getRangeLink().getUserObject() instanceof RangeUserObject rangeUserObject) {
				if (!rangeUserObject.isRowsAvailable()) {
					if (rangeUserObject.getRange() == Range.next7Days || rangeUserObject.getRange() == Range.today) {
						if (rangeUserObject.isShow()) {
							ProjTimelineRow emptyMessageRow = new ProjTimelineRow();
							emptyMessageRow.setRangeEmpty(translate("timeline.range.empty"));
							emptyMessageRow.setDate(DateUtils.addMinutes(row.getDate(), 1));
							rows.add(i + 1, emptyMessageRow);
							i++;
						}
					} else {
						emptyRangeRows.add(row);
					}
				}
			}
		}
		rows.removeAll(emptyRangeRows);
	}

	private void loadArtefactRows() {
		artefactRows.clear();
		loadToDoRows();
		loadAppointmentRows();
		loadMilestoneRows();
	}
	
	private void loadToDoRows() {
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		searchParams.setToDoStatus(List.of(ToDoStatus.open, ToDoStatus.inProgress));
		searchParams.setDueDateNull(Boolean.FALSE);
		List<ProjToDo> toDos = projectService.getToDos(searchParams);
		
		for (ProjToDo toDo : toDos) {
			ProjTimelineRow row = createToDoRow(toDo);
			artefactRows.add(row);
		}
	}

	private ProjTimelineRow createToDoRow(ProjToDo toDo) {
		ProjTimelineRow row = new ProjTimelineRow();
		
		String icon = "<i class=\"o_icon o_icon-lg o_icon_todo_task\"> </i>";
		StaticTextElement iconItem = uifactory.addStaticTextElement("o_tl_" + counter++, null, icon, flc);
		iconItem.setDomWrapperElement(DomWrapperElement.span);
		row.setIconItem(iconItem);
		
		String message = ToDoUIFactory.getDisplayName(getTranslator(), toDo.getToDoTask());
		row.setMessage(message);
		FormLink link = uifactory.addFormLink("art_" + counter++, CMD_ARTEFACT,
				StringHelper.escapeHtml(row.getMessage()), null, flc, Link.LINK + Link.NONTRANSLATED);
		String url = bcFactory.getToDoUrl(toDo);
		link.setUrl(url);
		link.setUserObject(toDo.getArtefact());
		row.setMessageItem(link);
		
		row.setDate(DateUtils.addSeconds(toDo.getToDoTask().getDueDate(), 4));
		row.setFormattedDate(activityRowsFactory.getFormattedDate(row.getDate(), false));
		row.setToday(DateUtils.isSameDay(new Date(), row.getDate()));
		row.setActionTarget(ActionTarget.toDo);
		return row;
	}

	private void loadAppointmentRows() {
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		searchParams.setDatesNull(Boolean.FALSE);
		List<ProjAppointment> appointments = projectService.getAppointments(searchParams);
		Map<String, ProjAppointment> appointmentIdentToAppointment = appointments.stream()
				.collect(Collectors.toMap(ProjAppointment::getIdentifier, Function.identity()));
		
		Kalendar kalendar = projectService.getAppointmentsKalendar(appointments);
		List<KalendarEvent> appointmentEvents = calendarManager.getEvents(kalendar, project.getCreationDate(), DateUtils.addYears(new Date(), 10), true);
		
		for (KalendarEvent appointmentEvent : appointmentEvents) {
			ProjTimelineRow row = createAppointmentRow(appointmentEvent, appointmentIdentToAppointment.get(appointmentEvent.getExternalId()));
			artefactRows.add(row);
		}
	}

	private ProjTimelineRow createAppointmentRow(KalendarEvent kalendarEvent, ProjAppointment appointment) {
		ProjTimelineRow row = new ProjTimelineRow();
		
		String icon = "<i class=\"o_icon o_icon-lg o_icon_proj_appointment\"> </i>";
		StaticTextElement iconItem = uifactory.addStaticTextElement("o_tl_" + counter++, null, icon, flc);
		iconItem.setDomWrapperElement(DomWrapperElement.span);
		row.setIconItem(iconItem);
		
		String message = StringHelper.containsNonWhitespace(kalendarEvent.getSubject())
				? kalendarEvent.getSubject()
				: ProjectUIFactory.getNoTitle(getTranslator());
		row.setMessage(message);
		FormLink link = uifactory.addFormLink("art_" + counter++, CMD_ARTEFACT,
				StringHelper.escapeHtml(row.getMessage()), null, flc, Link.LINK + Link.NONTRANSLATED);
		String url = bcFactory.getAppointmentUrl(appointment);
		link.setUrl(url);
		link.setUserObject(appointment.getArtefact());
		row.setMessageItem(link);
		
		row.setDate(DateUtils.addSeconds(kalendarEvent.getBegin(), 5));
		row.setFormattedDate(activityRowsFactory.getFormattedDate(row.getDate(), !appointment.isAllDay()));
		row.setToday(DateUtils.isSameDay(new Date(), row.getDate()));
		row.setActionTarget(ActionTarget.appointment);
		return row;
	}
	
	private void loadMilestoneRows() {
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		searchParams.setDueDateNull(Boolean.FALSE);
		List<ProjMilestone> milestones = projectService.getMilestones(searchParams);
		
		for (ProjMilestone milestone : milestones) {
			ProjTimelineRow row = createMilestoneRow(milestone);
			artefactRows.add(row);
		}
	}

	private ProjTimelineRow createMilestoneRow(ProjMilestone milestone) {
		ProjTimelineRow row = new ProjTimelineRow();
		
		String icon = "<i class=\"o_icon o_icon-lg o_icon_proj_milestone\"> </i>";
		StaticTextElement iconItem = uifactory.addStaticTextElement("o_tl_" + counter++, null, icon, flc);
		iconItem.setDomWrapperElement(DomWrapperElement.span);
		row.setIconItem(iconItem);
		
		String message = ProjectUIFactory.getDisplayName(getTranslator(), milestone);
		row.setMessage(message);
		FormLink link = uifactory.addFormLink("art_" + counter++, CMD_ARTEFACT,
				StringHelper.escapeHtml(row.getMessage()), null, flc, Link.LINK + Link.NONTRANSLATED);
		String url = bcFactory.getMilestoneUrl(milestone);
		link.setUrl(url);
		link.setUserObject(milestone.getArtefact());
		row.setMessageItem(link);
		
		row.setDate(DateUtils.addSeconds(milestone.getDueDate(), 4));
		row.setFormattedDate(activityRowsFactory.getFormattedDate(row.getDate(), false));
		row.setToday(DateUtils.isSameDay(new Date(), row.getDate()));
		row.setActionTarget(ActionTarget.milestone);
		return row;
	}

	private void loadActivityTodayRows(UserRequest ureq) {
		activityTodayRows.clear();
		loadActivites(ureq, activityTodayRows, todayDateRange);
	}
	
	private void loadActivityEarlierRows(UserRequest ureq) {
		FormLink rangeLink = rangeLinks.get(rangeLinks.size()-1);
		loadActivityMoreRows(ureq, rangeLink, 0);
		fillAllRows();
	}

	private void loadActivityMoreRows(UserRequest ureq, FormLink rangeLink, int numLoaded) {
		if (rangeLink.getUserObject() instanceof RangeUserObject rangeUserObject
				&& !rangeUserObject.getDateRange().getFrom().before(project.getCreationDate())) {
			FormLink prevLink = createPrevLink(rangeUserObject);
			if (prevLink.getUserObject() instanceof RangeUserObject prevRangeUserObject) {
				List<ProjActivity> activites = loadActivites(ureq, activityEarlierRows, prevRangeUserObject.getDateRange());
				int totalLoaded = numLoaded + activites.size();
				if (activites.isEmpty() || totalLoaded <= 20) {
					loadActivityMoreRows(ureq, prevLink, totalLoaded);
				}
			}
		}
	}
	
	private void evaluateNextOffset() {
		Date earliestDate = allRows.get(allRows.size()-1).getDate();
		int numRows = 0;
		int rowIndex = 0;
		
		// Init until already displayed rows
		ProjTimelineRow row = null;
		if (!allRows.isEmpty()) {
			row = allRows.get(rowIndex);
			while (rowIndex < allRows.size() && row.getDate().after(offsetDate)) {
				row = allRows.get(rowIndex);
				rowIndex++;
			}
		}
		
		// Find next date to end of the range to display about 10 more rows.
		while (numRows <= 10 && offsetDate.after(earliestDate)) {
			offsetDate = DateUtils.addDays(offsetDate, -1);
			if (!allRows.isEmpty() && rowIndex < allRows.size()) {
				row = allRows.get(rowIndex);
				while (rowIndex < allRows.size() && row.getDate().after(offsetDate)) {
					row = allRows.get(rowIndex);
					numRows++;
					rowIndex++;
				}
			}
		}
		
		if (numRows == 0) {
			moreLink.setVisible(false);
		}
	}

	private List<ProjActivity> loadActivites(UserRequest ureq, List<ProjTimelineRow> rows, DateRange dateRange) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setActions(ProjActivity.TIMELINE_ACTIONS);
		searchParams.setCreatedDateRanges(List.of(dateRange));
		applyFilters(searchParams);
		
		List<ProjActivity> activities = projectService.getActivities(searchParams, 0, -1);
		Set<ProjArtefact> artefacts = activities.stream()
				.map(ProjActivity::getArtefact)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		ProjArtefactSearchParams artefactSearchParams = new ProjArtefactSearchParams();
		artefactSearchParams.setProject(project);
		artefactSearchParams.setArtefacts(artefacts);
		ProjArtefactItems artefactItems = projectService.getArtefactItems(artefactSearchParams);
		
		Map<Long, Set<Long>> artefactKeyToIdentityKeys = projectService.getArtefactKeyToIdentityKeys(artefacts);
		
		List<ActivityRowData> activityRowDatas = activities
				.stream()
				.collect(Collectors.groupingBy(ProjTimelineActivityRowsFactory::keyWithDate))
				.values()
				.stream()
				.map(activityRowsFactory::createActivityRowData)
				.toList();
		
		activities.sort((a1, a2) -> a2.getCreationDate().compareTo(a1.getCreationDate()));
		for (ActivityRowData activityRowData : activityRowDatas) {
			activityRowsFactory.addActivityRows(ureq, rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		}
		
		return activities;
	}
	
	private void applyFilters(ProjActivitySearchParams searchParams) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_TYPE.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<ActionTarget> targets = values.stream().filter(ActionTarget::isValid).map(ActionTarget::valueOf).collect(Collectors.toList());
					searchParams.setTargets(targets);
				} else {
					searchParams.setTargets(null);
				}
			}
		}
	}

	@Override
	public void addArtefactMesssageItem(ProjTimelineRow row, ProjArtefact artefact) {
		if (artefact != null && ProjectStatus.deleted != artefact.getStatus()) {
			FormLink link = uifactory.addFormLink("art_" + counter++, CMD_ARTEFACT,
					StringHelper.escapeHtml(row.getMessage()), null, flc, Link.LINK + Link.NONTRANSLATED);
			String url = bcFactory.getArtefactUrl(project, artefact.getType(), row.getBusinessPathKey());
			link.setUrl(url);
			link.setUserObject(artefact);
			row.setMessageItem(link);
		} else {
			addStaticMessageItem(row);
		}
	}

	@Override
	public void addStaticMessageItem(ProjTimelineRow row) {
		StaticTextElement messageItem = uifactory.addStaticTextElement("o_tl_" + counter++, null,
				StringHelper.escapeHtml(row.getMessage()), flc);
		messageItem.setDomWrapperElement(DomWrapperElement.span);
		row.setMessageItem(messageItem);
	}

	@Override
	public void addActionIconItem(ProjTimelineRow row, ProjActivity activity) {
		String icon = "<i class=\"o_icon o_icon-lg " + row.getIconCssClass() +"\"> </i>";
		StaticTextElement iconItem = uifactory.addStaticTextElement("o_tl_" + counter++, null, icon, flc);
		iconItem.setDomWrapperElement(DomWrapperElement.span);
		row.setIconItem(iconItem);
	}

	@Override
	public void addAvatarIcon(UserRequest ureq, ProjTimelineRow row, Identity member) {
		UsersPortraitsComponent portraitComp = UsersPortraitsFactory.create(ureq, "portrair_" + counter++, flc.getFormItemComponent(), null, avatarMapperKey);
		portraitComp.setUsers(UsersPortraitsFactory.createPortraitUsers(List.of(member)));
		portraitComp.setSize(PortraitSize.small);
		row.setIconItem(new ComponentWrapperElement(portraitComp));
	}

	private void resetRangeLinks() {
		rangeLinks.clear();
		FormLink link = createPrevLink(null);
		link = createPrevLink((RangeUserObject)link.getUserObject());
		link = createPrevLink((RangeUserObject)link.getUserObject());
	}
	
	private FormLink createPrevLink(RangeUserObject rangeUserObject) {
		Date today = DateUtils.setTime(new Date(), 0, 0, 0);
		FormLink link = null;
		if (rangeUserObject == null) {
			link = createRangeLink(Range.nextLater, DateUtils.addYears(today, 10), DateUtils.addDays(today, 8), translate("timeline.range.later"), false);
		} else if (rangeUserObject.getRange() == Range.nextLater) {
			link = createRangeLink(Range.next7Days, DateUtils.addDays(today, 8), DateUtils.addDays(today, 1), translate("timeline.range.next.7.days"), true);
		} else if (rangeUserObject.getRange() == Range.next7Days) {
			link = createRangeLink(Range.today, DateUtils.addDays(today, 1), today, translate("timeline.range.today"), true);
		} else if (rangeUserObject.getRange() == Range.today) {
			link = createRangeLink(Range.last7Days, today, DateUtils.addDays(today, -7),translate( "timeline.range.last.7.days"), true);
		} else if (rangeUserObject.getRange() == Range.last7Days) {
			Date to = rangeUserObject.getDateRange().getFrom();
			Date from = DateUtils.getStartOfYear(to);
			link = createRangeLink(Range.lastYear, to, from, translate("timeline.range.earlier"), true);
		} else if (rangeUserObject.getRange() == Range.lastYear || rangeUserObject.getRange() == Range.lastYearEarlier) {
			Date to = DateUtils.getEndOfYear(DateUtils.addYears(rangeUserObject.getDateRange().getFrom(), -1));
			Date from = DateUtils.getStartOfYear(to);
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(from);
			int year = calendar.get(java.util.Calendar.YEAR);
			link = createRangeLink(Range.lastYearEarlier, to, from, translate("timeline.range.year", String.valueOf(year)), true);
		}
		if (link != null) {
			rangeLinks.add(link);
		}
		return link;
	}

	private FormLink createRangeLink(Range range, Date to, Date from, String text, boolean show) {
		RangeUserObject rangeUserObject = new RangeUserObject(range, from, to);
		rangeUserObject.setShow(show);
		
		FormLink rangeLink = uifactory.addFormLink("range_" + counter++, CMD_RANGE, null, null, flc, Link.LINK + Link.NONTRANSLATED);
		rangeLink.setI18nKey(text);
		updateRangeIcon(rangeLink, rangeUserObject);
		rangeLink.setUserObject(rangeUserObject);
		
		return rangeLink;
	}
	
	private void updateRangeIcon(FormLink link, RangeUserObject rangeUserObject) {
		String iconCss = rangeUserObject.isShow()
				? "o_icon o_icon-fw o_icon_close_togglebox"
				: "o_icon o_icon-fw o_icon_open_togglebox";
		link.setIconLeftCSS(iconCss);
	}
	
	private void addRangeLinkRow(List<ProjTimelineRow> rows, FormLink rangeLink) {
		RangeUserObject rangeUserObject = (RangeUserObject)rangeLink.getUserObject();
		
		ProjTimelineRow rangeRow = new ProjTimelineRow();
		rangeRow.setRangeLink(rangeLink);
		rangeRow.setDate(rangeUserObject.getDateRange().getTo());
		rangeRow.setToday(DateUtils.isSameDay(new Date(), rangeUserObject.getDateRange().getFrom()));
		rows.add(rangeRow);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof ProjTimelineRow) {
			ProjTimelineRow timelineRow = (ProjTimelineRow)rowObject;
			if (timelineRow.getIconItem() != null) {
				cmps.add(timelineRow.getIconItem().getComponent());
			}
			if (timelineRow.getMessageItem() != null) {
				cmps.add(timelineRow.getMessageItem().getComponent());
			}
			if (timelineRow.getRangeLink() != null) {
				cmps.add(timelineRow.getRangeLink().getComponent());
			}
		}
		return cmps;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				if (FlexiTableReduceEvent.FILTER.equals(event.getCommand())) {
					// Filter selected
					loadModel(ureq, true);
				} else {
					// Quick search
					addRows(false);
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				initFilters();
				loadModel(ureq, true);
			}
		} else if (source == moreLink) {
			doMore(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_RANGE.equals(link.getCmd()) && link.getUserObject() instanceof RangeUserObject) {
				doToggleRange(link);
			} else if (CMD_ARTEFACT.equals(link.getCmd()) && link.getUserObject() instanceof ProjArtefact) {
				fireEvent(ureq, new OpenArtefactEvent((ProjArtefact)link.getUserObject()));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doMore(UserRequest ureq) {
		loadActivityEarlierRows(ureq);
		evaluateNextOffset();
		addRows(true);
	}

	private void doToggleRange(FormLink link) {
		RangeUserObject rangeUserObject = (RangeUserObject)link.getUserObject();
		rangeUserObject.setShow(!rangeUserObject.isShow());
		updateRangeIcon(link, rangeUserObject);
		
		addRows(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private final class ProjTimelineListCssDelegate extends DefaultFlexiTableCssDelegate {
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o_proj_timeline_list";
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			ProjTimelineRow row = dataModel.getObject(pos);
			String cssClass = "o_proj_timeline_row";
			if (row != null && row.isToday()) {
				cssClass += " o_proj_timeline_today";
			}
			if (row != null && row.getRangeLink() != null) {
				cssClass += " o_proj_timeline_range_row";
			}
			return cssClass;
		}
	}
	
	private enum Range { nextLater, next7Days, today, last7Days, lastYear, lastYearEarlier }
	
	private final static class RangeUserObject {
		
		private final Range range;
		private final DateRange dateRange;
		private boolean rowsAvailable;
		private boolean show;
		
		public RangeUserObject(Range range, Date from, Date to) {
			this.range = range;
			this.dateRange = new DateRange(from, to);
		}
		
		public Range getRange() {
			return range;
		}

		public DateRange getDateRange() {
			return dateRange;
		}

		public boolean isRowsAvailable() {
			return rowsAvailable;
		}

		public void setRowsAvailable(boolean rowsAvailable) {
			this.rowsAvailable = rowsAvailable;
		}

		public boolean isShow() {
			return show;
		}

		public void setShow(boolean show) {
			this.show = show;
		}
		
	}
	
	
	private final class TimelineRowFilter implements Predicate<ProjTimelineRow> {
		
		private String searchString;
		private Long identityKey;
		private Set<Long> selectedIdentityKeys;
		private List<ActionTarget> targets;

		public TimelineRowFilter () {
			searchString = tableEl.getQuickSearchString().toLowerCase();
			
			if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabMy) {
				identityKey = getIdentity().getKey();
			}
			
			List<FlexiTableFilter> filters = tableEl.getFilters();
			if (filters == null || filters.isEmpty()) return;
			
			for (FlexiTableFilter filter : filters) {
				if (FILTER_USER.equals(filter.getFilter())) {
					List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
					if (values != null && !values.isEmpty()) {
						selectedIdentityKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					}
				}
				if (FILTER_TYPE.equals(filter.getFilter())) {
					List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
					if (values != null && !values.isEmpty()) {
						targets = values.stream().filter(ActionTarget::isValid).map(ActionTarget::valueOf).collect(Collectors.toList());
					}
				}
			}
		}
		
		@Override
		public boolean test(ProjTimelineRow row) {
			if (StringHelper.containsNonWhitespace(searchString)) {
				if (row.getMessage() != null && row.getMessage().toLowerCase().indexOf(searchString) < 0) {
					return false;
				}
			}
			if (identityKey != null) {
				if (row.getIdentityKeys() != null && !row.getIdentityKeys().contains(identityKey)) {
					return false;
				}
			}
			if (selectedIdentityKeys != null) {
				if (row.getIdentityKeys() != null && !row.getIdentityKeys().stream().anyMatch(key -> selectedIdentityKeys.contains(key))) {
					return false;
				}
			}
			
			if (targets != null) {
				if (row.getActionTarget() != null && !targets.contains(row.getActionTarget())) {
					return false;
				}
			}
			
			return true;
		}
		
	}

}
