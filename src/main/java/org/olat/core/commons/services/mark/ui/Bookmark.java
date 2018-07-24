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
package org.olat.core.commons.services.mark.ui;

import java.util.Date;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Internal wrapper
 * 
 * @author srosse
 *
 */
class Bookmark {

	private final Mark mark;
	private final String title;
	private final String description;
	private final Date creationDate;
	private final String displayrestype;
	private RepositoryEntryStatusEnum status;
	
	public Bookmark(Mark mark, RepositoryEntry entry) {
		this.mark = mark;
		title = entry.getDisplayname();
		description = entry.getDescription();
		status = entry.getEntryStatus();
		creationDate = entry.getCreationDate();
		displayrestype = entry.getOlatResource().getResourceableTypeName();
	}
	
	public Bookmark(Mark mark, BusinessGroup group) {
		this.mark = mark;
		title = group.getName();
		description = group.getDescription();
		status = null;
		creationDate = group.getCreationDate();
		displayrestype = group.getResourceableTypeName();
	}
	
	public Long getKey() {
		return mark.getKey();
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public String getDisplayrestype() {
		return displayrestype;
	}
	
	public RepositoryEntryStatusEnum getEntryStatus() {
		return status;
	}
	
	public OLATResourceable getOLATResourceable() {
		return mark.getOLATResourceable();
	}
	
	public String getResSubPath() {
		return mark.getResSubPath();
	}
	
	public String getBusinessPath() {
		return mark.getBusinessPath();
	}
}
