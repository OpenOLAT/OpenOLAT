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
package org.olat.group.model;

import org.olat.basesecurity.Group;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupReference {

	private Long key;
	private String name;
	private Group group;
	
	private Long originalKey;
	private String originalName;
	private Group originalGroup;
	
	public BusinessGroupReference() {
		//
	}
	
	public BusinessGroupReference(BusinessGroup group) {
		this.key = group.getKey();
		this.name = group.getName();
		this.group = group.getBaseGroup();
		this.originalKey = group.getKey();
		this.originalName = group.getName();
		this.originalGroup = group.getBaseGroup();
	}
	
	public BusinessGroupReference(BusinessGroup group, Long originalKey, String originalName) {
		this.key = group.getKey();
		this.name = group.getName();
		this.originalKey = originalKey;
		this.originalName = originalName;
	}
	
	public BusinessGroupReference(BusinessGroup newGroup, BusinessGroup originalGroup) {
		this.key = newGroup.getKey();
		this.name = newGroup.getName();
		this.group = newGroup.getBaseGroup();
		this.originalKey = originalGroup.getKey();
		this.originalName = originalGroup.getName();
		this.originalGroup = originalGroup.getBaseGroup();
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Long getOriginalKey() {
		return originalKey;
	}

	public void setOriginalKey(Long originalKey) {
		this.originalKey = originalKey;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public Group getOriginalGroup() {
		return originalGroup;
	}
	
	public void setOriginalGroup(Group originalGroup) {
		this.originalGroup = originalGroup;
	}
}
