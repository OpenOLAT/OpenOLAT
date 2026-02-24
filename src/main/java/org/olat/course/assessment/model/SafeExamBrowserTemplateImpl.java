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

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Persistable;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.SafeExamBrowserTemplate;
import org.olat.course.assessment.manager.SafeExamBrowserConfigurationSerializer;

/**
 *
 * Initial date: 19 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="courseassessmentsebtemplate")
@Table(name="o_as_seb_template")
public class SafeExamBrowserTemplateImpl implements Persistable, SafeExamBrowserTemplate {

	private static final long serialVersionUID = 6284875491827839856L;

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

	@Column(name="a_active", nullable=false, insertable=true, updatable=true)
	private boolean active;

	@Column(name="a_default", nullable=false, insertable=true, updatable=true)
	private boolean isDefault;

	@Column(name="a_name", nullable=true, insertable=true, updatable=true)
	private String name;

	@Column(name="a_safeexambrowserconfig_xml", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserConfigXml;

	@Column(name="a_safeexambrowserconfig_plist", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserConfigPlist;

	@Column(name="a_safeexambrowserconfig_pkey", nullable=true, insertable=true, updatable=true)
	private String safeExamBrowserConfigPlistKey;

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
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
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
	public SafeExamBrowserConfiguration getSafeExamBrowserConfiguration() {
		if (StringHelper.containsNonWhitespace(safeExamBrowserConfigXml)) {
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
	public int hashCode() {
		return getKey() == null ? 829461 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof SafeExamBrowserTemplateImpl other) {
			return getKey() != null && getKey().equals(other.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
