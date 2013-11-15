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
package org.olat.repository.portlet;

import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryLight;

/**
 * Description:<br>
 * Wrapper for a repository entry
 * 
 * <P>
 * Initial Date: 06.03.2009 <br>
 * 
 * @author gnaegi
 */
public class RepositoryPortletEntry implements PortletEntry<RepositoryEntryLight> {
	private RepositoryEntryLight value;

	public RepositoryPortletEntry(RepositoryEntryLight repoEntry) {
		value = new REShort(repoEntry);
	}
	
	public RepositoryPortletEntry(RepositoryEntry repoEntry) {
		value = new REShort(repoEntry);
	}

	public Long getKey() {
		return value.getKey();
	}

	public RepositoryEntryLight getValue() {
		return value;
	}
	
	public String getDescription() {
		return value.getDescription();
	}

	private static class REShort implements RepositoryEntryLight {
		private final Long key;
		private final String displayname;
		private final String description;
		private final String type;
		private final int access;
		private final int statusCode;
		private final boolean membersOnly;
		
		public REShort(RepositoryEntryLight entry) {
			key = entry.getKey();
			displayname = entry.getDisplayname();
			description = entry.getDescription();
			type = entry.getResourceType();
			statusCode = entry.getStatusCode();
			access = entry.getAccess();
			membersOnly = entry.isMembersOnly();
		}
		
		public REShort(RepositoryEntry entry) {
			key = entry.getKey();
			displayname = entry.getDisplayname();
			description = entry.getDescription();
			type = entry.getOlatResource().getResourceableTypeName();
			statusCode = entry.getStatusCode();
			access = entry.getAccess();
			membersOnly = entry.isMembersOnly();
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public String getDisplayname() {
			return displayname;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public int getAccess() {
			return access;
		}

		@Override
		public boolean isMembersOnly() {
			return membersOnly;
		}

		@Override
		public String getResourceType() {
			return type;
		}

		@Override
		public int getStatusCode() {
			return statusCode;
		}
	}
}
