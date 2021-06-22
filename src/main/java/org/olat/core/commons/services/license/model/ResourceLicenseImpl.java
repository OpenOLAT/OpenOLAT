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
package org.olat.core.commons.services.license.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 22.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="license")
@Table(name="o_lic_license")
public class ResourceLicenseImpl implements ResourceLicense, Persistable {

	private static final long serialVersionUID = -2044258667835611801L;
	
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
	
	@Column(name="l_resname", nullable=false, insertable=true, updatable=false)
	private String resName;
	@Column(name="l_resid", nullable=false, insertable=true, updatable=false)
	private Long resId;
	@Column(name="l_licensor", nullable=true, insertable=true, updatable=true)
	private String licensor;
	@Column(name="l_freetext", nullable=true, insertable=true, updatable=true)
	private String freetext;
	
	@ManyToOne(targetEntity=LicenseTypeImpl.class, optional=false)
	@JoinColumn(name="fk_license_type_id", nullable=false, insertable=true, updatable=true)
	private LicenseType licenseType;

	@Override
	public Long getKey() {
		return key;
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
	public String getResName() {
		return resName;
	}

	@Override
	public Long getResId() {
		return resId;
	}

	public void setOLATResourceable(OLATResourceable ores) {
		this.resName = ores.getResourceableTypeName();
		this.resId = ores.getResourceableId();
	}

	@Override
	public String getLicensor() {
		return licensor;
	}

	@Override
	public void setLicensor(String licensor) {
		this.licensor = licensor;
	}

	@Override
	public String getFreetext() {
		return freetext;
	}

	@Override
	public void setFreetext(String freetext) {
		this.freetext = freetext;
	}

	@Override
	public LicenseType getLicenseType() {
		return licenseType;
	}

	@Override
	public void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceLicenseImpl other = (ResourceLicenseImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
