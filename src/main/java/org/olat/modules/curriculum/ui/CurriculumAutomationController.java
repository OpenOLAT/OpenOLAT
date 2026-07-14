/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.date.RelativeDateElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
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
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumAutomationService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.component.AutomationContextCellRenderer;
import org.olat.modules.curriculum.ui.component.AutomationTargetStatusCellRenderer;
import org.olat.modules.curriculum.ui.component.PlannedExecutionCellRenderer;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reusable form controller for the automation rules table. Intended to be embedded as a
 * nested child (via rootForm + getInitialFormItem()) inside a parent FormBasicController.
 * The parent is responsible for persisting; it reads back via isAutomationEnabled() and
 * getAutomationConfig() in its formOK.
 *
 * Initial date: 2026-06-30<br>
 * @author uhensler, https://www.frentix.com
 */
public class CurriculumAutomationController extends FormBasicController {

	private static final String FILTER_CONTEXT = "Context";
	private static final String FILTER_STATUS = "Status";
	private static final String TAB_ALL = "All";
	private static final String TAB_RELEVANT = "Relevant";
	private static final String TAB_IMPLEMENTATION = "Implementation";
	private static final String TAB_CONTENT = "Content";

	private FormToggle automationEnabledEl;
	private FlexiTableElement automationTable;
	private AutomationRuleTableModel automationTableModel;
	private FlexiFiltersTab relevantTab;

	private CloseableModalController cmc;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private AutomationToolsController toolsCtrl;
	private EditCurriculumAutomationController editRuleCtrl;

	private int automationRowCount = 0;

	private List<CurriculumAutomationConfig> automationConfig;
	private final AutomationFormConfig formConfig;

	@Autowired
	private CurriculumAutomationService automationService;
	@Autowired
	private CurriculumService curriculumService;

