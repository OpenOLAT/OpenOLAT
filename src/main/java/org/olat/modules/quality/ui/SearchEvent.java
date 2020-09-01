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
import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 28 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SearchEvent extends Event {

	private static final long serialVersionUID = -8809079365428887946L;
	
	private String searchString;
	private String title;
	private String topic;
	private Date startAfter;
	private Date startBefore;
	private Date deadlineAfter;
	private Date deadlineBefore;
	private QualityDataCollectionRef dataCollectionRef;
	private List<? extends QualityGeneratorRef> generatorRefs;
	private List<? extends RepositoryEntryRef> formEntryRefs;
	private List<QualityDataCollectionTopicType> topicTypes;
	private List<QualityDataCollectionStatus> status;

	public SearchEvent() {
		super("qulity-dc-search");
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Date getStartAfter() {
		return startAfter;
	}

	public void setStartAfter(Date startAfter) {
		this.startAfter = startAfter;
	}

	public Date getStartBefore() {
		return startBefore;
	}

	public void setStartBefore(Date startBefore) {
		this.startBefore = startBefore;
	}

	public Date getDeadlineAfter() {
		return deadlineAfter;
	}

	public void setDeadlineAfter(Date deadlineAfter) {
		this.deadlineAfter = deadlineAfter;
	}

	public Date getDeadlineBefore() {
		return deadlineBefore;
	}

	public void setDeadlineBefore(Date deadlineBefore) {
		this.deadlineBefore = deadlineBefore;
	}
	
	public QualityDataCollectionRef getDataCollectionRef() {
		return dataCollectionRef;
	}
	
	public void setDataCollectionRef(QualityDataCollectionRef dataCollectionRef) {
		this.dataCollectionRef = dataCollectionRef;
	}
	
	public List<? extends QualityGeneratorRef> getGeneratorRefs() {
		return generatorRefs;
	}

	public void setGeneratorRefs(List<? extends QualityGeneratorRef> generatorRefs) {
		this.generatorRefs = generatorRefs;
	}

	public List<? extends RepositoryEntryRef> getFormEntryRefs() {
		return formEntryRefs;
	}

	public void setFormEntryRefs(List<? extends RepositoryEntryRef> formEntryRefs) {
		this.formEntryRefs = formEntryRefs;
	}

	public List<QualityDataCollectionTopicType> getTopicTypes() {
		return topicTypes;
	}

	public void setTopicTypes(List<QualityDataCollectionTopicType> topicTypes) {
		this.topicTypes = topicTypes;
	}

	public List<QualityDataCollectionStatus> getStatus() {
		return status;
	}

	public void setStatus(List<QualityDataCollectionStatus> status) {
		this.status = status;
	}

}
