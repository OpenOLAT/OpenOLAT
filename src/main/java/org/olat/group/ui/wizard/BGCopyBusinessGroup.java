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
package org.olat.group.ui.wizard;

import java.util.List;

import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupShort;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGCopyBusinessGroup implements BusinessGroupShort {

	private String name;
	private List<String> names;
	private String description;
	private Integer minParticipants;
	private Integer maxParticipants;
	private Boolean allowToLeave;
	
	private final BusinessGroup original;
	
	public BGCopyBusinessGroup(BusinessGroup original) {
		this.original = original;
	}
	
	
	@Override
	public Long getKey() {
		return null;
	}

	@Override
	public Long getResourceableId() {
		return null;
	}

	@Override
	public String getResourceableTypeName() {
		return "BusinessGroup";
	}

	public BusinessGroup getOriginal() {
		return original;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}


	@Override
	public BusinessGroupManagedFlag[] getManagedFlags() {
		return BusinessGroupManagedFlag.EMPTY_ARRAY;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}

	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	public Boolean getAllowToLeave() {
		return allowToLeave;
	}

	public void setAllowToLeave(Boolean allowToLeave) {
		this.allowToLeave = allowToLeave;
	}
}
