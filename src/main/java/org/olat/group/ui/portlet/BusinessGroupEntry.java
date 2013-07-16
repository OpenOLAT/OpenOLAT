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
package org.olat.group.ui.portlet;

import java.util.Date;

import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupShort;

/**
 * Short version of group for the portlet
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class BusinessGroupEntry {
	
	private final String name;
	private String description;
	private final Date creationDate;
	
	public BusinessGroupEntry(String name, Date creationDate) {
		this.name = name;
		this.creationDate = creationDate;
	}
	
	public BusinessGroupEntry(BusinessGroup group) {
		this.name = group.getName() == null ? "???" : group.getName();
		this.creationDate = group.getCreationDate();
	}
	
	public BusinessGroupEntry(BusinessGroupShort group) {
		this.name = group.getName() == null ? "???" : group.getName();
		this.creationDate = null;//group.getCreationDate();
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
}
