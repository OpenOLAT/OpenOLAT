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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.repository.ui.catalog;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.InsertEvent;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreePosition;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.CatalogEntry;
import org.olat.repository.manager.CatalogManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This subworkflow creates a selection tree to move a level from within the
 * catalog to another level
 * <P>
 * Events fired by this controller:
 * <UL>
 * <LI>Event.FAILED_EVENT</LI>
 * <LI>Event.DONE_EVENT</LI>
 * <LI>Event.CANCELLED_EVENT</LI>
 * </UL>
 * <P>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author BPS
 */
public class CatalogEntryMoveController extends BasicController {
	
	private Link selectButton, cancelButton;
	private final MenuTree selectionTree;
	private final CatalogEntry moveMe;
	
	@Autowired
	private CatalogManager catalogManager;

	/**
	 * Constructor
	 * 
	 * @param wControl
	 * @param ureq
	 * @param moveMe The catalog entry to be moved
	 * @param trans
	 */
	public CatalogEntryMoveController(WindowControl wControl, UserRequest ureq, CatalogEntry moveMe, Translator trans) {
		super(ureq, wControl, trans);
		this.moveMe = moveMe;
		List<CatalogEntry> ownedEntries = getOwnedEntries(ureq);
		List<CatalogEntry> catEntryList = fetchChildren(ownedEntries);

		VelocityContainer mainVC = createVelocityContainer("catMove");
		selectionTree = new MenuTree(null, "catSelection", this);
		selectionTree.enableInsertTool(true);
		selectionTree.setTreeModel(new CatalogTreeModel(catEntryList, moveMe, ownedEntries));
		
		selectButton = LinkFactory.createButton("cat.move.submit", mainVC, this);
		selectButton.setCustomEnabledLinkCSS("btn btn-primary");
		selectButton.setCustomDisabledLinkCSS("btn btn-default");
		selectButton.setEnabled(false);
		cancelButton = LinkFactory.createButton("cancel", mainVC, this);
		
		mainVC.put("tree", selectionTree);
		putInitialPanel(mainVC);
	}
	
	public CatalogEntry getMoveMe() {
		return moveMe;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(cancelButton == source) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(selectButton == source) {
			TreePosition tp = selectionTree.getInsertionPosition();
			Long newParentId = Long.parseLong(tp.getParentTreeNode().getIdent());
			CatalogEntry newParent = catalogManager.loadCatalogEntry(newParentId);
			if (!catalogManager.moveCatalogEntry(moveMe, newParent)) {
				fireEvent(ureq, Event.FAILED_EVENT);
			} else {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(event instanceof InsertEvent) {
			boolean canSelect = selectionTree.getInsertionPoint() != null;
			selectButton.setEnabled(canSelect);
		}
	}

	/**
	 * Internal helper to get all children for a given list of parent category items
	 * @param parents
	 * @return
	 */
	private List<CatalogEntry> fetchChildren(List<CatalogEntry> parents) {
		List<CatalogEntry> tmp = new ArrayList<>();
		for (CatalogEntry child : parents) {
			tmp.add(child);
			if (child.getType() == CatalogEntry.TYPE_NODE) {
				tmp.addAll(fetchChildren(catalogManager.getChildrenOf(child)));
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
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			return catalogManager.getRootCatalogEntries();
		} else {
			return catalogManager.getCatalogEntriesOwnedBy(getIdentity());
		}
	}
}
