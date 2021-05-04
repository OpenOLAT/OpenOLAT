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
package org.olat.modules.curriculum.ui;

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
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementTypesTableModel.TypesCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypesEditController extends FormBasicController implements Activateable2 {
	
	private FormLink addRootTypeButton;
	private FlexiTableElement tableEl;
	private CurriculumElementTypesTableModel model;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteDialog;
	private EditCurriculumElementTypeController rootElementTypeCtrl;
	private EditCurriculumElementTypeController editElementTypeCtrl;
	protected CloseableCalloutWindowController toolsCalloutCtrl;

	private int counter = 1;

	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementTypesEditController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_types");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addRootTypeButton = uifactory.addFormLink("add.root.type", formLayout, Link.BUTTON);
		addRootTypeButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TypesCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TypesCols.externalId));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
				new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit")));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(TypesCols.tools);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		model = new CurriculumElementTypesTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "types", model, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("table.type.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-el-types");
	}
	
	private void loadModel() {
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		List<CurriculumElementTypeRow> rows = types
				.stream().map(t -> forgeRow(t))
				.collect(Collectors.toList());
		model.setObjects(rows);
		tableEl.reset(false, true, true);
	}
	
	private CurriculumElementTypeRow forgeRow(CurriculumElementType type) {
		CurriculumElementTypeRow row = new CurriculumElementTypeRow(type);
		if(isToolsEnable(type)) {
			FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		return row;
	}
	
	private boolean isToolsEnable(CurriculumElementType type) {
		return !CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.copy)
				|| !CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.delete);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rootElementTypeCtrl == source || editElementTypeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteDialog == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				CurriculumElementTypeRow row = (CurriculumElementTypeRow)confirmDeleteDialog.getUserObject();
				doDelete(row);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rootElementTypeCtrl);
		removeAsListenerAndDispose(cmc);
		rootElementTypeCtrl = null;
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
				CurriculumElementTypeRow row = (CurriculumElementTypeRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			} 
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					CurriculumElementTypeRow row = model.getObject(se.getIndex());
					doEditCurriculElementType(ureq, row.getType());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementTypeRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElementType type = curriculumService.getCurriculumElementType(row);
		if(type == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.type.deleted");
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
		rootElementTypeCtrl = new EditCurriculumElementTypeController(ureq, getWindowControl(), null);
		listenTo(rootElementTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", rootElementTypeCtrl.getInitialComponent(), true, translate("add.root.type"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditCurriculElementType(UserRequest ureq, CurriculumElementTypeRef type) {
		CurriculumElementType reloadedType = curriculumService.getCurriculumElementType(type);
		editElementTypeCtrl = new EditCurriculumElementTypeController(ureq, getWindowControl(), reloadedType);
		listenTo(editElementTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editElementTypeCtrl.getInitialComponent(), true, translate("edit"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCopy(CurriculumElementTypeRow row) {
		curriculumService.cloneCurriculumElementType(row);
		loadModel();
		showInfo("info.copy.element.type.sucessfull", row.getDisplayName());
	}
	
	private void doConfirmDelete(UserRequest ureq, CurriculumElementTypeRow row) {
		String[] args = new String[] { StringHelper.escapeHtml(row.getDisplayName()) };
		String title = translate("confirmation.delete.type.title", args);
		String text = translate("confirmation.delete.type", args);
		confirmDeleteDialog = activateOkCancelDialog(ureq, title, text, confirmDeleteDialog);
		confirmDeleteDialog.setUserObject(row);
	}
	
	private void doDelete(CurriculumElementTypeRow row) {
		if(curriculumService.deleteCurriculumElementType(row)) {
			showInfo("confirm.delete.element.type.sucessfull", row.getDisplayName());
			loadModel();
			tableEl.reset(true, true, true);
		} else {
			showWarning("warning.delete.element.type", row.getDisplayName());
		}
	}

	private class ToolsController extends BasicController {
		
		private final CurriculumElementTypeRow row;

		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumElementTypeRow row, CurriculumElementType type) {
			super(ureq, wControl);
			setTranslator(CurriculumElementTypesEditController.this.getTranslator());
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();
			
			if(!CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.copy)) {
				addLink("details.copy", "copy", "o_icon o_icon-fw o_icon_copy", links);
			}
			if(!CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.delete)) {
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

		@Override
		protected void doDispose() {
			//
		}
	}
}
