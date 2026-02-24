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
package org.olat.modules.curriculum.ui.importwizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
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
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
abstract class AbstractImportListController extends StepFormBasicController implements FlexiTableCssDelegate {

	protected static final String IGNORE = "xignore";
	protected static final String CMD_VALIDATION_RESULTS_PREFIX = "ovalidationresults";
	
	protected static final String STATUS_KEY = "status";
	protected static final String IGNORED_KEY = "ignored";
	protected static final String USERNAME_KEY = "username";
	protected static final String CURRICULUM_KEY = "curriculum";
	protected static final String OBJECT_TYPE_KEY = "objecttype";
	protected static final String ORGANISATION_KEY = "organisation";
	protected static final String IMPLEMENTATION_KEY = "implementation";
	
	protected static final String STATUS_NEW = "new";
	protected static final String STATUS_MODIFIED = "modified";
	protected static final String STATUS_WITH_ERRORS = "error";
	protected static final String STATUS_WITH_CHANGES = "change";
	protected static final String STATUS_WITH_WARNINGS = "warning";
	
	protected FlexiFiltersTab allTab;
	protected FlexiFiltersTab newTab;
	protected FlexiFiltersTab ignoredTab;
	protected FlexiFiltersTab modifiedTab;
	protected FlexiFiltersTab withErrorsTab;
	protected FlexiFiltersTab withChangesTab;
	protected FlexiFiltersTab withWarningsTab;
	
	protected FormLink errorsLink;
	protected FlexiTableElement tableEl;
	private DefaultFlexiTableDataModel<? extends AbstractImportRow> tableModel;

	protected int count = 0;
	private final boolean withWarnings;
	private final boolean withChanges;
	
	protected final String id;
	protected final String cmdValidationResults;
	protected final ImportCurriculumsContext context;
	 
	private ToolsController toolsCtrl;
	protected ValidationResultController validationCtrl;
	protected CloseableCalloutWindowController calloutCtrl;
	private ValidationResultListController validationListCtrl;
	
	@Autowired
	protected UserManager userManager;
	
