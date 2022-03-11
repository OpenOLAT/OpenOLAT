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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.InstantMessageNotification;

/**
 * 
 * Initial date: 10.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="imnotification")
@Table(name="o_im_notification")
@NamedQuery(name="loadIMPrivateNotificationByIdentity", query="select notification from imnotification notification where notification.toIdentityKey=:identityKey and notification.resSubPath is null and notification.channel is null order by notification.creationDate desc")
@NamedQuery(name="loadIMTypedNotificationByIdentity", query="select notification from imnotification notification where notification.toIdentityKey=:identityKey and notification.type=:type order by notification.creationDate desc")
@NamedQuery(name="countIMTypedNotificationByIdentity", query="select count(notification.key) from imnotification notification where notification.toIdentityKey=:identityKey and notification.type=:type")
public class InstantMessageNotificationImpl implements InstantMessageNotification, Persistable, CreateInfo  {

	private static final long serialVersionUID = -1244360269062615091L;

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
	
	@Column(name="fk_to_identity_id", nullable=false, insertable=true, updatable=false)
	private Long toIdentityKey;
	
	@Column(name="fk_from_identity_id", nullable=false, insertable=true, updatable=false)
	private Long fromIdentityKey;
	
	@Column(name="chat_resname", nullable=false, insertable=true, updatable=false)
	private String resourceTypeName;
	@Column(name="chat_resid", nullable=false, insertable=true, updatable=false)
	private Long resourceId;
	@Column(name="chat_ressubpath", nullable=true, insertable=true, updatable=false)
	private String resSubPath;
	@Column(name="chat_channel", nullable=true, insertable=true, updatable=false)
	private String channel;
	
	@Enumerated(EnumType.STRING)
	@Column(name="chat_type", nullable=false, insertable=true, updatable=false)
	private InstantMessageNotificationTypeEnum type;
	

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
	public Long getToIdentityKey() {
		return toIdentityKey;
	}

	public void setToIdentityKey(Long toIdentityKey) {
		this.toIdentityKey = toIdentityKey;
	}

	@Override
	public Long getFromIdentityKey() {
		return fromIdentityKey;
	}

	public void setFromIdentityKey(Long fromIdentityKey) {
		this.fromIdentityKey = fromIdentityKey;
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
	public OLATResourceable getChatResource() {
		return OresHelper.createOLATResourceableInstance(resourceTypeName, resourceId);
	}

	@Override
	public String getResSubPath() {
		return resSubPath;
	}

	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public InstantMessageNotificationTypeEnum getType() {
		return type;
	}

	public void setType(InstantMessageNotificationTypeEnum type) {
		this.type = type;
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
		if(obj instanceof InstantMessageNotificationImpl) {
			InstantMessageNotificationImpl msg = (InstantMessageNotificationImpl)obj;
			return key != null && key.equals(msg.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}