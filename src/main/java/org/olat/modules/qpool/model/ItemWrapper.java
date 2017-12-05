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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ItemWrapper implements QuestionItemView {
	
	private static final OLog log = Tracing.createLoggerFor(ItemWrapper.class);

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
	
	private final boolean isAuthor;
	private final boolean isReviewer;
	private final boolean isManager;
	private final boolean isEditableInPool;
	private final boolean isEditableInShare;
	private final boolean isMarked;
	private final Double rating;
	
	private ItemWrapper(ItemWrapperBuilder builder) {
		key = builder.item.getKey();
		creationDate = builder.item.getCreationDate();
		lastModified = builder.item.getLastModified();
		
		identifier = builder.item.getIdentifier();
		masterIdentifier = builder.item.getMasterIdentifier();
		title = builder.item.getTitle();
		topic = builder.item.getTopic();
		keywords = builder.item.getKeywords();
		coverage = builder.item.getCoverage();
		additionalInformations = builder.item.getAdditionalInformations();
		language = builder.item.getLanguage();
		
		taxonomyLevel = builder.item.getTaxonomyLevelName();
		educationalContextLevel = builder.item.getEducationalContextLevel();
		educationalLearningTime = builder.item.getEducationalLearningTime();
		
		itemType = builder.item.getItemType();
		difficulty = builder.item.getDifficulty();
		stdevDifficulty = builder.item.getStdevDifficulty();
		differentiation = builder.item.getDifferentiation();
		numOfAnswerAlternatives = builder.item.getNumOfAnswerAlternatives();
		usage = builder.item.getUsage();
		
		itemVersion = builder.item.getItemVersion();
		status = builder.item.getQuestionStatus().name();

		format = builder.item.getFormat();
		
		this.isAuthor = builder.isAuthor;
		this.isReviewer = builder.isTeacher && !builder.isAuthor;
		this.isManager = builder.isManager;
		this.isEditableInPool = builder.isEditableInPool;
		this.isEditableInShare = builder.isEditableInShare;
		this.isMarked = builder.isMarked;
		this.rating = builder.rating;

		log.debug("Question item wrapped:" + this.toString());
	}

    public static ItemWrapperBuilder builder(QuestionItem item) {
        return new ItemWrapperBuilder(item);
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
	public boolean isReviewer() {
		return isReviewer;
	}

	@Override
	public boolean isManager() {
		return isManager;
	}

	@Override
	public boolean isEditableInPool() {
		return isEditableInPool;
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
		  .append("name=").append(getTitle()).append(":")
		  .append("isAuthor=").append(isAuthor()).append(":")
		  .append("isReviewer=").append(isReviewer()).append(":")
		  .append("isManager=").append(isManager()).append(":")
		  .append("isEditableInPool=").append(isEditableInPool()).append(":")
		  .append("isEditableInShare=").append(isEditableInShare()).append(":")
		  .append("isMarked=").append(isMarked()).append(":")
		  .append("rating=").append(getRating()).append("]");
		return sb.toString();
	}
	
	public static class ItemWrapperBuilder {
		
		private final QuestionItem item;
		private boolean isAuthor = false;
		private boolean isTeacher = false;
		private boolean isManager = false;
		private boolean isEditableInPool = false;
		private boolean isEditableInShare = false;
		private boolean isMarked = false;
		private Double rating;
		
		public ItemWrapperBuilder(QuestionItem item) {
			this.item = item;
		}

		public ItemWrapperBuilder setAuthor(boolean isAuthor) {
			this.isAuthor = isAuthor;
			return this;
		}

		public ItemWrapperBuilder setAuthor(Number authorCount) {
			this.isAuthor = authorCount == null ? false : authorCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setTeacher(boolean isTeacher) {
			this.isTeacher = isTeacher;
			return this;
		}

		public ItemWrapperBuilder setTeacher(Number teacherCount) {
			this.isTeacher = teacherCount == null ? false : teacherCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setManager(boolean isManager) {
			this.isManager = isManager;
			return this;
		}

		public ItemWrapperBuilder setManager(Number managerCount) {
			this.isManager = managerCount == null ? false : managerCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setEditableInPool(boolean isEditableInPool) {
			this.isEditableInPool = isEditableInPool;
			return this;
		}

		public ItemWrapperBuilder setEditableInPool(Number editableInPoolCount) {
			this.isEditableInPool = editableInPoolCount == null ? false : editableInPoolCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setEditableInShare(boolean isEditableInShare) {
			this.isEditableInShare = isEditableInShare;
			return this;
		}

		public ItemWrapperBuilder setEditableInShare(Number editableInShareCount) {
			this.isEditableInShare = editableInShareCount == null ? false : editableInShareCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setMarked(boolean isMarked) {
			this.isMarked = isMarked;
			return this;
		}

		public ItemWrapperBuilder setMarked(Number markedCount) {
			this.isMarked = markedCount == null ? false : markedCount.longValue() > 0;
			return this;
		}

		public ItemWrapperBuilder setRating(Double rating) {
			this.rating = rating;
			return this;
		}
		
		public ItemWrapper create() {
			return new ItemWrapper(this);
		}
	}

}