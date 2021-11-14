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
package org.olat.repository.ui.catalog;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.CatalogManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This subworkflow creates a selection tree to move a level from within the
 * catalog to another level
 * <P>
 * Events fired by this controller:
 * <UL>
 * <LI>Event.DONE_EVENT</LI>
 * <LI>Event.CANCELLED_EVENT</LI>
 * </UL>
 * <P>
 * Initial Date: 04.06.2008 <br>
 * 
 * @author Florian Gnägi, frentix GmbH
 */
public class CatalogEntryAddController extends BasicController {
	
	protected MenuTree selectionTree;
	private VelocityContainer mainVC;
	private Link okButton, cancelButton;
	private RepositoryEntry toBeAddedEntry;
	
	@Autowired
	protected CatalogManager catalogManager;

	/**
	 * Constructor
	 * 
	 * @param wControl
	 * @param ureq
	 * @param toBeAddedEntry
	 */
	public CatalogEntryAddController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry toBeAddedEntry, boolean withButtons, boolean title) {
		super(ureq, wControl);
		
		this.toBeAddedEntry = toBeAddedEntry;
		
		List<CatalogEntry> catEntryList = catalogManager.getAllCatalogNodes();
		Collections.sort(catEntryList, new CatalogEntryNodeComparator(getLocale()));

		mainVC = createVelocityContainer("catMove");
		mainVC.contextPut("withTitle", Boolean.valueOf(title));
		selectionTree = new MenuTree("catSelection");
		selectionTree.setExpandSelectedNode(true);
		selectionTree.setUnselectNodes(true);
		selectionTree.addListener(this);
		selectionTree.setTreeModel(new CatalogTreeModel(catEntryList, null, null));
		mainVC.put("tree", selectionTree);
		
		if(withButtons) {
			okButton = LinkFactory.createButton("ok", mainVC, this);
			okButton.setElementCssClass("btn btn-primary o_sel_catalog_add_select");
			cancelButton = LinkFactory.createButton("cancel", mainVC, this);
			cancelButton.setElementCssClass("o_sel_catalog_add_cancel");
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		mainVC = null;
		selectionTree = null;
        super.doDispose();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == okButton) {
			TreeNode node = selectionTree.getSelectedNode();
			if(node != null) {//no selection is allowed
				Long newParentId = Long.parseLong(node.getIdent());
				insertNode(ureq, newParentId);
			}
		} else if(source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
	
	protected void insertNode(UserRequest ureq, Long newParentId) {
		CatalogEntry newParent = catalogManager.loadCatalogEntry(newParentId);
		// check first if this repo entry is already attached to this new parent
		List<CatalogEntry> existingChildren = catalogManager.getChildrenOf(newParent);
		for (CatalogEntry existingChild : existingChildren) {
			RepositoryEntry existingRepoEntry = existingChild.getRepositoryEntry();
			if (existingRepoEntry != null && existingRepoEntry.equalsByPersistableKey(toBeAddedEntry)) {
				showError("catalog.tree.add.already.exists", toBeAddedEntry.getDisplayname());
				return;
			}
		}
		CatalogEntry newEntry = catalogManager.createCatalogEntry();
		newEntry.setRepositoryEntry(toBeAddedEntry);
		newEntry.setName(toBeAddedEntry.getDisplayname());
		newEntry.setDescription(toBeAddedEntry.getDescription());
		newEntry.setType(CatalogEntry.TYPE_LEAF);
		// save entry
		catalogManager.addCatalogEntry(newParent, newEntry);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
