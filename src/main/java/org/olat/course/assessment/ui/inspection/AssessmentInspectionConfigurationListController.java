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
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.model.AssessmentInspectionConfigurationWithUsage;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionConfigurationListModel.InspectionCols;
import org.olat.course.assessment.ui.inspection.elements.MinuteCellRenderer;
import org.olat.course.assessment.ui.inspection.elements.ResultsDisplayCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationListController extends FormBasicController {

	private FormLink batchDeleButton;
	private FlexiTableElement tableEl;
	private FormLink addConfigurationButton;
	private TooledStackedPanel toolbarPanel;
	private AssessmentInspectionConfigurationListModel tableModel;
	
	private int counter = 0;
	private final RepositoryEntry entry;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private OptionsCalloutController optionsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CloseableCalloutWindowController optionsCalloutCtrl;
	private AssessmentInspectionConfigurationEditController editCtrl;
	private ConfirmDeleteConfigurationController confirmDeleteConfigCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionConfigurationListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbarPanel, RepositoryEntry entry) {
		super(ureq, wControl, "inspection_configuration_list");
		this.entry = entry;
		this.toolbarPanel = toolbarPanel;
		this.toolbarPanel.addListener(this);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addConfigurationButton = uifactory.addFormLink("add.configuration", formLayout, Link.BUTTON);
		addConfigurationButton.setIconLeftCSS("o_icon o_icon_add");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InspectionCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InspectionCols.duration,
				new MinuteCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InspectionCols.resultsDisplay,
				new ResultsDisplayCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InspectionCols.ips));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InspectionCols.seb,
				new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InspectionCols.usages));
		
		DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel("edit", "", "edit", "o_icon_edit o_icon-fw o_icon-lg");
		editCol.setIconHeader("o_icon o_icon_edit o_icon-fw o_icon-lg");
		editCol.setHeaderTooltip(translate("edit"));
		editCol.setExportable(false);
		editCol.setColumnCssClass("o_col_sticky_right o_col_action");
		columnsModel.addFlexiColumnModel(editCol);
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(InspectionCols.tools);
		toolsCol.setExportable(false);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);

		tableModel = new AssessmentInspectionConfigurationListModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		batchDeleButton = uifactory.addFormLink("delete", "delete", "delete", formLayout, Link.BUTTON);
		tableEl.addBatchButton(batchDeleButton);
	}
	
	private void doFilter() {
		tableModel.filter(tableEl.getQuickSearchString(), null);
		tableEl.reset(true, true, true);
	}
	
	private void loadModel() {
		List<AssessmentInspectionConfigurationWithUsage> configurations = inspectionService.getInspectionConfigurationsWithUsage(entry);
		List<AssessmentInspectionConfigurationRow> rows = new ArrayList<>(configurations.size());
		for(AssessmentInspectionConfigurationWithUsage configuration:configurations) {
			rows.add(forgeConfigurationRow(configuration));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AssessmentInspectionConfigurationRow forgeConfigurationRow(AssessmentInspectionConfigurationWithUsage configurationWithUsage) {
		AssessmentInspectionConfigurationRow row = new AssessmentInspectionConfigurationRow(configurationWithUsage.configuration(),
				configurationWithUsage.usage());
		
		FormLink infosButton = uifactory.addFormLink("infos_" + (++counter), "infos", "", tableEl, Link.LINK | Link.NONTRANSLATED);
		infosButton.setIconLeftCSS("o_icon o_icon-fw o_icon_description");
		infosButton.setUserObject(row);
		row.setInfosButton(infosButton);
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		row.setToolsButton(toolsLink);
		toolsLink.setUserObject(row);
		
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editCtrl) {
			if(event == Event.CANCELLED_EVENT) {
				toolbarPanel.popController(editCtrl);
			}
		} else if(optionsCtrl == source) {
			optionsCalloutCtrl.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(confirmDeleteConfigCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(optionsCalloutCtrl == source || toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteConfigCtrl);
		removeAsListenerAndDispose(optionsCalloutCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(optionsCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteConfigCtrl = null;
		optionsCalloutCtrl = null;
		toolsCalloutCtrl = null;
		optionsCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == toolbarPanel) {
			if(event instanceof PopEvent pe && pe.getController() == editCtrl) {
				loadModel();
			}
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addConfigurationButton == source) {
			doAddConfiguration(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se && "edit".equals(se.getCommand())) {
				AssessmentInspectionConfigurationRow row = tableModel.getObject(se.getIndex());
				doEditConfiguration(ureq, row);
			} else if(event instanceof FlexiTableSearchEvent) {
				doFilter();
			}
		} else if(source instanceof FormLink link) {
			if("infos".equals(link.getCmd())
					&& link.getUserObject() instanceof AssessmentInspectionConfigurationRow configurationRow) {
				doOpenOptions(ureq, configurationRow, link);
			} else if("tools".equals(link.getCmd())
					&& link.getUserObject() instanceof AssessmentInspectionConfigurationRow configurationRow) {
				doOpenTools(ureq, configurationRow);
			} else if("delete".equals(link.getCmd())) {
				doConfirmBatchDelete(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenOptions(UserRequest ureq, AssessmentInspectionConfigurationRow configurationRow, FormLink link) {
		optionsCtrl = new OptionsCalloutController(ureq, getWindowControl(),
				configurationRow.getConfiguration().getOverviewOptionsAsList());
		listenTo(optionsCtrl);
		
		optionsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				optionsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(optionsCalloutCtrl);
		optionsCalloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, AssessmentInspectionConfigurationRow configurationRow) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), configurationRow);
		listenTo(toolsCtrl);
		
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), configurationRow.getToolsButton().getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doAddConfiguration(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);
		
		AssessmentInspectionConfiguration newConfiguration = inspectionService.createInspectionConfiguration(entry);
		editCtrl = new AssessmentInspectionConfigurationEditController(ureq, getWindowControl(), newConfiguration, entry);
		listenTo(editCtrl);
		toolbarPanel.pushController(translate("new.configuration"), editCtrl);
	}
	
	private void doEditConfiguration(UserRequest ureq, AssessmentInspectionConfigurationRow row) {
		removeAsListenerAndDispose(editCtrl);
		
		AssessmentInspectionConfiguration configuration = inspectionService.getConfigurationById(row.getKey());
		editCtrl = new AssessmentInspectionConfigurationEditController(ureq, getWindowControl(), configuration, entry);
		listenTo(editCtrl);
		toolbarPanel.pushController(row.getName(), editCtrl);
	}
	

	private void doConfirmBatchDelete(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(selectedIndexes.size() == 1) {
			AssessmentInspectionConfigurationRow row = tableModel.getObject(selectedIndexes.iterator().next().intValue());
			doConfirmDelete(ureq, row);
		} else {
		
			List<AssessmentInspectionConfiguration> configurations = new ArrayList<>();
			
			int numOfInspections = 0;
			for(Integer selectedIndex:selectedIndexes) {
				AssessmentInspectionConfigurationRow row = tableModel.getObject(selectedIndex.intValue());
				if(row != null) {
					configurations.add(row.getConfiguration());
					numOfInspections += inspectionService.hasInspection(row.getConfiguration());
				}
			}

			if(numOfInspections > 0) {
				String i18n = numOfInspections == 1  ? "warning.configurations.in.use.singular" : "warning.configurations.in.use.plural";
				showWarning(i18n, new String[] { Integer.toString(selectedIndexes.size()), Integer.toString(numOfInspections) });
			} else {
				confirmDeleteConfigCtrl = new ConfirmDeleteConfigurationController(ureq, getWindowControl(), configurations);
				listenTo(confirmDeleteConfigCtrl);
				
				String title = translate("confirm.batch.delete.title", Integer.toString(selectedIndexes.size()));
				cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteConfigCtrl.getInitialComponent(), true, title);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, AssessmentInspectionConfigurationRow row) {
		int numOfInspections = inspectionService.hasInspection(row.getConfiguration());
		if(numOfInspections > 0) {
			String i18n = numOfInspections == 1  ? "warning.configuration.in.use.singular" : "warning.configuration.in.use.plural";
			showWarning(i18n, new String[] { row.getName(), Integer.toString(numOfInspections) });
		} else {
			AssessmentInspectionConfiguration configuration = row.getConfiguration();
			confirmDeleteConfigCtrl = new ConfirmDeleteConfigurationController(ureq, getWindowControl(), List.of(configuration));
			listenTo(confirmDeleteConfigCtrl);
			
			String title = translate("confirm.delete.title", StringHelper.escapeHtml(row.getName()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteConfigCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doDuplicateConfiguration(AssessmentInspectionConfigurationRow row) {
		String name = translate("copy.name", row.getName());
		AssessmentInspectionConfiguration duplicateConfiguration = inspectionService
				.duplicateConfiguration(row.getConfiguration(), name);
		dbInstance.commit();
		loadModel();
		if(duplicateConfiguration != null) {
			showInfo("info.configuration.duplicated", StringHelper.escapeHtml(name));
		}
	}
	
	private static class OptionsCalloutController extends BasicController {
		
		public OptionsCalloutController(UserRequest ureq, WindowControl wControl, List<String> options) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("options_callout");
			
			ResultsDisplayCellRenderer renderer = new ResultsDisplayCellRenderer(getTranslator());
			List<String> translatedOptions = new ArrayList<>(options.size());
			for(String option:options) {
				translatedOptions.add(renderer.translatedOption(option));
			}
			mainVC.contextPut("options", translatedOptions);
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
	
	private class ToolsController extends BasicController {

		private Link editLink;
		private Link duplicateLink;
		private Link deleteLink;
		
		private final AssessmentInspectionConfigurationRow configurationRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, AssessmentInspectionConfigurationRow configurationRow) {
			super(ureq, wControl);
			this.configurationRow = configurationRow;
			
			VelocityContainer mainVC = createVelocityContainer("configuration_tools");
			
			editLink = LinkFactory.createLink("edit", getTranslator(), this);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			mainVC.put("edit.configuration", editLink);
			
			duplicateLink = LinkFactory.createLink("duplicate", getTranslator(), this);
			duplicateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
			mainVC.put("duplicate.configuration", duplicateLink);
			
			deleteLink = LinkFactory.createLink("delete", getTranslator(), this);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("delete.configuration", deleteLink);
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(editLink == source) {
				doEditConfiguration(ureq, configurationRow);
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, configurationRow);
			} else if(duplicateLink == source) {
				doDuplicateConfiguration(configurationRow);
			}
		}
	}
}
