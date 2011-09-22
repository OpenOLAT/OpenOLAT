/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.restapi.support.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for GroupVO
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "groupVO")
public class GroupVO {
	private Long key;
	private String description;
	private String name;
	private String type;
	private Integer minParticipants;
	private Integer maxParticipants;

	public GroupVO() {
		//make jaxb happy
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
