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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.manager.SafeExamBrowserConfigurationSerializer;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="courseassessmentmode")
@Table(name="o_as_mode_course")
@NamedQuery(name="assessmentModeById", query="select mode from courseassessmentmode mode where mode.key=:modeKey")
@NamedQuery(name="assessmentModeByRepoEntry", query="select mode from courseassessmentmode mode inner join fetch mode.repositoryEntry v inner join fetch v.olatResource res where mode.repositoryEntry.key=:entryKey order by mode.begin desc")
public class AssessmentModeImpl implements Persistable, AssessmentMode {

	private static final long serialVersionUID = 5208551950937018842L;

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

	@Column(name="a_name", nullable=true, insertable=true, updatable=true)
	private String name;
	@Column(name="a_description", nullable=true, insertable=true, updatable=true)
	private String description;

	@Column(name="a_status", nullable=true, insertable=true, updatable=true)
	private String statusString;
	@Column(name="a_end_status", nullable=true, insertable=true, updatable=true)
	private String endStatusString;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_begin", nullable=false, insertable=true, updatable=true)
	private Date begin;
	@Column(name="a_leadtime", nullable=true, insertable=true, updatable=true)
	private int leadTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_begin_with_leadtime", nullable=false, insertable=true, updatable=true)
	private Date beginWithLeadTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_end", nullable=false, insertable=true, updatable=true)
	private Date end;
	@Column(name="a_followuptime", nullable=true, insertable=true, updatable=true)
	private int followupTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_end_with_followuptime", nullable=false, insertable=true, updatable=true)
	private Date endWithFollowupTime;

	@Column(name="a_manual_beginend", nullable=false, insertable=true, updatable=true)
	private boolean manualBeginEnd;

	@Column(name="a_targetaudience", nullable=true, insertable=true, updatable=true)
	private String targetAudienceString;
	
	@OneToMany(targetEntity=AssessmentModeToGroupImpl.class, mappedBy="assessmentMode", cascade= { CascadeType.REMOVE })
	private Set<AssessmentModeToGroup> groups;
	
	@OneToMany(targetEntity=AssessmentModeToAreaImpl.class, mappedBy="assessmentMode", cascade= { CascadeType.REMOVE })
	private Set<AssessmentModeToArea> areas;
	
	@OneToMany(targetEntity=AssessmentModeToCurriculumElementImpl.class, mappedBy="assessmentMode", cascade= { CascadeType.REMOVE })
	private Set<AssessmentModeToCurriculumElement> curriculumElements;
	
	
	@Column(name="a_restrictaccesselements", nullable=true, insertable=true, updatable=true)
	private boolean restrictAccessElements;
	@Column(name="a_elements", nullable=true, insertable=true, updatable=true)
	private String elementList;
	@Column(name="a_start_element", nullable=true, insertable=true, updatable=true)
	private String startElement;
	

	@Column(name="a_restrictaccessips", nullable=true, insertable=true, updatable=true)
	private boolean restrictAccessIps;
	@Column(name="a_ips", nullable=true, insertable=true, updatable=true)
	private String ipList;
	
