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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;

/**
 * 
 * Initial date: 21.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@MappedSuperclass
abstract class AbstractItemView implements QuestionItemView, CreateInfo, ModifiedInfo, Persistable {

	private static final long serialVersionUID = 503607331953283037L;

	@Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="item_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	//calculated
	@Column(name="item_rating", nullable=true, insertable=false, updatable=false)
	private Double rating;
	@Column(name="mark_creator", nullable=false, insertable=false, updatable=false)
	private Long markCreatorKey;
	@Column(name="marked", nullable=false, insertable=false, updatable=false)
	private boolean marked;

	//general
	@Column(name="item_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="item_master_identifier", nullable=false, insertable=true, updatable=false)
	private String masterIdentifier;
	@Column(name="item_title", nullable=false, insertable=true, updatable=true)
	private String title;
	@Column(name="item_keywords", nullable=true, insertable=true, updatable=true)
	private String keywords;
	@Column(name="item_coverage", nullable=true, insertable=true, updatable=true)
	private String coverage;
	@Column(name="item_additional_informations", nullable=true, insertable=true, updatable=true)
	private String additionalInformations;
	@Column(name="item_language", nullable=false, insertable=true, updatable=true)
	private String language;
	
	//classification
	@Column(name="item_taxonomy_level", nullable=true, insertable=false, updatable=false)
	private String taxonomyLevel;
	
	//educational
	@Column(name="item_edu_context", nullable=false, insertable=true, updatable=true)
	private String educationalContextLevel;
	@Column(name="item_educational_learningtime", nullable=false, insertable=true, updatable=true)
	private String educationalLearningTime;
	
	//question
	@Column(name="item_type", nullable=false, insertable=true, updatable=true)
	private String itemType;
	@Column(name="item_difficulty", nullable=true, insertable=true, updatable=true)
	private BigDecimal difficulty;
	@Column(name="item_stdev_difficulty", nullable=true, insertable=true, updatable=true)
	private BigDecimal stdevDifficulty;
	@Column(name="item_differentiation", nullable=true, insertable=true, updatable=true)
	private BigDecimal differentiation;
	@Column(name="item_num_of_answers_alt", nullable=false, insertable=true, updatable=true)
	private int numOfAnswerAlternatives;
	@Column(name="item_usage", nullable=false, insertable=true, updatable=true)
	private int usage;
	
	//life cycle
	@Column(name="item_version", nullable=true, insertable=true, updatable=true)
	private String itemVersion;
	@Column(name="item_status", nullable=false, insertable=true, updatable=true)
	private String status;

	//technics
	@Column(name="item_format", nullable=false, insertable=true, updatable=true)
	private String format;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="item_creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="item_lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}
	
	public Long getMarkCreatorKey() {
		return markCreatorKey;
	}

	public void setMarkCreatorKey(Long markCreatorKey) {
		this.markCreatorKey = markCreatorKey;
	}

	@Override
	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
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

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	@Override
	public String getEducationalContextLevel() {
		return educationalContextLevel;
	}
	
	public void setEducationalContextLevel(String educationalContextLevel) {
		this.educationalContextLevel = educationalContextLevel;
	}
	
	@Override
	public String getTaxonomyLevelName() {
		return taxonomyLevel;
	}

	public String getTaxonomyLevel() {
		return taxonomyLevel;
	}

	public void setTaxonomyLevel(String taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
	}

	@Override
	public String getEducationalLearningTime() {
		return educationalLearningTime;
	}

	public void setEducationalLearningTime(String educationalLearningTime) {
		this.educationalLearningTime = educationalLearningTime;
	}

	@Override
	public BigDecimal getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(BigDecimal difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public BigDecimal getStdevDifficulty() {
		return stdevDifficulty;
	}

	public void setStdevDifficulty(BigDecimal stdevDifficulty) {
		this.stdevDifficulty = stdevDifficulty;
	}

	@Override
	public BigDecimal getDifferentiation() {
		return differentiation;
	}

	public void setDifferentiation(BigDecimal differentiation) {
		this.differentiation = differentiation;
	}

	@Override
	public int getNumOfAnswerAlternatives() {
		return numOfAnswerAlternatives;
	}

	public void setNumOfAnswerAlternatives(int numOfAnswerAlternatives) {
		this.numOfAnswerAlternatives = numOfAnswerAlternatives;
	}

	@Override
	public int getUsage() {
		return usage;
	}

	public void setUsage(int usage) {
		this.usage = usage;
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	public String getAdditionalInformations() {
		return additionalInformations;
	}

	public void setAdditionalInformations(String additionalInformations) {
		this.additionalInformations = additionalInformations;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	
	public String getItemVersion() {
		return itemVersion;
	}

	public void setItemVersion(String itemVersion) {
		this.itemVersion = itemVersion;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Transient
	public QuestionStatus getQuestionStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return QuestionStatus.valueOf(status);
		}
		return null;
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
		if(obj instanceof AbstractItemView) {
			AbstractItemView q = (AbstractItemView)obj;
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
		sb.append("itemView[key=").append(this.key)
			.append("]").append(super.toString());
		return sb.toString();
	}
}
