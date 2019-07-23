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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.vfs.Quota;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGConfigBusinessGroup {
	
	private final List<String> toolsToEnable = new ArrayList<>();
	private final List<String> toolsToDisable = new ArrayList<>();
	
	private int calendarAccess;
	private int folderAccess;
	private Quota quota;
	
	private List<RepositoryEntry> resources;
	
	public List<String> getToolsToEnable() {
		return toolsToEnable;
	}

	public List<String> getToolsToDisable() {
		return toolsToDisable;
	}

	public int getCalendarAccess() {
		return calendarAccess;
	}

	public void setCalendarAccess(int calendarAccess) {
		this.calendarAccess = calendarAccess;
	}

	public int getFolderAccess() {
		return folderAccess;
	}

	public void setFolderAccess(int folderAccess) {
		this.folderAccess = folderAccess;
	}

	public Quota getQuota() {
		return quota;
	}

	public void setQuota(Quota quota) {
		this.quota = quota;
	}

	public List<RepositoryEntry> getResources() {
		return resources;
	}

	public void setResources(List<RepositoryEntry> resources) {
		this.resources = resources;
	}
}