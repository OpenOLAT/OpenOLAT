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
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchExportMetadataParameters {
	
	private IdentityRef hasAuthor;
	private IdentityRef hasAdministrator;
	private List<RepositoryEntryRef> repositoryEntries;
	
	private String resSubPath;
	private List<ArchiveType> archiveTypes;
	private boolean ongoingExport;
	private Boolean onlyAdministrators;
	
	public SearchExportMetadataParameters(List<ArchiveType> archiveTypes) {
		this.archiveTypes = archiveTypes;
	}
	
	public SearchExportMetadataParameters(RepositoryEntryRef entry, String resSubPath, List<ArchiveType> archiveTypes) {
		this.archiveTypes = archiveTypes;
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

	public IdentityRef getHasAuthor() {
		return hasAuthor;
	}

	public void setHasAuthor(IdentityRef hasAuthor) {
		this.hasAuthor = hasAuthor;
	}

	public IdentityRef getHasAdministrator() {
		return hasAdministrator;
	}

	public void setHasAdministrator(IdentityRef hasAdministrator) {
		this.hasAdministrator = hasAdministrator;
	}

	public String getResSubPath() {
		return resSubPath;
	}
	
	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	public List<ArchiveType> getArchiveTypes() {
		return archiveTypes;
	}
	
	public ArchiveType[] getArchiveTypesArray() {
		return archiveTypes == null ? new ArchiveType[0] : archiveTypes.toArray(new ArchiveType[archiveTypes.size()]);
	}

	public void setArchiveTypes(List<ArchiveType> archiveTypes) {
		this.archiveTypes = archiveTypes;
	}

	public boolean isOngoingExport() {
		return ongoingExport;
	}

	public void setOngoingExport(boolean ongoingExport) {
		this.ongoingExport = ongoingExport;
	}

	public Boolean getOnlyAdministrators() {
		return onlyAdministrators;
	}

	public void setOnlyAdministrators(Boolean onlyAdministrators) {
		this.onlyAdministrators = onlyAdministrators;
	}
}
