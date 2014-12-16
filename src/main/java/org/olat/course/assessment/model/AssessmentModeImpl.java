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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.Persistable;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="courseassessmentmode")
@Table(name="o_as_mode_course")
@NamedQueries({
	@NamedQuery(name="assessmentModeByRepoEntry", query="select mode from courseassessmentmode mode where mode.entry.key=:entryKey"),
	@NamedQuery(name="currentAssessmentModes", query="select mode from courseassessmentmode mode where mode.begin<=:now and mode.end>=:now")
})
public class AssessmentModeImpl implements Persistable, AssessmentMode {

	private static final long serialVersionUID = 5208551950937018842L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="a_name", nullable=true, insertable=true, updatable=true)
	private String name;
	@Column(name="a_description", nullable=true, insertable=true, updatable=true)
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_begin", nullable=false, insertable=true, updatable=true)
	private Date begin;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_creationdate", nullable=false, insertable=true, updatable=true)
	private Date end;
	@Column(name="a_leadtime", nullable=true, insertable=true, updatable=true)
	private int leadTime;
	
	@Column(name="a_targetaudience", nullable=true, insertable=true, updatable=true)
	private String targetAudienceString;
	
	@OneToMany(targetEntity=AssessmentModeToGroupImpl.class, mappedBy="assessmentMode")
	private Set<AssessmentModeToGroup> groups;
	
	@Column(name="a_restrictaccesselements", nullable=true, insertable=true, updatable=true)
	private boolean restrictAccessElements;
	@Column(name="a_elements", nullable=true, insertable=true, updatable=true)
	private String elementList;

	@Column(name="a_restrictaccessips", nullable=true, insertable=true, updatable=true)
	private boolean restrictAccessIps;
	@Column(name="a_ips", nullable=true, insertable=true, updatable=true)
	private String ipList;
	
	@Column(name="a_safeexambrowser", nullable=true, insertable=true, updatable=true)
	private boolean safeExamBrowser;
	@Column(name="a_safeexambrowserkey", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserKey;
	@Column(name="a_safeexambrowserhint", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserHint;
	
	@Column(name="a_applysettingscoach", nullable=true, insertable=true, updatable=true)
	private boolean applySettingsForCoach;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, updatable=false)
	private RepositoryEntry entry;
	
	
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
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Date getBegin() {
		return begin;
	}

	@Override
	public void setBegin(Date begin) {
		this.begin = begin;
	}

	@Override
	public Date getEnd() {
		return end;
	}

	@Override
	public void setEnd(Date end) {
		this.end = end;
	}

	@Override
	public int getLeadTime() {
		return leadTime;
	}

	@Override
	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}

	@Override
	public Target getTargetAudience() {
		return targetAudienceString == null || targetAudienceString.isEmpty()
				? null : Target.valueOf(targetAudienceString);
	}

	@Override
	public void setTargetAudience(Target target) {
		if(target == null) {
			this.targetAudienceString = null;
		} else {
			this.targetAudienceString = target.name();
		}
	}

	@Override
	public Set<AssessmentModeToGroup> getGroups() {
		if(groups == null) {
			groups = new HashSet<>();
		}
		return groups;
	}

	public void setGroups(Set<AssessmentModeToGroup> groups) {
		this.groups = groups;
	}

	@Override
	public boolean isRestrictAccessElements() {
		return restrictAccessElements;
	}

	@Override
	public void setRestrictAccessElements(boolean restrictAccessElements) {
		this.restrictAccessElements = restrictAccessElements;
	}

	@Override
	public String getElementList() {
		return elementList;
	}

	@Override
	public void setElementList(String elementList) {
		this.elementList = elementList;
	}

	@Override
	public boolean isRestrictAccessIps() {
		return restrictAccessIps;
	}

	@Override
	public void setRestrictAccessIps(boolean restrictAccessIps) {
		this.restrictAccessIps = restrictAccessIps;
	}

	@Override
	public String getIpList() {
		return ipList;
	}

	@Override
	public void setIpList(String ipList) {
		this.ipList = ipList;
	}

	@Override
	public boolean isSafeExamBrowser() {
		return safeExamBrowser;
	}

	@Override
	public void setSafeExamBrowser(boolean safeExamBrowser) {
		this.safeExamBrowser = safeExamBrowser;
	}

	@Override
	public String getSafeExamBrowserKey() {
		return safeExamBrowserKey;
	}

	@Override
	public void setSafeExamBrowserKey(String safeExamBrowserKey) {
		this.safeExamBrowserKey = safeExamBrowserKey;
	}

	@Override
	public String getSafeExamBrowserHint() {
		return safeExamBrowserHint;
	}

	@Override
	public void setSafeExamBrowserHint(String safeExamBrowserHint) {
		this.safeExamBrowserHint = safeExamBrowserHint;
	}

	@Override
	public boolean isApplySettingsForCoach() {
		return applySettingsForCoach;
	}

	@Override
	public void setApplySettingsForCoach(boolean applySettingsForCoach) {
		this.applySettingsForCoach = applySettingsForCoach;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 775423 : getKey().hashCode();
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessmentModeImpl) {
			AssessmentModeImpl mode = (AssessmentModeImpl)obj;
			return getKey() != null && getKey().equals(mode.getKey());	
		}
		return false;
	}
}