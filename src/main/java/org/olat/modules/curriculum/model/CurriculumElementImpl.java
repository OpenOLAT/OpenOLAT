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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Automation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.TaughtBy;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.model.RepositoryEntryEducationalTypeImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="curriculumelement")
@Table(name="o_cur_curriculum_element")
@NamedQuery(name="hasCurriculumElementchildren", query="select el.key from curriculumelement el where el.parent.key=:elementKey")
public class CurriculumElementImpl implements CurriculumElement, Persistable {

	private static final long serialVersionUID = 547658342562646552L;

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
	
	/** Only used for order by (hibernate hack) */
	@Column(name="pos", insertable=false, updatable=false)
	private Integer pos;
	
	/** Only used for order by (hibernate hack) */
	@Column(name="pos_cur", insertable=false, updatable=false)
	private Integer posCurriculum;
	
	@Column(name="pos_impl", insertable=false, updatable=false)
	private String numberImpl;
	
	@Column(name="c_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="c_displayname", nullable=true, insertable=true, updatable=true)
	private String displayName;
	@Column(name="c_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="c_teaser", nullable=true, insertable=true, updatable=true)
	private String teaser;
	@Column(name="c_authors", nullable=true, insertable=true, updatable=true)
	private String authors;
	
	@Column(name="c_mainlanguage", nullable=true, insertable=true, updatable=true)
	private String mainLanguage;
	@Column(name="c_location", nullable=true, insertable=true, updatable=true)
	private String location;
	@Column(name="c_objectives", nullable=true, insertable=true, updatable=true)
	private String objectives;
	@Column(name="c_requirements", nullable=true, insertable=true, updatable=true)
	private String requirements;
	@Column(name="c_credits", nullable=true, insertable=true, updatable=true)
	private String credits;
	@Column(name="c_expenditureofwork", nullable=true, insertable=true, updatable=true)
	private String expenditureOfWork;
	
	@Column(name="c_calendars", nullable=true, insertable=true, updatable=true)
	private String calendarsEnabledString;
	@Column(name="c_lectures", nullable=true, insertable=true, updatable=true)
	private String lecturesEnabledString;
	@Column(name="c_learning_progress", nullable=true, insertable=true, updatable=true)
	private String learningProgressEnabledString;
	@Column(name="c_show_outline", nullable=true, insertable=true, updatable=true)
	private boolean showOutline;
	@Column(name="c_show_lectures", nullable=true, insertable=true, updatable=true)
	private boolean showLectures;
	
	@Column(name="c_show_certificate", nullable=true, insertable=true, updatable=true)
	private boolean showCertificateBenefit;
	@Column(name="c_show_creditpoints", nullable=true, insertable=true, updatable=true)
	private boolean showCreditPointsBenefit;
	
	@Column(name="c_status", nullable=true, insertable=true, updatable=true)
	private String status;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="c_begin", nullable=true, insertable=true, updatable=true)
	private Date beginDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="c_end", nullable=true, insertable=true, updatable=true)
	private Date endDate;
	
	@Column(name="c_min_participants", nullable=true, insertable=true, updatable=true)
	private Long minParticipants;
	@Column(name="c_max_participants", nullable=true, insertable=true, updatable=true)
	private Long maxParticipants;
	@Column(name="c_taught_by", nullable=true, insertable=true, updatable=true)
	private String taughtByValue;
	private transient Set<TaughtBy> taughtBys;
	@Column(name="c_catalog_sort_priority", nullable=true, insertable=true, updatable=true)
	private Integer catalogSortPriority;
	
