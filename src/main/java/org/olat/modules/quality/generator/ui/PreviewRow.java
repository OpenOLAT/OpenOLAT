/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.generator.ui;

import java.util.Date;

import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;

/**
 * 
 * Initial date: 1 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PreviewRow {
	
	private final String identifier;
	private final QualityPreviewStatus status;
	private String translatedStatus;
	private final String title;
	private final Date creationDate;
	private final Date start;
	private final Date deadline;
	private String topicType;
	private String topic;
	private final String formName;
	private final Long numberParticipants;
	private final Long generatorId;
	private final String generatorTitle;
	private final Long dataCollectionKey;
	
	public PreviewRow(QualityPreview preview) {
		identifier = preview.getIdentifier();
		status = preview.getStatus();
		title = preview.getTitle();
		creationDate = preview.getCreationDate();
		start = preview.getStart();
		deadline = preview.getDeadline();
		formName = preview.getFormEntry().getDisplayname();
		numberParticipants = preview.getNumParticipants();
		generatorId = preview.getGenerator() != null? preview.getGenerator().getKey(): null;
		generatorTitle =  preview.getGenerator() != null? preview.getGenerator().getTitle(): null;
		dataCollectionKey = preview.getDataCollectionKey();
	}
	
	public PreviewRow(QualityDataCollectionView dataCollection) {
		identifier = null;
		status = QualityPreviewStatus.dataCollection;
		title = dataCollection.getTitle();
		creationDate = dataCollection.getCreationDate();
		start = dataCollection.getStart();
		deadline = dataCollection.getDeadline();
		topicType = dataCollection.getTranslatedTopicType();
		topic = dataCollection.getTopic();
		formName = dataCollection.getFormName();
		numberParticipants = dataCollection.getNumberOfParticipants();
		generatorId = dataCollection.getGeneratorKey();
		generatorTitle =  dataCollection.getGeneratorTitle();
		dataCollectionKey = dataCollection.getKey();
	}

	public String getIdentifier() {
		return identifier;
	}

	public QualityPreviewStatus getStatus() {
		return status;
	}

	public String getTranslatedStatus() {
		return translatedStatus;
	}

	public void setTranslatedStatus(String translatedStatus) {
		this.translatedStatus = translatedStatus;
	}

	public String getTitle() {
		return title;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public Date getStart() {
		return start;
	}
	
	public Date getDeadline() {
		return deadline;
	}
	
	public String getTopicType() {
		return topicType;
	}
	
	public void setTopicType(String topicType) {
		this.topicType = topicType;
	}
	
	public String getTopic() {
		return topic;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public String getFormName() {
		return formName;
	}
	
	public Long getNumberParticipants() {
		return numberParticipants;
	}
	
	public Long getGeneratorId() {
		return generatorId;
	}
	
	public String getGeneratorTitle() {
		return generatorTitle;
	}

	public Long getDataCollectionKey() {
		return dataCollectionKey;
	}
	
}
