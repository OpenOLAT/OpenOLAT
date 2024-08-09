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
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionLog;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionLogListModel.LogCols;
import org.olat.course.assessment.ui.inspection.elements.ActionLogCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionLogController extends FormBasicController {

	private static final String FILTER_DATE = "date";
	private static final String ALL_TAB_ID = "All";
	private static final String LAST_7_DAYS_ID = "Last7Days";
	private static final String LAST_4_WEEKS_ID = "Last4Weeks";
	private static final String LAST_12_MONTHS_ID = "Last12Months";
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab last7DaysTab;
	private FlexiFiltersTab last4WeeksTab;
	private FlexiFiltersTab last12MonthsTab;
	
	private FlexiTableElement tableEl;
	private AssessmentInspectionLogListModel tableModel;

	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentInspection inspection;
	private final AssessmentInspectionConfiguration inspectionConfiguration;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionLogController(UserRequest ureq, WindowControl wControl,
			AssessmentInspection inspection) {
		super(ureq, wControl, "inspection_logs");
		this.inspection = inspection;
		inspectionConfiguration = inspection.getConfiguration();
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogCols.date));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogCols.action,
				new ActionLogCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogCols.before));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LogCols.after));
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		tableModel = new AssessmentInspectionLogListModel(columnsModel, inspectionConfiguration, getTranslator());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(false);
		tableEl.setSelectAllEnable(false);
		
		initFiltersPresets(ureq);
		initFilters();
		
		tableEl.setSelectedFilterTab(ureq, last7DaysTab);
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		DateRange last7Days = new DateRange();
		last7Days.setStart(CalendarUtils.startOfDay(DateUtils.addDays(ureq.getRequestTimestamp(), -7)));
		last7DaysTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_7_DAYS_ID, translate("filter.last.7.days"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_DATE, last7Days)));
		last7DaysTab.setElementCssClass("o_sel_inspection_log_last_7_days");
		last7DaysTab.setFiltersExpanded(false);
		tabs.add(last7DaysTab);
		
		DateRange last4Weeks = new DateRange();
		last4Weeks.setStart(CalendarUtils.startOfDay(DateUtils.addWeeks(ureq.getRequestTimestamp(), -4)));
		last4WeeksTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_4_WEEKS_ID, translate("filter.last.4.weeks"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_DATE, last4Weeks)));
		last4WeeksTab.setFiltersExpanded(false);
		tabs.add(last4WeeksTab);
		
		DateRange last12Months = new DateRange();
		last12Months.setStart(CalendarUtils.startOfDay(DateUtils.addMonths(ureq.getRequestTimestamp(), -12)));
		last12MonthsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_12_MONTHS_ID, translate("filter.last.12.months"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_DATE, last12Months)));
		last12MonthsTab.setFiltersExpanded(false);
		tabs.add(last12MonthsTab);

		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_inspection_log_all");
		allTab.setFiltersExpanded(false);
		tabs.add(allTab);

		tableEl.setFilterTabs(true, tabs);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		FlexiTableDateRangeFilter dateFilter = new FlexiTableDateRangeFilter(translate("filter.event.date"), FILTER_DATE,
				true, true, translate("filter.event.date.from"),
				translate("filter.event.date.to"), getLocale());
		filters.add(dateFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void loadModel() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		
		Date from = null;
		Date to = null;
		FlexiTableFilter dFilter = FlexiTableFilter.getFilter(filters, FILTER_DATE);
		if(dFilter instanceof FlexiTableDateRangeFilter dateFilter && dateFilter.getDateRange() != null) {
			from = dateFilter.getDateRange().getStart();
			to = dateFilter.getDateRange().getEnd();
		}
		
		List<AssessmentInspectionLog> inspectionLogList = inspectionService.getLogFor(inspection, from, to);
		List<AssessmentInspectionLogRow> rows = new ArrayList<>(inspectionLogList.size());
		for(AssessmentInspectionLog inspectionLog:inspectionLogList) {
			AssessmentInspectionLogRow logRow = new AssessmentInspectionLogRow(inspectionLog, userPropertyHandlers, getLocale());
			rows.add(logRow);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableFilterTabEvent fe) {
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
