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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.core.util.mail.model;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DBMailRecipient extends PersistentObject {

	private static final long serialVersionUID = -6793421633217512631L;
	
	private Identity recipient;
	private String emailAddress;
	private Boolean deleted;
	private Boolean visible;
	private Boolean read;
	private Boolean marked;
	private String group;
	
	public DBMailRecipient() {
		//make Hibernate happy
	}
	
	public DBMailRecipient(Identity recipient) {
		this.recipient = recipient;
	}

	public Identity getRecipient() {
		return recipient;
	}

	public void setRecipient(Identity recipient) {
		this.recipient = recipient;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public Boolean getMarked() {
		return marked;
	}

	public void setMarked(Boolean marked) {
		this.marked = marked;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2981 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailRecipient) {
			DBMailRecipient recipient = (DBMailRecipient)obj;
			return getKey() != null && getKey().equals(recipient.getKey());
		}
		return false;
	}
}
