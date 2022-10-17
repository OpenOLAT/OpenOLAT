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

import java.util.Date;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@NamedQuery(name="loadCurriculumElementTypeByKey", query="select el from curriculumelementtype el where el.key=:key")
@NamedQuery(name="loadCurriculumElementTypes", query="select elementType from curriculumelementtype elementType")	
@Entity(name="curriculumelementtype")
@Table(name="o_cur_element_type")
public class CurriculumElementTypeImpl implements Persistable, CurriculumElementType {

	private static final long serialVersionUID = -8986849768915639239L;

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
	@Column(name="c_css_class", nullable=true, insertable=true, updatable=true)
	private String cssClass;
	@Column(name="c_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="c_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	@Column(name="c_calendars", nullable=true, insertable=true, updatable=true)
	private String calendarsEnabledString;
	@Column(name="c_lectures", nullable=true, insertable=true, updatable=true)
	private String lecturesEnabledString;
	@Column(name="c_learning_progress", nullable=true, insertable=true, updatable=true)
	private String learningProgressEnabledString;
	
	@OneToMany(targetEntity=CurriculumElementTypeToTypeImpl.class, fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="fk_type")
	public Set<CurriculumElementTypeToType> allowedSubTypes;

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
	public String getCssClass() {
		return cssClass;
	}

	@Override
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getCalendarsEnabled() {
		return calendarsEnabledString;
	}

	public void setCalendarsEnabled(String calendarsEnabledString) {
		this.calendarsEnabledString = calendarsEnabledString;
	}

	@Override
	public CurriculumCalendars getCalendars() {
		return StringHelper.containsNonWhitespace(calendarsEnabledString)
				? CurriculumCalendars.valueOf(calendarsEnabledString): CurriculumCalendars.disabled;
	}

	@Override
	public void setCalendars(CurriculumCalendars calendars) {
		if(calendars == null) {
			calendarsEnabledString = null;
		} else {
			calendarsEnabledString = calendars.name();
		}
	}

	public String getLecturesEnabled() {
		return lecturesEnabledString;
	}

	public void setLecturesEnabled(String lecturesEnabledString) {
		this.lecturesEnabledString = lecturesEnabledString;
	}

	@Override
	public CurriculumLectures getLectures() {
		return StringHelper.containsNonWhitespace(lecturesEnabledString)
				? CurriculumLectures.valueOf(lecturesEnabledString): CurriculumLectures.disabled;
	}

	@Override
	public void setLectures(CurriculumLectures lectures) {
		if(lectures == null) {
			lecturesEnabledString = null;
		} else {
			lecturesEnabledString = lectures.name();
		}
	}
	
	public String getLearningProgressEnabled() {
		return learningProgressEnabledString;
	}

	public void setLearningProgressEnabled(String learningProgressEnabledString) {
		this.learningProgressEnabledString = learningProgressEnabledString;
	}

	@Override
	public CurriculumLearningProgress getLearningProgress() {
		return StringHelper.containsNonWhitespace(learningProgressEnabledString)
				? CurriculumLearningProgress.valueOf(learningProgressEnabledString): CurriculumLearningProgress.disabled;
	}

	@Override
	public void setLearningProgress(CurriculumLearningProgress learningProgress) {
		if(learningProgress == null) {
			learningProgressEnabledString = null;
		} else {
			learningProgressEnabledString = learningProgress.name();
		}
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public CurriculumElementTypeManagedFlag[] getManagedFlags() {
		return CurriculumElementTypeManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(CurriculumElementTypeManagedFlag[] flags) {
		managedFlagsString = CurriculumElementTypeManagedFlag.toString(flags);
	}

	@Override
	public Set<CurriculumElementTypeToType> getAllowedSubTypes() {
		return allowedSubTypes;
	}

	public void setAllowedSubTypes(Set<CurriculumElementTypeToType> allowedSubTypes) {
		this.allowedSubTypes = allowedSubTypes;
	}

	@Override
	public int hashCode() {
		return key == null ? 26169661 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CurriculumElementTypeImpl) {
			CurriculumElementTypeImpl type = (CurriculumElementTypeImpl)obj;
			return getKey() != null && getKey().equals(type.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
