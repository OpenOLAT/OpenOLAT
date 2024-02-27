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
package org.olat.core.commons.services.export.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchExportMetadataParameters {
	
	private IdentityRef creator;
	private List<RepositoryEntryRef> repositoryEntries;
	
	private String resSubPath;
	private ArchiveType archiveType;
	private boolean ongoingExport;
	private boolean onlyAdministrators;
	
	public SearchExportMetadataParameters() {
		//
	}
	
	public SearchExportMetadataParameters(RepositoryEntry entry, String resSubPath) {
		if(entry != null) {
			repositoryEntries = new ArrayList<>(2);
			repositoryEntries.add(entry);
		}
		this.resSubPath = resSubPath;
	}
	
	public boolean hasRepositoryEntries() {
		return repositoryEntries != null && !repositoryEntries.isEmpty();
	}
	
	public List<RepositoryEntryRef> getRepositoryEntries() {
		return repositoryEntries;
	}

	public void setRepositoryEntries(List<RepositoryEntryRef> repositoryEntries) {
		this.repositoryEntries = repositoryEntries;
	}

	public IdentityRef getCreator() {
		return creator;
	}

	public void setCreator(IdentityRef creator) {
		this.creator = creator;
	}

	public String getResSubPath() {
		return resSubPath;
	}
	
	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	public ArchiveType getArchiveType() {
		return archiveType;
	}

	public void setArchiveType(ArchiveType archiveType) {
		this.archiveType = archiveType;
	}

	public boolean isOngoingExport() {
		return ongoingExport;
	}

	public void setOngoingExport(boolean ongoingExport) {
		this.ongoingExport = ongoingExport;
	}

	public boolean isOnlyAdministrators() {
		return onlyAdministrators;
	}

	public void setOnlyAdministrators(boolean onlyAdministrators) {
		this.onlyAdministrators = onlyAdministrators;
	}
}
