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
package org.olat.core.gui.components.tree;

import org.olat.core.gui.control.Event;

/**
 * 
 * Description:<br>
 * Event for the drag and drop function in menu tree
 * 
 * <P>
 * Initial Date:  23 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-9: drag and drop in menu tree
public class TreeDropEvent extends Event {

	private static final long serialVersionUID = -2204436311054973710L;
	
	private final String droppedNodeId;
	private final String targetNodeId;
	private final boolean asChild;
	private final boolean atTheEnd;

	/**
	 * 
	 * @param command
	 * @param nodeId
	 */
	public TreeDropEvent(String command, String droppedNodeId, String targetNodeId, boolean asChild, boolean atTheEnd) {
		super(command);
		this.droppedNodeId = droppedNodeId;
		this.targetNodeId = targetNodeId;
		this.asChild = asChild;
		this.atTheEnd = atTheEnd;
	}

	/**
	 * @return the dropped nodeId
	 */
	public String getDroppedNodeId() {
		return droppedNodeId;
	}
	
	/**
	 * @return The targeted node id
	 */
	public String getTargetNodeId() {
		return targetNodeId;
	}
	
	public boolean isAsChild() {
		return asChild;
	}

	public boolean isAtTheEnd() {
		return atTheEnd;
	}

	@Override
	public String toString() {
		return "TreeDropEvent:{cmd:"+getCommand()+", droppedNodeId:"+droppedNodeId+", targetNodeId:"+targetNodeId+"}";
	}
}