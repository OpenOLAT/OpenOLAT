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
package org.olat.modules.topicbroker.model;

import java.util.List;
import java.util.Map;

import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBTopic;

public final class TBImportTopic {
	
	private String identifier;
	private String title;
	private String description;
	private String minParticipants;
	private String maxParticipants;
	private String groupRestrictions;
	private TBTopic topic;
	private List<String> customFieldValues;
	private Map<TBCustomFieldDefinition, String>  customFieldDefinitionToValue;
	private String message;
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(String minParticipants) {
		this.minParticipants = minParticipants;
	}

	public String getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(String maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	public String getGroupRestrictions() {
		return groupRestrictions;
	}

	public void setGroupRestrictions(String groupRestrictions) {
		this.groupRestrictions = groupRestrictions;
	}

	public TBTopic getTopic() {
		return topic;
	}

	public void setTopic(TBTopic topic) {
		this.topic = topic;
	}

	public List<String> getCustomFieldValues() {
		return customFieldValues;
	}

	public void setCustomFieldValues(List<String> customFieldValues) {
		this.customFieldValues = customFieldValues;
	}

	public Map<TBCustomFieldDefinition, String> getCustomFieldDefinitionToValue() {
		return customFieldDefinitionToValue;
	}

	public void setCustomFieldDefinitionToValue(Map<TBCustomFieldDefinition, String> customFieldDefinitionToValue) {
		this.customFieldDefinitionToValue = customFieldDefinitionToValue;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}