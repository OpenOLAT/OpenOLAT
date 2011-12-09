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

package org.olat.modules.fo;

import java.io.Serializable;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.util.nodes.GenericNode;

/**
 * Initial Date: 2005-11-08
 * @author Alexander Schneider
 */
public class MessageNode extends GenericNode implements Serializable {
	private Long key;
	private String title;
	private String body;
	private Identity creator;
	private Date creationDate;
	private Identity modifier;
	private Date modifiedDate;
	private boolean isSticky;
	private boolean isClosed;
	private boolean isHidden;
	private boolean isMoved;

	/**
	 * 
	 * @param message
	 */
	public MessageNode(Message message) {
		this.key = message.getKey();
		this.title = message.getTitle();
		this.body = message.getBody();
		this.creator = message.getCreator();
		this.creationDate = message.getCreationDate();
		this.modifier = message.getModifier();
		this.modifiedDate = message.getLastModified();
		if(message.getParent()==null) {
			//relevant only for the threadtop messages
			this.isSticky = Status.getStatus(message.getStatusCode()).isSticky();
			this.isClosed = Status.getStatus(message.getStatusCode()).isClosed();
			this.isHidden = Status.getStatus(message.getStatusCode()).isHidden();
		}
		this.isMoved = Status.getStatus(message.getStatusCode()).isMoved();
	}

	/**
	 * 
	 */
	public MessageNode() {
	//
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Identity getModifier() {
		return modifier;
	}

	public void setModifier(Identity modifier) {
		this.modifier = modifier;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isSticky() {
		return isSticky;
	}

	public void setSticky(boolean isSticky) {
		this.isSticky = isSticky;
	}

	public boolean isMoved() {
		return isMoved;
	}

	public void setMoved(boolean isMoved) {
		this.isMoved = isMoved;
	}
	
}