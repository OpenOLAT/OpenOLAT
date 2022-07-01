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
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemEditable;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="questionitem")
@Table(name="o_qp_item")
@NamedQuery(name="loadQuestionItemByKey", query="select item from questionitem item where item.key=:itemKey")
public class QuestionItemImpl implements QuestionItemFull, QuestionItemEditable, Persistable {

	private static final long serialVersionUID = 6264601750280239307L;

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
	
	//general
	@Column(name="q_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="q_master_identifier", nullable=true, insertable=true, updatable=false)
	private String masterIdentifier;
	@Column(name="q_title", nullable=false, insertable=true, updatable=true)
	private String title;
	@Column(name="q_topic", nullable=true, insertable=true, updatable=true)
	private String topic;
	@Column(name="q_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="q_keywords", nullable=true, insertable=true, updatable=true)
	private String keywords;
	@Column(name="q_coverage", nullable=true, insertable=true, updatable=true)
	private String coverage;
	@Column(name="q_additional_informations", nullable=true, insertable=true, updatable=true)
	private String additionalInformations;
	@Column(name="q_language", nullable=false, insertable=true, updatable=true)
	private String language;
	
	//classification
	@ManyToOne(targetEntity=TaxonomyLevelImpl.class)
	@JoinColumn(name="fk_taxonomy_level_v2", nullable=true, insertable=true, updatable=true)
	private TaxonomyLevel taxonomyLevel;
	
	//educational
	@ManyToOne(targetEntity=QEducationalContext.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_edu_context", nullable=true, insertable=true, updatable=true)
	private QEducationalContext educationalContext;
	@Column(name="q_educational_learningtime", nullable=true, insertable=true, updatable=true)
	private String educationalLearningTime;
	
	//question
	@ManyToOne(targetEntity=QItemType.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_type", nullable=false, insertable=true, updatable=true)
	private QItemType type;
	@Column(name="q_difficulty", nullable=true, insertable=true, updatable=true)
	private BigDecimal difficulty;
	@Column(name="q_stdev_difficulty", nullable=true, insertable=true, updatable=true)
	private BigDecimal stdevDifficulty;
	@Column(name="q_differentiation", nullable=true, insertable=true, updatable=true)
	private BigDecimal differentiation;
	@Column(name="q_num_of_answers_alt", nullable=false, insertable=true, updatable=true)
	private int numOfAnswerAlternatives;
	@Column(name="q_usage", nullable=false, insertable=true, updatable=true)
	private int usage;
	@Column(name="q_assessment_type", nullable=true, insertable=true, updatable=true)
	private String assessmentType;
	
	//management
	@Column(name="q_correction_time", nullable=true, insertable=true, updatable=true)
	private Integer correctionTime;
	
	//life cycle
	@Column(name="q_version", nullable=true, insertable=true, updatable=true)
	private String itemVersion;
	@Column(name="q_status", nullable=false, insertable=true, updatable=true)
	private String status;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="q_status_last_modified", nullable=true, insertable=true, updatable=true)
	private Date statusLastModified;

	//rights
	@ManyToOne(targetEntity=QLicense.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_license", nullable=true, insertable=true, updatable=true)
	private QLicense license;
	@Column(name="q_creator", nullable=true, insertable=true, updatable=true)
	private String creator;

	//technics
	@Column(name="q_editor", nullable=true, insertable=true, updatable=true)
	private String editor;
	@Column(name="q_editor_version", nullable=true, insertable=true, updatable=true)
	private String editorVersion;
	
	@Column(name="q_format", nullable=false, insertable=true, updatable=true)
	private String format;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	//intern
	@Column(name="q_dir", nullable=true, insertable=true, updatable=false)
	private String directory;
	@Column(name="q_root_filename", nullable=true, insertable=true, updatable=false)
	private String rootFilename;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_ownergroup", nullable=false, insertable=true, updatable=false)
	private SecurityGroup ownerGroup;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public String getResourceableTypeName() {
		return "QuestionItem";
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getMasterIdentifier() {
		return masterIdentifier;
	}

	public void setMasterIdentifier(String masterIdentifier) {
		this.masterIdentifier = masterIdentifier;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getCoverage() {
		return coverage;
	}

	@Override
	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	@Override
	public String getAdditionalInformations() {
		return additionalInformations;
	}

	@Override
	public void setAdditionalInformations(String additionalInformations) {
		this.additionalInformations = additionalInformations;
	}

	@Override
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
	}
	
	@Transient
	@Override
	public String getTaxonomicPath() {
		if(taxonomyLevel != null) {
			return taxonomyLevel.getMaterializedPathIdentifiers();
		}
		return null;
	}

	@Override
	public QEducationalContext getEducationalContext() {
		return educationalContext;
	}

	@Override
	public void setEducationalContext(QEducationalContext educationalContext) {
		this.educationalContext = educationalContext;
	}

	@Override
	public String getEducationalContextLevel() {
		if(educationalContext != null) {
			return educationalContext.getLevel();
		}
		return null;
	}

	@Override
	public String getEducationalLearningTime() {
		return educationalLearningTime;
	}

	@Override
	public void setEducationalLearningTime(String educationalLearningTime) {
		this.educationalLearningTime = educationalLearningTime;
	}

	@Override
	public BigDecimal getDifficulty() {
		return difficulty;
	}

	@Override
	public void setDifficulty(BigDecimal difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public BigDecimal getStdevDifficulty() {
		return stdevDifficulty;
	}

	@Override
	public void setStdevDifficulty(BigDecimal stdevDifficulty) {
		this.stdevDifficulty = stdevDifficulty;
	}

	@Override
	public BigDecimal getDifferentiation() {
		return differentiation;
	}

	@Override
	public void setDifferentiation(BigDecimal differentiation) {
		this.differentiation = differentiation;
	}

	@Override
	public int getNumOfAnswerAlternatives() {
		return numOfAnswerAlternatives;
	}

	@Override
	public void setNumOfAnswerAlternatives(int numOfAnswerAlternatives) {
		this.numOfAnswerAlternatives = numOfAnswerAlternatives;
	}

	@Override
	public int getUsage() {
		return usage;
	}

	@Override
	public void setUsage(int usage) {
		this.usage = usage;
	}

	@Override
	public String getAssessmentType() {
		return assessmentType;
	}

	@Override
	public void setAssessmentType(String assessmentType) {
		this.assessmentType = assessmentType;
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
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public QItemType getType() {
		return type;
	}
	
	@Override
	public String getItemType() {
		if(type != null) {
			return type.getType();
		}
		return null;
	}

	public void setType(QItemType type) {
		this.type = type;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Integer getCorrectionTime() {
		return correctionTime;
	}

	@Override
	public void setCorrectionTime(Integer correctionTime) {
		this.correctionTime = correctionTime;
	}

	@Override
	public String getKeywords() {
		return keywords;
	}

	@Override
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	@Override
	@Transient
	public QuestionStatus getQuestionStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return QuestionStatus.valueOf(status);
		}
		return null;
	}
	
	public void setQuestionStatus(QuestionStatus status) {
		setStatus(status.name());
	}

	@Override
	public Date getQuestionStatusLastModified() {
		return statusLastModified;
	}

	public void setQuestionStatusLastModified(Date statusLastModified) {
		this.statusLastModified = statusLastModified;
	}

	@Override
	public QLicense getLicense() {
		return license;
	}

	@Deprecated
	public void setLicense(QLicense license) {
		this.license = license;
	}

	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Deprecated
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	@Override
	public String getEditorVersion() {
		return editorVersion;
	}

	public void setEditorVersion(String editorVersion) {
		this.editorVersion = editorVersion;
	}

	@Override
	public String getItemVersion() {
		return itemVersion;
	}

	public void setItemVersion(String itemVersion) {
		this.itemVersion = itemVersion;
	}

	@Override
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Override
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
		sb.append("question[");
		sb.append("key=").append(this.key);
		sb.append(", title=").append(title);
		sb.append("] ").append(super.toString());
		return sb.toString();
	}

}
