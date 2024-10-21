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
package org.olat.modules.lecture.ui.addwizard;

import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRow {
	
	private String statusHtml;
	private String thumbnailUrl;
	private final RepositoryEntry entry;
	
	public RepositoryEntryRow(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailUrl);
	}
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	
	public Long getKey() {
		return entry.getKey();
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public String getDisplayname() {
		return entry.getDisplayname();
	}
	
	public String getExternalRef() {
		return entry.getExternalRef();
	}
	
	public String getStatusHtml() {
		return statusHtml;
	}

	public void setStatusHtml(String statusHtml) {
		this.statusHtml = statusHtml;
	}

	@Override
	public int hashCode() {
		return entry.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RepositoryEntryRow row) {
			return entry != null && entry.equals(row.entry);
		}
		return false;
	}

}
