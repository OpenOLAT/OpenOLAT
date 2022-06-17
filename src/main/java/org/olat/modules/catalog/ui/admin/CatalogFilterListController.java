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
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogFilterDataModel.CatalogFilterCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogFilterListController extends FormBasicController {
	
	private static final String CMD_ADD = "add";
	private static final String CMD_EDIT = "edit";
	
	private FormLayoutContainer dummyCont;
	private DropdownItem addFilterDropdown;
	private FlexiTableElement tableEl;
	private CatalogFilterDataModel dataModel;
	
	private CloseableModalController cmc;
	private Controller catalogFilterEditController;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;
	
	@Autowired
	private CatalogV2Service catalogService;

	public CatalogFilterListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
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
		
		addFilterDropdown = uifactory.addDropdownMenu("admin.filter.add",
				"admin.filter.add", null, buttonsTopCont, getTranslator());
		addFilterDropdown.setOrientation(DropdownOrientation.right);
		addFilterDropdown.setExpandContentHeight(true);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogFilterCols.upDown));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogFilterCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogFilterCols.details
				));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogFilterCols.enabled));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.edit", -1, CMD_EDIT,
				new StaticFlexiCellRenderer("", CMD_EDIT, "o_icon o_icon-lg o_icon_edit", null)));
		
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(CatalogFilterCols.tools);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		dataModel = new CatalogFilterDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
	}

	private void initAddLinks() {
		addFilterDropdown.removeAllFormItems();
		
		Set<String> unaddableHanderTypes = dataModel.getObjects().stream()
				.map(row -> row.getCatalogFilter().getType())
				.filter(type-> {
					CatalogFilterHandler handler = catalogService.getCatalogFilterHandler(type);
					return handler != null && !handler.isMultiInstance()? true: false;
				})
				.collect(Collectors.toSet());
		
		catalogService.getCatalogFilterHandlers().stream()
				.filter(handler -> handler.isEnabled(false))
				.filter(handler -> !unaddableHanderTypes.contains(handler.getType()))
				.sorted((h1, h2) -> Integer.compare(h2.getSortOrder(), h1.getSortOrder()))
				.forEach(handler -> {
					FormLink link = uifactory.addFormLink(handler.getType(), CMD_ADD, handler.getTypeI18nKey(), null, dummyCont, Link.LINK);
					link.setUserObject(handler);
					addFilterDropdown.addElement(link);
				});
		
		addFilterDropdown.setVisible(addFilterDropdown.size() > 0);
	}

	private void loadModel() {
		List<CatalogFilter> catalogFilters = catalogService.getCatalogFilters(new CatalogFilterSearchParams());
		Collections.sort(catalogFilters);
		List<CatalogFilterRow> rows = new ArrayList<>(catalogFilters.size());
		for (int i = 0; i < catalogFilters.size(); i++) {
			CatalogFilter catalogFilter = catalogFilters.get(i);
			CatalogFilterRow row = new CatalogFilterRow(catalogFilter);
			
			CatalogFilterHandler handler = catalogService.getCatalogFilterHandler(catalogFilter.getType());
			if (handler != null) {
				row.setHandler(handler);
				row.setTranslatedType(translate(handler.getTypeI18nKey()));
				row.setDetails(handler.getDetails(getTranslator(), catalogFilter));
			}
			
			UpDown upDown = UpDownFactory.createUpDown("up_down_" + catalogFilter.getKey(), UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
			upDown.setUserObject(row);
			if (i == 0) {
				upDown.setTopmost(true);
			}
			if (i == catalogFilters.size() - 1) {
				upDown.setLowermost(true);
			} 
			row.setUpDown(upDown);
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + catalogFilter.getSortOrder(), "tools", "", null, null, Link.NONTRANSLATED);
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
			if (userObject instanceof CatalogFilterRow) {
				CatalogFilterRow catalogFilterRow = (CatalogFilterRow)userObject;
				doMove(catalogFilterRow, ude.getDirection());
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
				CatalogFilterHandler handler = (CatalogFilterHandler)link.getUserObject();
				doEditFilter(ureq, handler, null);
			} else if (cmd.startsWith("tools")) {
				CatalogFilterRow catalogFilterRow = (CatalogFilterRow)link.getUserObject();
				doOpenTools(ureq, catalogFilterRow, link);
			}
		} else if (tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					CatalogFilterRow catalogFilterRow  = dataModel.getObject(se.getIndex());
					doEditFilter(ureq, catalogFilterRow.getHandler(), catalogFilterRow.getCatalogFilter());
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (catalogFilterEditController == source) {
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
		removeAsListenerAndDispose(catalogFilterEditController);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		catalogFilterEditController = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doMove(CatalogFilterRow catalogFilterRow, Direction direction) {
		catalogService.doMove(catalogFilterRow.getCatalogFilter(), Direction.UP == direction);
		loadModel();
	}

	private void doEditFilter(UserRequest ureq, CatalogFilterHandler handler, CatalogFilter catalogFilter) {
		guardModalController(catalogFilterEditController);
		
		catalogFilterEditController = handler.createEditController(ureq, getWindowControl(), catalogFilter);
		listenTo(catalogFilterEditController);
		
		String title = catalogFilter != null? translate(handler.getEditI18nKey()): translate(handler.getAddI18nKey());
		cmc = new CloseableModalController(getWindowControl(), "close", catalogFilterEditController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(CatalogFilterRow catalogFilterRow) {
		catalogService.deleteCatalogFilter(catalogFilterRow.getCatalogFilter());
		loadModel();
		initAddLinks();
	}
	
	private void doOpenTools(UserRequest ureq, CatalogFilterRow catalogFilterRow, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), catalogFilterRow);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final Link deleteLink;
		
		private final CatalogFilterRow catalogFilterRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CatalogFilterRow catalogFilterRow) {
			super(ureq, wControl);
			this.catalogFilterRow = catalogFilterRow;
			
			VelocityContainer mainVC = createVelocityContainer("catalog_filter_tools");
			
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			this.fireEvent(ureq, Event.DONE_EVENT);
			if(deleteLink == source) {
				doDelete(catalogFilterRow);
			}
		}
		
	}

}
