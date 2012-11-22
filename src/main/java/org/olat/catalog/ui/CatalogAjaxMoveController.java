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

/**
 * Description:<br>
 * The ajax catalog move controller implements a dynamic tree that offers
 * catalog navigating and selection of a catalog element. The to be moved entry
 * is then moved to the selected catalog category
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELED_EVENT</li>
 * <li>Event.DONE_EVENT</li>
 * <li>Event.FAILED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: 30.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CatalogAjaxMoveController extends BasicController implements CatalogMoveController {

	private TreeController treeCtr;
	private AjaxTreeModel treeModel;
	private VelocityContainer contentVC;
	private CatalogEntry toBeMovedEntry;
	private List<CatalogEntry> ownedEntries;
	private Link cancelLink, selectLink;
	private CatalogEntry selectedParent;

	/**
	 * Constructor for the ajax move catalog entry controller
	 * @param ureq
	 * @param wControl
	 * @param toBeMovedEntry
	 */
	public CatalogAjaxMoveController(UserRequest ureq, WindowControl wControl, CatalogEntry toBeMovedEntry) {
		super(ureq, wControl);
		this.toBeMovedEntry = toBeMovedEntry;
		// Main view is a velocity container
		contentVC = createVelocityContainer("catalogentryajaxmove");
		contentVC.contextPut("entryname", toBeMovedEntry.getName());
		// build current path for gui
		CatalogEntry tempEntry = toBeMovedEntry;
		String path = "";
		while (tempEntry != null) {
			path = "/" + tempEntry.getName() + path;
			tempEntry = tempEntry.getParent();
		}
		contentVC.contextPut("path", path.toString());
		
		// Fetch all entries that can be accessed by this user. This is kept as a
		// local copy for performance reasons. This is only used when a node is
		// moved, when moving leafs we don't check for ownership of the category
		if (toBeMovedEntry.getType() == CatalogEntry.TYPE_NODE) {
			ownedEntries = getOwnedEntries(ureq);
		}
		
		CatalogManager cm = CatalogManager.getInstance();
		CatalogEntry rootce = (CatalogEntry) cm.getRootCatalogEntries().get(0);
		// Build tree model
		treeModel = new CatalogAjaxTreeModel(rootce, toBeMovedEntry, ownedEntries, false, false);
		
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
	
	@Override
	public CatalogEntry getMovedCatalogEntry() {
		return toBeMovedEntry;
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
		ownedEntries = null;
		toBeMovedEntry = null;
		cancelLink = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == treeCtr) {
			if (event instanceof TreeNodeClickedEvent) {
				TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
				// try to update the catalog
				CatalogManager cm = CatalogManager.getInstance();
				String nodeId = clickedEvent.getNodeId();
				Long newParentId = Long.parseLong(nodeId);
				CatalogEntry newParent = cm.loadCatalogEntry(newParentId);
				boolean hasAccess;
				if (toBeMovedEntry.getType() == CatalogEntry.TYPE_LEAF) {
					// Leafs can be attached anywhere in the catalog, no need to check for
					// category ownership
					hasAccess = true;
				} else {
					// Check if the user owns this category or one of the preceding
					// categories. 
					hasAccess = cm.isEntryWithinCategory(newParent, ownedEntries);					
				}
				if (hasAccess) {
					// don't move entry right away, user must select submit button first
					selectedParent = newParent;
					// enable link, set dirty button class and trigger redrawing
					selectLink.setEnabled(true);
					selectLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
					selectLink.setDirty(true); 
				} else {
					showWarning("catalog.tree.move.noaccess");
				}
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
				boolean success = cm.moveCatalogEntry(toBeMovedEntry, selectedParent);
				if (success) {
					fireEvent(ureq, Event.DONE_EVENT);					
				} else {
					fireEvent(ureq, Event.FAILED_EVENT);
				}					
			}
		}
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
			return CatalogManager.getInstance().getRootCatalogEntries();
		} else {
			return CatalogManager.getInstance().getCatalogEntriesOwnedBy(ureq.getIdentity());
		}
	}

}
