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
package org.olat.course.assessment.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Persistable;
import org.olat.course.assessment.ScoreAccountingTrigger;
import org.olat.group.BusinessGroupRef;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="scoreaccountingtrigger")
@Table(name="o_as_score_accounting_trigger")
public class ScoreAccountingTriggerImpl implements CreateInfo, ScoreAccountingTrigger {

	private static final long serialVersionUID = 9088685691814850734L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="e_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="e_business_group_key", nullable=true, insertable=true, updatable=true)
	private Long businessGroupKey;
	private transient BusinessGroupRef businessGroupRef;
	@Column(name="e_organisation_key", nullable=true, insertable=true, updatable=true)
	private Long organisationKey;
	private transient OrganisationRef organisationRef;
	@Column(name="e_curriculum_element_key", nullable=true, insertable=true, updatable=true)
	private Long curriculumElementKey;
	private transient CurriculumElementRef curriculumElementRef;
	@Column(name="e_user_property_name", nullable=true, insertable=true, updatable=true)
	private String userPropertyName;
	@Column(name="e_user_property_value", nullable=true, insertable=true, updatable=true)
	private String userPropertyValue;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry repositoryEntry;
	@Column(name="e_subident", nullable=false, insertable=true, updatable=false)
	private String subIdent;
	
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
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public BusinessGroupRef getBusinessGroupRef() {
		if (businessGroupKey != null && businessGroupRef == null) {
			businessGroupRef = new BusinessGroupRefImpl(businessGroupKey);
		}
		return businessGroupRef;
	}
	
	public void setBusinessGroupRef(BusinessGroupRef businessGroupRef) {
		this.businessGroupRef = businessGroupRef;
		if (businessGroupRef != null) {
			this.businessGroupKey = businessGroupRef.getKey();
		} else {
			this.businessGroupKey = null;
		}
	}

	@Override
	public OrganisationRef getOrganisationRef() {
		if (organisationKey != null && organisationRef == null) {
			organisationRef = new OrganisationRefImpl(organisationKey);
		}
		return organisationRef;
	}
	
	public void setOrganisationRef(OrganisationRef organisationRef) {
		this.organisationRef = organisationRef;
		if (organisationRef != null) {
			this.organisationKey = organisationRef.getKey();
		} else {
			this.organisationKey = null;
		}
	}

	@Override
	public CurriculumElementRef getCurriculumElementRef() {
		if (curriculumElementKey != null && curriculumElementRef == null) {
			curriculumElementRef = new CurriculumElementRefImpl(curriculumElementKey);
		}
		return curriculumElementRef;
	}
	
	public void setCurriculumElementRef(CurriculumElementRef curriculumElementRef) {
		this.curriculumElementRef = curriculumElementRef;
		if (curriculumElementRef != null) {
			this.curriculumElementKey = curriculumElementRef.getKey();
		} else {
			this.curriculumElementKey = null;
		}
	}
	
	@Override
	public String getUserPropertyName() {
		return userPropertyName;
	}

	public void setUserPropertyName(String userPropertyName) {
		this.userPropertyName = userPropertyName;
	}

	@Override
	public String getUserPropertyValue() {
		return userPropertyValue;
	}

	public void setUserPropertyValue(String userPropertyValue) {
		this.userPropertyValue = userPropertyValue;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}
	
	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}
	
	@Override
	public String getSubIdent() {
		return subIdent;
	}
	
	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
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
		ScoreAccountingTriggerImpl other = (ScoreAccountingTriggerImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
