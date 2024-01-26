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
package org.olat.course.assessment.model;

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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Persistable;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.manager.SafeExamBrowserConfigurationSerializer;
import org.olat.repository.RepositoryEntry;

@Entity(name="courseassessmentinspectionconfig")
@Table(name="o_as_inspection_configuration")
@NamedQuery(name="hasAssessmentInspectionConfiguration", query="select config.key from courseassessmentinspectionconfig as config where config.repositoryEntry.key=:entryKey")
public class AssessmentInspectionConfigurationImpl implements AssessmentInspectionConfiguration, Persistable {
	
	private static final long serialVersionUID = -8047542170697719545L;

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
	
	@Column(name="a_name", nullable=true, insertable=true, updatable=true)
	private String name;
	
	@Column(name="a_duration", nullable=true, insertable=true, updatable=true)
	private int duration;
	
	@Column(name="a_overview_options", nullable=true, insertable=true, updatable=true)
	private String overviewOptions;
	
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

	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, updatable=false)
	private RepositoryEntry repositoryEntry;
	
	public AssessmentInspectionConfigurationImpl() {
		//
	}
	
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
	
	public void setCreationDate(Date date) {
		creationDate = date;
	}
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public void setLastModified(Date date) {
		lastModified = date;
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
	public int getDuration() {
		return duration;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public String getOverviewOptions() {
		return overviewOptions;
	}

	@Override
	public List<String> getOverviewOptionsAsList() {
		List<String> list = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(overviewOptions)) {
			String[] options = overviewOptions.split(",");
			for(String option:options) {
				if(StringHelper.containsNonWhitespace(option)) {
					list.add(option);
				}
			}
		}
		return list;
	}

	@Override
	public void setOverviewOptions(String overviewOptions) {
		this.overviewOptions = overviewOptions;
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
			AssessmentModule assessmentModule = CoreSpringFactory.getImpl(AssessmentModule.class);
			String xml = SafeExamBrowserConfigurationSerializer.toXml(configuration);
			setSafeExamBrowserConfigXml(xml);
			String plist = SafeExamBrowserConfigurationSerializer.toPList(configuration, assessmentModule);
			setSafeExamBrowserConfigPList(plist);
			String json = SafeExamBrowserConfigurationSerializer.toJson(configuration, assessmentModule);
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

	public void setSafeExamBrowserConfigPList(String safeExamBrowserConfigPlist) {
		this.safeExamBrowserConfigPlist = safeExamBrowserConfigPlist;
	}

	@Override
	public String getSafeExamBrowserConfigPListKey() {
		return safeExamBrowserConfigPlistKey;
	}

	public void setSafeExamBrowserConfigPListKey(String safeExamBrowserConfigPlistKey) {
		this.safeExamBrowserConfigPlistKey = safeExamBrowserConfigPlistKey;
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
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 72678 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessmentInspectionConfigurationImpl inspection) {
			return getKey() != null && getKey().equals(inspection.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
