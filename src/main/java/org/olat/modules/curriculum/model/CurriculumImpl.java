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
package org.olat.modules.curriculum.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumManagedFlag;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="curriculum")
@Table(name="o_cur_curriculum")
public class CurriculumImpl implements Persistable, Curriculum {

	private static final long serialVersionUID = 1219053866793474521L;

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
	@Column(name="c_status", nullable=true, insertable=true, updatable=true)
	private String status;
	@Column(name="c_degree", nullable=true, insertable=true, updatable=true)
	private String degree;
	
	@Column(name="c_lectures", nullable=false, insertable=true, updatable=true)
	private boolean lecturesEnabled;
	
	@Column(name="c_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="c_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group", nullable=false, insertable=true, updatable=false)
	private Group group;
	
	@ManyToOne(targetEntity=OrganisationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_organisation", nullable=true, insertable=true, updatable=true)
	private Organisation organisation;
	
	@OneToMany(targetEntity=CurriculumElementImpl.class, mappedBy="curriculumParent", fetch=FetchType.LAZY)
	@OrderColumn(name="pos_cur")
	private List<CurriculumElement> rootElements;

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
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getDegree() {
		return degree;
	}

	@Override
	public void setDegree(String degree) {
		this.degree = degree;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public boolean isLecturesEnabled() {
		return lecturesEnabled;
	}

	@Override
	public void setLecturesEnabled(boolean lecturesEnabled) {
		this.lecturesEnabled = lecturesEnabled;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public CurriculumManagedFlag[] getManagedFlags() {
		return CurriculumManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(CurriculumManagedFlag[] flags) {
		setManagedFlagsString(CurriculumManagedFlag.toString(flags));
	}

	@Override
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	@Override
	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public List<CurriculumElement> getRootElements() {
		if(rootElements == null) {
			rootElements = new ArrayList<>();
		}
		return rootElements;
	}

	public void setRootElements(List<CurriculumElement> rootElements) {
		this.rootElements = rootElements;
	}

	@Override
	public int hashCode() {
		return key == null ? 261825789 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumImpl) {
			CurriculumImpl curriculum = (CurriculumImpl)obj;
			return key != null && key.equals(curriculum.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
