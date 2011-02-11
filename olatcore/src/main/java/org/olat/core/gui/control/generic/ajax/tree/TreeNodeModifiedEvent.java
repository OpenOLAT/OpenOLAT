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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.core.gui.control.generic.ajax.tree;

import org.olat.core.gui.control.Event;

/**
 * <h3>Description:</h3> Event fired when a tree node has been edited
 * <p>
 * Initial Date:280.04.2009 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class TreeNodeModifiedEvent extends Event {
	private String nodeId;
	private String modifiedValue;

	/**
	 * Constructor
	 * 
	 * @param nodeId
	 *            ID of the clicked tree node
	 * @param modifiedValue
	 *            The modified value of the node
	 */
	public TreeNodeModifiedEvent(String nodeId, String modifiedValue) {
		super("modified");
		this.nodeId = nodeId;
		this.modifiedValue = modifiedValue;
	}

	/**
	 * @return ID of the clicked tree node
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @return The modified node value
	 */
	public String getModifiedValue() {
		return modifiedValue;
	}
}
