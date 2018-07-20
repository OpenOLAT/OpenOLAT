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

import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationStatus;

/**
 * 
 * Initial date: 20.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityExcecutorParticipationImpl implements QualityExecutorParticipation {
	
	private final Long participationKey;
	private final QualityExecutorParticipationStatus executionStatus;
	private final Date start;
	private final Date deadline;
	private final String title;
	private final QualityDataCollectionTopicType topicType;
	private final String translatedTopicType;
	private final String topic;
	
	public QualityExcecutorParticipationImpl(Long participationKey, Integer executionStatusOrder, Date start,
			Date deadline, String title, QualityDataCollectionTopicType topicType, String translatedTopicType,
			String topic) {
		super();
		this.participationKey = participationKey;
		this.executionStatus = QualityExecutorParticipationStatus.getEnum(executionStatusOrder);
		this.start = start;
		this.deadline = deadline;
		this.title = title;
		this.topicType = topicType;
		this.translatedTopicType = translatedTopicType;
		this.topic = topic;
	}
	
	@Override
	public EvaluationFormParticipationRef getParticipationRef() {
		return new EvaluationFormParticipationRef() {
			
			@Override
			public Long getKey() {
				return participationKey;
			}
		};
	}

	@Override
	public QualityExecutorParticipationStatus getExecutionStatus() {
		return executionStatus;
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
	public String getTitle() {
		return title;
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

}
