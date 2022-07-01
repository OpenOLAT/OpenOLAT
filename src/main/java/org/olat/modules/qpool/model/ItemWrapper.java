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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ItemWrapper implements QuestionItemView {
	
	private static final Logger log = Tracing.createLoggerFor(ItemWrapper.class);

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
	
	private TaxonomyLevel taxonomyLevel;
	private String taxonomyPath;
	private String educationalContextLevel;
	private String educationalLearningTime;
	
	private Integer correctionTime;
	
	private String itemType;
	private BigDecimal difficulty;
	private BigDecimal stdevDifficulty;
	private BigDecimal differentiation;
	private int numOfAnswerAlternatives;
	private int usage;
	
	private String itemVersion;
	private String status;
	private Date statusLastModified;

	private String format;
	
	private boolean isAuthor;
	private boolean isTeacher;
	private boolean isManager;
	private boolean isRater;
	private boolean isEditableInPool;
	private boolean isEditableInShare;
	private boolean isMarked;
	private Double rating;
	private int numberOfRatings;
	
	private ItemWrapper() {
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public boolean isAuthor() {
		return isAuthor;
	}

	@Override
	public boolean isTeacher() {
		return isTeacher;
	}

	@Override
	public boolean isReviewer() {
		return isTeacher && !isAuthor && !isRater;
	}

	@Override
	public boolean isManager() {
		return isManager;
	}
	
	@Override
	public boolean isRater() {
		return isRater;
	}

	@Override
	public boolean isEditableInPool() {
		return isEditableInPool;
	}

	@Override
	public boolean isReviewableFormat() {
		return !"IMS QTI 1.2".equals(getFormat());
	}

	@Override
	public boolean isEditableInShare() {
		return isEditableInShare;
	}

	@Override
	public boolean isEditable() {
		return false;
	}
	
	@Override
	public boolean isMarked() {
		return isMarked;
	}

	@Override
	public Double getRating() {
		return rating;
	}

	@Override
	public int getNumberOfRatings() {
		return numberOfRatings;
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

	@Override
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	public String getTaxonomicPath() {
		return taxonomyPath;
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
	public Integer getCorrectionTime() {
		return correctionTime;
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
	public Date getQuestionStatusLastModified() {
		return statusLastModified;
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
		  .append("name=").append(getTitle()).append(":")
		  .append("isAuthor=").append(isAuthor()).append(":")
		  .append("isTeacher=").append(isTeacher()).append(":")
		  .append("isReviewer=").append(isReviewer()).append(":")
		  .append("isManager=").append(isManager()).append(":")
		  .append("isRater=").append(isRater()).append(":")
		  .append("isEditableInPool=").append(isEditableInPool()).append(":")
		  .append("isEditableInShare=").append(isEditableInShare()).append(":")
		  .append("isMarked=").append(isMarked()).append(":")
		  .append("rating=").append(getRating()).append("]");
		return sb.toString();
	}

    public static ItemWrapperBuilder builder(QuestionItem item) {
        return new ItemWrapperBuilder(item);
    }
	
	public static class ItemWrapperBuilder {
		
		private final ItemWrapper itemWrapper;
		
		public ItemWrapperBuilder(QuestionItem item) {
			itemWrapper = new ItemWrapper();
			itemWrapper.key = item.getKey();
			itemWrapper.creationDate = item.getCreationDate();
			itemWrapper.lastModified = item.getLastModified();
			
			itemWrapper.identifier = item.getIdentifier();
			itemWrapper.masterIdentifier = item.getMasterIdentifier();
			itemWrapper.title = item.getTitle();
			itemWrapper.topic = item.getTopic();
			itemWrapper.keywords = item.getKeywords();
			itemWrapper.coverage = item.getCoverage();
			itemWrapper.additionalInformations = item.getAdditionalInformations();
			itemWrapper.language = item.getLanguage();
			
			itemWrapper.taxonomyLevel = item.getTaxonomyLevel();
			itemWrapper.taxonomyPath = item.getTaxonomicPath();
			itemWrapper.educationalContextLevel = item.getEducationalContextLevel();
			itemWrapper.educationalLearningTime = item.getEducationalLearningTime();
			
			itemWrapper.correctionTime = item.getCorrectionTime();
			
			itemWrapper.itemType = item.getItemType();
			itemWrapper.difficulty = item.getDifficulty();
			itemWrapper.stdevDifficulty = item.getStdevDifficulty();
			itemWrapper.differentiation = item.getDifferentiation();
			itemWrapper.numOfAnswerAlternatives = item.getNumOfAnswerAlternatives();
			itemWrapper.usage = item.getUsage();
			
			itemWrapper.itemVersion = item.getItemVersion();
			itemWrapper.status = item.getQuestionStatus().name();
			itemWrapper.statusLastModified = item.getQuestionStatusLastModified();

			itemWrapper.format = item.getFormat();
		}

		public ItemWrapperBuilder setAuthor(boolean isAuthor) {
			itemWrapper.isAuthor = isAuthor;
			return this;
		}

		public ItemWrapperBuilder setAuthor(Number authorCount) {
			itemWrapper.isAuthor = authorCount != null && authorCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setTeacher(boolean isTeacher) {
			itemWrapper.isTeacher = isTeacher;
			return this;
		}

		public ItemWrapperBuilder setTeacher(Number teacherCount) {
			itemWrapper.isTeacher = teacherCount != null && teacherCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setManager(boolean isManager) {
			itemWrapper.isManager = isManager;
			return this;
		}

		public ItemWrapperBuilder setManager(Number managerCount) {
			itemWrapper.isManager = managerCount != null && managerCount.longValue() > 0;
			return this;
		}
		
		public ItemWrapperBuilder setRater(Number ratingsCount) {
			itemWrapper.isRater = ratingsCount != null && ratingsCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setEditableInPool(boolean isEditableInPool) {
			itemWrapper.isEditableInPool = isEditableInPool;
			return this;
		}

		public ItemWrapperBuilder setEditableInPool(Number editableInPoolCount) {
			itemWrapper.isEditableInPool = editableInPoolCount != null && editableInPoolCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setEditableInShare(boolean isEditableInShare) {
			itemWrapper.isEditableInShare = isEditableInShare;
			return this;
		}

		public ItemWrapperBuilder setEditableInShare(Number editableInShareCount) {
			itemWrapper.isEditableInShare = editableInShareCount != null && editableInShareCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setMarked(boolean isMarked) {
			itemWrapper.isMarked = isMarked;
			return this;
		}

		public ItemWrapperBuilder setMarked(Number markedCount) {
			itemWrapper.isMarked = markedCount != null && markedCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setRating(Double rating) {
			itemWrapper.rating = rating;
			return this;
		}
		
		public ItemWrapperBuilder setNumberOfRatings(Number numberOfRatings) {
			itemWrapper.numberOfRatings = numberOfRatings != null? numberOfRatings.intValue(): 0;
			return this;
		}
		
		public ItemWrapper create() {
			log.debug("Question item wrapped: {}", itemWrapper);
			return itemWrapper;
		}
	}
}