	public AbstractImportListController(UserRequest ureq, WindowControl wControl, Form rootForm, String pageName,
			ImportCurriculumsContext context, StepsRunContext runContext,
			String id, boolean withWarnings, boolean withChanges) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, pageName);
		this.withChanges = withChanges;
		this.withWarnings = withWarnings;
		this.context = context;
		this.id = id;
		cmdValidationResults = CMD_VALIDATION_RESULTS_PREFIX + id;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.rowNum));
		
		initColumns(columnsModel);
		
		ActionsColumnModel actionsCol = new ActionsColumnModel(ImportCurriculumsCols.tools);
        actionsCol.setCellRenderer(new BooleanCellRenderer(new ActionsCellRenderer(getTranslator()), null));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = initTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setSearchEnabled(true);
		tableEl.setCssDelegate(this);
		sortOptions();
	}
	
	protected void sortOptions() {
		//
	}
	
	protected abstract void initColumns(FlexiTableColumnModel columnsModel);
	
	protected abstract DefaultFlexiTableDataModel<? extends AbstractImportRow> initTableModel(FlexiTableColumnModel columnsModel);
	
	protected void initFilterTabs() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("All", translate("search.all"),
				TabSelectionBehavior.reloadData, List.of());
		tabs.add(allTab);

		if(withChanges) {
			modifiedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Modified", translate("search.status.modified"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_MODIFIED)));
			tabs.add(modifiedTab);
		}
		
		newTab = FlexiFiltersTabFactory.tabWithImplicitFilters("New", translate("search.status.new"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_NEW)));
		tabs.add(newTab);
		
		ignoredTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Ignored", translate("search.ignored"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(IGNORED_KEY, IGNORED_KEY)));
		tabs.add(ignoredTab);
		
		withErrorsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Errors", translate("search.status.errors"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_WITH_ERRORS)));
		tabs.add(withErrorsTab);
		
		if(withWarnings) {
			withWarningsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Warnings", translate("search.status.warnings"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_WITH_WARNINGS)));
			tabs.add(withWarningsTab);
		}
		
		if(withChanges) {
			withChangesTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Changes", translate("search.status.changes"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_WITH_CHANGES)));
			tabs.add(withChangesTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		// Ignored
		SelectionValues ignoredKeyValue = new SelectionValues();
		ignoredKeyValue.add(SelectionValues.entry("ignored", translate("search.ignored")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("search.ignored"), IGNORED_KEY, ignoredKeyValue, true));
		
		initFilters(filters);
		
		tableEl.setFilters(true, filters, false, false);
	}

	protected abstract void initFilters(List<FlexiTableExtendedFilter> filters);
	
	protected void filterModel() {
		if(tableModel instanceof FilterableFlexiTableModel filterTableModel) {
			filterTableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
			tableEl.reset(true, true, true);
		}
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		AbstractImportRow importedRow = tableModel.getObject(pos);
		String cssClass = null;
		if(importedRow != null) {
			if(importedRow.getStatus() == ImportCurriculumsStatus.ERROR) {
				cssClass = "o_import_error";
			} else {
				CurriculumImportedStatistics statistics = importedRow.getValidationStatistics();
				if(importedRow.getStatus() == ImportCurriculumsStatus.NEW) {
					if(statistics.errors() > 0) {
						cssClass = "o_import_error";
					} else if(importedRow.isIgnored()) {
						cssClass = "o_import_ignored";
					} else {
						cssClass = "o_import_new";
					}
				} else if(importedRow.isIgnored()) {
					cssClass = "o_import_ignored";
				} else if(statistics.changes() > 0) {
					cssClass = "o_import_changed";
				} 
			}
		}
		return cssClass;
	}
	
	protected void forgeRow(AbstractImportRow row, SelectionValues ignorePK) {
		ImportCurriculumsStatus status = row.getStatus();
		CurriculumImportedStatistics statistics = row.getValidationStatistics();
		
		if(status != null && status != ImportCurriculumsStatus.NO_CHANGES) {
			MultipleSelectionElement ignoreEl = uifactory
					.addCheckboxesHorizontal("ignore_" + (count++), null, flc, ignorePK.keys(), ignorePK.values());
			ignoreEl.setAjaxOnly(true);
			ignoreEl.setUserObject(row);
			row.setIgnoreEl(ignoreEl);
			if(isIgnored(row)) {
				ignoreEl.select(IGNORE, true);
				ignoreEl.setEnabled(false);
			}
		}
			
		if(!statistics.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(statistics.errors());
			if(withWarnings) {
				sb.append("/").append(statistics.warnings());
			}
			if(withChanges) {
				sb.append("/").append(statistics.changes());
			}

			FormLink validationResults = uifactory.addFormLink("results_" + (count++), CMD_VALIDATION_RESULTS_PREFIX + id, sb.toString(), tableEl, Link.LINK | Link.NONTRANSLATED);
			row.setValidationResultsLink(validationResults);
			validationResults.setUserObject(row);
		}
	}
	
	private boolean isIgnored(AbstractImportRow row) {
		CurriculumImportedStatistics statistics = row.getValidationStatistics();
		if(statistics.errors() > 0) {
			return true;
		}
		if(row.getIgnoreEl() != null && row.getIgnoreEl().isAtLeastSelected(1)) {
			return true;
		}
		if(row.getCurriculumElementParentRow() != null && isIgnored(row.getCurriculumElementParentRow())) {
			return true;
		}
		return false;
	}
	
	protected void loadErrorMessage(List<? extends AbstractImportRow> rows, String suffix) {
		long numOfErrors = rows.stream()
				.filter(row -> row.getStatus() == ImportCurriculumsStatus.ERROR || row.getValidationStatistics().errors() > 0)
				.count();

		if(numOfErrors > 0) {
			String i18nKey = suffix + (numOfErrors > 1 ? ".link.plural" : ".link");
			String link = translate(i18nKey, Long.toString(numOfErrors));	
			errorsLink = uifactory.addFormLink(suffix + ".link", link, null, flc, Link.LINK | Link.NONTRANSLATED);
		} else {
			flc.remove("errors.link");
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calloutCtrl == source) {
        	cleanUp();
        } else if(toolsCtrl == source) {
        	if(event == Event.CLOSE_EVENT) {
        		calloutCtrl.deactivate();
        		cleanUp();
        	}
        }
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		calloutCtrl = null;
		toolsCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(errorsLink == source) {
			doFilterErrors(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					AbstractImportRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				} else if(ImportValueCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					AbstractImportRow selectedRow = tableModel.getObject(se.getIndex());
					ImportCurriculumsCols col = ImportValueCellRenderer.getCol(ureq);
					if(col != null) {
						String targetId = ImportValueCellRenderer.getId(se.getIndex(), col);
						if(targetId != null) {
							doOpenValidationCallout(ureq, selectedRow, col, targetId);
						}
					}
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link && cmdValidationResults.equals(link.getCmd())
				&& link.getUserObject() instanceof AbstractImportRow importedRow) {
			doOpenValidationResultsCallout(ureq, importedRow, link.getFormDispatchId());
		} else if(source instanceof MultipleSelectionElement check
				&& check.getUserObject() instanceof ImportedRow importedRow) {
			doIgnore(importedRow, check.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected final void formOK(UserRequest ureq) {
		//
	}
	
	private void doIgnore(ImportedRow importedRow, boolean ignore) {
		List<? extends AbstractImportRow> rows = tableModel.getObjects();
		if(ignore) {
			for(AbstractImportRow row:rows) {
				if(row.getIgnoreEl() == null) continue;

				for(ImportedRow parentRow=row.getCurriculumElementParentRow(); parentRow != null; parentRow=parentRow.getCurriculumElementParentRow()) {
					if(importedRow == parentRow) {
						row.getIgnoreEl().select(IGNORE, true);
						row.getIgnoreEl().setEnabled(false);
					}
				}
			}
		} else {
			recalculateIgnoredEnabled(rows);
		}
	}
	
	private void recalculateIgnoredEnabled(List<? extends AbstractImportRow> rows) {
		for(AbstractImportRow row:rows) {
			if(row.getIgnoreEl() == null) continue;
			
			boolean enabled = !row.hasValidationErrors() && row.getStatus() != ImportCurriculumsStatus.ERROR;
			for(ImportedRow parentRow=row.getCurriculumElementParentRow(); parentRow != null && !parentRow.hasValidationErrors(); parentRow=parentRow.getCurriculumElementParentRow()) {
				enabled &= !parentRow.isIgnored() && !parentRow.hasValidationErrors() && parentRow.getStatus() != ImportCurriculumsStatus.ERROR;
			}
			
			if(enabled && !row.getIgnoreEl().isEnabled()) {
				row.getIgnoreEl().setEnabled(true);
			} else if(!enabled && row.getIgnoreEl().isEnabled()) {
				row.getIgnoreEl().setEnabled(false);
			}
		}
	}
	
	private void doFilterErrors(UserRequest ureq) {
		tableEl.setSelectedFilterTab(ureq, withErrorsTab);
		filterModel();
	}

	private void doOpenValidationCallout(UserRequest ureq, AbstractImportRow row, ImportCurriculumsCols col, String targetId) {
		removeAsListenerAndDispose(validationCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		CurriculumImportedValue value = row.getValidation(col);
		validationCtrl = new ValidationResultController(ureq, getWindowControl(), value);
		listenTo(validationCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				validationCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenValidationResultsCallout(UserRequest ureq, AbstractImportRow row, String targetId) {
		removeAsListenerAndDispose(validationListCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		List<CurriculumImportedValue> values = row.getValues();
		validationListCtrl = new ValidationResultListController(ureq, getWindowControl(), values);
		listenTo(validationListCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				validationListCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, AbstractImportRow row, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doToggleIgnoreEl(AbstractImportRow row, boolean ignore) {
		if(row.getIgnoreEl() == null) return;
		
		if(ignore) {
			row.getIgnoreEl().select(IGNORE, true);
		} else {
			row.getIgnoreEl().uncheckAll();
		}
		tableEl.reset(false, false, true);
	}
	
	private class ToolsController extends BasicController {
		
		private Link ignoreForImportLink;
		private Link includeForImportLink;
		
		private final AbstractImportRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, AbstractImportRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tool_import");
			
			if(row.isIgnored()) {
				includeForImportLink = LinkFactory.createLink("include.for.import", "include.for.import", getTranslator(), mainVC, this, Link.LINK);
				includeForImportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			} else {
				ignoreForImportLink = LinkFactory.createLink("ignore.for.import", "ignore.for.import", getTranslator(), mainVC, this, Link.LINK);
				ignoreForImportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(includeForImportLink == source) {
				doToggleIgnoreEl(row, false);
			} else if(ignoreForImportLink == source) {
				doToggleIgnoreEl(row, true);
			}
		}
	}
}