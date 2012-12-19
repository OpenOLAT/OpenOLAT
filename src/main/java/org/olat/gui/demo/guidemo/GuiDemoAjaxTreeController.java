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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
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
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.core.gui.control.generic.ajax.tree.MoveTreeNodeEvent;
import org.olat.core.gui.control.generic.ajax.tree.TreeController;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeModifiedEvent;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.core.logging.OLATRuntimeException;

/**
 * Description:<br>
 * Demo of the ajax based menu tree
 * 
 * <P>
 * Initial Date:  29.05.2008 <br>
 * @author gnaegi
 */
public class GuiDemoAjaxTreeController extends BasicController {
	private TreeController treeCtr;
	private AjaxTreeModel treeModel;
	private VelocityContainer contentVC;
	private Link sortLink, inlineEditLink, selectNodeLink, removeNodeLink;
	private boolean isSorted = false, isInlineEdit = false;
	
	public GuiDemoAjaxTreeController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// Main view is a velocity container
		contentVC = createVelocityContainer("guidemo-ajaxtree");

		// Build tree model
		treeModel = buildTreeModel();
		
		// Create the ajax tree controller, add it to your main view
		treeCtr = new TreeController(ureq, getWindowControl(), "Time machine", treeModel, "myjsCallback");
		treeCtr.setTreeSorting(false, false, false);
		listenTo(treeCtr);
		contentVC.put("treeCtr", treeCtr.getInitialComponent());
		// Add link for sorting
		sortLink = LinkFactory.createButton("GuiDemoAjaxTreeController.sortlink", contentVC, this);
		// Add link for inline editing
		inlineEditLink = LinkFactory.createButton("GuiDemoAjaxTreeController.editlink", contentVC, this);
		// Start with no sorting and not inline editing
		contentVC.contextPut("isSorted", Boolean.valueOf(isSorted));
		contentVC.contextPut("isInlineEdit", Boolean.valueOf(isInlineEdit));
		// Add link to select certain node
		selectNodeLink = LinkFactory.createLink("GuiDemoAjaxTreeController.selectlink", contentVC, this);
		// Add link to remove a certain node
		removeNodeLink = LinkFactory.createLink("GuiDemoAjaxTreeController.removelink", contentVC, this);
		
	//add source view control
    Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), contentVC);
    contentVC.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(contentVC);
	}
	

	/**
	 * Internal helper to build a dummy tree model which displays some time codes
	 * @return
	 */
	private AjaxTreeModel buildTreeModel() {
		AjaxTreeModel model = new AjaxTreeModel("demomodelsdf"){
			@Override
			public List<AjaxTreeNode> getChildrenFor(String nodeId) {
				List<AjaxTreeNode> children = new ArrayList<AjaxTreeNode>();
				AjaxTreeNode child;
				try {
					child = new AjaxTreeNode( nodeId + ".1", "A wonderful day " + Calendar.getInstance().getTime().toString());
					// Setting some node attributes - see the Treenode or the extjs
					// documentation on what else you could use
					child.put(AjaxTreeNode.CONF_LEAF, true);// leafs can't be opened
					child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
					child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
					child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
					children.add(child);
					child = new AjaxTreeNode( nodeId + ".2", " Hello World " + Calendar.getInstance().getTime().toString());
					child.put(AjaxTreeNode.CONF_LEAF, false);
					child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, false); // sort folders above leafs
					child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
					child.put(AjaxTreeNode.CONF_ALLOWDROP, true);
					children.add(child);
					child = new AjaxTreeNode( nodeId + ".3", "I'm number two " + Calendar.getInstance().getTime().toString());
					child.put(AjaxTreeNode.CONF_LEAF, true); // leafs can't be opened
					child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
					child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "b_filetype_doc"); // a custom icon css class
					child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
					child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
					children.add(child);
					child = new AjaxTreeNode( nodeId + ".4", "Folder " + Calendar.getInstance().getTime().toString());
					child.put(AjaxTreeNode.CONF_LEAF, false);
					child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, false); // sort folders above leafs
					child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
					child.put(AjaxTreeNode.CONF_ALLOWDROP, true);
					children.add(child);
				} catch (JSONException e) {
					throw new OLATRuntimeException("Error while creating gui demo ajax tree model", e);
				}
				return children;
			}			
		};
		// Set a custom icon for the root node
		model.setCustomRootIconCssClass("o_st_icon");
		return model;
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
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == treeCtr) {
			// Catch move tree event. Here on the server side we can still prevent the
			// move operation to happen...
			if (event instanceof MoveTreeNodeEvent) {
				MoveTreeNodeEvent moveEvent = (MoveTreeNodeEvent) event;
				getWindowControl().setInfo("Node::" + moveEvent.getNodeId() + " moved to new parent::" + moveEvent.getNewParentNodeId() + " at position::" + moveEvent.getPosition());
				// Set status: allow move or don't allow move. For this demo we just say yes...
				// See also the js code in the guidemo-ajaxtree.html file!
				moveEvent.setResult(true, null, null);

			} else if (event instanceof TreeNodeClickedEvent) {
				TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
				getWindowControl().setInfo("Node::" + clickedEvent.getNodeId() + " got clicked!");

			} else if (event instanceof TreeNodeModifiedEvent) {
				TreeNodeModifiedEvent modifiedEvent = (TreeNodeModifiedEvent) event;
				getWindowControl().setInfo("Node::" + modifiedEvent.getNodeId() + " got modified, new value is \"" + modifiedEvent.getModifiedValue() + "\"!");
			}
		}

	}
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == sortLink) {
			// change sort order to opposite
			isSorted = !isSorted;
			treeCtr.setTreeSorting(isSorted, isSorted, isSorted);
			contentVC.contextPut("isSorted", Boolean.valueOf(isSorted));
		} else if (source == inlineEditLink) {
			isInlineEdit = !isInlineEdit;
			treeCtr.setTreeInlineEditing(isInlineEdit, null, null);
			contentVC.contextPut("isInlineEdit", Boolean.valueOf(isInlineEdit));
		} else if (source == selectNodeLink) {
			// create a path to a node and select this one
			treeCtr.selectPath("/demomodelsdf/demomodelsdf.4/demomodelsdf.4.2");
		} else if (source == removeNodeLink) {
			// create a path to a node and remove this one
			treeCtr.removePath("/demomodelsdf/demomodelsdf.4/demomodelsdf.4.2");
		}
	}

}
