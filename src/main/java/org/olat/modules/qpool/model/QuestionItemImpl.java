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
package org.olat.modules.qpool.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.StudyField;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="questionitem")
@Table(name="o_qp_item")
@NamedQueries({
	@NamedQuery(name="loadQuestionItemByKey", query="select item from questionitem item where item.key=:itemKey")
})
public class QuestionItemImpl implements QuestionItem, CreateInfo, ModifiedInfo, Persistable {

	private static final long serialVersionUID = 6264601750280239307L;

	@Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Column(name="q_uuid", nullable=false, insertable=true, updatable=false)
	private String uuid;

	//general
	@Column(name="q_subject", nullable=false, insertable=true, updatable=true)
	private String subject;
	@ManyToOne(targetEntity=StudyFieldImpl.class)
	@JoinColumn(name="fk_study_field", nullable=true, insertable=true, updatable=true)
	private StudyField studyField;
	@Column(name="q_keywords", nullable=true, insertable=true, updatable=true)
	private String keywords;
	@Column(name="q_type", nullable=false, insertable=true, updatable=true)
	private String type;
	@Column(name="q_language", nullable=false, insertable=true, updatable=true)
	private String language;
	@Column(name="q_status", nullable=false, insertable=true, updatable=true)
	private String status;
	@Column(name="q_description", nullable=true, insertable=true, updatable=true)
	private String description;
	
	//rights
	@Column(name="q_copyright", nullable=true, insertable=true, updatable=true)
	private String copyright;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_ownergroup", nullable=false, insertable=true, updatable=false)
	private SecurityGroup ownerGroup;
	
	//usage
	@Column(name="q_point", nullable=true, insertable=true, updatable=true)
	private BigDecimal point;
	@Column(name="q_difficulty", nullable=true, insertable=true, updatable=true)
	private BigDecimal difficulty;
	@Column(name="q_selectivity", nullable=true, insertable=true, updatable=true)
	private BigDecimal selectivity;
	@Column(name="q_usage", nullable=false, insertable=true, updatable=true)
	private int usage;
	@Column(name="q_test_type", nullable=false, insertable=true, updatable=true)
	private String testType;
	@Column(name="q_level", nullable=false, insertable=true, updatable=true)
	private String level;

	//technics
	@Column(name="q_format", nullable=false, insertable=true, updatable=true)
	private String format;
	@Column(name="q_editor", nullable=true, insertable=true, updatable=true)
	private String editor;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	@Column(name="q_version", nullable=true, insertable=true, updatable=true)
	private String itemVersion;
	
	@Column(name="q_dir", nullable=true, insertable=true, updatable=false)
	private String directory;
	@Column(name="q_root_filename", nullable=true, insertable=true, updatable=false)
	private String rootFilename;
	

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getResourceableTypeName() {
		return "QuestionItem";
	}

	public Long getResourceableId() {
		return getKey();
	}

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
	public void setLastModified(Date date) {
		lastModified = date;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public BigDecimal getPoint() {
		return point;
	}

	public void setPoint(BigDecimal point) {
		this.point = point;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public QuestionType getQuestionType() {
		if(StringHelper.containsNonWhitespace(type)) {
			return QuestionType.valueOf(type);
		}
		return null;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getUsage() {
		return usage;
	}

	public void setUsage(int usage) {
		this.usage = usage;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public BigDecimal getSelectivity() {
		return selectivity;
	}

	public void setSelectivity(BigDecimal selectivity) {
		this.selectivity = selectivity;
	}

	@Transient
	public QuestionStatus getQuestionStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return QuestionStatus.valueOf(status);
		}
		return null;
	}

	public StudyField getStudyField() {
		return studyField;
	}

	public void setStudyField(StudyField studyField) {
		this.studyField = studyField;
	}
	
	@Transient
	@Override
	public String getStudyFieldPath() {
		if(studyField != null) {
			String path = studyField.getMaterializedPathNames();
			if(StringHelper.containsNonWhitespace(path)) {
				return path + "/" + studyField.getField();
			} 
			return "/" + studyField.getField();
		}
		return null;
	}

	@Override
	public String getStudyFieldName() {
		if(studyField != null) {
			return studyField.getField();
		}
		return null;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	public BigDecimal getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(BigDecimal difficulty) {
		this.difficulty = difficulty;
	}

	public String getTestType() {
		return testType;
	}

	public void setTestType(String testType) {
		this.testType = testType;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public String getItemVersion() {
		return itemVersion;
	}

	public void setItemVersion(String itemVersion) {
		this.itemVersion = itemVersion;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getRootFilename() {
		return rootFilename;
	}

	public void setRootFilename(String rootFilename) {
		this.rootFilename = rootFilename;
	}

	@Override
	public int hashCode() {
		return key == null ? 97489 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof QuestionItemImpl) {
			QuestionItemImpl q = (QuestionItemImpl)obj;
			return key != null && key.equals(q.key);
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
		sb.append("question[key=").append(this.key)
			.append("]").append(super.toString());
		return sb.toString();
	}
}
