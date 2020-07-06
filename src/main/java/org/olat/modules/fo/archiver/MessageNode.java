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

package org.olat.modules.fo.archiver;

import java.io.Serializable;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.util.nodes.GenericNode;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;

/**
 * Initial Date: 2005-11-08
 * @author Alexander Schneider
 */
public class MessageNode extends GenericNode implements Serializable {

	private static final long serialVersionUID = -521407664860133662L;
	
	private Long key;
	private String title;
	private String body;
	private boolean guest;
	private String pseudonym;
	private Identity creator;
	private Date creationDate;
	private Identity modifier;
	private Date modifiedDate;
	private boolean isSticky;
	private boolean isClosed;
	private boolean isHidden;
	private boolean isMoved;

	public MessageNode() {
		//
	}
	
	public MessageNode(Message message) {
		key = message.getKey();
		title = message.getTitle();
		body = message.getBody();
		creator = message.getCreator();
		creationDate = message.getCreationDate();
		modifier = message.getModifier();
		if(message.getModificationDate() != null) {
			modifiedDate = message.getModificationDate();
		} else {
			modifiedDate = message.getLastModified();
		}
		guest = message.isGuest();
		pseudonym = message.getPseudonym();
		if(message.getParent()==null) {
			//relevant only for the threadtop messages
			isSticky = Status.getStatus(message.getStatusCode()).isSticky();
			isClosed = Status.getStatus(message.getStatusCode()).isClosed();
			isHidden = Status.getStatus(message.getStatusCode()).isHidden();
		}
		isMoved = Status.getStatus(message.getStatusCode()).isMoved();
	}

	public Long getKey() {
		return key;
	}
	
	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Identity getCreator() {
		return creator;
	}

	public boolean isGuest() {
		return guest;
	}
	
	public String getPseudonym() {
		return pseudonym;
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