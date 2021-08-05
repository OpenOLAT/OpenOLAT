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
package org.olat.instantMessaging.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.instantMessaging.InstantMessage;

/**
 * 
 * Initial date: 04.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="instantmessage")
@Table(name="o_im_message")
@NamedQuery(name="loadIMessageByKey",query="select msg from instantmessage msg  where msg.key=:key")
@NamedQuery(name="loadIMessageByResource", query="select msg from instantmessage msg where msg.resourceId=:resid and msg.resourceTypeName=:resname order by msg.creationDate desc")
@NamedQuery(name="loadIMessageByResourceAndDate", query="select msg from instantmessage msg where msg.resourceId=:resid and msg.resourceTypeName=:resname and msg.creationDate>=:from order by msg.creationDate desc")
public class InstantMessageImpl implements InstantMessage, Persistable, CreateInfo {
	
	private static final long serialVersionUID = 1425964260797865080L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="fk_from_identity_id", nullable=false, insertable=true, updatable=false)
	private Long fromKey;
	
	@Column(name="msg_resname", nullable=false, insertable=true, updatable=false)
	private String resourceTypeName;
	
	@Column(name="msg_resid", nullable=false, insertable=true, updatable=false)
	private Long resourceId;
	
	@Column(name="msg_anonym", nullable=false, insertable=true, updatable=false)
	private boolean anonym;
	
	@Column(name="msg_from", nullable=false, insertable=true, updatable=false)
	private String fromNickName;

	@Column(name="msg_body", nullable=false, insertable=true, updatable=false)
	private String body;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Long getFromKey() {
		return fromKey;
	}

	public void setFromKey(Long fromKey) {
		this.fromKey = fromKey;
	}

	@Override
	public boolean isFromMe(Identity me) {
		if(me == null || fromKey == null) return false;
		return fromKey.equals(me.getKey());
	}

	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public boolean isAnonym() {
		return anonym;
	}

	public void setAnonym(boolean anonym) {
		this.anonym = anonym;
	}

	@Override
	public String getFromNickName() {
		return fromNickName;
	}

	public void setFromNickName(String fromNickName) {
		this.fromNickName = fromNickName;
	}

	@Override
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public int hashCode() {
		return key == null ? 92867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof InstantMessageImpl) {
			InstantMessageImpl msg = (InstantMessageImpl)obj;
			return key != null && key.equals(msg.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
