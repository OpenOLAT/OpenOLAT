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
package org.olat.modules.certificationprogram.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;


/**
 * This is an extra mapping to update only the counter. It's there
 * to prevent the main configuration to overwrite the counter.
 * 
 * Initial date: 6 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="certificationcounter")
@Table(name="o_cer_program")
public class CertificationProgramCounterImpl implements Persistable {
	
	private static final long serialVersionUID = -1192407381910629821L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=false, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=false, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=false, updatable=true)
	private Date lastModified;
	
	@Column(name="c_sn_start_number", nullable=false, insertable=false, updatable=false)
	private long serialNumberStartNumber;
	@Column(name="c_sn_counter", nullable=false, insertable=false, updatable=true)
	private long serialNumberCounter;
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public long getSerialNumberStartNumber() {
		return serialNumberStartNumber;
	}
	
	public void setSerialNumberStartNumber(long serialNumberStartNumber) {
		this.serialNumberStartNumber = serialNumberStartNumber;
	}
	
	public long getSerialNumberCounter() {
		return serialNumberCounter;
	}
	
	public void setSerialNumberCounter(long serialNumberCounter) {
		this.serialNumberCounter = serialNumberCounter;
	}
	
	@Override
	public int hashCode() {
		return key == null ? 45887 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CertificationProgramCounterImpl counter) {
			return getKey() != null && getKey().equals(counter.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
