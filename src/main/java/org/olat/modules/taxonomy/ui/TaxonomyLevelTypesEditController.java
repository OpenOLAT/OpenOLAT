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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelTypeRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyLevelTypesTableModel.TypesCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelTypesEditController extends FormBasicController implements Activateable2 {
	
	private FormLink addRootTypeButton;
	private FlexiTableElement tableEl;
	private TaxonomyLevelTypesTableModel model;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteDialog;
	private EditTaxonomyLevelTypeController rootLevelTypeCtrl;
	private EditTaxonomyLevelTypeController editLevelTypeCtrl;
	protected CloseableCalloutWindowController toolsCalloutCtrl;

	private int counter = 1;
	
	private Taxonomy taxonomy;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public TaxonomyLevelTypesEditController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl, "admin_level_types");
		this.taxonomy = taxonomy;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addRootTypeButton = uifactory.addFormLink("add.root.type", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.allowedAsCompetence, new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("yes"), (String)null), new StaticFlexiCellRenderer(translate("no"), (String)null))));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.allowedAsSubject, new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("yes"), (String)null), new StaticFlexiCellRenderer(translate("no"), (String)null))));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
				new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit")));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(TypesCols.tools);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		model = new TaxonomyLevelTypesTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "types", model, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("table.taxonomy.level.type.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "tax-level-types-competences");
	}
	
	public void loadModel() {
		List<TaxonomyLevelType> types = taxonomyService.getTaxonomyLevelTypes(taxonomy);
		List<TaxonomyLevelTypeRow> rows = types
				.stream().map(t -> forgeRow(t))
				.collect(Collectors.toList());
		model.setObjects(rows);
		tableEl.reset(false, true, true);
	}
	
	private TaxonomyLevelTypeRow forgeRow(TaxonomyLevelType type) {
		TaxonomyLevelTypeRow row = new TaxonomyLevelTypeRow(type);
		if(isToolsEnable(type)) {
			FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		return row;
	}
	
	private boolean isToolsEnable(TaxonomyLevelType type) {
		boolean toolable = !TaxonomyLevelTypeManagedFlag.isManaged(type.getManagedFlags(), TaxonomyLevelTypeManagedFlag.copy)
				|| !TaxonomyLevelTypeManagedFlag.isManaged(type.getManagedFlags(), TaxonomyLevelTypeManagedFlag.delete);
		return toolable;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rootLevelTypeCtrl == source || editLevelTypeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteDialog == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				TaxonomyLevelTypeRow row = (TaxonomyLevelTypeRow)confirmDeleteDialog.getUserObject();
				doDelete(row);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rootLevelTypeCtrl);
		removeAsListenerAndDispose(cmc);
		rootLevelTypeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addRootTypeButton == source) {
			doAddRootType(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				TaxonomyLevelTypeRow row = (TaxonomyLevelTypeRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			} 
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					TaxonomyLevelTypeRow row = model.getObject(se.getIndex());
					doEditLevelType(ureq, row.getType());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, TaxonomyLevelTypeRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		TaxonomyLevelType type = taxonomyService.getTaxonomyLevelType(row);
		if(type == null) {
			tableEl.reloadData();
			showWarning("warning.taxonomy.level.type.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, type);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}

	private void doAddRootType(UserRequest ureq) {
		rootLevelTypeCtrl = new EditTaxonomyLevelTypeController(ureq, this.getWindowControl(), null, taxonomy);
		listenTo(rootLevelTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", rootLevelTypeCtrl.getInitialComponent(), true, translate("add.root.type"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditLevelType(UserRequest ureq, TaxonomyLevelTypeRef type) {
		TaxonomyLevelType reloadedType = taxonomyService.getTaxonomyLevelType(type);
		editLevelTypeCtrl = new EditTaxonomyLevelTypeController(ureq, this.getWindowControl(), reloadedType, taxonomy);
		listenTo(editLevelTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editLevelTypeCtrl.getInitialComponent(), true, translate("edit"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCopy(TaxonomyLevelTypeRow row) {
		taxonomyService.cloneTaxonomyLevelType(row);
		loadModel();
		showInfo("info.copy.level.type.sucessfull", row.getDisplayName());
	}
	
	private void doConfirmDelete(UserRequest ureq, TaxonomyLevelTypeRow row) {
		String[] args = new String[] { StringHelper.escapeHtml(row.getDisplayName()) };
		String title = translate("confirmation.delete.type.title", args);
		String text = translate("confirmation.delete.type", args);
		confirmDeleteDialog = activateOkCancelDialog(ureq, title, text, confirmDeleteDialog);
		confirmDeleteDialog.setUserObject(row);
	}
	
	private void doDelete(TaxonomyLevelTypeRow row) {
		if(taxonomyService.deleteTaxonomyLevelType(row)) {
			showInfo("confirm.delete.level.type.sucessfull", row.getDisplayName());
			loadModel();
			tableEl.reset(true, true, true);
		} else {
			showWarning("warning.delete.level.type", row.getDisplayName());
		}
	}

	private class ToolsController extends BasicController {
		
		private final TaxonomyLevelTypeRow row;

		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, TaxonomyLevelTypeRow row, TaxonomyLevelType type) {
			super(ureq, wControl);
			setTranslator(TaxonomyLevelTypesEditController.this.getTranslator());
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();
			
			if(!TaxonomyLevelTypeManagedFlag.isManaged(type.getManagedFlags(), TaxonomyLevelTypeManagedFlag.copy)) {
				addLink("details.copy", "copy", "o_icon o_icon-fw o_icon_copy", links);
			}
			if(!TaxonomyLevelTypeManagedFlag.isManaged(type.getManagedFlags(), TaxonomyLevelTypeManagedFlag.delete)) {
				addLink("details.delete", "delete", "o_icon o_icon-fw o_icon_delete_item", links);
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if("copy".equals(cmd)) {
					close();
					doCopy(row);
				} else if("delete".equals(cmd)) {
					close();
					doConfirmDelete(ureq, row);
				}
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
