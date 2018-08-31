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
	private final Date creationDate;
	private final String generatorTitle;
	private final String formName;
	private final QualityDataCollectionTopicType topicType;
	private final String translatedTopicType;
	private final String topic;
	private final String previousTitle;
	private final Long numberOfParticipants;
	
	public QualityDataCollectionViewImpl(Long key, QualityDataCollectionStatus status, String title, Date start,
			Date deadline, Date creationDate, String generatorTitle, String formName,
			QualityDataCollectionTopicType topicType, String translatedTopicType, String topic,
			String previousTitle, Long numberOfParticipants) {
		super();
		this.key = key;
		this.status = status;
		this.title = title;
		this.start = start;
		this.deadline = deadline;
		this.creationDate = creationDate;
		this.generatorTitle = generatorTitle;
		this.formName = formName;
		this.topicType = topicType;
		this.translatedTopicType = translatedTopicType;
		this.topic = topic;
		this.previousTitle = previousTitle;
		this.numberOfParticipants = numberOfParticipants;
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
	public Date getCreationDate() {
		return creationDate;
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
	public String getPreviousTitle() {
		return previousTitle;
	}

	@Override
	public Long getNumberOfParticipants() {
		return numberOfParticipants;
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
