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
* <p>
*/ 

package org.olat.core.commons.controllers.linkchooser;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;


/**
 * Generates internal link. Show a tree-model to select an internal link. The user can select a course-node 
 * for which an internal link will be generated (gotoNode-link).
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>URLChoosenEvent(URL) containing the selected file URL
 * <li>Event.CANCELLED_EVENT
 * </ul>

 * @author Christian Guretzki
 */
public class CustomLinkChooserController extends BasicController {

	private final MenuTree jumpInSelectionTree;
	private final Link selectButton, cancelButton;
	private final CustomLinkTreeModel customLinkTreeModel;
	
	/**
	 * Constructor
	 */
	public CustomLinkChooserController(UserRequest ureq, WindowControl wControl, CustomLinkTreeModel customLinkTreeModel) {
		super(ureq, wControl);
		this.customLinkTreeModel = customLinkTreeModel;
		
		VelocityContainer mainVC = createVelocityContainer("internallinkchooser");
		jumpInSelectionTree = new MenuTree(null, "internalLinkTree", this);
		jumpInSelectionTree.setTreeModel(customLinkTreeModel);
		mainVC.put("internalLinkTree", jumpInSelectionTree);

		selectButton = LinkFactory.createButton("selectfile", mainVC, this);
		selectButton.setCustomEnabledLinkCSS("btn btn-default");
		cancelButton = LinkFactory.createButton("cancel", mainVC, this);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(selectButton == source) {
			if(jumpInSelectionTree.getSelectedNode() != null) {
				String url = customLinkTreeModel.getInternalLinkUrlFor(jumpInSelectionTree.getSelectedNode().getIdent());
				fireEvent(ureq, new URLChoosenEvent(url));
			}
		} else if (source == jumpInSelectionTree) {
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				if(jumpInSelectionTree.getSelectedNode() != null) {
					selectButton.setCustomEnabledLinkCSS("btn btn-default o_button_dirty");
				} else {
					selectButton.setCustomEnabledLinkCSS("btn btn-default");
				}
			}
		}
	}
}