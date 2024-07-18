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

import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBSelectionRef;
import org.olat.modules.topicbroker.TBSelectionStatus;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 30 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBSelectionRow {
	
	private TBTopic topic;
	private TBSelectionRef selectionRef;
	private String titleAbbr;
	private String participants;
	private String thumbnailUrl;
	private boolean enrolled;
	private TBSelectionStatus status;
	private String translatedStatus;
	private String statusLabel;
	private String priorityLabel;
	private List<TBCustomField> customFields;
	private List<FormItem> customFieldItems;
	private int topicSortOrder;
	private int selectionSortOrder;
	private UpDown upDown;
	private FormLink selectionToolsLink;
	private FormLink topicToolsLink;
	private FormLink detailsButton;
	private FormLink unselectButton;
	private FormLink selectButton;
	private DropdownItem selectDropdown;
	
	public TBTopic getTopic() {
		return topic;
	}

	public void setTopic(TBTopic topic) {
		this.topic = topic;
	}

	public TBSelectionRef getSelectionRef() {
		return selectionRef;
	}

	public void setSelectionRef(TBSelectionRef selectionRef) {
		this.selectionRef = selectionRef;
	}

	public String getTitle() {
		return topic != null? topic.getTitle(): null;
	}

	public Integer getMinParticipants() {
		return topic != null? topic.getMinParticipants(): null;
	}

	public Integer getMaxParticipants() {
		return topic != null? topic.getMaxParticipants(): null;
	}
	
	public Set<Long> getGroupRestrictionKeys() {
		return topic != null? topic.getGroupRestrictionKeys(): null;
	}
	
	public String getTitleAbbr() {
		return titleAbbr;
	}

	public void setTitleAbbr(String titleAbbr) {
		this.titleAbbr = titleAbbr;
	}
	
	public String getParticipants() {
		return participants;
	}

	public void setParticipants(String participants) {
		this.participants = participants;
	}

	public boolean isThumbnailAvailable() {
		return thumbnailUrl != null;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	public TBSelectionStatus getStatus() {
		return status;
	}

	public void setStatus(TBSelectionStatus status) {
		this.status = status;
	}

	public String getTranslatedStatus() {
		return translatedStatus;
	}

	public void setTranslatedStatus(String translatedStatus) {
		this.translatedStatus = translatedStatus;
	}

	public String getStatusLabel() {
		return statusLabel;
	}

	public void setStatusLabel(String statusLabel) {
		this.statusLabel = statusLabel;
	}

	public String getPriorityLabel() {
		return priorityLabel;
	}

	public void setPriorityLabel(String priorityLabel) {
		this.priorityLabel = priorityLabel;
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

	public int getSelectionSortOrder() {
		return selectionSortOrder;
	}

	public void setSelectionSortOrder(int selectionSortOrder) {
		this.selectionSortOrder = selectionSortOrder;
	}

	public int getTopicSortOrder() {
		return topicSortOrder;
	}

	public void setTopicSortOrder(int topicSortOrder) {
		this.topicSortOrder = topicSortOrder;
	}

	public UpDown getUpDown() {
		return upDown;
	}

	public void setUpDown(UpDown upDown) {
		this.upDown = upDown;
	}

	public FormLink getSelectionToolsLink() {
		return selectionToolsLink;
	}

	public void setSelectionToolsLink(FormLink selectionToolsLink) {
		this.selectionToolsLink = selectionToolsLink;
	}

	public FormLink getTopicToolsLink() {
		return topicToolsLink;
	}

	public void setTopicToolsLink(FormLink topicToolsLink) {
		this.topicToolsLink = topicToolsLink;
	}
	
	public String getDetailsButtonName() {
		return detailsButton != null? detailsButton.getComponent().getComponentName(): null;
	}

	public FormLink getDetailsButton() {
		return detailsButton;
	}

	public void setDetailsButton(FormLink detailsButton) {
		this.detailsButton = detailsButton;
	}
	
	public String getUnselectButtonName() {
		return unselectButton != null? unselectButton.getComponent().getComponentName(): null;
	}

	public FormLink getUnselectButton() {
		return unselectButton;
	}

	public void setUnselectButton(FormLink unselectButton) {
		this.unselectButton = unselectButton;
	}
	
	public String getSelectButtonName() {
		return selectButton != null? selectButton.getComponent().getComponentName(): null;
	}

	public FormLink getSelectButton() {
		return selectButton;
	}

	public void setSelectButton(FormLink selectButton) {
		this.selectButton = selectButton;
	}
	
	public String getSelectDropdownName() {
		return selectDropdown != null? selectDropdown.getComponent().getComponentName(): null;
	}

	public DropdownItem getSelectDropdown() {
		return selectDropdown;
	}

	public void setSelectDropdown(DropdownItem selectDropdown) {
		this.selectDropdown = selectDropdown;
	}
	
}
