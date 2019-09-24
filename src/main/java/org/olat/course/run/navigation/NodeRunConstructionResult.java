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

package org.olat.course.run.navigation;

import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.logging.AssertException;

/**
 * Initial Date: 19.01.2005 <br>
 * @author Felix Jost
 */
public class NodeRunConstructionResult {
	private Controller runController;
	private TreeModel subTreeModel;
	private ControllerEventListener subTreeListener;
	private final String selectedTreeNodeId;
	
	/**
	 * 
	 * @param runController
	 * @param subTreeModel
	 * @param selectedTreeNodeId
	 * @param subTreeListener
	 */
	public NodeRunConstructionResult(Controller runController, TreeModel subTreeModel, String selectedTreeNodeId, ControllerEventListener subTreeListener) {
		this.selectedTreeNodeId = selectedTreeNodeId;
		if ((subTreeModel == null) != (subTreeListener == null)) throw new AssertException("subTreeModel must also have a subTreeListener");
		this.runController = runController;
		this.subTreeModel = subTreeModel;
		this.subTreeListener = subTreeListener;
	}
	
	

	/**
	 * @param runController
	 */
	public NodeRunConstructionResult(Controller runController) {
		this(runController, null, null, null);
	}

	/**
	 * @return the runcontroller
	 */
	public Controller getRunController() {
		return runController;
	}

	/**
	 * @return the ControllerEventListener for clicks in the nodes of the subtreemodel
	 */
	protected ControllerEventListener getSubTreeListener() {
		return subTreeListener;
	}

	/**
	 * @return the subtreemodel
	 */
	protected TreeModel getSubTreeModel() {
		return subTreeModel;
	}



	/**
	 * @return Returns the selectedTreeNodeId.
	 */
	protected String getSelectedTreeNodeId() {
		return selectedTreeNodeId;
	}
}

