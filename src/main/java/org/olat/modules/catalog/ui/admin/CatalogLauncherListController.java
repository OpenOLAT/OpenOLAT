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
package org.olat.modules.catalog.ui.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherDataModel.CatalogLauncherCols;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherListController extends FormBasicController {
	
	private static final String CMD_ADD = "add";
	private static final String CMD_EDIT = "edit";
	
	private FormLayoutContainer dummyCont;
	private DropdownItem addLauncherDropdown;
	private FlexiTableElement tableEl;
	private CatalogLauncherDataModel dataModel;
	
	private CloseableModalController cmc;
	private Controller catalogLauncherEditController;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;
	
	@Autowired
	private CatalogV2Service catalogService;

	public CatalogLauncherListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale(), getTranslator()));
		
		initForm(ureq);
		loadModel();
		initAddLinks();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dummyCont = FormLayoutContainer.createBareBoneFormLayout("dummy", getTranslator());
		dummyCont.setRootForm(mainForm);
		
		FormLayoutContainer buttonsTopCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		buttonsTopCont.setElementCssClass("o_button_group o_button_group_right");
		buttonsTopCont.setRootForm(mainForm);
		formLayout.add(buttonsTopCont);
		
		addLauncherDropdown = uifactory.addDropdownMenu("admin.launcher.add",
				"admin.launcher.add", null, buttonsTopCont, getTranslator());
		addLauncherDropdown.setOrientation(DropdownOrientation.right);
		addLauncherDropdown.setExpandContentHeight(true);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogLauncherCols.upDown));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogLauncherCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogLauncherCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogLauncherCols.details, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogLauncherCols.enabled));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.edit", -1, CMD_EDIT,
				new StaticFlexiCellRenderer("", CMD_EDIT, "o_icon o_icon-lg o_icon_edit", null)));
		
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(CatalogLauncherCols.tools);
		toolsColumn.setAlwaysVisible(true);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		dataModel = new CatalogLauncherDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
	}

	private void initAddLinks() {
		addLauncherDropdown.removeAllFormItems();
		
		Set<String> unaddableHanderTypes = dataModel.getObjects().stream()
				.map(row -> row.getCatalogLauncher().getType())
				.filter(type-> {
					CatalogLauncherHandler handler = catalogService.getCatalogLauncherHandler(type);
					return handler != null && !handler.isMultiInstance()? true: false;
				})
				.collect(Collectors.toSet());
		
		catalogService.getCatalogLauncherHandlers().stream()
				.filter(CatalogLauncherHandler::isEnabled)
				.filter(handler -> !unaddableHanderTypes.contains(handler.getType()))
				.sorted((h1, h2) -> Integer.compare(h2.getSortOrder(), h1.getSortOrder()))
				.forEach(handler -> {
					FormLink link = uifactory.addFormLink(handler.getType(), CMD_ADD, handler.getTypeI18nKey(), null, dummyCont, Link.LINK);
					link.setUserObject(handler);
					addLauncherDropdown.addElement(link);
				});
		
		addLauncherDropdown.setVisible(addLauncherDropdown.size() > 0);
	}

	void loadModel() {
		List<CatalogLauncher> catalogLaunchers = catalogService.getCatalogLaunchers(new CatalogLauncherSearchParams());
		Collections.sort(catalogLaunchers);
		List<CatalogLauncherRow> rows = new ArrayList<>(catalogLaunchers.size());
		for (int i = 0; i < catalogLaunchers.size(); i++) {
			CatalogLauncher catalogLauncher = catalogLaunchers.get(i);
			CatalogLauncherRow row = new CatalogLauncherRow(catalogLauncher);
			
			CatalogLauncherHandler handler = catalogService.getCatalogLauncherHandler(catalogLauncher.getType());
			if (handler != null) {
				row.setHandler(handler);
				row.setTranslatedType(translate(handler.getTypeI18nKey()));
				row.setTranslatedName(CatalogV2UIFactory.translateLauncherName(getTranslator(), handler, catalogLauncher));
				row.setDetails(handler.getDetails(getTranslator(), catalogLauncher));
			}
			
			UpDown upDown = UpDownFactory.createUpDown("up_down_" + catalogLauncher.getKey(), UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
			upDown.setUserObject(row);
			if (i == 0) {
				upDown.setTopmost(true);
			}
			if (i == catalogLaunchers.size() - 1) {
				upDown.setLowermost(true);
			} 
			row.setUpDown(upDown);
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + catalogLauncher.getSortOrder(), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
			
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent && source instanceof UpDown) {
			UpDownEvent ude = (UpDownEvent) event;
			UpDown upDown = (UpDown)source;
			Object userObject = upDown.getUserObject();
			if (userObject instanceof CatalogLauncherRow) {
				CatalogLauncherRow catalogLauncherRow = (CatalogLauncherRow)userObject;
				doMove(catalogLauncherRow, ude.getDirection());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_ADD.equals(cmd)){
				CatalogLauncherHandler handler = (CatalogLauncherHandler)link.getUserObject();
				doEditLauncher(ureq, handler, null);
			} else if (cmd.startsWith("tools")) {
				CatalogLauncherRow catalogLauncherRow = (CatalogLauncherRow)link.getUserObject();
				doOpenTools(ureq, catalogLauncherRow, link);
			}
		} else if (tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					CatalogLauncherRow catalogLauncherRow  = dataModel.getObject(se.getIndex());
					doEditLauncher(ureq, catalogLauncherRow.getHandler(), catalogLauncherRow.getCatalogLauncher());
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (catalogLauncherEditController == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
				initAddLinks();
			}
			cmc.deactivate();
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(catalogLauncherEditController);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		catalogLauncherEditController = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doMove(CatalogLauncherRow catalogLauncherRow, Direction direction) {
		catalogService.doMove(catalogLauncherRow.getCatalogLauncher(), Direction.UP == direction);
		loadModel();
	}

	private void doEditLauncher(UserRequest ureq, CatalogLauncherHandler handler, CatalogLauncher catalogLauncher) {
		guardModalController(catalogLauncherEditController);
		
		catalogLauncherEditController = handler.createEditController(ureq, getWindowControl(), catalogLauncher);
		listenTo(catalogLauncherEditController);
		
		String title = catalogLauncher != null? translate(handler.getEditI18nKey()): translate(handler.getAddI18nKey());
		cmc = new CloseableModalController(getWindowControl(), "close", catalogLauncherEditController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(CatalogLauncherRow catalogLauncherRow) {
		catalogService.deleteCatalogLauncher(catalogLauncherRow.getCatalogLauncher());
		loadModel();
		initAddLinks();
	}
	
	private void doOpenTools(UserRequest ureq, CatalogLauncherRow catalogLauncherRow, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), catalogLauncherRow);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final Link deleteLink;
		
		private final CatalogLauncherRow catalogLauncherRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CatalogLauncherRow catalogLauncherRow) {
			super(ureq, wControl);
			this.catalogLauncherRow = catalogLauncherRow;
			
			VelocityContainer mainVC = createVelocityContainer("catalog_launcher_tools");
			
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			this.fireEvent(ureq, Event.DONE_EVENT);
			if(deleteLink == source) {
				doDelete(catalogLauncherRow);
			}
		}
		
	}

}
