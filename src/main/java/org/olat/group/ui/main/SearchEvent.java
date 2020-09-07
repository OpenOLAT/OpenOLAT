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
package org.olat.group.ui.main;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.group.model.BusinessGroupQueryParams;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchEvent extends Event implements StateEntry {

	private static final long serialVersionUID = 6630250536374073143L;
	
	private String idRef;
	private String name;
	private String description;
	private String ownerName;
	private String courseTitle;
	private boolean owner;
	private boolean attendee;
	private boolean waiting;
	private boolean headless = false;
	private Boolean publicGroups;
	private Boolean managed;
	private Boolean resources;
	
	public SearchEvent() {
		super("search");
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String owner) {
		this.ownerName = owner;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
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

	public Boolean getPublicGroups() {
		return publicGroups;
	}

	public void setPublicGroups(Boolean publicGroups) {
		this.publicGroups = publicGroups;
	}
	
	public Boolean getManaged() {
		return managed;
	}

	public void setManaged(Boolean managed) {
		this.managed = managed;
	}

	public Boolean getResources() {
		return resources;
	}

	public void setResources(Boolean resources) {
		this.resources = resources;
	}

	public boolean isHeadless() {
		return headless;
	}

	public void setHeadless(boolean headless) {
		this.headless = headless;
	}
	
	public BusinessGroupQueryParams convertToBusinessGroupQueriesParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setIdRef(StringHelper.containsNonWhitespace(idRef) ? idRef : null);
		params.setName(StringHelper.containsNonWhitespace(name) ? name : null);
		params.setDescription(StringHelper.containsNonWhitespace(description) ? description : null);
		params.setOwnerName(StringHelper.containsNonWhitespace(ownerName) ? ownerName : null);
		params.setCourseTitle(StringHelper.containsNonWhitespace(courseTitle) ? courseTitle : null);
		params.setOwner(isOwner());
		params.setAttendee(isAttendee());
		params.setWaiting(isWaiting());
		params.setPublicGroups(getPublicGroups());
		params.setManaged(getManaged());
		params.setResources(getResources());
		params.setHeadless(isHeadless());
		return params;
	}

	@Override
	public SearchEvent clone() {
		SearchEvent clone = new SearchEvent();
		clone.idRef = idRef;
		clone.name = name;
		clone.description = description;
		clone.ownerName = ownerName;
		clone.courseTitle = courseTitle;
		clone.owner = owner;
		clone.attendee = attendee;
		clone.waiting = waiting;
		clone.headless = headless;
		clone.publicGroups = publicGroups;
		clone.resources = resources;
		return clone;
	}
}
