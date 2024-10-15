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
package org.olat.core.commons.controllers.activity;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.controllers.activity.ActivityLogTableModel.ActivityLogCols;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
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
public abstract class ActivityLogController extends FormBasicController {

	private static final String TAB_ID_LAST_7_DAYS = "Last7Days";
	private static final String TAB_ID_LAST_4_WEEKS = "Last4Weeks";
	private static final String TAB_ID_LAST_12_MONTH = "Last12Month";
	private static final String TAB_ID_ALL = "All";
	private static final String FILTER_ACTIVITY = "activity";
	private static final String FILTER_USER = "user";

	private final List<UserPropertyHandler> userPropertyHandlers;
	private FormLayoutContainer logCont;
	private FlexiFiltersTab tabLast7Days;
	private FlexiFiltersTab tabLast4Weeks;
	private FlexiFiltersTab tabLast12Month;
	private FlexiFiltersTab tabAll;
	private ActivityLogTableModel dataModel;
	private FlexiTableElement tableEl;

	protected final Formatter formatter;
	private Boolean logOpen = Boolean.FALSE;

	@Autowired
	protected UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;

	public ActivityLogController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, LAYOUT_BAREBONE, null, mainForm);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(ActivityLogController.class, getLocale(), getTranslator()));
		formatter = Formatter.getInstance(getLocale());
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(ActivityLogTableModel.USAGE_IDENTIFIER,
				isAdministrativeUser);
	}
	
	protected abstract SelectionValuesSupplier getActivityFilterValues();
	
	protected abstract List<Identity> getFilterIdentities();
	
	protected abstract List<ActivityLogRow> loadRows(DateRange dateRange, Set<Long> doerKeys);

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Add the table on a custom layout to enable inheritance from other packages.
		String page = Util.getPackageVelocityRoot(ActivityLogController.class) + "/activity_log.html";
		logCont = FormLayoutContainer.createCustomFormLayout("activity.log", getTranslator(), page);
		logCont.setRootForm(mainForm);
		formLayout.add(logCont);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(ActivityLogCols.date.name(), false));

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.date));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.message));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.originalValue));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.newValue));

		int colIndex = ActivityLogTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance()
					.isMandatoryUserProperty(ActivityLogTableModel.USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, true, "userProp-" + colIndex));
		}
		
		dataModel = new ActivityLogTableModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), logCont);
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(true);
		tableEl.setSortSettings(options);

		initFilterTabs(ureq);
		initFilters();
		logCont.getFormItemComponent().contextPut("logOpen", logOpen);
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
		
		SelectionValues identityValues = new SelectionValues();
		List<Identity> filteridentities = getFilterIdentities();
		if (filteridentities != null && !filteridentities.isEmpty()) {
			filteridentities.stream().forEach(identity -> identityValues.add(
					SelectionValues.entry(
							identity.getKey().toString(),
							StringHelper.escapeHtml(userManager.getUserDisplayName(identity.getKey())))));
			identityValues.sort(SelectionValues.VALUE_ASC);
			if (!identityValues.isEmpty()) {
				filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.user"), FILTER_USER,
						identityValues, true));
			}
		}
		
		tableEl.setFilters(true, filters, false, false);
	}

	public void loadModel() {
		DateRange dateRange = getFilterDateRange();
		Set<Long> doerKeys = getFilterIdentityKeys();
		List<ActivityLogRow> rows = loadRows(dateRange, doerKeys);
		
		applyFilters(rows);
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private DateRange getFilterDateRange() {
		if (tableEl.getSelectedFilterTab() != null) {
			if (tableEl.getSelectedFilterTab() == tabLast7Days) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				return new DateRange(DateUtils.addDays(today, -7), DateUtils.addDays(today, 1));
			} else if (tableEl.getSelectedFilterTab() == tabLast4Weeks) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				return new DateRange(DateUtils.addDays(today, -28), DateUtils.addDays(today, 1));
			} else if (tableEl.getSelectedFilterTab() == tabLast12Month) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				return new DateRange(DateUtils.addMonth(today, -12), DateUtils.addDays(today, 1));
			}
		}
		return null;
	}
	
	private Set<Long> getFilterIdentityKeys() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return null;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_USER.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					return values.stream().map(Long::valueOf).collect(Collectors.toSet());
				}
			}
		}
		
		return null;
	}
	
	private void applyFilters(List<ActivityLogRow> rows) {
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
	
	private boolean isSeachStringFound(ActivityLogRow row, String searchString) {
		return (row.getMessage() != null && row.getMessage().toLowerCase().indexOf(searchString) >= 0)
				|| (row.getOriginalValue() != null && row.getOriginalValue().toLowerCase().indexOf(searchString) >= 0)
				|| (row.getNewValue() != null && row.getNewValue().toLowerCase().indexOf(searchString) >=  0);
	}
	
	protected void addActivityFilterValue(SelectionValues filterSV, String messageI18nKey) {
		filterSV.add(entry(messageI18nKey, translate(messageI18nKey)));
	}
	
	protected ActivityLogRow createRow(Identity identity) {
		return new ActivityLogRow(identity, userPropertyHandlers, getLocale());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String logOpenVal = ureq.getParameter("logOpen");
			if (StringHelper.containsNonWhitespace(logOpenVal)) {
				logOpen = Boolean.valueOf(logOpenVal);
				logCont.getFormItemComponent().contextPut("logOpen", logOpen);
			}
		}
		super.event(ureq, source, event);
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
