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
package org.olat.catalog.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;

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
	private SelectionTree selectionTree;
	private VelocityContainer mainVC;
	private RepositoryEntry toBeAddedEntry;

	/**
	 * Constructor
	 * 
	 * @param wControl
	 * @param ureq
	 * @param toBeAddedEntry
	 */
	public CatalogEntryAddController(UserRequest ureq, WindowControl wControl, RepositoryEntry toBeAddedEntry) {
		super(ureq, wControl);
		this.toBeAddedEntry = toBeAddedEntry;
		List<CatalogEntry> catEntryList = CatalogManager.getInstance().getAllCatalogNodes();

		mainVC = createVelocityContainer("catMove");
		selectionTree = new SelectionTree("catSelection", getTranslator());
		selectionTree.addListener(this);
		selectionTree.setMultiselect(false);
		selectionTree.setFormButtonKey("cat.move.submit");
		selectionTree.setShowCancelButton(true);
		selectionTree.setTreeModel(new CatalogTreeModel(catEntryList, null, null));
		mainVC.put("tree", selectionTree);

		putInitialPanel(mainVC);

	}

	@Override
	protected void doDispose() {
		this.mainVC = null;
		this.selectionTree = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == selectionTree) {
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
				GenericTreeNode node = (GenericTreeNode) selectionTree.getSelectedNode();
				CatalogManager cm = CatalogManager.getInstance();
				Long newParentId = Long.parseLong(node.getIdent());
				CatalogEntry newParent = cm.loadCatalogEntry(newParentId);
				// check first if this repo entry is already attached to this new parent
				List<CatalogEntry> existingChildren = cm.getChildrenOf(newParent);
				for (CatalogEntry existingChild : existingChildren) {
					RepositoryEntry existingRepoEntry = existingChild.getRepositoryEntry();
					if (existingRepoEntry != null && existingRepoEntry.equalsByPersistableKey(toBeAddedEntry)) {
						showError("catalog.tree.add.already.exists", toBeAddedEntry.getDisplayname());
						return;
					}
				}
				CatalogEntry newEntry = cm.createCatalogEntry();
				newEntry.setRepositoryEntry(toBeAddedEntry);
				newEntry.setName(toBeAddedEntry.getDisplayname());
				newEntry.setDescription(toBeAddedEntry.getDescription());
				newEntry.setType(CatalogEntry.TYPE_LEAF);
				newEntry.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
				// save entry
				cm.addCatalogEntry(newParent, newEntry);
				fireEvent(ureq, Event.DONE_EVENT);

			} else if (te.getCommand().equals(TreeEvent.COMMAND_CANCELLED)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}

	}

	/**
	 * Internal helper to get all children for a given list of parent category items
	 * @param parents
	 * @return
	 */
	private List<CatalogEntry> fetchChildren(List<CatalogEntry> parents) {
		List<CatalogEntry> tmp = new ArrayList<CatalogEntry>();
		for (CatalogEntry child : parents) {
			tmp.add(child);
			if (child.getType() == CatalogEntry.TYPE_NODE) {
				tmp.addAll(fetchChildren((List<CatalogEntry>) CatalogManager.getInstance().getChildrenOf(child)));
			}
		}
		return tmp;
	}

	/**
	 * Internal helper method to get list of catalog entries where current user is
	 * in the owner group
	 * 
	 * @param ureq
	 * @return List of repo entries
	 */
	private List<CatalogEntry> getOwnedEntries(UserRequest ureq) {
		if (ureq.getUserSession().getRoles().isOLATAdmin()) {
			return (List<CatalogEntry>) CatalogManager.getInstance().getRootCatalogEntries();
		} else {
			return (List<CatalogEntry>) CatalogManager.getInstance().getCatalogEntriesOwnedBy(ureq.getIdentity());
		}
	}
}
