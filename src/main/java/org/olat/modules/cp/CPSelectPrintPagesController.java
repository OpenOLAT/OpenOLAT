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
package org.olat.modules.cp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.nodes.INode;

/**
 * 
 * Description:<br>
 * Controller to select to list of nodes to print
 * VCRP-14
 * 
 * <P>
 * Initial Date:  9 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-14: print cp
public class CPSelectPrintPagesController extends FormBasicController {
	
	private final Map<String,MultipleSelectionElement> identToSelectionMap = new HashMap<String,MultipleSelectionElement>();
	private final List<MultipleSelectionElement> nodeSelections = new ArrayList<MultipleSelectionElement>();

	private final  CPManifestTreeModel ctm;
	
	private FormLink selectAll;
	private FormLink deselectAll;
	private FormSubmit submit;
	
	public CPSelectPrintPagesController(UserRequest ureq, WindowControl wControl, CPManifestTreeModel ctm) {
		super(ureq, wControl, "cpprint");
		
		this.ctm = ctm;
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("print.node.list.title");
		setFormDescription("print.node.list.desc");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			TreeNode rootNode = ctm.getRootNode();
			initTreeRec(0, rootNode, layoutContainer);
			layoutContainer.contextPut("nodeSelections", nodeSelections);
		}

		selectAll = uifactory.addFormLink("checkall", "form.checkall", null, formLayout, Link.LINK);
		deselectAll = uifactory.addFormLink("uncheckall", "form.uncheckall", null, formLayout, Link.LINK);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("print-cancel", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setRootForm(mainForm);
		submit = uifactory.addFormSubmitButton("print-button", "print.node", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private void initTreeRec(int level, TreeNode node, FormLayoutContainer layoutcont) {
		String[] cssClass = new String[]{"b_tree_l" + level};
		String[] singleKey = new String[]{node.getIdent()};
		String[] singleValue = new String[]{node.getTitle()};
		MultipleSelectionElement nodeSelection = uifactory.addCheckboxesVertical("print.node.list." + nodeSelections.size(), layoutcont, singleKey, singleValue, cssClass, 1);
		nodeSelection.setLabel("print.node.list", null);
		nodeSelection.setUserObject(new SelectNodeObject(node, level));
		nodeSelection.addActionListener(this, FormEvent.ONCLICK);
		nodeSelection.select(node.getIdent(), true); 		
		nodeSelections.add(nodeSelection);
		identToSelectionMap.put(node.getIdent(), nodeSelection);
		layoutcont.add(nodeSelection.getComponent().getComponentName(), nodeSelection);
		
		int numOfChildren = node.getChildCount();
		for(int i=0; i<numOfChildren; i++) {
			initTreeRec(level + 1, (TreeNode)node.getChildAt(i), layoutcont);
		}
	}
	
	public List<String> getSelectedNodeIdents() {
		if(nodeSelections == null || nodeSelections.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<String> selectedNodeIdents = new ArrayList<String>();
		for(MultipleSelectionElement nodeSelection:nodeSelections) {
			if(nodeSelection.isMultiselect() &&nodeSelection.isSelected(0)) {
				SelectNodeObject treeNode = (SelectNodeObject)nodeSelection.getUserObject();
				String ident = treeNode.getNode().getIdent();
				selectedNodeIdents.add(ident);
			}
		}
		return selectedNodeIdents;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(nodeSelections.contains(source)) {
			MultipleSelectionElement nodeSelection = (MultipleSelectionElement)source;
			if(nodeSelection.isMultiselect()) {
				selectRec(nodeSelection, nodeSelection.isSelected(0));
			}
			// check for at least one selected node
			submit.setEnabled(false);
			for(MultipleSelectionElement selection:nodeSelections) {
				if (selection.isSelected(0)) {
					submit.setEnabled(true);
					break;
				}
			}
		} else if (source == selectAll) {
			for(MultipleSelectionElement nodeSelection:nodeSelections) {
				if(nodeSelection.isMultiselect() && !nodeSelection.isSelected(0)) {
					SelectNodeObject treeNode = (SelectNodeObject)nodeSelection.getUserObject();
					String ident = treeNode.getNode().getIdent();
					nodeSelection.select(ident, true);
				}
			} 
			submit.setEnabled(true);
		} else if (source == deselectAll) {
			for(MultipleSelectionElement nodeSelection:nodeSelections) {
				if(nodeSelection.isMultiselect() && nodeSelection.isSelected(0)) {
					SelectNodeObject treeNode = (SelectNodeObject)nodeSelection.getUserObject();
					String ident = treeNode.getNode().getIdent();
					nodeSelection.select(ident, false);
				}
			}
			submit.setEnabled(false);
		} else {
				super.formInnerEvent(ureq, source, event);	
		}
	}
	
	private void selectRec(MultipleSelectionElement nodeSelection, boolean select) {
		SelectNodeObject userObject = (SelectNodeObject)nodeSelection.getUserObject();
		TreeNode node = userObject.getNode();
		if(nodeSelection.isMultiselect()) {
			nodeSelection.select(node.getIdent(), select);
		}

		for(int i=node.getChildCount(); i-->0; ) {
			INode child = node.getChildAt(i);
			String ident = child.getIdent();
			MultipleSelectionElement childNodeSelection = identToSelectionMap.get(ident);
			selectRec(childNodeSelection, select);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public class SelectNodeObject {
		private final int indentation;
		private final TreeNode node;
		
		public SelectNodeObject(TreeNode node, int indentation) {
			this.node = node;
			this.indentation = indentation;
		}

		public String getIndentation() {
			return Integer.toString(indentation);
		}

		public TreeNode getNode() {
			return node;
		}
	}
}