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
package org.olat.repository.ui.settings;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.catalog.CatalogEntryAddController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSettingsController extends FormBasicController {
	
	private FormLink addToCatalogLink;
	private FlexiTableElement tableEl;
	private CategoriesListModel model;

	private CloseableModalController cmc;
	private Controller catalogAdddController;
	private AccessInformationController infosCtrl;
	
	private RepositoryEntry entry;

	@Autowired
	private CatalogManager catalogManager;
	
	public CatalogSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "catalog_settings");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		
		infosCtrl = new AccessInformationController(ureq, wControl, entry);
		listenTo(infosCtrl);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("infos", infosCtrl.getInitialFormItem());
		
		addToCatalogLink = uifactory.addFormLink("details.catadd", formLayout, Link.BUTTON);
		addToCatalogLink.setIconLeftCSS("o_icon o_icon_add");
		addToCatalogLink.setElementCssClass("o_sel_repo_add_to_catalog");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("catalog.path", 0));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", translate("remove"), "remove"));
		
		model = new CategoriesListModel(new ArrayList<>(), columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 200, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyTableSettings("no.catalog.entries" ,"no.catalog.entries.hint", "o_icon_catalog");
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CatalogEntry row = model.getObject(se.getIndex());
				if("remove".equals(cmd)) {
					doRemove(row);
				}
			}
		} else if(addToCatalogLink == source) {
			doAddCatalog(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(catalogAdddController == source) {
			cmc.deactivate();
			loadModel();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(catalogAdddController);
		removeAsListenerAndDispose(cmc);
		catalogAdddController = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	
	
	/**
	 * Internal helper to initiate the add to catalog workflow
	 * @param ureq
	 */
	private void doAddCatalog(UserRequest ureq) {
		removeAsListenerAndDispose(catalogAdddController);
		removeAsListenerAndDispose(cmc);
		
		catalogAdddController = new CatalogEntryAddController(ureq, getWindowControl(), entry, true, false);
		listenTo(catalogAdddController);
		cmc = new CloseableModalController(getWindowControl(), "close",
				catalogAdddController.getInitialComponent(), true, translate("details.catadd"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRemove(CatalogEntry catEntry) {
		List<CatalogEntry> children = catalogManager.getChildrenOf(catEntry);
		// find all child element of this level that reference our repo entry
		for (CatalogEntry child : children) {
			RepositoryEntry childRepoEntry = child.getRepositoryEntry();
			if (childRepoEntry != null && childRepoEntry.equalsByPersistableKey(entry)) {
				// remove from catalog
				catalogManager.deleteCatalogEntry(child);
			}
		}

		//update table
		loadModel();
	}
	
	private void loadModel() {
		List<CatalogEntry> catalogEntries = catalogManager.getCatalogCategoriesFor(entry);
		model.setObjects(catalogEntries);
		flc.contextPut("hasContent", Boolean.valueOf(!catalogEntries.isEmpty()));
		tableEl.reset();
	}

	private class CategoriesListModel extends DefaultFlexiTableDataModel<CatalogEntry> {

		public CategoriesListModel(List<CatalogEntry> catalogEntries, FlexiTableColumnModel columnModel) {
			super(catalogEntries, columnModel);
		}

		@Override
		public DefaultFlexiTableDataModel<CatalogEntry> createCopyWithEmptyList() {
			return new CategoriesListModel(new ArrayList<CatalogEntry>(), getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			CatalogEntry catEntry = getObject(row);
			if(col == 0) {
				// calculate cat entry path: travel up to the root node
				String path = "";
				CatalogEntry tempEntry = catEntry;
				while (tempEntry != null) {
					path = "/" + tempEntry.getName() + path;
					tempEntry = tempEntry.getParent();
				}
				return path;
			}
			return "ERROR";
		}
	}
}
