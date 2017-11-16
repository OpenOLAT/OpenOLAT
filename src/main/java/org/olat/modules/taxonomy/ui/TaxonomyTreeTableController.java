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

import org.olat.core.commons.persistence.DB;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyTreeTableModel.TaxonomyLevelCols;
import org.olat.modules.taxonomy.ui.events.DeleteTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.NewTaxonomyLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeTableController extends FormBasicController implements BreadcrumbPanelAware {
	
	private FormLink newLevelButton;
	private FlexiTableElement tableEl;
	private TaxonomyTreeTableModel model;
	private BreadcrumbPanel stackPanel;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteDialog;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditTaxonomyLevelController createTaxonomyLevelCtrl;
	
	private int counter = 0;
	private Taxonomy taxonomy;
	private boolean dirty = false;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyService taxonomyService;

	public TaxonomyTreeTableController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl, "admin_taxonomy_levels");
		this.taxonomy = taxonomy;
		initForm(ureq);
		loadModel();
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		newLevelButton = uifactory.addFormLink("add.taxonomy.level", formLayout, Link.BUTTON);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyLevelCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.displayName, new TreeNodeFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.typeIdentifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.numOfChildren));
		DefaultFlexiColumnModel selectColumn = new DefaultFlexiColumnModel("select", translate("select"), "select");
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
		tableEl.setEmtpyTableMessageKey("table.taxonomy.level.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(24);
		tableEl.setFilters(null, getFilters(), true);
		tableEl.setRootCrumb(new TaxonomyCrumb(taxonomy.getDisplayName()));
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<TaxonomyLevelType> types = taxonomyService.getTaxonomyLevelTypes(taxonomy);
		List<FlexiTableFilter> resources = new ArrayList<>(types.size() + 1);
		for(TaxonomyLevelType type:types) {
			resources.add(new FlexiTableFilter(type.getDisplayName(), type.getKey().toString()));
		}
		resources.add(new FlexiTableFilter(translate("filter.no.level.type"), "-"));
		return resources;
	}
	
	private void loadModel() {
		List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomy);
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
		
		//rows = new TaxonomyAllTreesBuilder().toTree(rows);
		
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private TaxonomyLevelRow forgeRow(TaxonomyLevel taxonomyLevel) {
		//tools
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		TaxonomyLevelRow row = new TaxonomyLevelRow(taxonomyLevel, toolsLink);
		toolsLink.setUserObject(row);
		return row;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newLevelButton == source) {
			doNewLevel(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					TaxonomyLevelRow row = model.getObject(se.getIndex());
					doSelectTaxonomyLevel(ureq, row);
				}
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
				loadModel();
				tableEl.reset(false, false, true);
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
				loadModel();
				tableEl.reset(false, false, true);
			} else if(event instanceof NewTaxonomyLevelEvent) {
				stackPanel.popController(source);
				loadModel();
				tableEl.reset(false, false, true);
				doSelectTaxonomyLevel(ureq, ((NewTaxonomyLevelEvent)event).getTaxonomyLevel());
			} else if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				dirty = true;
			}
		} else if(createTaxonomyLevelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				tableEl.reset(false, false, true);
				doSelectTaxonomyLevel(ureq, createTaxonomyLevelCtrl.getTaxonomyLevel());
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteDialog == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				TaxonomyLevel taxonomyLevel = (TaxonomyLevel)confirmDeleteDialog.getUserObject();
				doDelete(ureq, taxonomyLevel);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(createTaxonomyLevelCtrl);
		removeAsListenerAndDispose(cmc);
		createTaxonomyLevelCtrl = null;
		cmc = null;
	}

	private void doSelectTaxonomyLevel(UserRequest ureq, TaxonomyLevelRow row) {
		TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(row);
		doSelectTaxonomyLevel(ureq, taxonomyLevel);
	}

	private void doSelectTaxonomyLevel(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		TaxonomyLevelOverviewController detailsLevelCtrl = new TaxonomyLevelOverviewController(ureq, getWindowControl(), taxonomyLevel);
		listenTo(detailsLevelCtrl);
		stackPanel.pushController(taxonomyLevel.getDisplayName(), detailsLevelCtrl);
	}
	
	private void doNewLevel(UserRequest ureq) {
		doCreateTaxonomyLevel(ureq, null);
	}
	
	private void doCreateTaxonomyLevel(UserRequest ureq, TaxonomyLevel parentLevel) {
		if(createTaxonomyLevelCtrl != null) return;

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
			showWarning("repositoryentry.not.existing");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), level);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		if(taxonomyService.canDeleteTaxonomyLevel(taxonomyLevel)) {
			String title = translate("confirmation.delete.level.title");
			String text = translate("confirmation.delete.level", new String[] { StringHelper.escapeHtml(taxonomyLevel.getDisplayName()) });
			confirmDeleteDialog = activateOkCancelDialog(ureq, title, text, confirmDeleteDialog);
			confirmDeleteDialog.setUserObject(taxonomyLevel);
		} else {
			showWarning("warning.delete.level");
		}
	}
	
	private void doDelete(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		if(taxonomyService.deleteTaxonomyLevel(taxonomyLevel)) {
			dbInstance.commit();//commit before sending event
			fireEvent(ureq, new DeleteTaxonomyLevelEvent());
			showInfo("confirm.deleted.level", new String[] { StringHelper.escapeHtml(taxonomyLevel.getDisplayName()) });
		}
	}
	
	private void doMove() {
		//TODO taxonomy
		showWarning("not.implemented");
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link editLink, moveLink, newLink, deleteLink;
		
		private TaxonomyLevel taxonomyLevel;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, TaxonomyLevel taxonomyLevel) {
			super(ureq, wControl);
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
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doSelectTaxonomyLevel(ureq, taxonomyLevel);
			} else if(moveLink == source) {
				close();
				doMove();
			} else if(newLink == source) {
				close();
				doCreateTaxonomyLevel(ureq, taxonomyLevel);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, taxonomyLevel);
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