	public CurriculumAutomationController(UserRequest ureq, WindowControl wControl,
			Form rootForm, AutomationFormConfig formConfig) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.formConfig = formConfig;
		this.automationConfig = formConfig.initialConfig();
		initForm(ureq);
	}

	public boolean isAutomationEnabled() {
		if (automationEnabledEl.isVisible()) {
			return automationEnabledEl.isOn();
		}
		return true;
	}

	public List<CurriculumAutomationConfig> getAutomationConfig() {
		return automationConfig;
	}

	public void setAutomationConfig(List<CurriculumAutomationConfig> config, boolean enabled) {
		this.automationConfig = config;
		if (automationEnabledEl.isVisible()) {
			automationEnabledEl.toggle(enabled);
		}
		loadAutomationTable();
		updateTableVisibility();
	}

	public void setReadOnly(boolean readOnly) {
		if (automationTableModel != null) {
			for (AutomationRuleRow row : automationTableModel.getBackupRows()) {
				if (row.getRuleEnabledEl() != null) {
					row.getRuleEnabledEl().setEnabled(!readOnly);
				}
				if (row.getToolsLink() != null) {
					row.getToolsLink().setVisible(!readOnly);
				}
			}
			automationTable.reset(false, false, false);
		}
	}

	public void recomputeDefaults() {
		if (automationConfig != null || formConfig.defaultConfigSupplier() == null) {
			return;
		}
		if (automationEnabledEl.isOn()) {
			automationConfig = formConfig.defaultConfigSupplier().get();
			loadAutomationTable();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean isImplOrElem = EditCurriculumElementTypeController.FOR_USE_AS_IMPL_OR_ELEM.equals(formConfig.forUseAs());

		FormLayoutContainer automationEnableContainer = FormLayoutContainer.createDefaultFormLayout("automationEnableWrapper", getTranslator());
		automationEnableContainer.setRootForm(mainForm);
		formLayout.add(automationEnableContainer);
		automationEnabledEl = uifactory.addToggleButton("automation.enable", "automation.enable", null, null, automationEnableContainer);
		automationEnabledEl.addActionListener(FormEvent.ONCHANGE);
		automationEnabledEl.setVisible(formConfig.showEnableToggle() && !isImplOrElem);
		if (automationEnabledEl.isVisible()) {
			automationEnabledEl.toggle(formConfig.initialEnabled());
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.context,
				new AutomationContextCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.automationType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.targetStatus,
				new AutomationTargetStatusCellRenderer(getTranslator(), true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.condition));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(statusIsHeaderKey(),
				AutomationCols.statusIs.ordinal()));
		if (formConfig.automationElement() != null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.plannedExecution,
					new PlannedExecutionCellRenderer(Formatter.getInstance(getLocale()))));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.executionDate));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.rule));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(AutomationCols.tools));

		FormLayoutContainer automationTableCont = FormLayoutContainer.createBareBoneFormLayout("automationTableWrapper", getTranslator());
		automationTableCont.setRootForm(mainForm);
		formLayout.add(automationTableCont);

		automationTableModel = new AutomationRuleTableModel(columnsModel, getTranslator());
		automationTable = uifactory.addTableElement(getWindowControl(), "automationRules",
				automationTableModel, getTranslator(), automationTableCont);
		automationTable.setElementCssClass("o_block_large_bottom");
		automationTable.setExportEnabled(true);

		if (!isImplOrElem) {
			initAutomationFilters();
			initAutomationFilterTabs();
			loadAutomationTable();
			automationTable.setSelectedFilterTab(ureq, relevantTab);
		}
		updateTableVisibility();
	}

	private String statusIsHeaderKey() {
		boolean isImplType = EditCurriculumElementTypeController.FOR_USE_AS_IMPL.equals(formConfig.forUseAs());
		return isImplType ? "automation.col.status.is.implementation" : "automation.col.status.is.element";
	}

	private void initAutomationFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues contextValues = new SelectionValues();
		for (AutomationContext context : AutomationContext.values()) {
			contextValues.add(SelectionValues.entry(context.name(), translate("automation.context." + context.name().toLowerCase())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("automation.filter.context"), FILTER_CONTEXT, contextValues, true));

		SelectionValues statusValues = new SelectionValues();
		for (CurriculumElementStatus status : CurriculumElementStatus.selectableAdmin()) {
			statusValues.add(SelectionValues.entry(status.name(), translate("status." + status.name())));
		}
		Translator repoTranslator = Util.createPackageTranslator(RepositoryEntryStatusEnum.class, getLocale(), getTranslator());
		for (RepositoryEntryStatusEnum status : RepositoryEntryStatusEnum.preparationToClosed()) {
			statusValues.add(SelectionValues.entry(status.name(), repoTranslator.translate(status.i18nKey())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("automation.filter.status"), FILTER_STATUS, statusValues, true));

		automationTable.setFilters(true, filters, true, false);
	}

	private void initAutomationFilterTabs() {
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tab(TAB_ALL, translate("automation.filter.all"), TabSelectionBehavior.nothing);
		relevantTab = FlexiFiltersTabFactory.tab(TAB_RELEVANT, translate("automation.filter.relevant"), TabSelectionBehavior.nothing);
		boolean isElemType = EditCurriculumElementTypeController.FOR_USE_AS_ELEM.equals(formConfig.forUseAs());
		String implTabLabel = isElemType ? translate("automation.filter.element") : translate("automation.filter.implementation");
		AutomationContext implTabContext = isElemType ? AutomationContext.ELEMENT : AutomationContext.IMPLEMENTATION;
		FlexiFiltersTab implementationTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_IMPLEMENTATION,
				implTabLabel, TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_CONTEXT, implTabContext.name())));
		FlexiFiltersTab contentTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_CONTENT,
				translate("automation.filter.content"), TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_CONTEXT, AutomationContext.CONTENT.name())));
		automationTable.setFilterTabs(true, List.of(allTab, relevantTab, implementationTab, contentTab));
	}

	private void loadAutomationTable() {
		List<AutomationRuleRow> rows = new ArrayList<>();
		if (automationConfig != null) {
			Map<CurriculumAutomationConfig, Date> executionDates = formConfig.automationElement() != null
					? automationService.getExecutionDates(formConfig.automationElement(), automationConfig)
					: Map.of();
			Map<CurriculumAutomationConfig, Date> plannedDates = formConfig.automationElement() != null
					? automationService.getPlannedExecutionDates(formConfig.automationElement(), automationConfig)
					: Map.of();
			for (CurriculumAutomationConfig config : automationConfig) {
				rows.add(forgeAutomationRow(config, plannedDates.get(config), executionDates.get(config)));
			}
		}
		automationTableModel.setObjects(rows);
		automationTable.reset(true, true, true);
	}

	private AutomationRuleRow forgeAutomationRow(CurriculumAutomationConfig config, Date plannedExecution, Date executionDate) {
		AutomationRuleRow row = new AutomationRuleRow(config);
		row.setPlannedExecution(plannedExecution);
		row.setExecutionDate(executionDate);
		FormToggle ruleEl = uifactory.addToggleButton("rule_" + (++automationRowCount), null,
				translate("on"), translate("off"), null);
		ruleEl.toggle(config.isEnabled());
		ruleEl.addActionListener(FormEvent.ONCHANGE);
		ruleEl.setUserObject(row);
		row.setRuleEnabledEl(ruleEl);
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		return row;
	}

	private void updateTableVisibility() {
		boolean isImplOrElem = EditCurriculumElementTypeController.FOR_USE_AS_IMPL_OR_ELEM.equals(formConfig.forUseAs());
		if (isImplOrElem) {
			automationTable.setVisible(false);
		} else if (formConfig.showEnableToggle()) {
			automationTable.setVisible(automationEnabledEl.isOn());
		} else {
			automationTable.setVisible(automationTableModel != null && !automationTableModel.getBackupRows().isEmpty());
		}
	}

	private void doOpenAutomationTools(UserRequest ureq, AutomationRuleRow row, FormLink link) {
		toolsCtrl = new AutomationToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doEditRule(UserRequest ureq, CurriculumAutomationRule rule) {
		CurriculumElement element = formConfig.automationElement();
		if (element != null) {
			element = curriculumService.getCurriculumElement(element);
		}
		editRuleCtrl = new EditCurriculumAutomationController(ureq, getWindowControl(), rule, formConfig.implType(), element);
		listenTo(editRuleCtrl);
		String title = translate("automation.rule.edit.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editRuleCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editRuleCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		editRuleCtrl = null;
		toolsCtrl = null;
		toolsCalloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editRuleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadAutomationTable();
				fireEvent(ureq, FormEvent.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (toolsCtrl == source || toolsCalloutCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (automationEnabledEl == source) {
			if (automationEnabledEl.isOn() && automationConfig == null
					&& formConfig.defaultConfigSupplier() != null) {
				automationConfig = formConfig.defaultConfigSupplier().get();
				loadAutomationTable();
			}
			updateTableVisibility();
			fireEvent(ureq, FormEvent.CHANGED_EVENT);
		} else if (automationTable == source) {
			if (event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				automationTableModel.filter(automationTable.getQuickSearchString(), automationTable.getFilters());
				automationTable.reset(true, true, false);
			}
		} else if (source instanceof FormToggle tg && tg.getUserObject() instanceof AutomationRuleRow r) {
			r.getConfig().setEnabled(tg.isOn());
			loadAutomationTable();
			fireEvent(ureq, FormEvent.CHANGED_EVENT);
		} else if (source instanceof FormLink link && "tools".equals(link.getCmd())
				&& link.getUserObject() instanceof AutomationRuleRow r) {
			doOpenAutomationTools(ureq, r, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc instanceof FormLink || fiSrc == automationTable) {
			return;
		}
		super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}

	private class AutomationRuleTableModel extends DefaultFlexiTableDataModel<AutomationRuleRow>
			implements FilterableFlexiTableModel {

		private static final AutomationCols[] COLS = AutomationCols.values();

		private final Translator translator;
		private List<AutomationRuleRow> backupRows = List.of();

		public AutomationRuleTableModel(FlexiTableColumnModel columnsModel, Translator translator) {
			super(columnsModel);
			Translator withDate = Util.createPackageTranslator(RelativeDateElement.class,
					translator.getLocale(), translator);
			this.translator = Util.createPackageTranslator(RepositoryEntryStatusEnum.class,
					translator.getLocale(), withDate);
		}

		public List<AutomationRuleRow> getBackupRows() {
			return backupRows;
		}

		@Override
		public void setObjects(List<AutomationRuleRow> objects) {
			backupRows = objects;
			super.setObjects(objects);
		}

		@Override
		public void filter(String searchString, List<FlexiTableFilter> filters) {
			List<AutomationRuleRow> rows = backupRows;

			FlexiTableFilter contextFilter = FlexiTableFilter.getFilter(filters, FILTER_CONTEXT);
			if (contextFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if (values != null && !values.isEmpty()) {
					rows = rows.stream()
							.filter(r -> values.contains(r.getContext().name()))
							.toList();
				}
			}

			FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, FILTER_STATUS);
			if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if (values != null && !values.isEmpty()) {
					rows = rows.stream()
							.filter(r -> {
								Object ts = r.getTargetStatus();
								if (ts instanceof CurriculumElementStatus ces) {
									return values.contains(ces.name());
								} else if (ts instanceof RepositoryEntryStatusEnum res) {
									return values.contains(res.name());
								} else if (ts instanceof String s) {
									return values.contains(s);
								}
								return false;
							})
							.toList();
				}
			}

			if (relevantTab != null && relevantTab.equals(automationTable.getSelectedFilterTab())) {
				rows = rows.stream().filter(AutomationRuleRow::isEnabled).toList();
			}

			super.setObjects(rows);
		}

		@Override
		public Object getValueAt(int row, int col) {
			AutomationRuleRow ruleRow = getObject(row);
			return switch (COLS[col]) {
				case context -> ruleRow.getContext();
				case automationType -> translator.translate("automation.type." + ruleRow.getAutomationType().name().toLowerCase());
				case targetStatus -> ruleRow.getTargetStatus();
				case condition -> conditionText(ruleRow.getRule());
				case statusIs -> joinStatuses(ruleRow.getRule().getOnlyWhenStatus());
				case plannedExecution -> ruleRow;
				case executionDate -> ruleRow.getExecutionDate();
				case rule -> ruleRow.getRuleEnabledEl();
				case tools -> ruleRow.getToolsLink();
			};
		}

		private String conditionText(CurriculumAutomationRule rule) {
			return CurriculumUIFactory.translateAutomationCondition(translator, rule);
		}

		private String joinStatuses(Set<String> statuses) {
			if (statuses == null || statuses.isEmpty()) {
				return "-";
			}
			return Arrays.stream(CurriculumElementStatus.values())
					.filter(s -> statuses.contains(s.name()))
					.map(s -> CurriculumUIFactory.translateAutomationStatus(getTranslator(), s.name()))
					.collect(Collectors.joining(", "));
		}

	}

	private enum AutomationCols implements FlexiColumnDef {
		context("automation.col.context"),
		automationType("automation.col.automation"),
		targetStatus("automation.col.target.status"),
		condition("automation.col.condition"),
		statusIs("automation.col.status.is.element"),
		plannedExecution("automation.col.planned.execution"),
		executionDate("automation.col.execution.date"),
		rule("automation.col.rule"),
		tools("action.more");

		private final String i18nKey;

		AutomationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

	private class AutomationToolsController extends BasicController {

		private final AutomationRuleRow row;
		private final Link editLink;

		public AutomationToolsController(UserRequest ureq, WindowControl wControl, AutomationRuleRow row) {
			super(ureq, wControl);
			this.row = row;
			VelocityContainer mainVC = createVelocityContainer("tool_automation_rule");
			editLink = LinkFactory.createLink("automation.rule.edit", "automation.rule.edit",
					getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, org.olat.core.gui.components.Component source, Event event) {
			if (editLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doEditRule(ureq, row.getRule());
			}
		}
	}
}
