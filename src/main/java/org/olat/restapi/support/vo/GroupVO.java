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
package org.olat.restapi.support.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.group.BusinessGroup;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "groupVO")
public class GroupVO {
	private Long key;
	private String description;
	private String externalId;
	@Schema(required = true, description = "Action to be performed on managedFlags", allowableValues = { 
			"all",
			  "details(all) //details tab",
			    "title(details,all)",
			    "description(details,all)",
			    "settings(details,all) //max num of participants...",
			  "tools(all) //tools tab",
			  "members(all) //members tab",
			    "display(members,all) // members display options",
			    "membersmanagement(members,all)",
			  "resources(all) //add/remove courses",
			  "bookings(all) // change booking rules",
			  "delete(all)"})
	private String managedFlags;
	private String name;
	private String type;
	private Integer minParticipants;
	private Integer maxParticipants;

	public GroupVO() {
		//make jaxb happy
	}
	
	public static GroupVO valueOf(BusinessGroup grp) {
		GroupVO vo = new GroupVO();
		vo.setKey(grp.getKey());
		vo.setName(grp.getName());
		vo.setDescription(grp.getDescription());
		vo.setMaxParticipants(grp.getMaxParticipants());
		vo.setMinParticipants(grp.getMinParticipants());
		vo.setExternalId(grp.getExternalId());
		vo.setManagedFlags(grp.getManagedFlagsString());
		vo.setType("LearningGroup");
		return vo;
	}
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
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
}
