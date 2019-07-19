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
package org.olat.modules.curriculum.model;

import org.olat.repository.RepositoryEntry;

/**
 * This is only used for import/export operations
 * 
 * Initial date: 19 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementToRepositoryEntryRef {
	
	private Long repositoryEntryKey;
	private String repositoryEntryDisplayname;
	private String repositoryEntryDescription;
	private String repositoryEntryInitialAuthor;
	
	private Long curriculumElementKey;
	
	public CurriculumElementToRepositoryEntryRef() {
		//
	}
	
	public CurriculumElementToRepositoryEntryRef(RepositoryEntry repositoryEntry, Long curriculumElementKey) {
		repositoryEntryKey = repositoryEntry.getKey();
		repositoryEntryDisplayname = repositoryEntry.getDisplayname();
		repositoryEntryDescription = repositoryEntry.getDescription();
		repositoryEntryInitialAuthor = repositoryEntry.getInitialAuthor();
		this.curriculumElementKey = curriculumElementKey;
	}
	
	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}
	
	public void setRepositoryEntryKey(Long repositoryEntryKey) {
		this.repositoryEntryKey = repositoryEntryKey;
	}
	
	public String getRepositoryEntryDisplayname() {
		return repositoryEntryDisplayname;
	}

	public void setRepositoryEntryDisplayname(String repositoryEntryDisplayname) {
		this.repositoryEntryDisplayname = repositoryEntryDisplayname;
	}

	public String getRepositoryEntryDescription() {
		return repositoryEntryDescription;
	}

	public void setRepositoryEntryDescription(String repositoryEntryDescription) {
		this.repositoryEntryDescription = repositoryEntryDescription;
	}

	public String getRepositoryEntryInitialAuthor() {
		return repositoryEntryInitialAuthor;
	}

	public void setRepositoryEntryInitialAuthor(String repositoryEntryInitialAuthor) {
		this.repositoryEntryInitialAuthor = repositoryEntryInitialAuthor;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}
	
	public void setCurriculumElementKey(Long curriculumElementKey) {
		this.curriculumElementKey = curriculumElementKey;
	}
}
