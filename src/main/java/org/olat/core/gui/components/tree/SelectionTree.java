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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description: <br> 
 * Call <code>setEscapeHtml(boolean) </code> to instruct the renderer to HTML escape or not.
 * 
 * 
 * @author Felix Jost
 */
public class SelectionTree extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new SelectionTreeRenderer();	
	
	private TreeModel treeModel;
	private String selectedNodeId; // selection of single select
	private String formButtonKey;
	private String actionCommand;
	private boolean allowEmptySelection = false;
	private boolean showCancelButton = true;
	private boolean escapeHtml = true;
	private Object userObject;
	
	/**
	 * @param name
	 */
	public SelectionTree(String name, Translator translator ) {
		super(name, Util.createPackageTranslator(SelectionTree.class, translator.getLocale(), translator));		
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		selectedNodeId = null;
		
		int iMode = -1;//0 -> OK, 1 -> CANCEL
		if (ureq.getParameter(Form.SUBMIT_IDENTIFICATION) != null) {
			iMode = 0;
			selectedNodeId = ureq.getParameter(SelectionTreeRenderer.ATTR_SELECTION);
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
			setDirty(true);			
			fireEvent(ureq, new TreeEvent(actionCommand == null ? TreeEvent.COMMAND_TREENODE_CLICKED : actionCommand, selectedNodeId));
		} else {
			fireEvent(ureq, TreeEvent.CANCELLED_TREEEVENT);
		}
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