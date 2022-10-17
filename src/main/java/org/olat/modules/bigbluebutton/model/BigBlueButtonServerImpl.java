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
package org.olat.modules.bigbluebutton.model;

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
import org.olat.modules.bigbluebutton.BigBlueButtonServer;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bigbluebuttonserver")
@Table(name="o_bbb_server")
public class BigBlueButtonServerImpl implements Persistable, BigBlueButtonServer {

	private static final long serialVersionUID = 8664921045147695070L;

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
	
	@Column(name="b_name", nullable=true, insertable=true, updatable=true)
	private String name;
	
	@Column(name="b_url", nullable=false, insertable=true, updatable=true)
	private String url;
	@Column(name="b_shared_secret", nullable=false, insertable=true, updatable=true)
	private String sharedSecret;
	@Column(name="b_recording_url", nullable=true, insertable=true, updatable=true)
	private String recordingUrl;

	@Column(name="b_capacity_factor", nullable=false, insertable=true, updatable=true)
	private Double capacityFactory;
	@Column(name="b_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	@Column(name="b_manual_only", nullable=false, insertable=true, updatable=true)
	private boolean manualOnly;

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
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getSharedSecret() {
		return sharedSecret;
	}

	@Override
	public void setSharedSecret(String secret) {
		this.sharedSecret = secret;
	}

	@Override
	public String getRecordingUrl() {
		return recordingUrl;
	}

	@Override
	public void setRecordingUrl(String recordingUrl) {
		this.recordingUrl = recordingUrl;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isManualOnly() {
		return manualOnly;
	}

	@Override
	public void setManualOnly(boolean manualOnly) {
		this.manualOnly = manualOnly;
	}

	@Override
	public Double getCapacityFactory() {
		return capacityFactory;
	}

	@Override
	public void setCapacityFactory(Double capacityFactory) {
		this.capacityFactory = capacityFactory;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -378178 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof BigBlueButtonServerImpl) {
			BigBlueButtonServerImpl server = (BigBlueButtonServerImpl)obj;
			return getKey() != null && getKey().equals(server.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
