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

package org.olat.core.gui.components.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeHelper;

/**
 * Description: <br> 
 * Call <code>setEscapeHtml(boolean) </code> to instruct the renderer to HTML escape or not.
 * 
 * 
 * @author Felix Jost
 */
public class SelectionTree extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new SelectionTreeRenderer();	
	private static final String PACKAGE = Util.getPackageName(SelectionTree.class);
	
	
	private TreeModel treeModel;
	private String selectedNodeId = null; // selection of single select
	private List<String> selectedNodeIds = null; // selection of multiselect
	private String formButtonKey;
	private String actionCommand;
	private boolean multiselect = false;
	private boolean allowEmptySelection = false;
	private boolean greyOutNonSelectableEntries = false;
	private boolean showCancelButton = true;
	private boolean showAltTextAsHoverOnTitle = false;	
	private boolean escapeHtml = true;
	private Object userObject;
	

	/**
	 * @param name
	 */
	public SelectionTree(String name, Translator translator ) {
		super(name, new PackageTranslator(PACKAGE, translator.getLocale(), translator));		
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	protected void doDispatchRequest(UserRequest ureq) {
		selectedNodeId = null;
		selectedNodeIds = new ArrayList<String>();
		
		
		
		int iMode = -1;//0 -> OK, 1 -> CANCEL
		if (ureq.getParameter(Form.SUBMIT_IDENTIFICATION) != null) {
			iMode = 0;
			if(!multiselect){
				if (GUIInterna.isLoadPerformanceMode()) {
					String selPath = ureq.getParameter(SelectionTreeRenderer.ATTR_SELECTION);
					TreeNode tn = TreeHelper.resolveTreeNode(selPath, getTreeModel());
					selectedNodeId = tn.getIdent();
				} else {
					selectedNodeId = ureq.getParameter(SelectionTreeRenderer.ATTR_SELECTION);
				}
			} else {
				Enumeration<String> parameterNames = ureq.getHttpReq().getParameterNames();
				while (parameterNames.hasMoreElements()) {
					String parameterName = (String) parameterNames.nextElement();
					if (SelectionTreeRenderer.isMultiSelectParameter(parameterName)) {
						String selNodeId = null;
						if (GUIInterna.isLoadPerformanceMode()) {
							String treePath = ureq.getParameter(parameterName);
							TreeNode node = TreeHelper.resolveTreeNode(treePath, getTreeModel());
							selNodeId = node.getIdent();
						} else {
							selNodeId = ureq.getParameter(parameterName);
						}
						selectedNodeIds.add(selNodeId);
					}
				}
			}
		} else {
			iMode = 1;
		}
		//here are now events fired
		dispatchRequest(ureq, iMode);
	}

	/**
	 * @param ureq
	 * @param iMode
	 */
	private void dispatchRequest(UserRequest ureq, int iMode) {
		//test for recorder
		if (iMode == 0) {
			if (!multiselect) { // grab radio selection value
				setDirty(true);			
				fireEvent(ureq, new TreeEvent(actionCommand == null ? TreeEvent.COMMAND_TREENODE_CLICKED : actionCommand, selectedNodeId));
			} else { // grab checkbox selection values
				setDirty(true);				
				fireEvent(ureq, new TreeEvent(actionCommand == null ? TreeEvent.COMMAND_TREENODES_SELECTED : actionCommand, selectedNodeIds));
			}
		} else {
			fireEvent(ureq, TreeEvent.CANCELLED_TREEEVENT);
		}
	}

	/**
	 * Set wether to grey out non selectable entries in the tree.
	 * @param b
	 */
	public void setGreyOutNonSelectableEntries(boolean b) {
		this.greyOutNonSelectableEntries = b;
	}
	
	/**
	 * @return
	 */
	public boolean getGreyOutNonSelectableEntries() {
		return greyOutNonSelectableEntries;
	}
	
	/**
	 * @return
	 */
	public TreeNode getSelectedNode() {
		return (selectedNodeId == null ? null : treeModel.getNodeById(selectedNodeId));
	}

	/**
	 * @param nodeId
	 */
	public void setSelectedNodeId(String nodeId) {
		setDirty(true);
		selectedNodeId = nodeId;
	}

	/**
	 * @return MutableTreeModel
	 */
	public TreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * Sets the treeModel.
	 * 
	 * @param treeModel The treeModel to set
	 */
	public void setTreeModel(TreeModel treeModel) {
		setDirty(true);
		this.treeModel = treeModel;
	}

	/**
	 * If set to true, user may select more than one treenode.
	 * 
	 * @param b
	 */
	public void setMultiselect(boolean b) {
		multiselect = b;
	}

	/**
	 * @return
	 */
	public boolean isMultiselect() {
		return multiselect;
	}

	/**
	 * @return
	 */
	public String getFormButtonKey() {
		return formButtonKey;
	}

	/**
	 * @param string
	 */
	public void setFormButtonKey(String string) {
		formButtonKey = string;
	}

	/**
	 * @return
	 */
	public String getActionCommand() {
		return actionCommand;
	}

	/**
	 * @param string
	 */
	public void setActionCommand(String string) {
		actionCommand = string;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * @param showCancelButton The showCancelButton to set.
	 */
	public void setShowCancelButton(boolean showCancelButton) {
		this.showCancelButton = showCancelButton;
	}

	/**
	 * @return Returns the showCancelButton.
	 */
	public boolean isShowCancelButton() {
		return showCancelButton;
	}

	
	
	public boolean isAllowEmptySelection() {
		return allowEmptySelection;
	}

	public void setAllowEmptySelection(boolean allowEmptySelection) {
		this.allowEmptySelection = allowEmptySelection;
	}


	public boolean isShowAltTextAsHoverOnTitle() {
		return showAltTextAsHoverOnTitle;
	}


	public void setShowAltTextAsHoverOnTitle(boolean showAltTextAsHoverOnTitle) {
		this.showAltTextAsHoverOnTitle = showAltTextAsHoverOnTitle;
	}
	
	/**
	 *
	 * @param escapeHtml
	 */
	public void setEscapeHtml(boolean escapeHtml) {
		this.escapeHtml = escapeHtml;		
	}
	
	protected boolean isEscapeHtml() {
		return escapeHtml;
	}

}