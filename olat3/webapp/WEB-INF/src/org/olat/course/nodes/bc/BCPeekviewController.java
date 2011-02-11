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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.course.nodes.bc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * <h3>Description:</h3> The folder peekview controller displays the configurable
 * amount of the newest files in this briefcase
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>OlatCmdEvent to notify that a jump to the course node is desired</li>
 * </ul>
 * <p>
 * Initial Date: 29.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class BCPeekviewController extends BasicController implements Controller {
	// comparator to sort the messages list by creation date
	private static final Comparator<VFSLeaf> dateSortingComparator = new Comparator<VFSLeaf>(){
		public int compare(final VFSLeaf leaf1, final VFSLeaf leaf2) {
			return Long.valueOf(leaf2.getLastModified()).compareTo(leaf1.getLastModified()); //last first
		}};
	// the current course node id
	private final String nodeId;

	/**
	 * Constructor
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param rootFolder The root folder of this briefcase
	 * @param nodeId The course node ID
	 * @param itemsToDisplay number of items to be displayed, must be > 0
	 */
	public BCPeekviewController(UserRequest ureq, WindowControl wControl, OlatRootFolderImpl rootFolder, String nodeId, int itemsToDisplay) {		
		super(ureq, wControl);
		this.nodeId = nodeId;
	
		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		// add items, only as many as configured
		List<VFSLeaf> allLeafs = new ArrayList<VFSLeaf>();
		addItems(rootFolder, allLeafs);
		// Sort messages by last modified date
		Collections.sort(allLeafs, dateSortingComparator);
		// only take the configured amount of messages
		List<VFSLeaf> leafs = new ArrayList<VFSLeaf>();
		for (int i = 0; i < allLeafs.size(); i++) {
			if (leafs.size() == itemsToDisplay) {
				break;
			}
			VFSLeaf leaf = allLeafs.get(i);
			leafs.add(leaf);
			// add link to item
			// Add link to jump to course node
			if (leaf instanceof LocalFileImpl) {
				LocalFileImpl localFile = (LocalFileImpl) leaf;
				String relPath = localFile.getBasefile().getAbsolutePath().substring(rootFolder.getBasefile().getAbsolutePath().length());
				Link nodeLink = LinkFactory.createLink("nodeLink_" + (i+1), peekviewVC, this);
				nodeLink.setCustomDisplayText(leaf.getName());
				int lastDot = localFile.getName().lastIndexOf(".");
				String cssClass = "";
				if (lastDot > 0 ) {
					cssClass = "b_filetype_" + localFile.getName().substring(lastDot+1);
				}
				nodeLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_filetype_file o_gotoNode " + cssClass);
				nodeLink.setUserObject(relPath);				
			} else {
				// hu? don't konw how to work with non-local impls
			}
		}
		peekviewVC.contextPut("leafs", leafs);
		// Add link to show all items (go to node)
		Link allItemsLink = LinkFactory.createLink("peekview.allItemsLink", peekviewVC, this);
		allItemsLink.setCustomEnabledLinkCSS("b_float_right");
		//
		this.putInitialPanel(peekviewVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link nodeLink = (Link) source;
			String relPath = (String) nodeLink.getUserObject();
			if (relPath == null) {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));								
			} else {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId + "/" + relPath));				
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose
	}

	/**
	 * Private helper method to get all files in a directory. Traverses the directory tree recursively
	 * @param container
	 * @param allLeafs
	 */
	private void addItems(VFSContainer container, List<VFSLeaf> allLeafs) {
		for (VFSItem vfsItem : container.getItems()) {
			if (vfsItem instanceof VFSLeaf) {
				// add leaf to our list
				VFSLeaf leaf = (VFSLeaf) vfsItem;
				allLeafs.add(leaf);
			} else if (vfsItem instanceof VFSContainer) {
				// do it recursively for all children
				VFSContainer childContainer = (VFSContainer) vfsItem;
				addItems(childContainer, allLeafs);
			} else {
				// hu?
			}
		}
	}
}
