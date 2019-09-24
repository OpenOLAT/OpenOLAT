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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  28 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DBMailImpl extends PersistentObject implements DBMail {

	private static final long serialVersionUID = 6407865711769961684L;
	
	private String subject;
	private String body;
	private Date lastModified;
	private String metaId;
	
	private DBMailRecipient from;
	private List<DBMailRecipient> recipients;
	private DBMailContext context;
	
	
	
	@Override
	public DBMailRecipient getFrom() {
		return from;
	}

	public void setFrom(DBMailRecipient from) {
		this.from = from;
	}
	
	@Override
	public String getMetaId() {
		return metaId;
	}

	public void setMetaId(String metaId) {
		this.metaId = metaId;
	}

	@Override
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public List<DBMailRecipient> getRecipients() {
		if(recipients == null) {
			recipients = new ArrayList<>();
		}
		return recipients;
	}

	public void setRecipients(List<DBMailRecipient> recipients) {
		this.recipients = recipients;
	}
	
	
	
	public DBMailContext getContext() {
		if(context == null) {
			context = new DBMailContext();
		}
		return context;
	}

	public void setContext(DBMailContext context) {
		this.context = context;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2991 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailImpl) {
			DBMailImpl mail = (DBMailImpl)obj;
			return getKey() != null && getKey().equals(mail.getKey());
		}
		return false;
	}
}
