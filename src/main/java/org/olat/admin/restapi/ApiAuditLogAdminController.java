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
package org.olat.admin.restapi;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.olat.admin.restapi.ApiAuditLogTableModel.ApiAuditLogCols;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.scope.DateScope;
import org.olat.core.gui.components.scope.FormDateScopeSelection;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.restapi.audit.ApiAuditStatusClass;
import org.olat.user.ui.UserDisplayNameCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The audit log of the API: write calls and denied calls of the REST API with
 * filters, a detail view and an Excel export.
 * 
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogAdminController extends FormBasicController {
	
	private static final String SCOPE_TODAY = "today";
	private static final String SCOPE_LAST_7_DAYS = "last7Days";
	private static final String SCOPE_THIS_MONTH = "thisMonth";
	private static final String SCOPE_LAST_MONTH = "lastMonth";
	private static final String CMD_DETAILS = "details";
	
	private FormDateScopeSelection scopeEl;
	private FlexiTableElement tableEl;
	private ApiAuditLogTableModel tableModel;
	private ApiAuditLogDataSource dataSource;
	
	private CloseableModalController cmc;
	private ApiAuditLogDetailsController detailsCtrl;
	
	@Autowired
	private ApiAuditLogService auditLogService;
	
	public ApiAuditLogAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "auditlog");
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("auditlog.title");
		setFormInfo("auditlog.table.intro");
		
		scopeEl = uifactory.addDateScopeSelection(getWindowControl(), "scope", null, formLayout,
				createDateScopes(), getLocale());
		scopeEl.setSelectedKey(SCOPE_LAST_7_DAYS);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.channel,
				new ApiAuditChannelCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.identity,
				UserDisplayNameCellRenderer.get()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.loginAttempt));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.authProvider));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.ip));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.userAgent));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.method));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.path));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.query));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.resourceClass));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.resourceMethod));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.pathParams));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.status,
				new ApiAuditStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ApiAuditLogCols.durationMs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.requestBody));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.ref));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ApiAuditLogCols.nodeId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("auditlog.details", translate("auditlog.details"), CMD_DETAILS));
		
		dataSource = new ApiAuditLogDataSource(auditLogService);
		tableModel = new ApiAuditLogTableModel(dataSource, columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_log")
				.withMessageI18nKey("auditlog.empty")
				.build());
		initFilters();
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(ApiAuditLogCols.creationDate.sortKey(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "rest-api-audit-log-v1");
		
		loadModel();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableTextFilter(translate("auditlog.filter.user"),
				ApiAuditLogDataSource.FILTER_USER, true));
		
		SelectionValues channelValues = new SelectionValues();
		for(ApiAuditChannel channel:ApiAuditChannel.VALUES) {
			channelValues.add(SelectionValues.entry(channel.name(), translate(channel.i18nKey())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("auditlog.filter.channel"),
				ApiAuditLogDataSource.FILTER_CHANNEL, channelValues, false));
		
		SelectionValues methodValues = new SelectionValues();
		for(String method:List.of("GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")) {
			methodValues.add(SelectionValues.entry(method, method));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("auditlog.filter.method"),
				ApiAuditLogDataSource.FILTER_METHOD, methodValues, true));
		
		filters.add(new FlexiTableTextFilter(translate("auditlog.filter.resource"),
				ApiAuditLogDataSource.FILTER_RESOURCE, true));
		
		SelectionValues statusValues = new SelectionValues();
		for(ApiAuditStatusClass statusClass:ApiAuditStatusClass.VALUES) {
			statusValues.add(SelectionValues.entry(statusClass.name(), translate(statusClass.getI18nKey())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("auditlog.filter.status"),
				ApiAuditLogDataSource.FILTER_STATUS, statusValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private List<DateScope> createDateScopes() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		LocalDate firstOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate firstOfNextMonth = today.with(TemporalAdjusters.firstDayOfNextMonth());
		LocalDate firstOfLastMonth = firstOfThisMonth.minusMonths(1);
		
		DateRange todayRange = new DateRange(DateUtils.toDate(today), DateUtils.toDate(tomorrow));
		DateRange last7DaysRange = new DateRange(DateUtils.toDate(today.minusDays(6)), DateUtils.toDate(tomorrow));
		DateRange thisMonthRange = new DateRange(DateUtils.toDate(firstOfThisMonth), DateUtils.toDate(firstOfNextMonth));
		DateRange lastMonthRange = new DateRange(DateUtils.toDate(firstOfLastMonth), DateUtils.toDate(firstOfThisMonth));
		
		return List.of(
				ScopeFactory.createDateScope(SCOPE_TODAY, translate("auditlog.scope.today"), null, todayRange),
				ScopeFactory.createDateScope(SCOPE_LAST_7_DAYS, translate("auditlog.scope.last.7.days"), null, last7DaysRange),
				ScopeFactory.createDateScope(SCOPE_THIS_MONTH, translate("auditlog.scope.this.month"), null, thisMonthRange),
				ScopeFactory.createDateScope(SCOPE_LAST_MONTH, translate("auditlog.scope.last.month"), null, lastMonthRange));
	}
	
	private void loadModel() {
		ApiAuditLogSearchParams searchParams = dataSource.getSearchParams();
		DateRange dateRange = scopeEl.getSelectedDateRange();
		if(dateRange != null) {
			searchParams.setCreatedAfter(dateRange.getFrom());
			searchParams.setCreatedBefore(dateRange.getTo());
		} else {
			searchParams.setCreatedAfter(null);
			searchParams.setCreatedBefore(null);
		}
		dataSource.applyFilters(tableEl.getFilters());
		dataSource.reset();
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(scopeEl == source) {
			loadModel();
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se && CMD_DETAILS.equals(se.getCommand())) {
				doOpenDetails(ureq, tableModel.getObject(se.getIndex()));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == detailsCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(cmc);
		detailsCtrl = null;
		cmc = null;
	}
	
	private void doOpenDetails(UserRequest ureq, ApiAuditLog row) {
		if(row == null) return;
		
		ApiAuditLog auditLog = auditLogService.loadByKey(row.getKey());
		if(auditLog == null) {
			showWarning("auditlog.deleted");
			return;
		}
		
		detailsCtrl = new ApiAuditLogDetailsController(ureq, getWindowControl(), auditLog);
		listenTo(detailsCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				detailsCtrl.getInitialComponent(), true, translate("auditlog.details.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
