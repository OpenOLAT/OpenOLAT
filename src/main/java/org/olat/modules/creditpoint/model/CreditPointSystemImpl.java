/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;

/**
 * 
 * Initial date: 2 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="creditpointsystem")
@Table(name="o_cp_system")
public class CreditPointSystemImpl implements Persistable, CreditPointSystem {
	
	private static final long serialVersionUID = 3761521926928378887L;

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

	@Column(name="c_name", nullable=false, insertable=true, updatable=true)
	private String name;
	@Column(name="c_label", nullable=false, insertable=true, updatable=true)
	private String label;
	@Column(name="c_description", nullable=true, insertable=true, updatable=true)
	private String description;
	
	@Column(name="c_def_expiration", nullable=true, insertable=true, updatable=true)
	private Integer defaultExpiration;
	@Enumerated(EnumType.STRING)
	@Column(name="c_def_expiration_unit", nullable=true, insertable=true, updatable=true)
	private CreditPointExpirationType defaultExpirationUnit;

	@Enumerated(EnumType.STRING)
	@Column(name="c_status", nullable=true, insertable=true, updatable=true)
	private CreditPointSystemStatus status;
	
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
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
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
	public Integer getDefaultExpiration() {
		return defaultExpiration;
	}

	@Override
	public void setDefaultExpiration(Integer defaultExpiration) {
		this.defaultExpiration = defaultExpiration;
	}

	@Override
	public CreditPointExpirationType getDefaultExpirationUnit() {
		return defaultExpirationUnit;
	}

	@Override
	public void setDefaultExpirationUnit(CreditPointExpirationType defaultExpirationUnit) {
		this.defaultExpirationUnit = defaultExpirationUnit;
	}

	@Override
	public CreditPointSystemStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(CreditPointSystemStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -85156 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CreditPointSystemImpl system) {
			return getKey() != null && getKey().equals(system.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
