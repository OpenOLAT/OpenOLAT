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
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.tree.TreeController;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;


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
public class CustomLinkChooserController extends DefaultController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CustomLinkChooserController.class);
	
	private Translator trans;
	private VelocityContainer mainVC;

	private SelectionTree jumpInSelectionTree;
	private CustomLinkTreeModel customLinkTreeModel;

	private TreeController ajaxTreeController;
	private Link chooseLink, cancelLink;
	private String selectedAjaxTreePath;
	
	/**
	 * Constructor
	 */
	public CustomLinkChooserController(UserRequest ureq, WindowControl wControl, CustomLinkTreeModel customLinkTreeModel) {
		super(wControl);
		trans = Util.createPackageTranslator(this.getClass(), ureq.getLocale());
		mainVC = new VelocityContainer("mainVC", VELOCITY_ROOT + "/internallinkchooser.html", trans, this);

		this.customLinkTreeModel = customLinkTreeModel;
		boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		if (ajax) {
			// For real browsers we use the cool ajax tree
			ajaxTreeController = new TreeController(ureq, getWindowControl(), customLinkTreeModel.getRootNode().getTitle(), customLinkTreeModel, null);
			ajaxTreeController.addControllerListener(this);
			mainVC.put("internalLinkTree", ajaxTreeController.getInitialComponent());
			// choose and cancel link
			chooseLink = LinkFactory.createButton("selectfile", mainVC, this);
			cancelLink = LinkFactory.createButton("cancel", mainVC, this);
		} else {
			// Legacy mode with old selection component
			jumpInSelectionTree = new SelectionTree("internalLinkTree", trans);
			jumpInSelectionTree.setTreeModel(customLinkTreeModel);
			jumpInSelectionTree.addListener(this);
			jumpInSelectionTree.setFormButtonKey("select");
			mainVC.put("internalLinkTree", jumpInSelectionTree);
		}
		setInitialComponent(mainVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == jumpInSelectionTree) { // Events from the legacy selection tree
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
				// create something like imagepath="javascript:parent.gotonode(<nodeId>)"
				// notify parent controller
				String url = customLinkTreeModel.getInternalLinkUrlFor(jumpInSelectionTree.getSelectedNode().getIdent());
				fireEvent(ureq, new URLChoosenEvent(url));
				
			} else if (te.getCommand().equals(TreeEvent.COMMAND_CANCELLED)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} 
		} else // Events from ajax tree view
			if (source == chooseLink) {
				if (selectedAjaxTreePath != null) {
					String url = customLinkTreeModel.getInternalLinkUrlFor(selectedAjaxTreePath);
					fireEvent(ureq, new URLChoosenEvent(url));
				} else {
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			} else if (source == cancelLink) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
 

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == ajaxTreeController) {
			if (event instanceof TreeNodeClickedEvent) {
				// get the clicked node and resolve the corresponding file
				TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
				selectedAjaxTreePath = clickedEvent.getNodeId();
				// enable link, set dirty button class and trigger redrawing
				chooseLink.setEnabled(true);
				chooseLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
				chooseLink.setDirty(true);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
	}
}