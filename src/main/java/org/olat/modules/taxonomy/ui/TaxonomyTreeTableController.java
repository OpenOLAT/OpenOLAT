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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.LocaleUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
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
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;
import org.olat.modules.taxonomy.ui.TaxonomyTreeTableModel.TaxonomyLevelCols;
import org.olat.modules.taxonomy.ui.events.DeleteTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.MoveTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.NewTaxonomyLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TaxonomyTreeTableController extends FormBasicController implements BreadcrumbPanelAware, Activateable2 {

	private static final String TAB_ID_RELEVANT = "MyRelevant";
	private static final String TAB_ID_ALL = "All";
	private static final String TOOLS_IMPORT_EXPORT = "importExportTools";
	private static final String ACTION_SELECT = "select";

	private FormLink newLevelButton;
	private FormLink deleteButton;
	private FormLink mergeButton;
	private FormLink typeButton;
	private FormLink moveButton;
	private FlexiFiltersTab tabRelevant;
	private FlexiFiltersTab tabAll;
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
	
	private final TaxonomySecurityCallback secCallback;
	private final Taxonomy taxonomy;
	private final TaxonomyLevel parentLevel;
	private boolean dirty = false;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;

	public TaxonomyTreeTableController(UserRequest ureq, WindowControl wControl, TaxonomySecurityCallback secCallback,
			Taxonomy taxonomy, TaxonomyLevel parentLevel) {
		super(ureq, wControl, "admin_taxonomy_levels");
		this.secCallback = secCallback;
		this.taxonomy = taxonomy;
		this.parentLevel = parentLevel;
		initForm(ureq);
		initFilterTabs(ureq);
		loadModel(true, true);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		if (this.stackPanel != null) {
			this.stackPanel.removeListener(this);
		}
		
		this.stackPanel = stackPanel;
		if (this.stackPanel != null) {
			this.stackPanel.addListener(this);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (secCallback.canCreateChild(parentLevel)) {
			newLevelButton = uifactory.addFormLink("add.taxonomy.level", formLayout, Link.BUTTON);
			newLevelButton.setElementCssClass("o_sel_taxonomy_new_level");
		}
		
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		mergeButton = uifactory.addFormLink("merge.taxonomy.level", formLayout, Link.BUTTON);
		typeButton = uifactory.addFormLink("type.taxonomy.level", formLayout, Link.BUTTON);
		moveButton = uifactory.addFormLink("move.taxonomy.level", formLayout, Link.BUTTON);
		
		if (secCallback.canImportExport()) {
			FormLink importExportLink = uifactory.addFormLink(TOOLS_IMPORT_EXPORT, TOOLS_IMPORT_EXPORT, "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
			importExportLink.setIconLeftCSS("o_icon o_icon-lg o_icon_actions");
			importExportLink.setTitle(translate("action.more"));
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyLevelCols.key));
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer(ACTION_SELECT);
		treeNodeRenderer.setFlatBySearchAndFilter(true);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.identifier, ACTION_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.externalId, ACTION_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyLevelCols.typeIdentifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyLevelCols.order));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyLevelCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.numOfChildren));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(TaxonomyLevelCols.tools));
		
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
		tableEl.setAndLoadPersistedPreferences(ureq, "tax-tree-v2-" + taxonomy.getKey());

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
	
	protected void initFilterTabs(UserRequest ureq) {
		if (!secCallback.canFilterRelevant()) {
			return;
		}
		
		List<FlexiFiltersTab> tabs = new ArrayList<>(2);
		
		tabRelevant = FlexiFiltersTabFactory.tab(
				TAB_ID_RELEVANT,
				translate("relevant"),
				TabSelectionBehavior.nothing);
		tabs.add(tabRelevant);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.nothing);
		tabs.add(tabAll);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabRelevant);
	}
	
	private void loadModel(boolean resetPage, boolean resetInternal) {
		TaxonomyLevelSearchParameters searchParams = new TaxonomyLevelSearchParameters();
		searchParams.setParentLevel(parentLevel);
		searchParams.setQuickSearch(tableEl.getQuickSearchString());
		Set<String> quickSearchI18nSuffix = i18nManager.findI18nKeysByOverlayValue(
						tableEl.getQuickSearchString(),
						TaxonomyUIFactory.PREFIX_DISPLAY_NAME,
						getLocale(),
						TaxonomyUIFactory.BUNDLE_NAME, false).stream()
				.map(key -> key.substring(TaxonomyUIFactory.PREFIX_DISPLAY_NAME.length()))
				.collect(Collectors.toSet());
		searchParams.setQuickSearchI18nSuffix(quickSearchI18nSuffix);
		
		List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomy, searchParams);
		applyFilter(taxonomyLevels);
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
		
		try {
			rows.sort(new TaxonomyTreeNodeComparator());
		} catch (Exception e) {
			logError("Cannot sort taxonomy tree", e);
		}

		model.setObjects(rows);
		tableEl.reset(resetPage, resetInternal, true);
	}
	
	private void applyFilter(List<TaxonomyLevel> taxonomyLevels) {
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabRelevant) {
			taxonomyLevels.removeIf(level -> !secCallback.isRelevant(level));
		}
	}

	private TaxonomyLevelRow forgeRow(TaxonomyLevel taxonomyLevel) {
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel);
		String description = TaxonomyUIFactory.translateDescription(getTranslator(), taxonomyLevel);
		TaxonomyLevelRow row = new TaxonomyLevelRow(taxonomyLevel, getLocale().toString(), displayName, description, toolsLink);
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
			List<TaxonomyLevelRow> rows = model.getAllRows();
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
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if(ACTION_SELECT.equals(cmd)) {
					TaxonomyLevelRow row = model.getObject(se.getIndex());
					doSelectTaxonomyLevel(ureq, row);
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				loadModel(true, true);
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel(true, true);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				TaxonomyLevelRow row = (TaxonomyLevelRow)link.getUserObject();
				doOpenLevelTools(ureq, row, link);
			} else if (TOOLS_IMPORT_EXPORT.equals(cmd)) {
				doOpenTools(ureq, link);
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
				secCallback.refresh();
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
		} else if(mergeCtrl == source || typeLevelCtrl == source
				|| moveLevelCtrl == source || confirmDeleteCtrl == source) {
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

	private void doExportTaxonomyLevels(UserRequest ureq) {
		ureq.getDispatchResult().setResultingMediaResource(new ExportTaxonomyLevels("UTF-8", getTranslator(),
																					taxonomy, taxonomyService, i18nManager, i18nModule));
	}
	
	private void doAssignType(UserRequest ureq) {
		if(guardModalController(typeLevelCtrl)) return;
		
		List<TaxonomyLevel> levelsToMerge = getSelectedTaxonomyLevels(level -> secCallback.canEditMetadata(level), TaxonomyLevelManagedFlag.type);
		if(levelsToMerge.isEmpty()) {
			showWarning("warning.atleastone.level");
		} else {
			typeLevelCtrl = new TypeTaxonomyLevelController(ureq, getWindowControl(), levelsToMerge, taxonomy);
			listenTo(typeLevelCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), typeLevelCtrl.getInitialComponent(),
					true, translate("type.taxonomy.level"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doMerge(UserRequest ureq) {
		if(guardModalController(mergeCtrl)) return;
		
		List<TaxonomyLevel> levelsToMerge = getSelectedTaxonomyLevels(level -> secCallback.canDelete(level), TaxonomyLevelManagedFlag.delete);
		if(levelsToMerge.isEmpty()) {
			showWarning("warning.atleastone.level");
		} else {
			mergeCtrl = new MergeTaxonomyLevelController(ureq, getWindowControl(), levelsToMerge, taxonomy);
			listenTo(mergeCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), mergeCtrl.getInitialComponent(),
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
			if (context.getTaxonomyLevelTypeCreateList() != null) {
				for (TaxonomyLevelType newLevelType : context.getTaxonomyLevelTypeCreateList()) {
					createdTypes.add(taxonomyService.createTaxonomyLevelType(newLevelType.getIdentifier(), newLevelType.getIdentifier(), null, null, true, context.getTaxonomy()));
				}
			}

	        
	        // Collect created levels to find parents 
	        List<TaxonomyLevel> createdLevels = new ArrayList<>();
			if (context.getTaxonomyLevelCreateList() != null) {
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

					TaxonomyLevel createdLevel = taxonomyService.createTaxonomyLevel(newLevel.getIdentifier(), taxonomyService.createI18nSuffix(), null, null, parent, context.getTaxonomy());
					if (newLevel instanceof TaxonomyLevelImpl && context.getNameDescriptionByLanguage() != null) {
						saveOrUpdateI18nItemForTaxonomyLevel(createdLevel, context);
					}

					createdLevel.setSortOrder(newLevel.getSortOrder());

					if (newLevel.getType() != null && !createdTypes.isEmpty()) {
						createdLevel.setType(getLevelType(newLevel, createdTypes));
					}

					createdLevel = taxonomyService.updateTaxonomyLevel(createdLevel);
					createdLevels.add(createdLevel);
				}
			}

	        // Update existing taxonomies if needed
	        if (context.isUpdateExistingTaxonomies()) {
		        for (TaxonomyLevel updateLevel : context.getTaxonomyLevelUpdateList()) {
		        	if (updateLevel.getType() != null && !createdTypes.isEmpty()) {
						updateLevel.setType(getLevelType(updateLevel, createdTypes));
		        	}
					taxonomyService.updateTaxonomyLevel(updateLevel);
					if (context.getNameDescriptionByLanguage() != null) {
						saveOrUpdateI18nItemForTaxonomyLevel(updateLevel, context);
					}
		        }

				if (context.getTaxonomyLevelToImageMap() != null) {
					for (TaxonomyLevel level : context.getTaxonomyLevelToImageMap().keySet()) {
						File backImage = context.getTaxonomyLevelToImageMap().get(level).get("background");
						File teaserImage = context.getTaxonomyLevelToImageMap().get(level).get("teaser");

						TaxonomyLevel finalLevel = level;
						if (createdLevels.stream().anyMatch(cl -> cl.getIdentifier().equals(finalLevel.getIdentifier()))) {
							level = createdLevels.stream().filter(cl -> cl.getIdentifier().equals(finalLevel.getIdentifier())).findFirst().get();
						}
						if (backImage != null) {
							taxonomyService.storeBackgroundImage(level, ureq.getIdentity(), backImage, backImage.getName());
						}
						if (teaserImage != null) {
							taxonomyService.storeTeaserImage(level, ureq.getIdentity(), teaserImage, teaserImage.getName());
						}
					}
				}
	        }

	    	return StepsMainRunController.DONE_MODIFIED;
	    }
	}

	private TaxonomyLevelType getLevelType(TaxonomyLevel level, List<TaxonomyLevelType> createdTypes) {
		if (level.getType().getKey() == null) {
			return createdTypes.stream().filter(type -> type.getIdentifier().equals(level.getType().getIdentifier())).findFirst().orElse(null);
		}
		return level.getType();
	}

	private void saveOrUpdateI18nItemForTaxonomyLevel(TaxonomyLevel level, TaxonomyImportContext context) {
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		List<Locale> locales = context.getNameDescriptionByLanguage().keySet().stream()
				.map(key -> LocaleUtils.toLocale(key.toLowerCase()))
				.collect(Collectors.toList());
		String displayNameKey = TaxonomyUIFactory.PREFIX_DISPLAY_NAME + level.getI18nSuffix();
		String descriptionKey = TaxonomyUIFactory.PREFIX_DESCRIPTION + level.getI18nSuffix();

		if (context.getNameDescriptionByLanguage() != null) {
			for (int i = 0; i < locales.size(); i++) {
				I18nItem displayNameItem = i18nManager.getI18nItem(
						TaxonomyUIFactory.BUNDLE_NAME,
						displayNameKey,
						allOverlays.get(locales.get(i)));
				I18nItem descriptionItem = i18nManager.getI18nItem(
						TaxonomyUIFactory.BUNDLE_NAME,
						descriptionKey,
						allOverlays.get(locales.get(i)));

				if (context.getReviewList().stream().anyMatch(t -> t.getTaxonomyLevel().getIdentifier().equals(level.getIdentifier()))) {
					int finalI = i;
					if (!context.isUpdateExistingTaxonomies()
							|| (context.isUpdateExistingTaxonomies()
							&& !TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.displayName))) {
						i18nManager.saveOrUpdateI18nItem(displayNameItem, context.getReviewList().stream()
								.filter(t -> t.getTaxonomyLevel().getIdentifier().equals(level.getIdentifier()) && t.getLanguage().equals(locales.get(finalI).getLanguage().toUpperCase()))
								.findFirst()
								.get().getDisplayName());
					}
					if (!context.isUpdateExistingTaxonomies()
							|| (context.isUpdateExistingTaxonomies()
							&& !TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.description))) {
						i18nManager.saveOrUpdateI18nItem(descriptionItem, context.getReviewList().stream()
								.filter(t -> t.getTaxonomyLevel().getIdentifier().equals(level.getIdentifier()) && t.getLanguage().equals(locales.get(finalI).getLanguage().toUpperCase()))
								.findFirst()
								.get().getDescription());
					}
				}
			}
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
		TaxonomyLevel reloadedTaxonomyLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		if(reloadedTaxonomyLevel == null) {
			showWarning("warning.taxonomy.level.deleted");
			loadModel(false, false);
			return null;
		}
		addIntermediatePath(taxonomyLevel);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("TaxonomyLevel", taxonomyLevel.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		TaxonomyLevelOverviewController detailsLevelCtrl = new TaxonomyLevelOverviewController(ureq, bwControl, stackPanel, secCallback, reloadedTaxonomyLevel);
		listenTo(detailsLevelCtrl);
		String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), reloadedTaxonomyLevel);
		stackPanel.pushController(displayName, null, detailsLevelCtrl, reloadedTaxonomyLevel);
		return detailsLevelCtrl;
	}
	
	private void addIntermediatePath(TaxonomyLevel taxonomyLevel) {
		List<TaxonomyLevel> parentLine = taxonomyService.getTaxonomyLevelParentLine(taxonomyLevel, taxonomy);
		for(TaxonomyLevel parent:parentLine) {
			stackPanel.popUserObject(parent);
		}
		
		parentLine.remove(taxonomyLevel);
		for(TaxonomyLevel parent:parentLine) {
			stackPanel.pushController(TaxonomyUIFactory.translateDisplayName(getTranslator(), parent), null, parent);
		}
	}
	
	private void doNewLevel(UserRequest ureq) {
		doCreateTaxonomyLevel(ureq, null);
	}
	
	private void doCreateTaxonomyLevel(UserRequest ureq, TaxonomyLevel parentLevel) {
		if(guardModalController(createTaxonomyLevelCtrl)) return;

		createTaxonomyLevelCtrl = new EditTaxonomyLevelController(ureq, getWindowControl(), parentLevel, taxonomy);
		listenTo(createTaxonomyLevelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createTaxonomyLevelCtrl.getInitialComponent(), true, translate("add.taxonomy.level"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenLevelTools(UserRequest ureq, TaxonomyLevelRow row, FormLink link) {
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

	private void doOpenTools(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doConfirmMultiDelete(UserRequest ureq) {
		Set<Integer> indexList = tableEl.getMultiSelectedIndex();
		List<TaxonomyLevel> levelsToDelete = new ArrayList<>(indexList.size());
		for(Integer index:indexList) {
			TaxonomyLevelRow row = model.getObject(index.intValue());
			if(secCallback.canDelete(row.getTaxonomyLevel()) && !TaxonomyLevelManagedFlag.isManaged(row.getManagedFlags(), TaxonomyLevelManagedFlag.delete)) {
				TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(row);
				if(taxonomyLevel != null) {
					levelsToDelete.add(taxonomyLevel);
				}
			}
		}
		
		if(levelsToDelete.isEmpty()) {
			showWarning("warning.atleastone.level");
		} else {
			confirmDeleteCtrl = new DeleteTaxonomyLevelController(ureq, getWindowControl(), secCallback, levelsToDelete, taxonomy);
			listenTo(confirmDeleteCtrl);
			
			String title = translate("confirmation.delete.level.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true, title);
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
		confirmDeleteCtrl = new DeleteTaxonomyLevelController(ureq, getWindowControl(), secCallback, levelToDelete, taxonomy);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("confirmation.delete.level.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMove(UserRequest ureq) {
		if(moveLevelCtrl != null) return;
		
		List<TaxonomyLevel> levelsToMove = getSelectedTaxonomyLevels(level -> secCallback.canMove(level), TaxonomyLevelManagedFlag.move);
		doMove(ureq, levelsToMove);
	}
	
	private void doMove(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		if(moveLevelCtrl != null) return;
		
		List<TaxonomyLevel> levelsToMove = Collections.singletonList(taxonomyLevel);
		doMove(ureq, levelsToMove);
	}

	private void doMove(UserRequest ureq, List<TaxonomyLevel> taxonomyLevels) {
		moveLevelCtrl = new MoveTaxonomyLevelController(ureq, getWindowControl(), secCallback, taxonomyLevels, taxonomy);
		listenTo(moveLevelCtrl);
		
		String title = translate("move.taxonomy.levels.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), moveLevelCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private List<TaxonomyLevel> getSelectedTaxonomyLevels(Predicate<TaxonomyLevel> can, TaxonomyLevelManagedFlag flag) {
		Set<Integer> indexList = tableEl.getMultiSelectedIndex();
		List<TaxonomyLevel> allowedLevels = new ArrayList<>(indexList.size());
		for(Integer index:indexList) {
			TaxonomyLevelRow row = model.getObject(index.intValue());
			if(can.test(row.getTaxonomyLevel()) && !TaxonomyLevelManagedFlag.isManaged(row.getManagedFlags(), flag)) {
				TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(row);
				if(taxonomyLevel != null) {
					allowedLevels.add(taxonomyLevel);
				}
			}
		}
		return allowedLevels;
	}
	
	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC = createVelocityContainer("tools");
		private Link editLink, moveLink, newLink, deleteLink;
		private Link exportLink, importLink;
		
		private TaxonomyLevelRow row;
		private TaxonomyLevel taxonomyLevel;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, TaxonomyLevelRow row, TaxonomyLevel taxonomyLevel) {
			super(ureq, wControl);
			this.row = row;
			this.taxonomyLevel = taxonomyLevel;
			
			List<String> links = new ArrayList<>(6);
			
			if (secCallback.canEditMetadata(taxonomyLevel)) {
				editLink = addLink("edit", "o_icon_edit", links);
			} else {
				editLink = addLink("open.taxonomy.level", "o_icon_preview", links);
			}
			
			if(secCallback.canMove(taxonomyLevel) && !TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.move)) {
				moveLink = addLink("move.taxonomy.level", "o_icon_move", links);
			}
			
			if (secCallback.canCreateChild(taxonomyLevel)) {
				newLink = addLink("add.taxonomy.level.under", "o_icon_taxonomy_levels", links);
			}
			if(secCallback.canDelete(taxonomyLevel) && !TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}
			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}

		public ToolsController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);

			List<String> links = new ArrayList<>(3);

			exportLink = addLink("export.taxonomy.levels", "o_icon_download", links);
			importLink = addLink("import.taxonomy.levels", "o_icon_import", links);

			mainVC.contextPut("links", links);

			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
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
			} else if (importLink == source) {
				close();
				doOpenImportWizard(ureq);
			} else if (exportLink == source) {
				close();
				doExportTaxonomyLevels(ureq);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
	
	private static class TaxonomyTreeNodeComparator extends FlexiTreeNodeComparator {
		
		@Override
		protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
			TaxonomyLevelRow r1 = (TaxonomyLevelRow)o1;
			TaxonomyLevelRow r2 = (TaxonomyLevelRow)o2;

			int c = 0;
			if(r1 == null || r2 == null) {
				c = compareNullObjects(r1, r2);
			} else {
				Integer s1 = r1.getSortOrder();
				Integer s2 = r2.getSortOrder();
	
				if(s1 == null || s2 == null) {
					c = -compareNullObjects(s1, s2);
				} else {
					c = s1.compareTo(s2);
				}
				
				if(c == 0) {
					String c1 = r1.getDisplayName();
					String c2 = r2.getDisplayName();
					if(c1 == null || c2 == null) {
						c = -compareNullObjects(c1, c2);
					} else {
						c = c1.compareTo(c2);
					}
				}
				
				if(c == 0) {
					Long k1 = r1.getKey();
					Long k2 = r2.getKey();
					if(k1 == null || k2 == null) {
						c = -compareNullObjects(k1, k2);
					} else {
						c = k1.compareTo(k2);
					}
				}
			}
			return c;
		}
	}
}