	@Column(name="a_safeexambrowser", nullable=true, insertable=true, updatable=true)
	private boolean safeExamBrowser;
	@Column(name="a_safeexambrowserkey", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserKey;
	@Column(name="a_safeexambrowserconfig_xml", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserConfigXml;
	@Column(name="a_safeexambrowserconfig_plist", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserConfigPlist;
	@Column(name="a_safeexambrowserconfig_pkey", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserConfigPlistKey;
	@Column(name="a_safeexambrowserconfig_dload", nullable=true, insertable=true, updatable=true)
	private boolean safeExamBrowserConfigDownload;
	
	@Column(name="a_safeexambrowserhint", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserHint;
	
	@Column(name="a_applysettingscoach", nullable=true, insertable=true, updatable=true)
	private boolean applySettingsForCoach;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, updatable=false)
	private RepositoryEntry repositoryEntry;
	
	@ManyToOne(targetEntity=LectureBlockImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_lecture_block", nullable=true, updatable=false)
	private LectureBlock lectureBlock;
	
	
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
	public Status getStatus() {
		return StringHelper.containsNonWhitespace(getStatusString()) ? Status.valueOf(getStatusString()) : Status.none;
	}

	@Override
	public void setStatus(Status status) {
		if(status == null) {
			setStatusString(Status.none.name());
		} else {
			setStatusString(status.name());
		}
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	@Override
	public EndStatus getEndStatus() {
		return StringHelper.containsNonWhitespace(getEndStatusString()) ? EndStatus.valueOf(getEndStatusString()) : null;
	}

	@Override
	public void setEndStatus(EndStatus status) {
		if(status == null) {
			setEndStatusString(null);
		} else {
			setEndStatusString(status.name());	
		}
	}

	public String getEndStatusString() {
		return endStatusString;
	}

	public void setEndStatusString(String endStatusString) {
		this.endStatusString = endStatusString;
	}

	@Override
	public boolean isManualBeginEnd() {
		return manualBeginEnd;
	}

	@Override
	public void setManualBeginEnd(boolean manualBeginEnd) {
		this.manualBeginEnd = manualBeginEnd;
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
	public int getLeadTime() {
		return leadTime;
	}

	@Override
	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}

	@Override
	public Date getBeginWithLeadTime() {
		return beginWithLeadTime;
	}

	public void setBeginWithLeadTime(Date beginWithLeadTime) {
		this.beginWithLeadTime = beginWithLeadTime;
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
	public int getFollowupTime() {
		return followupTime;
	}

	@Override
	public void setFollowupTime(int followupTime) {
		this.followupTime = followupTime;
	}

	@Override
	public Date getEndWithFollowupTime() {
		return endWithFollowupTime;
	}

	@Override
	public void setEndWithFollowupTime(Date endWithFollowupTime) {
		this.endWithFollowupTime = endWithFollowupTime;
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
	public Set<AssessmentModeToArea> getAreas() {
		if(areas == null) {
			areas = new HashSet<>();
		}
		return areas;
	}

	public void setAreas(Set<AssessmentModeToArea> areas) {
		this.areas = areas;
	}

	@Override
	public Set<AssessmentModeToCurriculumElement> getCurriculumElements() {
		if(curriculumElements == null) {
			curriculumElements = new HashSet<>();
		}
		return curriculumElements;
	}

	public void setCurriculumElements(Set<AssessmentModeToCurriculumElement> curriculumElements) {
		this.curriculumElements = curriculumElements;
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
	@Transient
	public List<String> getElementAsList() {
		String nodes = getElementList();
		return StringHelper.containsNonWhitespace(nodes) ? Arrays.asList(nodes.split("[,]")) : null;
	}

	@Override
	public String getStartElement() {
		return startElement;
	}

	@Override
	public void setStartElement(String startElement) {
		this.startElement = startElement;
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
	public SafeExamBrowserConfiguration getSafeExamBrowserConfiguration() {
		if(StringHelper.containsNonWhitespace(safeExamBrowserConfigXml)) {
			return SafeExamBrowserConfigurationSerializer.fromXml(safeExamBrowserConfigXml);
		}
		return null;
	}

	@Override
	public void setSafeExamBrowserConfiguration(SafeExamBrowserConfiguration configuration) {
		if(configuration == null) {
			setSafeExamBrowserConfigXml(null);
			setSafeExamBrowserConfigPList(null);
			setSafeExamBrowserConfigPListKey(null);
		} else {
			String xml = SafeExamBrowserConfigurationSerializer.toXml(configuration);
			setSafeExamBrowserConfigXml(xml);
			String plist = SafeExamBrowserConfigurationSerializer.toPList(configuration);
			setSafeExamBrowserConfigPList(plist);
			String json = SafeExamBrowserConfigurationSerializer.toJson(configuration);
			if(json != null) {
				setSafeExamBrowserConfigPListKey(Encoder.sha256Exam(json));
			}
		}
	}

	public String getSafeExamBrowserConfigXml() {
		return safeExamBrowserConfigXml;
	}

	public void setSafeExamBrowserConfigXml(String safeExamBrowserConfigXml) {
		this.safeExamBrowserConfigXml = safeExamBrowserConfigXml;
	}

	@Override
	public String getSafeExamBrowserConfigPList() {
		return safeExamBrowserConfigPlist;
	}

	public void setSafeExamBrowserConfigPList(String config) {
		this.safeExamBrowserConfigPlist = config;
	}

	@Override
	public String getSafeExamBrowserConfigPListKey() {
		return safeExamBrowserConfigPlistKey;
	}

	public void setSafeExamBrowserConfigPListKey(String key) {
		this.safeExamBrowserConfigPlistKey = key;
	}

	@Override
	public boolean isSafeExamBrowserConfigDownload() {
		return safeExamBrowserConfigDownload;
	}

	@Override
	public void setSafeExamBrowserConfigDownload(boolean safeExamBrowserConfigDownload) {
		this.safeExamBrowserConfigDownload = safeExamBrowserConfigDownload;
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

	@Override
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry entry) {
		this.repositoryEntry = entry;
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