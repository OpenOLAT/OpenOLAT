/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.video.ui;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoEntryRow {
	
	private final Long key;
	private final String authors;
	private final String displayName;
	private final long launchCounter;
	private final String expenditureOfWork;
	private final OLATResourceable olatResource;
	
	public VideoEntryRow(RepositoryEntry entry) {
		this.key = entry.getKey();
		this.authors = entry.getAuthors();
		this.displayName = entry.getDisplayname();
		this.expenditureOfWork = entry.getExpenditureOfWork();
		launchCounter = entry.getStatistics().getLaunchCounter();
		olatResource = OresHelper.clone(entry.getOlatResource());
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}
	
	public String getAuthors() {
		return authors;
	}
	
	public long getLaunchCounter() {
		return launchCounter;
	}
	
	/**
	 * This is a clone of the repositoryEntry.getOLATResource();
	 * @return
	 */
	public OLATResourceable getOLATResourceable() {
		return olatResource;
	}
	
	

}
