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

import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ItemWrapper implements QuestionItemView {

	private Long key;
	private Date creationDate;
	private Date lastModified;
	
	private String identifier;
	private String masterIdentifier;
	private String title;
	private String topic;
	private String keywords;
	private String coverage;
	private String additionalInformations;
	private String language;
	
	private String taxonomyLevel;
	private String educationalContextLevel;
	private String educationalLearningTime;
	
	private String itemType;
	private BigDecimal difficulty;
	private BigDecimal stdevDifficulty;
	private BigDecimal differentiation;
	private int numOfAnswerAlternatives;
	private int usage;
	
	private String itemVersion;
	private String status;

	private String format;
	
	private boolean editable;
	private boolean reviewable;
	private boolean marked;
	private Double rating;
	
	public ItemWrapper(QuestionItemImpl item, boolean editable, boolean reviewable, boolean marked, Double rating) {
		key = item.getKey();
		creationDate = item.getCreationDate();
		lastModified = item.getLastModified();
		
		identifier = item.getIdentifier();
		masterIdentifier = item.getMasterIdentifier();
		title = item.getTitle();
		topic = item.getTopic();
		keywords = item.getKeywords();
		coverage = item.getCoverage();
		additionalInformations = item.getAdditionalInformations();
		language = item.getLanguage();
		
		taxonomyLevel = item.getTaxonomyLevelName();
		educationalContextLevel = item.getEducationalContextLevel();
		educationalLearningTime = item.getEducationalLearningTime();
		
		itemType = item.getItemType();
		difficulty = item.getDifficulty();
		stdevDifficulty = item.getStdevDifficulty();
		differentiation = item.getDifferentiation();
		numOfAnswerAlternatives = item.getNumOfAnswerAlternatives();
		usage = item.getUsage();
		
		itemVersion = item.getItemVersion();
		status = item.getStatus();

		format = item.getFormat();
		
		this.editable = editable;
		this.reviewable = reviewable;
		this.marked = marked;
		this.rating = rating;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}
	
	@Override
	public boolean isReviewable() {
		return reviewable;
	}

	@Override
	public boolean isMarked() {
		return marked;
	}

	@Override
	public Double getRating() {
		return rating;
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

	@Override
	public String getMasterIdentifier() {
		return masterIdentifier;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String getTopic() {
		return topic;
	}
	
	@Override
	public String getKeywords() {
		return keywords;
	}

	@Override
	public String getCoverage() {
		return coverage;
	}

	@Override
	public String getAdditionalInformations() {
		return additionalInformations;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	public String getTaxonomyLevelName() {
		return taxonomyLevel;
	}

	@Override
	public String getEducationalContextLevel() {
		return educationalContextLevel;
	}

	@Override
	public String getEducationalLearningTime() {
		return educationalLearningTime;
	}
	
	@Override
	public String getItemType() {
		return itemType;
	}

	@Override
	public BigDecimal getDifficulty() {
		return difficulty;
	}
	
	@Override
	public BigDecimal getStdevDifficulty() {
		return stdevDifficulty;
	}

	@Override
	public BigDecimal getDifferentiation() {
		return differentiation;
	}

	@Override
	public int getNumOfAnswerAlternatives() {
		return numOfAnswerAlternatives;
	}
	
	@Override
	public int getUsage() {
		return usage;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		//not its job
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public QuestionStatus getQuestionStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return QuestionStatus.valueOf(status);
		}
		return null;
	}

	@Override
	public String getItemVersion() {
		return itemVersion;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ItemWrapper) {
			ItemWrapper row = (ItemWrapper) obj;
			return key.equals(row.key);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("itemRow[key=").append(getKey()).append(":")
		  .append("name=").append(getTitle()).append("]");
		return sb.toString();
	}
}