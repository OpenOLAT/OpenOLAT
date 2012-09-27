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

import java.util.List;

import org.olat.core.gui.control.Event;

/**
*  Description:<br>
* @author Felix Jost
*/
public class TreeEvent extends Event {
	private static final long serialVersionUID = -1303677873858926922L;
	/**
	 * Comment for <code>COMMAND_TREENODE_CLICKED</code>
	 */
	public static final String COMMAND_TREENODE_CLICKED = "tectncl";
	/**
	 * Comment for <code>COMMAND_TREENODE_CLICKED</code>
	 */
	public static final String COMMAND_TREENODE_OPEN = "tectnopen";
	/**
	 * Comment for <code>COMMAND_TREENODE_CLICKED</code>
	 */
	public static final String COMMAND_TREENODE_CLOSE = "tectnclose";
	/**
	 * Comment for <code>COMMAND_TREENODES_SELECTED</code>
	 */
	public static final String COMMAND_TREENODES_SELECTED = "tectnsel";
	/**
	 * Comment for <code>COMMAND_CANCELLED</code>
	 */
	public static final String COMMAND_CANCELLED = "tecld";

	/**
	 * Comment for <code>CANCELLED_TREEEVENT</code>
	 */
	public static final TreeEvent CANCELLED_TREEEVENT = new TreeEvent(COMMAND_CANCELLED, "");
		
	private String nodeId;
	private List<String> nodeIds;
	private String subCommand;

	/**
	 * 
	 * @param command
	 * @param nodeId
	 */
	public TreeEvent(String command, String nodeId) {
		this(command, null, nodeId);
	}
	
	public TreeEvent(String command, String subCommand, String nodeId) {
		super(command);
		this.subCommand = subCommand;
		this.nodeId = nodeId;
	}

	/**
	 * @param command
	 * @param nodeIds
	 */
	public TreeEvent(String command, List<String> nodeIds) {
		this(command, null, nodeIds);
	}
	
	/**
	 * @param command
	 * @param nodeIds
	 */
	public TreeEvent(String command, String subCommand, List<String> nodeIds) {
		super(command);
		this.subCommand = subCommand;
		this.nodeIds = nodeIds;
	}

	/**
	 * @return the selected nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @return
	 */
	public List<String> getNodeIds() {
		return nodeIds;
	}
	
	public String getSubCommand() {
		return subCommand;
	}

	public String toString() {
		return "TreeEvent:{cmd:"+getCommand()+"," + (subCommand == null ? "" : "sub:" + subCommand) + " nodeid:"+nodeId+"}";
	}

}
