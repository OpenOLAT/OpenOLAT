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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.core.gui.control.generic.ajax.tree;

import org.olat.core.gui.control.Event;

/**
 * <h3>Description:</h3>
 * Event fired when a tree node has been moved within the tree.
 * <p>
 * Initial Date: 04.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class MoveTreeNodeEvent extends Event {
	private String nodeId;

	private String oldParentNodeId;

	private String newParentNodeId;

	private int position;

	private boolean resultSuccess;

	private String resultFailureTitle;

	private String resultFailureMessage;

	/**
	 * Constructor
	 * 
	 * @param nodeId
	 *            ID of the moved tree node
	 * @param oldParentNodeId
	 *            ID of the parent tree node before move
	 * @param newParentNodeId
	 *            ID of the parent tree node after move
	 * @param position
	 *            position in the parents child list after move
	 */
	public MoveTreeNodeEvent(String nodeId, String oldParentNodeId,
			String newParentNodeId, int position) {
		super("move");
		this.nodeId = nodeId;
		this.oldParentNodeId = oldParentNodeId;
		this.newParentNodeId = newParentNodeId;
		this.position = position;
	}

	/**
	 * @return The position in the parents child list after move
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return ID of the parent tree node after move
	 */
	public String getNewParentNodeId() {
		return newParentNodeId;
	}

	/**
	 * @return ID of the moved tree node
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @return ID of the parent tree node before move
	 */
	public String getOldParentNodeId() {
		return oldParentNodeId;
	}

	/**
	 * Set the result of this move operation. The event object is used as
	 * callback.
	 * 
	 * @param isSuccess
	 *            true if move is allowed and performed on the data model; false
	 *            if move not allowed or an error occured.
	 * @param failureTitle
	 *            The title of the message in case of success = false
	 * @param failureMessage
	 *            The message showed to the user in case of success = false
	 */
	public void setResult(boolean isSuccess, String failureTitle,
			String failureMessage) {
		this.resultSuccess = isSuccess;
		this.resultFailureTitle = failureTitle;
		this.resultFailureMessage = failureMessage;
	}

	/**
	 * @return true if move is allowed and performed on the data model; false if
	 *         move not allowed or an error occured.
	 */
	public boolean isResultSuccess() {
		return resultSuccess;
	}

	/**
	 * @return The title of the message in case of success = false or NULL if
	 *         not set
	 */
	public String getResultFailureTitle() {
		return resultFailureTitle;
	}

	/**
	 * @return The message showed to the user in case of success = false or NULL
	 *         if not set
	 */
	public String getResultFailureMessage() {
		return resultFailureMessage;
	}

}
