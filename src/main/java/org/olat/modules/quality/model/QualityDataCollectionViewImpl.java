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
package org.olat.modules.quality.model;

import java.util.Date;

import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;

/**
 * 
 * Initial date: 16.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityDataCollectionViewImpl implements QualityDataCollectionView {

	private final Long key;
	private final QualityDataCollectionStatus status;
	private final String title;
	private final Date start;
	private final Date deadline;
	private final boolean qualitativeFeedback;
	private final Date creationDate;
	private final Long generatorKey;
	private final String generatorTitle;
	private final String formName;
	private final QualityDataCollectionTopicType topicType;
	private final String translatedTopicType;
	private final String topic;
	private final Long topicRepositoryKey;
	private final String topicRepositoryExternalRef;
	private final Long topicCurriculumElementKey;
	private final String topicCurriculumElementIdentifier;
	private final Long topicCurriculumElementCurriculumKey;
	private final String previousTitle;
	private final Long numberOfParticipants;
	private final Long numToDoTaskDone;
	private final Long numToDoTaskTotal;
	
	public QualityDataCollectionViewImpl(Long key, QualityDataCollectionStatus status, String title, Date start,
			Date deadline, boolean qualitativeFeedback, Date creationDate, Long generatorKey, String generatorTitle,
			String formName, QualityDataCollectionTopicType topicType, String translatedTopicType, String topic,
			Long topicRepositoryKey, String topicRepositoryExternalRef, Long topicCurriculumElementKey,
			String topicCurriculumElementIdentifier, Long topicCurriculumElementCurriculumKey, String previousTitle,
			Long numberOfParticipants, Long numToDoTaskDone, Long numToDoTaskTotal) {
		super();
		this.key = key;
		this.status = status;
		this.title = title;
		this.start = start;
		this.deadline = deadline;
		this.qualitativeFeedback = qualitativeFeedback;
		this.creationDate = creationDate;
		this.generatorKey = generatorKey;
		this.generatorTitle = generatorTitle;
		this.formName = formName;
		this.topicType = topicType;
		this.translatedTopicType = translatedTopicType;
		this.topic = topic;
		this.topicRepositoryKey = topicRepositoryKey;
		this.topicRepositoryExternalRef = topicRepositoryExternalRef;
		this.topicCurriculumElementKey = topicCurriculumElementKey;
		this.topicCurriculumElementIdentifier = topicCurriculumElementIdentifier;
		this.topicCurriculumElementCurriculumKey = topicCurriculumElementCurriculumKey;
		this.previousTitle = previousTitle;
		this.numberOfParticipants = numberOfParticipants;
		this.numToDoTaskDone = numToDoTaskDone;
		this.numToDoTaskTotal = numToDoTaskTotal;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public String getResourceableTypeName() {
		return QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME;
	}

	@Override
	public Long getResourceableId() {
		return key;
	}

	@Override
	public QualityDataCollectionStatus getStatus() {
		return status;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Date getStart() {
		return start;
	}

	@Override
	public Date getDeadline() {
		return deadline;
	}

	@Override
	public boolean isQualitativeFeedback() {
		return qualitativeFeedback;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Long getGeneratorKey() {
		return generatorKey;
	}

	@Override
	public String getGeneratorTitle() {
		return generatorTitle;
	}

	@Override
	public String getFormName() {
		return formName;
	}

	@Override
	public QualityDataCollectionTopicType getTopicType() {
		return topicType;
	}
		
	@Override
	public String getTranslatedTopicType() {
		return translatedTopicType;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public Long getTopicRepositoryKey() {
		return topicRepositoryKey;
	}

	@Override
	public String getTopicRepositoryExternalRef() {
		return topicRepositoryExternalRef;
	}

	@Override
	public Long getTopicCurriculumElementKey() {
		return topicCurriculumElementKey;
	}

	@Override
	public String getTopicCurriculumElementIdentifier() {
		return topicCurriculumElementIdentifier;
	}

	@Override
	public Long getTopicCurriculumElementCurriculumKey() {
		return topicCurriculumElementCurriculumKey;
	}

	@Override
	public String getPreviousTitle() {
		return previousTitle;
	}

	@Override
	public Long getNumberOfParticipants() {
		return numberOfParticipants;
	}

	@Override
	public Long getNumToDoTaskDone() {
		return numToDoTaskDone;
	}

	@Override
	public Long getNumToDoTaskTotal() {
		return numToDoTaskTotal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QualityDataCollectionViewImpl other = (QualityDataCollectionViewImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("QualityDataCollectionViewImpl [key=");
		builder.append(key);
		builder.append(", title=");
		builder.append(title);
		builder.append("]");
		return builder.toString();
	}

}
