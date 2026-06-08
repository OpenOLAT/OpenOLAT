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
package org.olat.modules.selectus.model;

import java.util.Date;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rorganisationunit")
@Table(name="o_selectus_org_unit")
public class OrganisationUnitImpl implements OrganisationUnit {

	private static final long serialVersionUID = -4523349350306944774L;

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
	
	@Column(name="unitname", nullable=true, unique=false, insertable=true, updatable=true)
	private String name;
	@Column(name="unitnamede", nullable=true, unique=false, insertable=true, updatable=true)
	private String nameDe;
	@Column(name="unitnamefr", nullable=true, unique=false, insertable=true, updatable=true)
	private String nameFr;
	@Column(name="url", nullable=true, unique=false, insertable=true, updatable=true)
	private String url;
	@Column(name="description", nullable=true, unique=false, insertable=true, updatable=true)
	private String description;

	@Column(name="systemconfig", nullable=false, unique=false, insertable=true, updatable=true)
	private boolean systemConfiguration;
	
	@Column(name="staffmail", nullable=true, unique=false, insertable=true, updatable=true)
	private String staffMail;
	@Column(name="staffbcc", nullable=true, unique=false, insertable=true, updatable=true)
	private String staffBcc;
	@Column(name="mailsignature", nullable=true, unique=false, insertable=true, updatable=true)
	private String mailSignature;
	
	@ManyToOne(targetEntity=OrganisationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_organisation_id", nullable=true, insertable=true, updatable=true)
	private Organisation organisation;
	
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
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getNameDe() {
		return nameDe;
	}

	@Override
	public void setNameDe(String nameDe) {
		this.nameDe = nameDe;
	}
	
	@Override
	public String getNameFr() {
		return nameFr;
	}

	@Override
	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
	}

	@Transient
	@Override
	public String getName(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getNameDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getNameFr();
		}
		return getName();
	}

	@Transient
	@Override
	public void setName(String name, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setNameDe(name);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setNameFr(name);
		} else {
			setName(name);
		}
	}

	@Transient
	@Override
	public String getMLName(Locale locale) {
		String mlName = null;
		if(locale != null && locale.getLanguage().equals("de")) {
			mlName = getNameDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			mlName = getNameFr();
		}
		if(!StringHelper.containsNonWhitespace(mlName)) {
			mlName = getName();
		}
		return mlName;
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
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isSystemConfiguration() {
		return systemConfiguration;
	}

	@Override
	public void setSystemConfiguration(boolean systemConfiguration) {
		this.systemConfiguration = systemConfiguration;
	}

	@Override
	public String getStaffMail() {
		return staffMail;
	}

	@Override
	public void setStaffMail(String staffMail) {
		this.staffMail = staffMail;
	}

	@Override
	public String getStaffBcc() {
		return staffBcc;
	}

	@Override
	public void setStaffBcc(String staffBcc) {
		this.staffBcc = staffBcc;
	}

	@Override
	public String getMailSignature() {
		return mailSignature;
	}

	@Override
	public void setMailSignature(String mailSignature) {
		this.mailSignature = mailSignature;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 32576289 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrganisationUnitImpl unit) {
			return getKey() != null && getKey().equals(unit.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return false;
	}
}
