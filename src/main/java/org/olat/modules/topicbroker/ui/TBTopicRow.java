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
package org.olat.modules.topicbroker.ui;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormItemList;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBGroupRestrictionInfo;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicEnrollmentStatus;
import org.olat.modules.topicbroker.TBTopicRef;

/**
 * 
 * Initial date: 30 May 2024<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicRow implements TBTopicRef {

	private final TBTopic topic;
	private String minParticipantsString;
	private int minEnrollments;
	private int numEnrollments;
	private String enrolledString;
	private TBTopicEnrollmentStatus enrollmentStatus;
	private String translatedEnrollmentStatus;
	private String availability;
	private int waitingList;
	private String waitingListString;
	private List<TBGroupRestrictionInfo> groupRestrictions;
	private FormItemList groupRestrictionLinks;
	private String createdByDisplayname;
	private int sortOrder;
	private List<TBCustomField> customFields;
	private List<FormItem> customFieldItems;
	private UpDown upDown;
	private FormLink toolsLink;
	private String detailsComponentName;
	
	public TBTopicRow(TBTopic topic) {
		this.topic = topic;
	}
	
	public TBTopic getTopic() {
		return topic;
	}

	@Override
	public Long getKey() {
		return topic.getKey();
	}
	
	public String getIdentifier() {
		return topic.getIdentifier();
	}

	public String getTitle() {
		return topic.getTitle();
	}
	
	public Date getBeginDate() {
		return topic.getBeginDate();
	}
	
	public Date getEndDate() {
		return topic.getEndDate();
	}
	
	public Integer getMinParticipants() {
		return topic.getMinParticipants();
	}

	public String getMinParticipantsString() {
		return minParticipantsString;
	}

	public void setMinParticipantsString(String minParticipantsString) {
		this.minParticipantsString = minParticipantsString;
	}

	public Integer getMaxParticipants() {
		return topic.getMaxParticipants();
	}
	
	public Set<Long> getGroupRestrictionKeys() {
		return topic.getGroupRestrictionKeys();
	}

	public int getMinEnrollments() {
		return minEnrollments;
	}

	public void setMinEnrollments(int minEnrollments) {
		this.minEnrollments = minEnrollments;
	}
	
	public int getNumEnrollments() {
		return numEnrollments;
	}

	public void setNumEnrollments(int numEnrollments) {
		this.numEnrollments = numEnrollments;
	}

	public String getEnrolledString() {
		return enrolledString;
	}

	public void setEnrolledString(String enrolledString) {
		this.enrolledString = enrolledString;
	}

	public TBTopicEnrollmentStatus getEnrollmentStatus() {
		return enrollmentStatus;
	}

	public void setEnrollmentStatus(TBTopicEnrollmentStatus enrollmentStatus) {
		this.enrollmentStatus = enrollmentStatus;
	}

	public String getTranslatedEnrollmentStatus() {
		return translatedEnrollmentStatus;
	}

	public void setTranslatedEnrollmentStatus(String translatedEnrollmentStatus) {
		this.translatedEnrollmentStatus = translatedEnrollmentStatus;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public int getWaitingList() {
		return waitingList;
	}

	public void setWaitingList(int waitingList) {
		this.waitingList = waitingList;
	}

	public String getWaitingListString() {
		return waitingListString;
	}

	public void setWaitingListString(String waitingListString) {
		this.waitingListString = waitingListString;
	}

	public List<TBGroupRestrictionInfo> getGroupRestrictions() {
		return groupRestrictions;
	}

	public void setGroupRestrictions(List<TBGroupRestrictionInfo> groupRestrictions) {
		this.groupRestrictions = groupRestrictions;
	}

	public FormItemList getGroupRestrictionLinks() {
		return groupRestrictionLinks;
	}

	public void setGroupRestrictionLinks(FormItemList groupRestrictionLinks) {
		this.groupRestrictionLinks = groupRestrictionLinks;
	}

	public String getCreatedByDisplayname() {
		return createdByDisplayname;
	}

	public void setCreatedByDisplayname(String createdByDisplayname) {
		this.createdByDisplayname = createdByDisplayname;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public List<TBCustomField> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(List<TBCustomField> customFields) {
		this.customFields = customFields;
	}

	public List<FormItem> getCustomFieldItems() {
		return customFieldItems;
	}

	public void setCustomFieldItems(List<FormItem> customFieldItems) {
		this.customFieldItems = customFieldItems;
	}

	public UpDown getUpDown() {
		return upDown;
	}

	public void setUpDown(UpDown upDown) {
		this.upDown = upDown;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public String getDetailsComponentName() {
		return detailsComponentName;
	}

	public void setDetailsComponentName(String detailsComponentName) {
		this.detailsComponentName = detailsComponentName;
	}

}
