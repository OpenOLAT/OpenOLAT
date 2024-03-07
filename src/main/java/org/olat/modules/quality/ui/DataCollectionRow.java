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
package org.olat.modules.quality.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionView;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class DataCollectionRow {

	private final QualityDataCollectionView dataCollectionView;
	private String topicBusinessPath;
	private FormItem topicItem;
	private FormLink toDosItem;

	public DataCollectionRow(QualityDataCollectionView dataCollectionView) {
		this.dataCollectionView = dataCollectionView;
	}
	
	QualityDataCollectionView getDataCollection() {
		return dataCollectionView;
	}

	Long getKey() {
		return dataCollectionView.getKey();
	}
	
	QualityDataCollectionStatus getStatus() {
		return dataCollectionView.getStatus();
	}

	String getTitle() {
		return dataCollectionView.getTitle();
	}

	Date getStart() {
		return dataCollectionView.getStart();
	}

	Date getDeadline() {
		return dataCollectionView.getDeadline();
	}
	
	String getFormName() {
		return dataCollectionView.getFormName();
	}
	
	boolean isQualitativeFeedback() {
		return dataCollectionView.isQualitativeFeedback();
	}
	
	String getTopicType() {
		return dataCollectionView.getTranslatedTopicType();
	}
	
	String getTopic() {
		return dataCollectionView.getTopic();
	}
	
	Long getTopicRepositoryKey() {
		return dataCollectionView.getTopicRepositoryKey();
	}
	
	String getTopicRepositoryExternalRef() {
		return dataCollectionView.getTopicRepositoryExternalRef();
	}
	
	Long getTopicCurriculumElementKey() {
		return dataCollectionView.getTopicCurriculumElementKey();
	}
	
	String getTopicCurriculumElementIdentifier() {
		return dataCollectionView.getTopicCurriculumElementIdentifier();
	}
	
	Long getTopicCurriculumElementCurriculumKey() {
		return dataCollectionView.getTopicCurriculumElementCurriculumKey();
	}
	
	Long getNumberOfParticipants() {
		return dataCollectionView.getNumberOfParticipants();
	}
	
	Date getCreationDate() {
		return dataCollectionView.getCreationDate();
	}

	String getGeneratorTitle() {
		return dataCollectionView.getGeneratorTitle();
	}

	Long getNumToDoTasksDone() {
		return dataCollectionView.getNumToDoTaskDone();
	}

	Long getNumToDoTasksTotal() {
		return dataCollectionView.getNumToDoTaskTotal();
	}

	public String getTopicBusinessPath() {
		return topicBusinessPath;
	}

	public void setTopicBusinessPath(String topicBusinessPath) {
		this.topicBusinessPath = topicBusinessPath;
	}

	FormItem getTopicItem() {
		return topicItem;
	}

	void setTopicItem(FormItem topicItem) {
		this.topicItem = topicItem;
	}

	FormLink getToDosItem() {
		return toDosItem;
	}

	void setToDosItem(FormLink toDosItem) {
		this.toDosItem = toDosItem;
	}
}
