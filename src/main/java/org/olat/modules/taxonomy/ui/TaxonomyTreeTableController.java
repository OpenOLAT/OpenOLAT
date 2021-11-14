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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;
import org.olat.modules.taxonomy.ui.TaxonomyTreeTableModel.TaxonomyLevelCols;
import org.olat.modules.taxonomy.ui.events.DeleteTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.MoveTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.NewTaxonomyLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeTableController extends FormBasicController implements BreadcrumbPanelAware, Activateable2 {
	
	private FormLink newLevelButton;
	private FormLink deleteButton;
	private FormLink mergeButton;
	private FormLink typeButton;
	private FormLink moveButton;
	private FormLink importButton;
	private FlexiTableElement tableEl;
	private TaxonomyTreeTableModel model;
	private BreadcrumbPanel stackPanel;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private MergeTaxonomyLevelController mergeCtrl;
	private TypeTaxonomyLevelController typeLevelCtrl;
	private MoveTaxonomyLevelController moveLevelCtrl;
	private DeleteTaxonomyLevelController confirmDeleteCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditTaxonomyLevelController createTaxonomyLevelCtrl;
	private StepsMainRunController importWizardCtrl;
	
	private int counter = 0;
	private Taxonomy taxonomy;
	private boolean dirty = false;
	
	@Autowired
	private TaxonomyService taxonomyService;

	public TaxonomyTreeTableController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl, "admin_taxonomy_levels");
		this.taxonomy = taxonomy;
		initForm(ureq);
		loadModel(true, true);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		stackPanel.removeListener(this);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		newLevelButton = uifactory.addFormLink("add.taxonomy.level", formLayout, Link.BUTTON);
		newLevelButton.setElementCssClass("o_sel_taxonomy_new_level");
		importButton = uifactory.addFormLink("import.taxonomy.levels", formLayout, Link.BUTTON);
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		mergeButton = uifactory.addFormLink("merge.taxonomy.level", formLayout, Link.BUTTON);
		typeButton = uifactory.addFormLink("type.taxonomy.level", formLayout, Link.BUTTON);
		moveButton = uifactory.addFormLink("move.taxonomy.level", formLayout, Link.BUTTON);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyLevelCols.key));
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer("select");
		treeNodeRenderer.setFlatBySearchAndFilter(true);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.identifier, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.externalId, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.typeIdentifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.numOfChildren));
		DefaultFlexiColumnModel selectColumn = new DefaultFlexiColumnModel("zoom", translate("zoom"), "tt-focus");
		selectColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(selectColumn);
		DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(TaxonomyLevelCols.tools);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);

		model = new TaxonomyTreeTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_taxonomy_level_listing");
		tableEl.setEmptyTableMessageKey("table.taxonomy.level.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(24);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setFilters(null, getFilters(), true);
		tableEl.setRootCrumb(new TaxonomyCrumb(taxonomy.getDisplayName()));
		tableEl.setAndLoadPersistedPreferences(ureq, "tax-tree-" + taxonomy.getKey());

		tableEl.addBatchButton(typeButton);
		tableEl.addBatchButton(moveButton);
		tableEl.addBatchButton(mergeButton);
		tableEl.addBatchButton(deleteButton);
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<TaxonomyLevelType> types = taxonomyService.getTaxonomyLevelTypes(taxonomy);
		List<FlexiTableFilter> resources = new ArrayList<>(types.size() + 1);
		for(TaxonomyLevelType type:types) {
			resources.add(new FlexiTableFilter(type.getDisplayName(), type.getKey().toString()));
		}
		resources.add(new FlexiTableFilter(translate("filter.no.level.type"), "-"));
		resources.add(FlexiTableFilter.SPACER);
		resources.add(new FlexiTableFilter(translate("show.all"), "all", true));
		return resources;
	}
	
	private void loadModel(boolean resetPage, boolean resetInternal) {
		TaxonomyLevelSearchParameters searchParams = new TaxonomyLevelSearchParameters();
		searchParams.setQuickSearch(tableEl.getQuickSearchString());
		List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomy, searchParams);
		List<TaxonomyLevelRow> rows = new ArrayList<>(taxonomyLevels.size());
		Map<Long,TaxonomyLevelRow> levelToRows = new HashMap<>();
		for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
			TaxonomyLevelRow row = forgeRow(taxonomyLevel);
			rows.add(row);
			levelToRows.put(taxonomyLevel.getKey(), row);
		}
		
		for(TaxonomyLevelRow row:rows) {
			Long parentLevelKey = row.getParentLevelKey();
			TaxonomyLevelRow parentRow = levelToRows.get(parentLevelKey);
			row.setParent(parentRow);
		}
		
		for(TaxonomyLevelRow row:rows) {
			for(FlexiTreeTableNode parent=row.getParent(); parent != null; parent=parent.getParent()) {
				((TaxonomyLevelRow)parent).incrementNumberOfChildren();
			}
		}
		
		Collections.sort(rows, new FlexiTreeNodeComparator());

		model.setObjects(rows);
		tableEl.reset(resetPage, resetInternal, true);
	}
	
	private TaxonomyLevelRow forgeRow(TaxonomyLevel taxonomyLevel) {
		//tools
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		TaxonomyLevelRow row = new TaxonomyLevelRow(taxonomyLevel, toolsLink);
		toolsLink.setUserObject(row);
		return row;
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("TaxonomyLevel".equalsIgnoreCase(type)) {
			Long levelKey = entries.get(0).getOLATResourceable().getResourceableId();
			List<TaxonomyLevelRow> rows = model.getObjects();
			for(TaxonomyLevelRow row:rows) {
				if(levelKey.equals(row.getKey())) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					TaxonomyLevelOverviewController ctrl = doSelectTaxonomyLevel(ureq, row);
					if(ctrl != null) {
						ctrl.activate(ureq, subEntries, entries.get(0).getTransientState());
					}
					break;
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newLevelButton == source) {
			doNewLevel(ureq);
		} else if(deleteButton == source) {
			doConfirmMultiDelete(ureq);
		} else if(mergeButton == source) {
			doMerge(ureq);
		} else if(typeButton == source) {
			doAssignType(ureq);
		} else if(moveButton == source) {
			doMove(ureq);
		} else if(importButton == source) {
			doOpenImportWizard(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					TaxonomyLevelRow row = model.getObject(se.getIndex());
					doSelectTaxonomyLevel(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel(true, true);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				TaxonomyLevelRow row = (TaxonomyLevelRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(dirty && event instanceof PopEvent) {
				loadModel(false, false);
				dirty = false;
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof TaxonomyLevelOverviewController) {
			if(event instanceof DeleteTaxonomyLevelEvent || event == Event.CANCELLED_EVENT) {
				stackPanel.popController(source);
				loadModel(false, false);
			} else if(event instanceof NewTaxonomyLevelEvent) {
				stackPanel.popController(source);
				loadModel(false, false);
				doSelectTaxonomyLevel(ureq, ((NewTaxonomyLevelEvent)event).getTaxonomyLevel());
			}  else if(event instanceof MoveTaxonomyLevelEvent) {
				stackPanel.popController(source);
				loadModel(true, true);
				doSelectTaxonomyLevel(ureq, ((MoveTaxonomyLevelEvent)event).getTaxonomyLevel());
			} else if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				dirty = true;
			}
		} else if(createTaxonomyLevelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(false, false);
				if(createTaxonomyLevelCtrl.getParentLevel() != null) {
					int openIndex = model.indexOf(createTaxonomyLevelCtrl.getParentLevel());
					model.open(openIndex);
					tableEl.reset(false, false, true);
				}
				doSelectTaxonomyLevel(ureq, createTaxonomyLevelCtrl.getTaxonomyLevel());
			}
			cmc.deactivate();
			cleanUp();
		} else if(mergeCtrl == source || typeLevelCtrl == source || moveLevelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event instanceof DeleteTaxonomyLevelEvent) {
				loadModel(true, true);
			}
			cmc.deactivate();
			cleanUp();
		}  else if(confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event instanceof DeleteTaxonomyLevelEvent) {
				loadModel(true, true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(importWizardCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
	            // Close the dialog
	            getWindowControl().pop();

	            // Remove steps controller
	            cleanUp();

	            // Reload data
	            loadModel(true, true);
	        }
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(createTaxonomyLevelCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(importWizardCtrl);
		removeAsListenerAndDispose(moveLevelCtrl);
		removeAsListenerAndDispose(typeLevelCtrl);
		removeAsListenerAndDispose(mergeCtrl);
		removeAsListenerAndDispose(cmc);
		createTaxonomyLevelCtrl = null;
		confirmDeleteCtrl = null;
		importWizardCtrl = null;
		moveLevelCtrl = null;
		typeLevelCtrl = null;
		mergeCtrl = null;
		cmc = null;
	}
	
	private void doOpenImportWizard(UserRequest ureq) {
		// Create context wrapper (used to transfer data from step to step)
        TaxonomyImportContext context = new TaxonomyImportContext();
        context.setTaxonomy(taxonomy);

        // Create first step and finish callback
        Step importStep = new TaxonomyImportStep1(ureq, context);
        FinishedCallback finish = new FinishedCallback();
        CancelCallback cancel = new CancelCallback();

        // Create step controller
        importWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), importStep, finish, cancel, translate("import.taxonomy"), null);
        listenTo(importWizardCtrl);
        getWindowControl().pushAsModalDialog(importWizardCtrl.getInitialComponent());
	}
	
	private void doAssignType(UserRequest ureq) {
		if(guardModalController(typeLevelCtrl)) return;
		
		List<TaxonomyLevel> levelsToMerge = getSelectedTaxonomyLevels(TaxonomyLevelManagedFlag.type);
		if(levelsToMerge.isEmpty()) {
			showWarning("warning.atleastone.level");
		} else {
			typeLevelCtrl = new TypeTaxonomyLevelController(ureq, getWindowControl(), levelsToMerge, taxonomy);
			listenTo(typeLevelCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", typeLevelCtrl.getInitialComponent(),
					true, translate("type.taxonomy.level"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doMerge(UserRequest ureq) {
		if(guardModalController(mergeCtrl)) return;
		
		List<TaxonomyLevel> levelsToMerge = getSelectedTaxonomyLevels(TaxonomyLevelManagedFlag.delete);
		if(levelsToMerge.isEmpty()) {
			showWarning("warning.atleastone.level");
		} else {
			mergeCtrl = new MergeTaxonomyLevelController(ureq, getWindowControl(), levelsToMerge, taxonomy);
			listenTo(mergeCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", mergeCtrl.getInitialComponent(),
					true, translate("merge.taxonomy.level"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private class FinishedCallback implements StepRunnerCallback {
	    @Override
	    public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
	        TaxonomyImportContext context = (TaxonomyImportContext) runContext.get(TaxonomyImportContext.CONTEXT_KEY);
	         
	        // Collect the created types for the next step
	        List<TaxonomyLevelType> createdTypes = new ArrayList<>();
	        for (TaxonomyLevelType newLevelType : context.getTaxonomyLevelTypeCreateList()) {
	        	createdTypes.add(taxonomyService.createTaxonomyLevelType(newLevelType.getIdentifier(), newLevelType.getIdentifier(), null, null, true, context.getTaxonomy()));
	        }
	        
	        // Collect created levels to find parents 
	        List<TaxonomyLevel> createdLevels = new ArrayList<>();
	        for (TaxonomyLevel newLevel : context.getTaxonomyLevelCreateList()) {
	        	TaxonomyLevel parent = null;
	        	if (newLevel.getParent() != null) {
		        	// Check whether already existing
		        	if (newLevel.getParent().getKey() != null) {
		        		// Take existing Level
		        		parent = newLevel.getParent();
		        	} else {
		        		// Parent cannot throw exception because it must have been created already
		        		parent = createdLevels.stream().filter(level -> level.getIdentifier().equals(newLevel.getParent().getIdentifier())).collect(Collectors.toList()).get(0);
		        	}
	        	}
	        	TaxonomyLevel createdLevel = taxonomyService.createTaxonomyLevel(newLevel.getIdentifier(), newLevel.getDisplayName(), newLevel.getDescription(), null, null, parent, context.getTaxonomy());
	        	createdLevel.setSortOrder(newLevel.getSortOrder());
	        	
	        	if (newLevel.getType() != null) {
	        		TaxonomyLevelType levelType = null;
	        		
	        		if (newLevel.getType().getKey() == null) {
	        			levelType = createdTypes.stream().filter(type -> type.getIdentifier().equals(newLevel.getType().getIdentifier())).findFirst().orElse(null);
	        		} else {
	        			levelType = newLevel.getType();
	        		}
	        		
	        		createdLevel.setType(levelType);
	        	}	        	
	        	
	        	createdLevel = taxonomyService.updateTaxonomyLevel(createdLevel);
	        	createdLevels.add(createdLevel);
	        }
	        
	        // Update existing taxonomies if needed
	        if (context.isUpdatateExistingTaxonomies()) {
		        for (TaxonomyLevel updateLevel : context.getTaxonomyLevelUpdateList()) {		        	
		        	if (updateLevel.getType() != null) {
		        		TaxonomyLevelType levelType = null;
		        		
		        		if (updateLevel.getType().getKey() == null) {
		        			levelType = createdTypes.stream().filter(type -> type.getIdentifier().equals(updateLevel.getType().getIdentifier())).findFirst().orElse(null);
		        		} else {
		        			levelType = updateLevel.getType();
		        		}
		        		
		        		updateLevel.setType(levelType);
		        	}
		        	
		        	TaxonomyLevel savedUpdateLevel = updateLevel;
		        	taxonomyService.updateTaxonomyLevel(savedUpdateLevel);
		        }
	        }
	    	
	    	return StepsMainRunController.DONE_MODIFIED;
	    }
	}
	    
    private static class CancelCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            return Step.NOSTEP;
        }
    }

	private TaxonomyLevelOverviewController doSelectTaxonomyLevel(UserRequest ureq, TaxonomyLevelRow row) {
		TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(row);
		return doSelectTaxonomyLevel(ureq, taxonomyLevel);
	}

	private TaxonomyLevelOverviewController doSelectTaxonomyLevel(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		if(taxonomyLevel == null) {
			showWarning("warning.taxonomy.level.deleted");
			loadModel(false, false);
			return null;
		} else {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("TaxonomyLevel", taxonomyLevel.getKey());
			WindowControl bwControl = addToHistory(ureq, ores, null);
			TaxonomyLevelOverviewController detailsLevelCtrl = new TaxonomyLevelOverviewController(ureq, bwControl, taxonomyLevel);
			listenTo(detailsLevelCtrl);
			stackPanel.pushController(taxonomyLevel.getDisplayName(), detailsLevelCtrl);
			return detailsLevelCtrl;
		}
	}
	
	private void doNewLevel(UserRequest ureq) {
		doCreateTaxonomyLevel(ureq, null);
	}
	
	private void doCreateTaxonomyLevel(UserRequest ureq, TaxonomyLevel parentLevel) {
		if(guardModalController(createTaxonomyLevelCtrl)) return;

		createTaxonomyLevelCtrl = new EditTaxonomyLevelController(ureq, getWindowControl(), parentLevel, taxonomy);
		listenTo(createTaxonomyLevelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", createTaxonomyLevelCtrl.getInitialComponent(), true, translate("add.taxonomy.level"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenTools(UserRequest ureq, TaxonomyLevelRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		TaxonomyLevel level = taxonomyService.getTaxonomyLevel(row);
		if(level == null) {
			tableEl.reloadData();
			showWarning("warning.taxonomy.level.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, level);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	

	private void doConfirmMultiDelete(UserRequest ureq) {
		Set<Integer> indexList = tableEl.getMultiSelectedIndex();
		List<TaxonomyLevel> levelsToDelete = new ArrayList<>(indexList.size());
		for(Integer index:indexList) {
			TaxonomyLevelRow row = model.getObject(index.intValue());
			if(!TaxonomyLevelManagedFlag.isManaged(row.getManagedFlags(), TaxonomyLevelManagedFlag.delete)) {
				TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(row);
				if(taxonomyLevel != null) {
					levelsToDelete.add(taxonomyLevel);
				}
			}
		}
		
		if(levelsToDelete.isEmpty()) {
			showWarning("warning.atleastone.level");
		} else {
			confirmDeleteCtrl = new DeleteTaxonomyLevelController(ureq, getWindowControl(), levelsToDelete, taxonomy);
			listenTo(confirmDeleteCtrl);
			
			String title = translate("confirmation.delete.level.title");
			cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, TaxonomyLevelRow row) {
		if(TaxonomyLevelManagedFlag.isManaged(row.getManagedFlags(), TaxonomyLevelManagedFlag.delete)) {
			showWarning("warning.atleastone.level");
			return;
		}
		
		TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(row);
		List<TaxonomyLevel> levelToDelete = Collections.singletonList(taxonomyLevel);
		confirmDeleteCtrl = new DeleteTaxonomyLevelController(ureq, getWindowControl(), levelToDelete, taxonomy);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("confirmation.delete.level.title");
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMove(UserRequest ureq) {
		if(moveLevelCtrl != null) return;
		
		List<TaxonomyLevel> levelsToMove = getSelectedTaxonomyLevels(TaxonomyLevelManagedFlag.move);
		doMove(ureq, levelsToMove);
	}
	
	private void doMove(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		if(moveLevelCtrl != null) return;
		
		List<TaxonomyLevel> levelsToMove = Collections.singletonList(taxonomyLevel);
		doMove(ureq, levelsToMove);
	}

	private void doMove(UserRequest ureq, List<TaxonomyLevel> taxonomyLevels) {
		moveLevelCtrl = new MoveTaxonomyLevelController(ureq, getWindowControl(), taxonomyLevels, taxonomy);
		listenTo(moveLevelCtrl);
		
		String title = translate("move.taxonomy.levels.title");
		cmc = new CloseableModalController(getWindowControl(), "close", moveLevelCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private List<TaxonomyLevel> getSelectedTaxonomyLevels(TaxonomyLevelManagedFlag flag) {
		Set<Integer> indexList = tableEl.getMultiSelectedIndex();
		List<TaxonomyLevel> allowedLevels = new ArrayList<>(indexList.size());
		for(Integer index:indexList) {
			TaxonomyLevelRow row = model.getObject(index.intValue());
			if(!TaxonomyLevelManagedFlag.isManaged(row.getManagedFlags(), flag)) {
				TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(row);
				if(taxonomyLevel != null) {
					allowedLevels.add(taxonomyLevel);
				}
			}
		}
		return allowedLevels;
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link editLink, moveLink, newLink, deleteLink;
		
		private TaxonomyLevelRow row;
		private TaxonomyLevel taxonomyLevel;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, TaxonomyLevelRow row, TaxonomyLevel taxonomyLevel) {
			super(ureq, wControl);
			this.row = row;
			this.taxonomyLevel = taxonomyLevel;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(6);
			
			//edit
			editLink = addLink("edit", "o_icon_edit", links);
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.move)) {
				moveLink = addLink("move.taxonomy.level", "o_icon_move", links);
			}
			newLink = addLink("add.taxonomy.level.under", "o_icon_taxonomy_levels", links);
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}
			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doSelectTaxonomyLevel(ureq, taxonomyLevel);
			} else if(moveLink == source) {
				close();
				doMove(ureq, taxonomyLevel);
			} else if(newLink == source) {
				close();
				doCreateTaxonomyLevel(ureq, taxonomyLevel);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
	
	private static class TaxonomyCrumb implements FlexiTreeTableNode {
		
		private final String taxonomyDisplayName;
		
		public TaxonomyCrumb(String taxonomyDisplayName) {
			this.taxonomyDisplayName = taxonomyDisplayName;
		}

		@Override
		public FlexiTreeTableNode getParent() {
			return null;
		}

		@Override
		public String getCrump() {
			return taxonomyDisplayName;
		}
	}
}