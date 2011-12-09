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
package org.olat.core.util.mail.model;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.gui.util.CSSHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DBMailAttachment extends PersistentObject {

	private static final long serialVersionUID = -1713863670528439651L;

	private Long size;
	private String name;
	private String mimetype;
	private DBMailImpl mail;
	
	public DBMailAttachment() {
		//
	}
	
	public DBMailImpl getMail() {
		return mail;
	}

	public void setMail(DBMailImpl mail) {
		this.mail = mail;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
	
	public String getCssClass() {
		return CSSHelper.createFiletypeIconCssClassFor(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2951 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailAttachment) {
			DBMailAttachment attachment = (DBMailAttachment)obj;
			return getKey() != null && getKey().equals(attachment.getKey());
		}
		return false;
	}
}
