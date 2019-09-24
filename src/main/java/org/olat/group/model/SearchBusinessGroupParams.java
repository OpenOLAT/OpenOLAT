/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.group.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchBusinessGroupParams {
	
	private Collection<String> tools;
	private String nameOrDesc;
	private Collection<Long> groupKeys;
	private String name;
	private String exactName;
	private String description;
	private String courseTitle;
	private String externalId;
	private Boolean managed;
	private String idRef;
	
	private Identity identity;
	private boolean owner;
	private boolean attendee;
	private boolean waiting;
	
	public SearchBusinessGroupParams() {
		//
	}
	
	public SearchBusinessGroupParams(Identity identity, boolean owner, boolean attendee) {
		this.identity = identity;
		this.owner = owner;
		this.attendee = attendee;
	}
	
	public Collection<String> getTools() {
		return tools;
	}

	public void setTools(List<String> tools) {
		this.tools = tools;
	}
	
	public void addTools(String... toolsToAdd) {
		if(this.tools == null) {
			this.tools = new ArrayList<>();
		}
		for(String tool:toolsToAdd) {
			this.tools.add(tool);
		}
	}

	public Collection<Long> getGroupKeys() {
		return groupKeys;
	}

	public void setGroupKeys(Collection<Long> groupKeys) {
		this.groupKeys = groupKeys;
	}

	public String getIdRef() {
		return idRef;
	}

	public void setIdRef(String idRef) {
		this.idRef = idRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getExactName() {
		return exactName;
	}

	public void setExactName(String exactName) {
		this.exactName = exactName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public String getNameOrDesc() {
		return nameOrDesc;
	}

	public void setNameOrDesc(String nameOrDesc) {
		this.nameOrDesc = nameOrDesc;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Boolean getManaged() {
		return managed;
	}

	public void setManaged(Boolean managed) {
		this.managed = managed;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public boolean isAttendee() {
		return attendee;
	}

	public void setAttendee(boolean attendee) {
		this.attendee = attendee;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}



}
