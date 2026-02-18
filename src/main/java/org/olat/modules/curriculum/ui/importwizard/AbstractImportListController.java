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

/**
 * 
 * Initial date: 13 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
abstract class AbstractImportListController extends StepFormBasicController implements FlexiTableCssDelegate {

	protected static final String IGNORE = "xignore";
	protected static final String CMD_VALIDATION_RESULTS = "ovalidationresults";
	
	protected static final String STATUS_KEY = "status";
	protected static final String IGNORED_KEY = "ignored";
	protected static final String CURRICULUM_KEY = "curriculum";
	protected static final String OBJECT_TYPE_KEY = "objecttype";
	protected static final String ORGANISATION_KEY = "organisation";
	
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
	protected ImportCurriculumsReviewTableModel tableModel;

	protected int count = 0;
	protected final ImportCurriculumsContext context;
	
	private ToolsController toolsCtrl;
	private ValidationResultController validationCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ValidationResultListController validationListCtrl;
	
	public AbstractImportListController(UserRequest ureq, WindowControl wControl, Form rootForm, String pageName,
			ImportCurriculumsContext context, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, pageName);
		this.context = context;

	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.rowNum));
		
		initColumns(columnsModel);
		
		ActionsColumnModel actionsCol = new ActionsColumnModel(ImportCurriculumsCols.tools);
        actionsCol.setCellRenderer(new BooleanCellRenderer(new ActionsCellRenderer(getTranslator()), null));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new ImportCurriculumsReviewTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setSearchEnabled(true);
		tableEl.setCssDelegate(this);
	}
	
	protected abstract void initColumns(FlexiTableColumnModel columnsModel);
	
	protected void initFilterTabs() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("All", translate("search.all"),
				TabSelectionBehavior.reloadData, List.of());
		tabs.add(allTab);

		modifiedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Modified", translate("search.status.modified"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_MODIFIED)));
		tabs.add(modifiedTab);
		
		newTab = FlexiFiltersTabFactory.tabWithImplicitFilters("New", translate("search.status.new"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_NEW)));
		tabs.add(newTab);
		
		ignoredTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Ignored", translate("search.ignored"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(IGNORED_KEY, IGNORED_KEY)));
		tabs.add(ignoredTab);
		
		withErrorsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Errors", translate("search.status.errors"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_WITH_ERRORS)));
		tabs.add(withErrorsTab);
		
		withWarningsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Warnings", translate("search.status.warnings"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_WITH_WARNINGS)));
		tabs.add(withWarningsTab);
		
		withChangesTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Changes", translate("search.status.changes"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(STATUS_KEY, STATUS_WITH_CHANGES)));
		tabs.add(withChangesTab);
		
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
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
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
		ImportedRow importedRow = tableModel.getObject(pos);
		String cssClass = null;
		if(importedRow != null) {
			if(importedRow.isIgnored()) {
				cssClass = "o_import_ignored";
			} else if(importedRow.getStatus() == ImportCurriculumsStatus.ERROR) {
				cssClass = "o_import_error";
			} else {
				CurriculumImportedStatistics statistics = importedRow.getValidationStatistics();
				if(importedRow.getStatus() == ImportCurriculumsStatus.NEW) {
					if(statistics.errors() > 0) {
						cssClass = "o_import_error";
					} else {
						cssClass = "o_import_new";
					}
				} else if(statistics.changes() > 0) {
					cssClass = "o_import_changed";
				}
			}
		}
		return cssClass;
	}
	
	protected void forgeRow(ImportedRow row, SelectionValues ignorePK) {
		ImportCurriculumsStatus status = row.getStatus();
		if(status != null && status != ImportCurriculumsStatus.NO_CHANGES) {
			MultipleSelectionElement ignoreEl = uifactory
					.addCheckboxesHorizontal("ignore_" + (count++), null, flc, ignorePK.keys(), ignorePK.values());
			ignoreEl.setAjaxOnly(true);
			row.setIgnoreEl(ignoreEl);
		}
			
		CurriculumImportedStatistics statistics = row.getValidationStatistics();
		if(statistics != null && !statistics.isEmpty()) {
			String link = statistics.toString();
			FormLink validationResults = uifactory.addFormLink("results_" + (count++), CMD_VALIDATION_RESULTS, link, tableEl, Link.LINK | Link.NONTRANSLATED);
			row.setValidationResultsLink(validationResults);
			validationResults.setUserObject(row);
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
					ImportedRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				} else if(ImportValueCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					ImportedRow selectedRow = tableModel.getObject(se.getIndex());
					ImportCurriculumsCols col = ImportValueCellRenderer.getCol(ureq);
					String targetId = ImportValueCellRenderer.getId(se.getIndex(), col);
					if(targetId != null) {
						doOpenValidationCallout(ureq, selectedRow, col, targetId);
					}
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link && CMD_VALIDATION_RESULTS.equals(link.getCmd())
				&& link.getUserObject() instanceof ImportedRow importedRow) {
			doOpenValidationResultsCallout(ureq, importedRow, link.getFormDispatchId());
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doFilterErrors(UserRequest ureq) {
		tableEl.setSelectedFilterTab(ureq, withErrorsTab);
		filterModel();
	}

	private void doOpenValidationCallout(UserRequest ureq, ImportedRow row, ImportCurriculumsCols col, String targetId) {
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
	
	private void doOpenValidationResultsCallout(UserRequest ureq, ImportedRow row, String targetId) {
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
	
	private void doOpenTools(UserRequest ureq, ImportedRow row, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doToggleIgnoreEl(ImportedRow row, boolean ignore) {
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
		
		private final ImportedRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ImportedRow row) {
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