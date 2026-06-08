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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

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
import jakarta.persistence.Version;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rpositionlight")
@Table(name="o_selectus_position")
public class PositionLightImpl implements PositionLight, CreateInfo, Persistable {

	private static final long serialVersionUID = 7846992495959711760L;
	
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
	@Column(name="pos_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;
	
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	
	@Column(name="availablelanguages", nullable=true, unique=false, insertable=true, updatable=true)
	private String availableLanguages;

	@Column(name="positiontitle", nullable=true, unique=false, insertable=true, updatable=true)
	private String positionTitle;
	@Column(name="positiontitlede", nullable=true, unique=false, insertable=true, updatable=true)
	private String positionTitleDe;
	@Column(name="positiontitlefr", nullable=true, unique=false, insertable=true, updatable=true)
	private String positionTitleFr;
	
	@Column(name="planingsnumber", nullable=true, unique=false, insertable=true, updatable=true)
	private String planingsNumber;
	@Column(name="department", nullable=true, unique=false, insertable=true, updatable=true)
	private String department;
	@Column(name="departmentde", nullable=true, unique=false, insertable=true, updatable=true)
	private String departmentDe;
	@Column(name="departmentfr", nullable=true, unique=false, insertable=true, updatable=true)
	private String departmentFr;
	@Column(name="professorship", nullable=true, unique=false, insertable=true, updatable=true)
	private String professorship;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="applicationdeadline", nullable=true, insertable=true, updatable=false)
	private Date applicationDeadline;
	
	@ManyToOne(targetEntity=OrganisationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_organisation_id", nullable=true, insertable=true, updatable=true)
	private Organisation organisation;
	
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_committeegroup", nullable=false, insertable=true, updatable=true)
	private SecurityGroup committeeGroup;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_committeeheadgroup", nullable=true, insertable=true, updatable=true)
	private SecurityGroup committeeHeadGroup;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_secretarygroup", nullable=true, insertable=true, updatable=true)
	private SecurityGroup secretaryGroup;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_exofficiogroup", nullable=true, insertable=true, updatable=true)
	private SecurityGroup exOfficioGroup;

	@Column(name="status", nullable=true, insertable=true, updatable=true)
	private String status;
	@Column(name="is_valid", nullable=true, insertable=true, updatable=true)
	private boolean valid = true;

	public PositionLightImpl() {
		//
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getResourceableTypeName() {
		return "RecruitingPositionImpl";
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public String getPlaningsNumber() {
		return planingsNumber;
	}
	
	@Override
	public String getDepartment() {
		return department;
	}
	
	@Override
	public String getDepartmentDe() {
		return departmentDe;
	}
	
	@Override
	public String getDepartmentFr() {
		return departmentFr;
	}
	
	@Override
	public String getDepartment(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getDepartmentDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getDepartmentFr();
		}
		return getDepartment();
	}
	
	@Override @Transient
	public String getMLDepartment(Locale locale) {
		return PositionMLHelper.getPositionMLDepartment(this, locale);
	}

	@Override
	public String getAvailableLanguages() {
		return availableLanguages;
	}

	@Override
	public String getPositionTitle() {
		return positionTitle;
	}

	@Override
	public String getPositionTitleDe() {
		return positionTitleDe;
	}

	@Override
	public String getPositionTitleFr() {
		return positionTitleFr;
	}

	@Override
	public String getPositionTitle(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getPositionTitleDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getPositionTitleFr();
		}
		return getPositionTitle();
	}

	@Override @Transient
	public String getMLTitle(Locale locale) {
		return PositionMLHelper.getPositionMLTitle(this, locale);
	}

	@Override
	public Date getApplicationDeadline() {
		return applicationDeadline;
	}

	@Override
	public String getProfessorship() {
		return professorship;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public boolean isValid() {
		return valid;
	}
	
	@Override
	public int hashCode() {
		return key == null ? 39845893 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PositionImpl) {
			PositionImpl pos = (PositionImpl)obj;
			return getKey() != null && getKey().equals(pos.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		if(StringHelper.containsNonWhitespace(positionTitle)) {
			sb.append("[positionTitle:").append(positionTitle).append("]");
		}
		return sb.toString();
	}
	
	@Override
	public String toStringFull() {
		StringBuilder sb = new StringBuilder();
		sb.append("position[").append(super.toString()).append(";")
			.append("planingsNumber=").append(planingsNumber == null ? "" : planingsNumber).append(";")
			.append("positionTitle=").append(positionTitle == null ? "" : positionTitle).append(";")
			.append("applicationDeadline=").append(applicationDeadline == null ? "" : applicationDeadline).append(";")
			.append("status=").append(status == null ? "" : status).append(";")
			.append("valid=").append(valid ? "true" : "false").append(";");
		return sb.append("]").toString();
	}
}
