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
package org.olat.core.dispatcher.mapper.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="pmapper")
@Table(name="o_mapper")
@NamedQuery(name="loadMapperByKeyOrdered", query="select mapper from pmapper as mapper where mapper.mapperId=:mapperId order by mapper.key")
@NamedQuery(name="loadMapperByKey", query="select mapper from pmapper as mapper where mapper.mapperId=:mapperId")
@NamedQuery(name="updateMapperByMapperId", query="update pmapper set lastModified=:now, expirationDate=:expirationDate, xmlConfiguration=:config where mapperId=:mapperId")
public class PersistedMapper implements CreateInfo, ModifiedInfo, Persistable {

	private static final long serialVersionUID = 7297417374497607347L;
	
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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="expirationdate", nullable=true, insertable=true, updatable=true)
	private Date expirationDate;

	@Column(name="mapper_uuid", nullable=true, insertable=true, updatable=false)
	private String mapperId;

	@Column(name="orig_session_id", nullable=true, insertable=true, updatable=false)
	private String originalSessionId;

	@Column(name="xml_config", nullable=true, insertable=true, updatable=true)
	private String xmlConfiguration;
	
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
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getMapperId() {
		return mapperId;
	}
	
	public void setMapperId(String mapperId) {
		this.mapperId = mapperId;
	}
	
	public String getOriginalSessionId() {
		return originalSessionId;
	}
	
	public void setOriginalSessionId(String originalSessionId) {
		this.originalSessionId = originalSessionId;
	}
	
	public String getXmlConfiguration() {
		return xmlConfiguration;
	}
	
	public void setXmlConfiguration(String xmlConfiguration) {
		this.xmlConfiguration = xmlConfiguration;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -7526 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PersistedMapper) {
			PersistedMapper m = (PersistedMapper)obj;
			return getKey() != null && getKey().equals(m.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}