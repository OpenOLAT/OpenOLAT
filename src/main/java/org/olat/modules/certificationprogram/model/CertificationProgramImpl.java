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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.model.CertificateTemplateImpl;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.CertificationProgramToOrganisation;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.model.CreditPointSystemImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 25 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="certificationprogram")
@Table(name="o_cer_program")
public class CertificationProgramImpl implements CertificationProgram, Persistable {
	
	private static final long serialVersionUID = -2433565729019470469L;

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
	
	@Column(name="c_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="c_displayname", nullable=true, insertable=true, updatable=true)
	private String displayName;
	@Column(name="c_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Enumerated(EnumType.STRING)
	@Column(name="c_status", nullable=true, insertable=true, updatable=true)
	private CertificationProgramStatusEnum status;

	@Column(name="c_recert_enabled", nullable=false, insertable=true, updatable=true)
	private boolean recertificationEnabled;
	@Enumerated(EnumType.STRING)
	@Column(name="c_recert_mode", nullable=true, insertable=true, updatable=true)
	private RecertificationMode recertificationMode;
	
	@Column(name="c_recert_creditpoint", nullable=true, insertable=true, updatable=true)
	private BigDecimal creditPoints;
	
	@Column(name="c_recert_window_enabled", nullable=true, insertable=true, updatable=true)
	private boolean recertificationWindowEnabled;
	@Column(name="c_recert_window", nullable=true, insertable=true, updatable=true)
	private int recertificationWindow;
	@Enumerated(EnumType.STRING)
	@Column(name="c_recert_window_unit", nullable=true, insertable=true, updatable=true)
	private DurationType recertificationWindowUnit;
	
	@Column(name="c_premature_recert_enabled", nullable=true, insertable=true, updatable=true)
	private boolean prematureRecertificationByUserEnabled;
	
	@Column(name="c_validity_enabled", nullable=true, insertable=true, updatable=true)
	private boolean validityEnabled;
	@Column(name="c_validity_timelapse", nullable=true, insertable=true, updatable=true)
	private int validityTimelapse;
	@Enumerated(EnumType.STRING)
	@Column(name="c_validity_timelapse_unit", nullable=true, insertable=true, updatable=true)
	private DurationType validityTimelapseUnit;
	
	@Column(name="c_cer_custom_1", nullable=true, insertable=true, updatable=true)
	private String certificateCustom1;
	@Column(name="c_cer_custom_2", nullable=true, insertable=true, updatable=true)
	private String certificateCustom2;
	@Column(name="c_cer_custom_3", nullable=true, insertable=true, updatable=true)
	private String certificateCustom3;
	
	@ManyToOne(targetEntity=CertificateTemplateImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_template", nullable=true, insertable=true, updatable=true, unique=false)
	private CertificateTemplate template;
	
	@ManyToOne(targetEntity=CreditPointSystemImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_credit_point_system", nullable=true, insertable=true, updatable=true)
	private CreditPointSystem creditPointSystem;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group", nullable=false, insertable=true, updatable=false)
	private Group group;
	
	@OneToMany(targetEntity=CertificationProgramToOrganisationImpl.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_program")
	private Set<CertificationProgramToOrganisation> organisations;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_resource", nullable=true, updatable=true)
	private OLATResource resource;
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(CertificationProgram.class);
	}

	@Override
	public Long getResourceableId() {
		return getKey();
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
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
	public CertificationProgramStatusEnum getStatus() {
		return status;
	}

	@Override
	public void setStatus(CertificationProgramStatusEnum status) {
		this.status = status;
	}

	@Override
	public RecertificationMode getRecertificationMode() {
		return recertificationMode;
	}

	@Override
	public void setRecertificationMode(RecertificationMode recertificationMode) {
		this.recertificationMode = recertificationMode;
	}

	@Override
	public boolean isValidityEnabled() {
		return validityEnabled;
	}

	@Override
	public void setValidityEnabled(boolean validityEnabled) {
		this.validityEnabled = validityEnabled;
	}

	@Override
	public int getValidityTimelapse() {
		return validityTimelapse;
	}

	@Override
	public void setValidityTimelapse(int validityTimelapse) {
		this.validityTimelapse = validityTimelapse;
	}

	@Override
	public DurationType getValidityTimelapseUnit() {
		return validityTimelapseUnit;
	}

	@Override
	public void setValidityTimelapseUnit(DurationType validityTimelapseUnit) {
		this.validityTimelapseUnit = validityTimelapseUnit;
	}
	
	@Override
	@Transient
	public Duration getValidityTimelapseDuration() {
		if(isValidityEnabled() && getValidityTimelapse() > 0) {
			return new Duration(getValidityTimelapse(), getValidityTimelapseUnit());
		}
		return null;
	}

	@Override
	public boolean isRecertificationEnabled() {
		return recertificationEnabled;
	}

	@Override
	public void setRecertificationEnabled(boolean recertificationEnabled) {
		this.recertificationEnabled = recertificationEnabled;
	}

	@Override
	public boolean isRecertificationWindowEnabled() {
		return recertificationWindowEnabled;
	}

	@Override
	public void setRecertificationWindowEnabled(boolean recertificationWindowEnabled) {
		this.recertificationWindowEnabled = recertificationWindowEnabled;
	}

	@Override
	public int getRecertificationWindow() {
		return recertificationWindow;
	}

	@Override
	public void setRecertificationWindow(int recertificationWindow) {
		this.recertificationWindow = recertificationWindow;
	}

	@Override
	public DurationType getRecertificationWindowUnit() {
		return recertificationWindowUnit;
	}

	@Override
	public void setRecertificationWindowUnit(DurationType unit) {
		this.recertificationWindowUnit = unit;
	}

	public boolean isPrematureRecertificationByUserEnabled() {
		return prematureRecertificationByUserEnabled;
	}

	public void setPrematureRecertificationByUserEnabled(boolean enabled) {
		this.prematureRecertificationByUserEnabled = enabled;
	}
	
	@Override
	@Transient
	public boolean hasCreditPoints() {
		return getCreditPointSystem() != null && getCreditPoints() != null;
	}

	@Override
	public BigDecimal getCreditPoints() {
		return creditPoints;
	}

	@Override
	public void setCreditPoints(BigDecimal creditPoints) {
		this.creditPoints = creditPoints;
	}

	@Override
	public CreditPointSystem getCreditPointSystem() {
		return creditPointSystem;
	}

	@Override
	public void setCreditPointSystem(CreditPointSystem creditPointSystem) {
		this.creditPointSystem = creditPointSystem;
	}
	
	@Override
	public String getCertificateCustom1() {
		return certificateCustom1;
	}

	@Override
	public void setCertificateCustom1(String certificateCustom1) {
		this.certificateCustom1 = certificateCustom1;
	}

	@Override
	public String getCertificateCustom2() {
		return certificateCustom2;
	}

	@Override
	public void setCertificateCustom2(String certificateCustom2) {
		this.certificateCustom2 = certificateCustom2;
	}

	@Override
	public String getCertificateCustom3() {
		return certificateCustom3;
	}

	@Override
	public void setCertificateCustom3(String certificateCustom3) {
		this.certificateCustom3 = certificateCustom3;
	}

	@Override
	public CertificateTemplate getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(CertificateTemplate template) {
		this.template = template;
	}

	@Override
	public Set<CertificationProgramToOrganisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(Set<CertificationProgramToOrganisation> organisations) {
		this.organisations = organisations;
	}

	@Override
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@Override
	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	@Override
	public int hashCode() {
		return key == null ? -99545887 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CertificationProgramImpl program) {
			return getKey() != null && getKey().equals(program.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("CertificationProgram[id=").append(getKey() == null ? "NULL" : getKey().toString())
		  .append(";displayName=").append(getDisplayName() == null ? "NULL" : getDisplayName())
		  .append(";identifier=").append(getIdentifier() == null ? "NULL" : getIdentifier())
		  .append("]");
		return sb.toString();
	}
}
