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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * http://www.frentix.com,
 * <p>
 */
package org.olat.core.commons.controllers.filechooser;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.ajax.tree.TreeController;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.core.gui.control.generic.folder.FolderTreeModel;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Description:<br>
 * The file chooser controller allows selecting of files or directories
 * depending on the configuration. The controller offers a modern ajax based
 * dynamic tree and uses a static fallback for non ajax browsers
 * <p>
 * Note that the rootContainer can contain NamedContainers, however only on the
 * root level! The current implementation does not support hierarchical use of
 * NamedContainers.
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>FileChoosenEvent</li>
 * <li>Event.FAILED</li>
 * <li>Event.CANCELLED</li>
 * </ul>
 * In case of an Event.DONE you can use the getSelectedItem() method to get the
 * vfs item that was selected by the user
 * <P>
 * Initial Date: 12.06.2008 <br>
 * 
 * @author gnaegi
 */
public class FileChooserController extends BasicController {
	// ajax enabled file tree
	private TreeController treeCtr;
	private VFSAjaxTreeModel treeModel;
	private VelocityContainer contentVC;
	private Link cancelLink, selectLink;
	// fallback for non ajax
	private SelectionTree nonAjaxSelectionTree;
	private FolderTreeModel nonAjaxfolderTreeModel;
	// general variables
	private VFSItem selectedItem;
	private VFSContainer rootContainer;
	private boolean onlyLeafsSelectable;

	/**
	 * Constructor that allows the usage of a custom vfs item filter
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer
	 *            The root container that should be selected from
	 * @param customItemFilter
	 *            The custom filter to be used or NULL to not use any filter at
	 *            all
	 * @param onlyLeafsSelectable true: container elements can't be selected;
	 *          false: all items can be selected
	 * @param showTitle
	 *            true: show a file chooser title and description; false: show
	 *            only the tree without a title
	 */
	FileChooserController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, VFSItemFilter customItemFilter, boolean onlyLeafsSelectable, boolean showTitle) {
		super(ureq, wControl);
		this.rootContainer = rootContainer;
		this.onlyLeafsSelectable = onlyLeafsSelectable;

		boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		if (ajax) {
			// Main view is a velocity container
			contentVC = createVelocityContainer("filechooserajax");
			// Build tree model
			treeModel = new VFSAjaxTreeModel(rootContainer, customItemFilter);
			// Create the ajax tree controller, add it to your main view
			treeCtr = new TreeController(ureq, getWindowControl(), rootContainer.getName(), treeModel, null);
			listenTo(treeCtr);
			contentVC.put("treeCtr", treeCtr.getInitialComponent());
			// cancel and select links
			cancelLink = LinkFactory.createButton("cancel", contentVC, this);
			// select link is disabled until an item is selected
			selectLink = LinkFactory.createButton("select", contentVC, this);
			selectLink.setEnabled(false);
			// optional title
			contentVC.contextPut("showTitle", Boolean.valueOf(showTitle));
			//
			putInitialPanel(contentVC);
		} else {
			// fallback to old-school selection tree
			nonAjaxfolderTreeModel = new FolderTreeModel(ureq.getLocale(), rootContainer,  false, true, !onlyLeafsSelectable, false, customItemFilter);
			nonAjaxSelectionTree = new SelectionTree("stTree", getTranslator());
			nonAjaxSelectionTree.setTreeModel(nonAjaxfolderTreeModel);
			nonAjaxSelectionTree.addListener(this);
			nonAjaxSelectionTree.setFormButtonKey("select");
			//
			putInitialPanel(nonAjaxSelectionTree);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		// events from ajax tree controller
		if (source == treeCtr) {
			if (event instanceof TreeNodeClickedEvent) {
				// get the clicked node and resolve the corresponding file
				TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
				String path = clickedEvent.getNodeId();
				selectedItem = treeModel.resolveFileForTreeNodeId(path);
				// enable link, set dirty button class and trigger redrawing
				selectLink.setEnabled(true);
				selectLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
				selectLink.setDirty(true);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// events from ajax tree view
		if (source == cancelLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == selectLink) {
			if (selectedItem != null) {
				if (onlyLeafsSelectable && selectedItem instanceof VFSContainer) {
					showWarning("filechooser.tree.error.only.leafs", selectedItem.getName());
				} else {
					fireEvent(ureq, new FileChoosenEvent(selectedItem));					
				}
		
			} else {
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		}
		
		// events from legacy non-ajax selection tree
		else if (source == nonAjaxSelectionTree) {
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
				String selectedPath = nonAjaxfolderTreeModel.getSelectedPath(nonAjaxSelectionTree.getSelectedNode());
				selectedItem = rootContainer.resolve(selectedPath);
				if (selectedItem != null) {
					fireEvent(ureq, new FileChoosenEvent(selectedItem));
				} else {
					fireEvent(ureq, Event.FAILED_EVENT);
				}

			} else if (te.getCommand().equals(TreeEvent.COMMAND_CANCELLED)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}

		}

	}
	
	
	@Override
	protected void doDispose() {
		// Controllers auto disposed by basic controller. NULL composite objects to help GC
		treeCtr = null;
		contentVC = null;
		cancelLink = null;
		selectLink = null;
		nonAjaxfolderTreeModel = null;
		nonAjaxSelectionTree = null;
	}


}
