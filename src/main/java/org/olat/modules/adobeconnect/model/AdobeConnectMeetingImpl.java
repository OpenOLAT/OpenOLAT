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
package org.olat.modules.adobeconnect.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="adobeconnectmeeting")
@Table(name="o_aconnect_meeting")
public class AdobeConnectMeetingImpl implements Persistable, AdobeConnectMeeting {

	private static final long serialVersionUID = -474061937981603984L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="a_name", nullable=false, insertable=true, updatable=true)
	private String name;
	@Column(name="a_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="a_start_date", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Column(name="a_end_date", nullable=true, insertable=true, updatable=true)
	private Date endDate;
	
	@Column(name="a_sco_id", nullable=true, insertable=true, updatable=false)
	private String scoId;
	@Column(name="a_folder_id", nullable=true, insertable=true, updatable=false)
	private String folderId;
	@Column(name="a_env_name", nullable=true, insertable=true, updatable=false)
	private String envName;

	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_entry_id", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
	@Column(name="a_sub_ident", nullable=true, insertable=true, updatable=false)
	private String subIdent;
	
	@ManyToOne(targetEntity=BusinessGroupImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_group_id", nullable=true, insertable=true, updatable=false)
	private BusinessGroup businessGroup;
	
	
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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getScoId() {
		return scoId;
	}

	public void setScoId(String scoId) {
		this.scoId = scoId;
	}

	@Override
	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date start) {
		this.startDate = start;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date end) {
		this.endDate = end;
	}

	@Override
	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 860765 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AdobeConnectMeetingImpl) {
			AdobeConnectMeetingImpl meeting = (AdobeConnectMeetingImpl)obj;
			return getKey() != null && getKey().equals(meeting.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
