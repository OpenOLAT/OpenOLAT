/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.modules.curriculum.ui.component.MemberHistoryDetailsRowComparator;
import org.olat.modules.curriculum.ui.member.MemberHistoryDetailsTableModel.MemberHistoryCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractHistoryController extends FormBasicController {
	
	protected FlexiFiltersTab allTab;
	
	protected static final String FILTER_DATE = "date";
	
	private static final String ALL_TAB_ID = "all";
	private static final String LAST_7_DAYS_TAB_ID = "7days";
	private static final String LAST_4_WEEKS_TAB_ID = "4weeks";
	private static final String LAST_12_MONTHS_TAB_ID = "12months";
	
	protected FlexiTableElement tableEl;
	protected MemberHistoryDetailsTableModel tableModel;
	protected DefaultFlexiColumnModel curriculumElementCol;
	
	private int counter = 0;
	protected final CurriculumElement curriculumElement;

	private NoteCalloutController noteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	protected CurriculumService curriculumService;
	
	protected AbstractHistoryController(UserRequest ureq, WindowControl wControl, String page,
			CurriculumElement curriculumElement) {
		super(ureq, wControl, page, Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.curriculumElement = curriculumElement;
	}
	
	public AbstractHistoryController(UserRequest ureq, WindowControl wControl, String page, Form rootForm,
			CurriculumElement curriculumElement) {
		super(ureq, wControl, LAYOUT_CUSTOM, page, rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));
		this.curriculumElement = curriculumElement;
	}
	
	protected void initTable(FormItemContainer formLayout, boolean withMember, boolean withCurriculumElement) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberHistoryCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.creationDate));
		if(withMember) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.member));
		}
		curriculumElementCol = new DefaultFlexiColumnModel(withCurriculumElement, MemberHistoryCols.curriculumElement);
		columnsModel.addFlexiColumnModel(curriculumElementCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.activity));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.role,
				new CurriculumMembershipCellRenderer(getTranslator(), ", ")));
		GroupMembershipStatusRenderer memberStatusRenderer = new GroupMembershipStatusRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.previousStatus,
				memberStatusRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.status,
				memberStatusRenderer));
		DefaultFlexiColumnModel noteCol = new DefaultFlexiColumnModel(MemberHistoryCols.note);
		noteCol.setIconHeader("o_icon o_icon_notes");
		columnsModel.addFlexiColumnModel(noteCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberHistoryCols.actor));
		
		tableModel = new MemberHistoryDetailsTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MemberHistoryCols.creationDate.name(), false));
		tableEl.setSortSettings(sortOptions);
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		FlexiTableDateRangeFilter dateFilter = new FlexiTableDateRangeFilter(translate("filter.date"), FILTER_DATE,
				true, true, translate("filter.date.range.label"), translate("filter.date.to"), getLocale());
		filters.add(dateFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		tabs.add(allTab);

		Date now = DateUtils.getStartOfDay(new Date());
		Date end = DateUtils.getEndOfDay(new Date());
		
		DateRange last7Days = new DateRange();
		last7Days.setStart(DateUtils.addDays(now, -7));
		last7Days.setEnd(end);
		FlexiFiltersTab last7DaysTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_7_DAYS_TAB_ID, translate("filter.last.7.days"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last7Days)));
		tabs.add(last7DaysTab);
		
		DateRange last4Weeks = new DateRange();
		last4Weeks.setStart(DateUtils.addWeeks(now, -4));
		last4Weeks.setEnd(end);
		FlexiFiltersTab last4WeeksTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_4_WEEKS_TAB_ID, translate("filter.last.4.weeks"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last4Weeks)));
		tabs.add(last4WeeksTab);

		DateRange last12Months = new DateRange();
		last12Months.setStart(DateUtils.addMonth(now, -12));
		last12Months.setEnd(end);
		FlexiFiltersTab last12MonthsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_12_MONTHS_TAB_ID, translate("filter.last.12.months"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last12Months)));
		tabs.add(last12MonthsTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	protected abstract CurriculumElementMembershipHistorySearchParameters getSearchParameters();
	
	protected void loadModel(boolean reset) {
		CurriculumElementMembershipHistorySearchParameters searchParams = getSearchParameters();
		List<CurriculumElementMembershipHistory> membershipsHistory = curriculumService.getCurriculumElementMembershipsHistory(searchParams);
		Map<Group,CurriculumElement> groupToElements = getGroupsToCurriculumElementsMap(searchParams.getElements());
		
		List<MemberHistoryDetailsRow> rows = new ArrayList<>();
		for(CurriculumElementMembershipHistory elementHistory:membershipsHistory) {
			List<GroupMembershipHistory> points = elementHistory.getHistory();
			for(GroupMembershipHistory point:points) {
				rows.add(forgeRow(point, groupToElements));
			}
		}
		
		Collections.sort(rows, new MemberHistoryDetailsRowComparator());
		fillPreviousValue(rows);
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}
	
	private Map<Group,CurriculumElement> getGroupsToCurriculumElementsMap(List<CurriculumElement> elements) {
		Map<Group,CurriculumElement> groupToElements = new HashMap<>();
		if(elements != null && !elements.isEmpty()) {
			for(CurriculumElement element:elements) {
				groupToElements.put(element.getGroup(), element);
			}
		}
		return groupToElements;
	}
	
	private void fillPreviousValue(List<MemberHistoryDetailsRow> rows) {
		Map<MemberHistoryDetailsRecord,MemberHistoryDetailsRow> lastRecords = new HashMap<>();
		for(int i=rows.size(); i-->0; ) {
			MemberHistoryDetailsRow row = rows.get(i);
			MemberHistoryDetailsRecord detailsRecord = new MemberHistoryDetailsRecord(row.getGroup(),
					row.getIdentity().getKey(), row.getRole());
			
			MemberHistoryDetailsRow previousRow = lastRecords.get(detailsRecord);
			if(previousRow != null) {
				row.setPreviousStatus(previousRow.getStatus());
			}
			lastRecords.put(detailsRecord, row);
		}
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private MemberHistoryDetailsRow forgeRow(GroupMembershipHistory point, Map<Group,CurriculumElement> groupToElements) {
		Identity user = point.getIdentity();
		String userDisplayName = userManager.getUserDisplayName(user);
		CurriculumElement curriculumElement = groupToElements.get(point.getGroup());
		String curriculumElementName = curriculumElement == null ? null : curriculumElement.getDisplayName();
		MemberHistoryDetailsRow row = new MemberHistoryDetailsRow(user, userDisplayName, curriculumElementName, point);
		row.setActivity(toActivityString(row));

		Identity actor = point.getCreator();
		if(actor != null) {
			String actorDisplayName = userManager.getUserDisplayName(actor);
			row.setActorDisplayName(actorDisplayName);
		}
		
		if(StringHelper.containsNonWhitespace(point.getAdminNote())) {
			FormLink noteLink = uifactory.addFormLink("note_" + (++counter), "note", "", null, flc, Link.LINK | Link.NONTRANSLATED);
			noteLink.setDomReplacementWrapperRequired(false);
			noteLink.setIconLeftCSS("o_icon o_icon_notes");
			noteLink.setTitle(translate("note"));
			noteLink.setUserObject(row);
			row.setNoteLink(noteLink);
		}
		
		return row;
	}
	
	private String toActivityString(MemberHistoryDetailsRow row) {
		GroupMembershipStatus status = row.getStatus();
		return switch(status) {
			case active -> translate("activity.active");
			case cancel -> translate("activity.cancelled");
			case cancelWithFee -> translate("activity.cancelled.with.fee");
			case reservation -> translate("activity.reservation");
			default -> translate("membership.".concat(status.name()));
		};
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(noteCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(noteCtrl);
		calloutCtrl = null;
		noteCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link && "note".equals(link.getCmd())
				&& link.getUserObject() instanceof MemberHistoryDetailsRow row) {
			doOpenNote(ureq, link, row);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenNote(UserRequest ureq, FormLink link, MemberHistoryDetailsRow row) {
		StringBuilder sb = Formatter.stripTabsAndReturns(row.getAdminNote());
		String note = sb == null ? "" : sb.toString();
		noteCtrl = new NoteCalloutController(ureq, getWindowControl(), note);
		listenTo(noteCtrl);
		
		String title = translate("note");
		CalloutSettings settings = new CalloutSettings(title);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noteCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "", settings);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
}