	@Column(name="c_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="c_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@Column(name="c_m_path_keys", nullable=true, insertable=true, updatable=true)
	private String materializedPathKeys;
	
	@Embedded
    @AttributeOverride(name="value", column = @Column(name="c_auto_instantiation"))
    @AttributeOverride(name="unit", column = @Column(name="c_auto_instantiation_unit"))
	private AutomationImpl autoInstantiation;
	@Embedded
    @AttributeOverride(name="value", column = @Column(name="c_auto_access_coach"))
    @AttributeOverride(name="unit", column = @Column(name="c_auto_access_coach_unit"))
	private AutomationImpl autoAccessForCoach;
	@Embedded
    @AttributeOverride(name="value", column = @Column(name="c_auto_published"))
    @AttributeOverride(name="unit", column = @Column(name="c_auto_published_unit"))
	private AutomationImpl autoPublished;
	@Embedded
    @AttributeOverride(name="value", column = @Column(name="c_auto_closed"))
    @AttributeOverride(name="unit", column = @Column(name="c_auto_closed_unit"))
	private AutomationImpl autoClosed;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group", nullable=false, insertable=true, updatable=false)
	private Group group;
	
	@ManyToOne(targetEntity=CurriculumElementImpl.class)
	@JoinColumn(name="fk_parent", nullable=true, insertable=true, updatable=true)
	private CurriculumElement parent;
	
	@OneToMany(targetEntity=CurriculumElementImpl.class, mappedBy="parent", fetch=FetchType.LAZY)
	@OrderColumn(name="pos")
	private List<CurriculumElement> children;
	
	@ManyToOne(targetEntity=CurriculumImpl.class)
	@JoinColumn(name="fk_curriculum", nullable=true, insertable=true, updatable=true)
	private Curriculum curriculum;
	
	@ManyToOne(targetEntity=CurriculumImpl.class)
	@JoinColumn(name="fk_curriculum_parent", nullable=true, insertable=true, updatable=true)
	private Curriculum curriculumParent;
	
	@ManyToOne(targetEntity=CurriculumElementTypeImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_type", nullable=true, insertable=true, updatable=true)
	private CurriculumElementType type;
	@ManyToOne(targetEntity=RepositoryEntryEducationalTypeImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_educational_type", nullable=true, insertable=true, updatable=true)
	private RepositoryEntryEducationalType educationalType;
	
	@OneToMany(targetEntity=CurriculumElementToTaxonomyLevelImpl.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_cur_element")
	private Set<CurriculumElementToTaxonomyLevel> taxonomyLevels;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_resource", nullable=true, insertable=true, updatable=true)
	private OLATResource resource;
	
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
	public String getTeaser() {
		return teaser;
	}

	@Override
	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}

	@Override
	public String getAuthors() {
		return authors;
	}

	@Override
	public void setAuthors(String authors) {
		this.authors = authors;
	}

	@Override
	public String getMainLanguage() {
		return mainLanguage;
	}

	@Override
	public void setMainLanguage(String mainLanguage) {
		this.mainLanguage = mainLanguage;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String getObjectives() {
		return objectives;
	}

	@Override
	public void setObjectives(String objectives) {
		this.objectives = objectives;
	}

	@Override
	public String getRequirements() {
		return requirements;
	}

	@Override
	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}

	@Override
	public String getCredits() {
		return credits;
	}

	@Override
	public void setCredits(String credits) {
		this.credits = credits;
	}

	@Override
	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	@Override
	public void setExpenditureOfWork(String expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
	}

	public String getCalendarsEnabledString() {
		return calendarsEnabledString;
	}

	public void setCalendarsEnabledString(String calendarsEnabledString) {
		this.calendarsEnabledString = calendarsEnabledString;
	}

	public String getLecturesEnabledString() {
		return lecturesEnabledString;
	}

	public void setLecturesEnabledString(String lecturesEnabledString) {
		this.lecturesEnabledString = lecturesEnabledString;
	}

	public String getLearningProgressEnabledString() {
		return learningProgressEnabledString;
	}

	public void setLearningProgressEnabledString(String learningProgressEnabledString) {
		this.learningProgressEnabledString = learningProgressEnabledString;
	}

	public String getCalendarsEnabled() {
		return calendarsEnabledString;
	}

	public void setCalendarsEnabled(String calendarsEnabledString) {
		this.calendarsEnabledString = calendarsEnabledString;
	}

	@Override
	@Transient
	public CurriculumCalendars getCalendars() {
		CurriculumCalendars enabled;
		if(StringHelper.containsNonWhitespace(calendarsEnabledString)) {
			enabled = CurriculumCalendars.valueOf(calendarsEnabledString);
		} else if(this.type != null) {
			enabled = CurriculumCalendars.inherited;
		} else {
			enabled = CurriculumCalendars.disabled;
		}
		return enabled;
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
		CurriculumLectures enabled;
		if(StringHelper.containsNonWhitespace(lecturesEnabledString)) {
			enabled = CurriculumLectures.valueOf(lecturesEnabledString);
		} else if(this.type != null) {
			enabled = CurriculumLectures.inherited;
		} else {
			enabled = CurriculumLectures.disabled;
		}
		return enabled;
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
		CurriculumLearningProgress enabled;
		if(StringHelper.containsNonWhitespace(learningProgressEnabledString)) {
			enabled = CurriculumLearningProgress.valueOf(learningProgressEnabledString);
		} else if(this.type != null) {
			enabled = CurriculumLearningProgress.inherited;
		} else {
			enabled = CurriculumLearningProgress.disabled;
		}
		return enabled;
	}

	@Override
	public void setLearningProgress(CurriculumLearningProgress learningProgress) {
		if(learningProgress == null) {
			learningProgressEnabledString = null;
		} else {
			learningProgressEnabledString = learningProgress.name();
		}
	}

	@Override
	public boolean isShowOutline() {
		return showOutline;
	}

	@Override
	public void setShowOutline(boolean showOutline) {
		this.showOutline = showOutline;
	}

	@Override
	public boolean isShowLectures() {
		return showLectures;
	}

	@Override
	public void setShowLectures(boolean showLectures) {
		this.showLectures = showLectures;
	}

	@Override
	public boolean isShowCertificateBenefit() {
		return showCertificateBenefit;
	}

	@Override
	public void setShowCertificateBenefit(boolean showCertificateBenefit) {
		this.showCertificateBenefit = showCertificateBenefit;
	}

	@Override
	public boolean isShowCreditPointsBenefit() {
		return showCreditPointsBenefit;
	}

	@Override
	public void setShowCreditPointsBenefit(boolean showCreditPointsBenefit) {
		this.showCreditPointsBenefit = showCreditPointsBenefit;
	}

	@Override
	@Transient
	public CurriculumElementStatus getElementStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return CurriculumElementStatus.valueOf(status);
		}
		return null;
	}

	public void setElementStatus(CurriculumElementStatus status) {
		if(status == null) {
			this.status = null;
		} else {
			this.status = status.name();
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Date getBeginDate() {
		return beginDate;
	}

	@Override
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public Long getMinParticipants() {
		return minParticipants;
	}

	@Override
	public void setMinParticipants(Long minParticipants) {
		this.minParticipants = minParticipants;
	}

	@Override
	public Long getMaxParticipants() {
		return maxParticipants;
	}

	@Override
	public void setMaxParticipants(Long maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	@Override
	public Set<TaughtBy> getTaughtBys() {
		if (taughtBys == null) {
			taughtBys = TaughtBy.split(taughtByValue);
		}
		return taughtBys;
	}

	@Override
	public void setTaughtBys(Set<TaughtBy> taughtBys) {
		this.taughtBys = taughtBys;
		this.taughtByValue = TaughtBy.join(taughtBys);
	}

	@Override
	public Integer getCatalogSortPriority() {
		return catalogSortPriority;
	}

	@Override
	public void setCatalogSortPriority(Integer catalogSortPriority) {
		this.catalogSortPriority = catalogSortPriority;
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
	public String getMaterializedPathKeys() {
		return materializedPathKeys;
	}

	public void setMaterializedPathKeys(String materializedPathKeys) {
		this.materializedPathKeys = materializedPathKeys;
	}
	
	@Transient
	@Override
	public List<Long> getMaterializedPathKeysList() {
		List<Long> keys = new ArrayList<>();
		if(materializedPathKeys != null) {
			String[] segments = materializedPathKeys.split("/");
			for(int i=0; i<segments.length; i++) {
				if(StringHelper.isLong(segments[i])) {
					keys.add(Long.valueOf(segments[i]));
				}
			}
		}
		return keys;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public CurriculumElementManagedFlag[] getManagedFlags() {
		return CurriculumElementManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(CurriculumElementManagedFlag[] flags) {
		managedFlagsString = CurriculumElementManagedFlag.toString(flags);
	}

	@Override
	public Automation getAutoInstantiation() {
		return autoInstantiation;
	}

	@Override
	public void setAutoInstantiation(Automation autoInstantiation) {
		this.autoInstantiation = (AutomationImpl)autoInstantiation;
	}

	@Override
	public Automation getAutoAccessForCoach() {
		return autoAccessForCoach;
	}

	@Override
	public void setAutoAccessForCoach(Automation autoAccessForCoach) {
		this.autoAccessForCoach = (AutomationImpl)autoAccessForCoach;
	}

	@Override
	public Automation getAutoPublished() {
		return autoPublished;
	}

	@Override
	public void setAutoPublished(Automation autoPublished) {
		this.autoPublished = (AutomationImpl)autoPublished;
	}

	@Override
	public Automation getAutoClosed() {
		return autoClosed;
	}

	@Override
	public void setAutoClosed(Automation autoClosed) {
		this.autoClosed = (AutomationImpl)autoClosed;
	}
	
	@Transient
	@Override
	public boolean hasAutomation() {
		return getAutoInstantiation() != null && getAutoInstantiation().getUnit() != null
				|| getAutoAccessForCoach() != null && getAutoAccessForCoach().getUnit() != null
				|| getAutoPublished() != null && getAutoPublished().getUnit() != null
				|| getAutoClosed() != null && getAutoClosed().getUnit() != null;
	}

	@Transient
	@Override
	public boolean isSingleCourseImplementation() {
		CurriculumElement p = getParent();
		CurriculumElementType t = getType();
		return p == null && t != null && t.isSingleElement() && t.getMaxRepositoryEntryRelations() == 1;
	}

	@Override
	public Integer getPos() {
		return pos;
	}
	
	public void setPos(Integer pos) {
		this.pos = pos;
	}

	@Override
	public Integer getPosCurriculum() {
		return posCurriculum;
	}

	public void setPosCurriculum(Integer posCurriculum) {
		this.posCurriculum = posCurriculum;
	}
	
	@Override
	public String getNumberImpl() {
		return numberImpl;
	}

	public void setNumberImpl(String number) {
		this.numberImpl = number;
	}

	@Override
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@Override
	public CurriculumElement getParent() {
		return parent;
	}

	public void setParent(CurriculumElement parent) {
		this.parent = parent;
	}

	public Curriculum getCurriculumParent() {
		return curriculumParent;
	}

	public void setCurriculumParent(Curriculum curriculumParent) {
		this.curriculumParent = curriculumParent;
	}

	public List<CurriculumElement> getChildren() {
		if(children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	public void setChildren(List<CurriculumElement> children) {
		this.children = children;
	}

	@Override
	public Curriculum getCurriculum() {
		return curriculum;
	}

	public void setCurriculum(Curriculum curriculum) {
		this.curriculum = curriculum;
	}

	@Override
	public Set<CurriculumElementToTaxonomyLevel> getTaxonomyLevels() {
		if(taxonomyLevels == null) {
			taxonomyLevels = new HashSet<>();
		}
		return taxonomyLevels;
	}

	public void setTaxonomyLevels(Set<CurriculumElementToTaxonomyLevel> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}

	@Override
	public CurriculumElementType getType() {
		return type;
	}

	@Override
	public void setType(CurriculumElementType type) {
		this.type = type;
	}

	@Override
	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	@Override
	public void setEducationalType(RepositoryEntryEducationalType educationalType) {
		this.educationalType = educationalType;
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
		return key == null ? 28562153 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CurriculumElementImpl el) {
			return getKey() != null && getKey().equals(el.getKey());
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
		sb.append("CurriculumElement[id=").append(getKey() == null ? "NULL" : getKey().toString())
		  .append(";displayName=").append(getDisplayName() == null ? "NULL" : getDisplayName())
		  .append(";identifier=").append(getIdentifier() == null ? "NULL" : getIdentifier())
		  .append("]");
		return sb.toString();
	}
}
