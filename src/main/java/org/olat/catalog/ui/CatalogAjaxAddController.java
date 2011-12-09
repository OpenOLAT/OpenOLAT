/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.catalog.ui;

import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.core.gui.control.generic.ajax.tree.TreeController;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * The ajax catalog add controller implements a dynamic tree that offers catalog
 * navigating and selection of a catalog element. The to be added reporitory
 * entry is then added to the selected catalog category
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELED_EVENT</li>
 * <li>Event.DONE_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: 30.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CatalogAjaxAddController extends BasicController {

	protected TreeController treeCtr;
	private AjaxTreeModel treeModel;
	private VelocityContainer contentVC;
	protected RepositoryEntry toBeAddedEntry;
	protected Link cancelLink, selectLink;
	protected CatalogEntry selectedParent;
	
	public CatalogAjaxAddController(UserRequest ureq, WindowControl wControl, RepositoryEntry toBeAddedEntry) {
		super(ureq, wControl);
		this.toBeAddedEntry = toBeAddedEntry;
		// Main view is a velocity container
		contentVC = createVelocityContainer("catalogentryajaxadd");
		contentVC.contextPut("entryname", toBeAddedEntry.getDisplayname());
		
		CatalogManager cm = CatalogManager.getInstance();
		CatalogEntry rootce = (CatalogEntry) cm.getRootCatalogEntries().get(0);
		// Build tree model
		treeModel = new CatalogAjaxTreeModel(rootce, null, null, false, false);
		
		// Create the ajax tree controller, add it to your main view
		treeCtr = new TreeController(ureq, getWindowControl(), rootce.getName(), treeModel, null);
		listenTo(treeCtr);
		contentVC.put("treeCtr", treeCtr.getInitialComponent());

		cancelLink = LinkFactory.createButton("cancel", contentVC, this);
		// select link is disabled until an item is selected
		selectLink = LinkFactory.createButton("select", contentVC, this);
		selectLink.setEnabled(false);
		
		putInitialPanel(contentVC);
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		contentVC = null;
		treeModel = null;
		// Controllers auto disposed by basic controller
		treeCtr = null;
		toBeAddedEntry = null;
		cancelLink = null;
		selectLink = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == treeCtr) {
			if (event instanceof TreeNodeClickedEvent) {
				TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
				// build new entry for this catalog level
				CatalogManager cm = CatalogManager.getInstance();
				String nodeId = clickedEvent.getNodeId();
				Long newParentId = Long.parseLong(nodeId);
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
				// don't create entry right away, user must select submit button first
				selectedParent = newParent;
				// enable link, set dirty button class and trigger redrawing
				selectLink.setEnabled(true);
				selectLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
				selectLink.setDirty(true); 
			}
		}

	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == cancelLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);					
		} else if (source == selectLink) {
			if (selectedParent != null) {
				CatalogManager cm = CatalogManager.getInstance();
				CatalogEntry newEntry = cm.createCatalogEntry();
				newEntry.setRepositoryEntry(toBeAddedEntry);
				newEntry.setName(toBeAddedEntry.getDisplayname());
				newEntry.setDescription(toBeAddedEntry.getDescription());
				newEntry.setType(CatalogEntry.TYPE_LEAF);
				newEntry.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
				// save entry
				cm.addCatalogEntry(selectedParent, newEntry);
				fireEvent(ureq, Event.DONE_EVENT);								
			}
		}
	}

}
