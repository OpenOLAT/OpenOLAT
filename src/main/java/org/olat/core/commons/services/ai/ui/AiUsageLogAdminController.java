/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.ai.ui;

import java.text.Collator;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiUsageLogSearchParams;
import org.olat.core.commons.services.ai.AiUsageLogStatus;
import org.olat.core.commons.services.ai.manager.AiUsageLogDAO;
import org.olat.core.commons.services.ai.model.AiUsageLogStats;
import org.olat.core.commons.services.ai.ui.AiUsageLogTableModel.AiUsageLogCols;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.scope.DateScope;
import org.olat.core.gui.components.scope.FormDateScopeSelection;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.user.ui.UserDisplayNameCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin controller to display the AI usage log.
 *
 * Initial date: 07.04.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiUsageLogAdminController extends FormBasicController {

	private static final String SCOPE_THIS_MONTH = "thisMonth";
	private static final String SCOPE_LAST_MONTH = "lastMonth";
	private static final String SCOPE_THIS_YEAR = "thisYear";
	private static final String SCOPE_LAST_YEAR = "lastYear";

	private FormDateScopeSelection scopeEl;
	private WidgetGroup widgetGroup;
	private FigureWidget totalTokensWidget;
	private FlexiTableElement tableEl;
	private AiUsageLogTableModel tableModel;
	private AiUsageLogSearchParams searchParams;
	private AiUsageLogDataSource dataSource;

	@Autowired
	private AiUsageLogDAO usageLogDAO;

	public AiUsageLogAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "usagelog");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("usagelog");

		scopeEl = uifactory.addDateScopeSelection(getWindowControl(), "scope", null, formLayout,
				createDateScopes(), getLocale());
		scopeEl.setSelectedKey(SCOPE_THIS_MONTH);

		widgetGroup = WidgetFactory.createWidgetGroup("widgets", flc.getFormItemComponent());
		totalTokensWidget = WidgetFactory.createFigureWidget("totalTokensWidget", flc.getFormItemComponent(),
				translate("usagelog.col.totalTokens"), "o_icon_ai");
		totalTokensWidget.setDesc(translate("usagelog.widget.total.tokens.desc"));
		widgetGroup.add(totalTokensWidget);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AiUsageLogCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AiUsageLogCols.aiFeature, new AiFeatureCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.usageContextType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.usageContextId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.identity, UserDisplayNameCellRenderer.get()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.resourceType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.resourceId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.resourceSubId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.locale));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AiUsageLogCols.durationMillis));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AiUsageLogCols.status, new AiStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.errorCode));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.errorMessage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AiUsageLogCols.modelProvider));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AiUsageLogCols.requestModel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.requestTemperature));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.requestTopP));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.requestMaxOutputTokens));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.invocationId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.serviceInterface));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.serviceMethod));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.responseId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.responseModel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.responseFinishReason));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.inputTokens));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.outputTokens));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AiUsageLogCols.totalTokens));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.cachedInputTokens));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.reasoningTokens));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.requestNumMessages));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.requestTextLength));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AiUsageLogCols.cacheCreationInputTokens));

		searchParams = new AiUsageLogSearchParams();
		dataSource = new AiUsageLogDataSource(usageLogDAO, searchParams, getTranslator());
		tableModel = new AiUsageLogTableModel(dataSource, columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		initFilters();

		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(AiUsageLogCols.creationDate.sortKey(), false));
		tableEl.setSortSettings(sortOptions);

		tableEl.setAndLoadPersistedPreferences(ureq, "ai-usage-log-v1");
		loadModel();
	}

	private void initFilters() {
		Collator collator = Collator.getInstance(getLocale());

		SelectionValues featureValues = new SelectionValues();
		for (AiFeature feature : AiFeature.VALUES) {
			featureValues.add(SelectionValues.entry(feature.getType(), translate(feature.getI18nKey())));
		}
		featureValues.sort((a, b) -> collator.compare(a.getValue(), b.getValue()));

		SelectionValues statusValues = new SelectionValues();
		for (AiUsageLogStatus status : AiUsageLogStatus.VALUES) {
			statusValues.add(SelectionValues.entry(status.name(), translate(status.getI18nKey())));
		}
		statusValues.sort((a, b) -> collator.compare(a.getValue(), b.getValue()));

		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableMultiSelectionFilter(translate("usagelog.filter.feature"),
				AiUsageLogDataSource.FILTER_AI_FEATURE, featureValues, true));
		filters.add(new FlexiTableMultiSelectionFilter(translate("usagelog.filter.status"),
				AiUsageLogDataSource.FILTER_STATUS, statusValues, true));
		tableEl.setFilters(true, filters, false, false);
	}

	private List<DateScope> createDateScopes() {
		LocalDate today = LocalDate.now();

		LocalDate firstOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate firstOfNextMonth = today.with(TemporalAdjusters.firstDayOfNextMonth());
		LocalDate firstOfLastMonth = firstOfThisMonth.minusMonths(1);
		LocalDate firstOfThisYear = today.with(TemporalAdjusters.firstDayOfYear());
		LocalDate firstOfNextYear = today.with(TemporalAdjusters.firstDayOfNextYear());
		LocalDate firstOfLastYear = firstOfThisYear.minusYears(1);

		DateRange thisMonthRange = new DateRange(DateUtils.toDate(firstOfThisMonth), DateUtils.toDate(firstOfNextMonth));
		DateRange lastMonthRange = new DateRange(DateUtils.toDate(firstOfLastMonth), DateUtils.toDate(firstOfThisMonth));
		DateRange thisYearRange = new DateRange(DateUtils.toDate(firstOfThisYear), DateUtils.toDate(firstOfNextYear));
		DateRange lastYearRange = new DateRange(DateUtils.toDate(firstOfLastYear), DateUtils.toDate(firstOfThisYear));

		return List.of(
				ScopeFactory.createDateScope(SCOPE_THIS_MONTH, translate("usagelog.scope.this.month"), null, thisMonthRange),
				ScopeFactory.createDateScope(SCOPE_LAST_MONTH, translate("usagelog.scope.last.month"), null, lastMonthRange),
				ScopeFactory.createDateScope(SCOPE_THIS_YEAR, translate("usagelog.scope.this.year"), null, thisYearRange),
				ScopeFactory.createDateScope(SCOPE_LAST_YEAR, translate("usagelog.scope.last.year"), null, lastYearRange)
		);
	}

	private void loadModel() {
		DateRange dateRange = scopeEl.getSelectedDateRange();
		if (dateRange != null) {
			searchParams.setCreatedAfter(dateRange.getFrom());
			searchParams.setCreatedBefore(dateRange.getTo());
		} else {
			searchParams.setCreatedAfter(null);
			searchParams.setCreatedBefore(null);
		}
		dataSource.applyFilters(tableEl.getFilters());
		updateWidget();
		dataSource.reset();
		tableEl.reset(true, true, true);
	}

	private void updateWidget() {
		AiUsageLogStats stats = dataSource.loadStats();
		totalTokensWidget.setValue(String.valueOf(stats.getTotalTokens()));
		widgetGroup.setDirty(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == scopeEl) {
			loadModel();
		} else if (source == tableEl && event instanceof FlexiTableSearchEvent) {
			updateWidget();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
