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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValuesSupplier;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjDateRange;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.manager.ProjectXStream;
import org.olat.modules.project.ui.ProjActivityLogTableModel.ActivityLogCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Feb 2023<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjActivityLogController extends FormBasicController {

	private static final String TAB_ID_LAST_7_DAYS = "Last7Days";
	private static final String TAB_ID_LAST_4_WEEKS = "Last4Weeks";
	private static final String TAB_ID_LAST_12_MONTH = "Last12Month";
	private static final String TAB_ID_ALL = "All";
	private static final String FILTER_ACTIVITY = "activity";
	private static final String FILTER_USER = "user";

	private final List<UserPropertyHandler> userPropertyHandlers;
	private FlexiFiltersTab tabLast7Days;
	private FlexiFiltersTab tabLast4Weeks;
	private FlexiFiltersTab tabLast12Month;
	private FlexiFiltersTab tabAll;
	private ProjActivityLogTableModel dataModel;
	private FlexiTableElement tableEl;

	private final ProjArtefact artefact;
	private final List<Identity> members;
	private final Formatter formatter;

	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CalendarManager calendarManager;

	public ProjActivityLogController(UserRequest ureq, WindowControl wControl, Form mainForm, ProjArtefact artefact) {
		super(ureq, wControl, LAYOUT_CUSTOM, "activity_log", mainForm);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.artefact = artefact;
		this.members = projectService.getMembers(artefact.getProject(), ProjectRole.PROJECT_ROLES);
		this.formatter = Formatter.getInstance(getLocale());

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(ProjActivityLogTableModel.USAGE_IDENTIFIER,
				isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(ActivityLogCols.date.name(), false));

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.date));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.message));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.originalValue));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.newValue));

		int colIndex = ProjActivityLogTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance()
					.isMandatoryUserProperty(ProjActivityLogTableModel.USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, true, "userProp-" + colIndex));
		}

		dataModel = new ProjActivityLogTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(),
				formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(true);
		tableEl.setSortSettings(options);

		initFilterTabs(ureq);
		initFilters();
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(4);

		tabLast7Days = FlexiFiltersTabFactory.tab(TAB_ID_LAST_7_DAYS, translate("tab.last.7.days"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabLast7Days);

		tabLast4Weeks = FlexiFiltersTabFactory.tab(TAB_ID_LAST_4_WEEKS, translate("tab.last.4.weeks"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabLast4Weeks);

		tabLast12Month = FlexiFiltersTabFactory.tab(TAB_ID_LAST_12_MONTH, translate("tab.last.12.month"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabLast12Month);

		tabAll = FlexiFiltersTabFactory.tab(TAB_ID_ALL, translate("tab.all"), TabSelectionBehavior.reloadData);
		tabs.add(tabAll);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabLast7Days);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.activity"), FILTER_ACTIVITY,
				getActivityFilterValues(), true));
		SelectionValues userValues = new SelectionValues();

		members.stream().forEach(member -> userValues
				.add(SelectionValues.entry(member.getKey().toString(), userManager.getUserDisplayName(member))));
		userValues.sort(SelectionValues.VALUE_ASC);
		if (!userValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.user"), FILTER_USER,
					userValues, true));
		}
		
		tableEl.setFilters(true, filters, false, false);
	}

	private void loadModel() {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(artefact.getProject());
		searchParams.setArtefacts(List.of(artefact));
		searchParams.setFetchDoer(true);
		applyFilter(searchParams);
		List<ProjActivity> activities = projectService.getActivities(searchParams, 0, -1);
		
		List<ProjArtefact> artefactReferences = activities.stream()
				.map(ProjActivity::getArtefactReference)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		ProjArtefactSearchParams artefactSearchParams = new ProjArtefactSearchParams();
		artefactSearchParams.setArtefacts(artefactReferences);
		ProjArtefactItems artefactReferenceItems = projectService.getArtefactItems(artefactSearchParams);
		
		List<ProjActivityLogRow> rows = new ArrayList<>(activities.size());
		for (ProjActivity activity : activities) {
			addActivityRows(rows, activity, artefactReferenceItems);
		}
		
		applyFilters(rows);
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void applyFilter(ProjActivitySearchParams searchParams) {
		if (tableEl.getSelectedFilterTab() != null) {
			if (tableEl.getSelectedFilterTab() == tabLast7Days) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				searchParams.setCreatedDateRanges(
						List.of(new ProjDateRange(DateUtils.addDays(today, -7), DateUtils.addDays(today, 1))));
			} else if (tableEl.getSelectedFilterTab() == tabLast4Weeks) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				searchParams.setCreatedDateRanges(
						List.of(new ProjDateRange(DateUtils.addDays(today, -28), DateUtils.addDays(today, 1))));
			} else if (tableEl.getSelectedFilterTab() == tabLast12Month) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				searchParams.setCreatedDateRanges(
						List.of(new ProjDateRange(DateUtils.addMonth(today, -12), DateUtils.addDays(today, 1))));
			}
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_USER.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					Set<Long> selectedIdentityKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					searchParams.setDoerKeys(selectedIdentityKeys);
				}
			}
		}
	}
	
	private void applyFilters(List<ProjActivityLogRow> rows) {
		String searchString = tableEl.getQuickSearchString().toLowerCase();
		if (StringHelper.containsNonWhitespace(searchString)) {
			rows.removeIf(row -> !isSeachStringFound(row, searchString));
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_ACTIVITY.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					rows.removeIf(row -> !values.contains(row.getMessageI18nKey()));
				}
			}
		}
	}

	private boolean isSeachStringFound(ProjActivityLogRow row, String searchString) {
		return (row.getMessage() != null && row.getMessage().toLowerCase().indexOf(searchString) >= 0)
				|| (row.getOriginalValue() != null && row.getOriginalValue().toLowerCase().indexOf(searchString) >= 0)
				|| (row.getNewValue() != null && row.getNewValue().toLowerCase().indexOf(searchString) >=  0);
	}
	
	private SelectionValuesSupplier getActivityFilterValues() {
		return switch (artefact.getType()) {
				case ProjFile.TYPE -> getActivityFilterFileValues();
				case ProjNote.TYPE -> getActivityFilterNoteValues();
				case ProjAppointment.TYPE -> getActivityFilterAppointmentValues();
				default -> new SelectionValues();
			};
	}

	private void addActivityFilterValue(SelectionValues filterSV, String messageI18nKey) {
		filterSV.add(entry(messageI18nKey, translate(messageI18nKey)));
	}

	private void addActivityRows(List<ProjActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getActionTarget()) {
		case file: addActivityFileRows(rows, activity, artefactReferenceItems);
		case note: addActivityNoteRows(rows, activity, artefactReferenceItems);
		case appointment: addActivityAppointmentRows(rows, activity, artefactReferenceItems);
		default: //
		}
	}

	private SelectionValues getActivityFilterFileValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.upload");
		addActivityFilterValue(filterSV, "activity.log.message.read");
		addActivityFilterValue(filterSV, "activity.log.message.download");
		addActivityFilterValue(filterSV, "activity.log.message.edit.file");
		addActivityFilterValue(filterSV, "activity.log.message.edit.title");
		addActivityFilterValue(filterSV, "activity.log.message.edit.description");
		addActivityFilterValue(filterSV, "activity.log.message.edit.filename");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		return filterSV;
	}

	private void addActivityFileRows(List<ProjActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case fileRead: addRow(rows, activity, "activity.log.message.read"); break;
		case fileDownload: addRow(rows, activity, "activity.log.message.download"); break;
		case fileCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case fileUpload: addRow(rows, activity, "activity.log.message.upload"); break;
		case fileEdit: addRow(rows, activity, "activity.log.message.edit.file"); break;
		case fileStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case fileMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember())); break;
		case fileMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember()), null); break;
		case fileReferenceAdd: {
			String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
			if (StringHelper.containsNonWhitespace(value)) {
				addRow(rows, activity, "activity.log.message.reference.add", null, value); break;
			}
		}
		case fileReferenceRemove: {
			String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
			if (StringHelper.containsNonWhitespace(value)) {
				addRow(rows, activity, "activity.log.message.reference.remove", value, null); break;
			}
		}
		case fileContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjFile before = ProjectXStream.fromXml(activity.getBefore(), ProjFile.class);
				ProjFile after = ProjectXStream.fromXml(activity.getAfter(), ProjFile.class);
				VFSMetadata beforeMetadata = before.getVfsMetadata();
				VFSMetadata afterMetadata = after.getVfsMetadata();
				if (!Objects.equals(beforeMetadata.getTitle(), afterMetadata.getTitle())) {
					addRow(rows, activity, "activity.log.message.edit.title", beforeMetadata.getTitle(), afterMetadata.getTitle());
				}
				if (!Objects.equals(beforeMetadata.getComment(), afterMetadata.getComment())) {
					addRow(rows, activity, "activity.log.message.edit.description", beforeMetadata.getComment(), afterMetadata.getComment());
				}
				if (!Objects.equals(beforeMetadata.getFilename(), afterMetadata.getFilename())) {
					addRow(rows, activity, "activity.log.message.edit.filename", beforeMetadata.getFilename(), afterMetadata.getFilename());
				}
			}
			break;
		}
		default: //
		}
	}

	private SelectionValues getActivityFilterNoteValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.read");
		addActivityFilterValue(filterSV, "activity.log.message.download");
		addActivityFilterValue(filterSV, "activity.log.message.edit.title");
		addActivityFilterValue(filterSV, "activity.log.message.edit.text");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		return filterSV;
	}
	
	private void addActivityNoteRows(List<ProjActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case noteRead: addRow(rows, activity, "activity.log.message.read"); break;
		case noteDownload: addRow(rows, activity, "activity.log.message.download"); break;
		case noteCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case noteStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case noteMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember())); break;
		case noteMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember()), null); break;
		case noteReferenceAdd: {
			String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
			if (StringHelper.containsNonWhitespace(value)) {
				addRow(rows, activity, "activity.log.message.reference.add", null, value); break;
			}
		}
		case noteReferenceRemove: {
			String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
			if (StringHelper.containsNonWhitespace(value)) {
				addRow(rows, activity, "activity.log.message.reference.remove", value, null); break;
			}
		}
		case noteContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjNote before = ProjectXStream.fromXml(activity.getBefore(), ProjNote.class);
				ProjNote after = ProjectXStream.fromXml(activity.getAfter(), ProjNote.class);
				if (!Objects.equals(before.getTitle(), after.getTitle())) {
					addRow(rows, activity, "activity.log.message.edit.title", before.getTitle(), after.getTitle());
				}
				if (!Objects.equals(before.getText(), after.getText())) {
					addRow(rows, activity, "activity.log.message.edit.text", before.getText(), after.getText());
				}
			}
			break;
		}
		default: //
		}
	}

	private SelectionValues getActivityFilterAppointmentValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.read");
		addActivityFilterValue(filterSV, "activity.log.message.edit.start.date");
		addActivityFilterValue(filterSV, "activity.log.message.edit.end.date");
		addActivityFilterValue(filterSV, "activity.log.message.edit.subject");
		addActivityFilterValue(filterSV, "activity.log.message.edit.description");
		addActivityFilterValue(filterSV, "activity.log.message.edit.location");
		addActivityFilterValue(filterSV, "activity.log.message.edit.color");
		addActivityFilterValue(filterSV, "activity.log.message.edit.all.day");
		addActivityFilterValue(filterSV, "activity.log.message.edit.recurrence.rule");
		addActivityFilterValue(filterSV, "activity.log.message.edit.recurrence.end");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.delete.occurrence");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		return filterSV;
	}
	
	private void addActivityAppointmentRows(List<ProjActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case appointmentCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case appointmentOccurrenceDelete:
			addRow(rows, activity, "activity.log.message.delete.occurrence",
					formatter.formatDateAndTime(ProjectXStream.fromXml(activity.getBefore(), Date.class)), null);
			break;
		case appointmentStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case appointmentMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember())); break;
		case appointmentMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember()), null); break;
		case appointmentReferenceAdd: {
			String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
			if (StringHelper.containsNonWhitespace(value)) {
				addRow(rows, activity, "activity.log.message.reference.add", null, value); break;
			}
		}
		case appointmentReferenceRemove: {
			String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
			if (StringHelper.containsNonWhitespace(value)) {
				addRow(rows, activity, "activity.log.message.reference.remove",value, null); break;
			}
		}
		case appointmentContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjAppointment before = ProjectXStream.fromXml(activity.getBefore(), ProjAppointment.class);
				ProjAppointment after = ProjectXStream.fromXml(activity.getAfter(), ProjAppointment.class);
				Date beforeStartDate = new Date(before.getStartDate().getTime());
				Date afterStartDate = new Date(after.getStartDate().getTime());
				if (!Objects.equals(beforeStartDate, afterStartDate)) {
					addRow(rows, activity, "activity.log.message.edit.start.date", formatter.formatDateAndTime(beforeStartDate), formatter.formatDateAndTime(afterStartDate));
				}
				Date beforeEndDate = new Date(before.getEndDate().getTime());
				Date afterEndDate = new Date(after.getEndDate().getTime());
				if (!Objects.equals(beforeEndDate, after.getEndDate())) {
					addRow(rows, activity, "activity.log.message.edit.end.date", formatter.formatDateAndTime(beforeEndDate), formatter.formatDateAndTime(afterEndDate));
				}
				if (!Objects.equals(before.getSubject(), after.getSubject())) {
					addRow(rows, activity, "activity.log.message.edit.subject", before.getSubject(), after.getSubject());
				}
				if (!Objects.equals(before.getDescription(), after.getDescription())) {
					addRow(rows, activity, "activity.log.message.edit.description", before.getDescription(), after.getDescription());
				}
				if (!Objects.equals(before.getLocation(), after.getLocation())) {
					addRow(rows, activity, "activity.log.message.edit.location", before.getLocation(), after.getLocation());
				}
				if (!Objects.equals(before.getColor(), after.getColor())) {
					addRow(rows, activity, "activity.log.message.edit.color", before.getColor(), after.getColor());
				}
				if (before.isAllDay() != after.isAllDay()) {
					addRow(rows, activity, "activity.log.message.edit.all.day", Boolean.valueOf(before.isAllDay()).toString(), Boolean.valueOf(after.isAllDay()).toString());
				}
				
				String beforeRecurrence = CalendarUtils.getRecurrence(before.getRecurrenceRule());
				String afterRecurrence = CalendarUtils.getRecurrence(after.getRecurrenceRule());
				if (!Objects.equals(beforeRecurrence, afterRecurrence)) {
					addRow(rows, activity, "activity.log.message.edit.recurrence.rule", getTranslatedRecurrenceRule(beforeRecurrence), getTranslatedRecurrenceRule(afterRecurrence));
				}
				
				Date beforeRecurrenceEnd = calendarManager.getRecurrenceEndDate(before.getRecurrenceRule());
				Date afterRecurrenceEnd = calendarManager.getRecurrenceEndDate(after.getRecurrenceRule());
				if (!Objects.equals(beforeRecurrenceEnd, afterRecurrenceEnd)) {
					addRow(rows, activity, "activity.log.message.edit.recurrence.end", formatter.formatDate(beforeRecurrenceEnd), formatter.formatDate(afterRecurrenceEnd));
				}
			}
			break;
		}
		default: //
		}
	}

	private void addRow(List<ProjActivityLogRow> rows, ProjActivity activity, String messageI18n) {
		addRow(rows, activity, messageI18n, null, null);
	}
	
	private void addRow(List<ProjActivityLogRow> rows, ProjActivity activity, String messageI18nKey, String originalValue, String newValue) {
		ProjActivityLogRow row = new ProjActivityLogRow(activity.getDoer(), userPropertyHandlers, getLocale());
		row.setDate(activity.getCreationDate());
		row.setMessageI18nKey(messageI18nKey);
		row.setMessage(translate(messageI18nKey));
		row.setOriginalValue(originalValue);
		row.setNewValue(newValue);
		rows.add(row);
	}
	
	private String getArtefactValue(ProjArtefact artefact, ProjArtefactItems artefactItems) {
		if (ProjFile.TYPE.equals(artefact.getType())) {
			ProjFile file = artefactItems.getFile(artefact);
			if (file != null) {
				return translate("activity.log.file", ProjectUIFactory.getDisplayName(file));
			}
		} else if (ProjNote.TYPE.equals(artefact.getType())) {
			ProjNote note = artefactItems.getNote(artefact);
			if (note != null) {
				return translate("activity.log.note", ProjectUIFactory.getDisplayName(getTranslator(), note));
			}
		} else if (ProjFile.TYPE.equals(artefact.getType())) {
			ProjAppointment appointment = artefactItems.getAppointment(artefact);
			if (appointment != null) {
				return translate("activity.log.appointment", ProjectUIFactory.getDisplayName(getTranslator(), appointment));
			}
		}
		return null;
	}
	
	private String getTranslatedRecurrenceRule(String beforeRecurrence) {
		if (KalendarEvent.DAILY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.daily");
		} else if (KalendarEvent.WORKDAILY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.workdaily");
		} else if (KalendarEvent.WEEKLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.weekly");
		} else if (KalendarEvent.BIWEEKLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.biweekly");
		} else if (KalendarEvent.MONTHLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.monthly");
		} else if (KalendarEvent.YEARLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.yearly");
		}
		return null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				loadModel();
			} else if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
