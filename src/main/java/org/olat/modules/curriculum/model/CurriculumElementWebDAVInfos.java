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

/**
 * 
 * Initial date: 21 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWebDAVInfos {
	
	private final Long repositoryEntryKey;
	
	private final Long curriculumElementKey;
	private final String curriculumElementDisplayName;
	private final String curriculumElementIdentifier;
	
	private final Long parentCurriculumElementKey;
	private final String parentCurriculumElementDisplayName;
	private final String parentCurriculumElementIdentifier;
	
	public CurriculumElementWebDAVInfos(Long repositoryEntryKey,
			Long curriculumElementKey, String curriculumElementDisplayName, String curriculumElementIdentifier,
			Long parentCurriculumElementKey, String parentCurriculumElementDisplayName,String parentCurriculumElementIdentifier) {
		this.repositoryEntryKey = repositoryEntryKey;
		this.curriculumElementKey = curriculumElementKey;
		this.curriculumElementDisplayName = curriculumElementDisplayName;
		this.curriculumElementIdentifier = curriculumElementIdentifier;
		this.parentCurriculumElementKey = parentCurriculumElementKey;
		this.parentCurriculumElementDisplayName = parentCurriculumElementDisplayName;
		this.parentCurriculumElementIdentifier = parentCurriculumElementIdentifier;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}

	public String getCurriculumElementDisplayName() {
		return curriculumElementDisplayName;
	}

	public String getCurriculumElementIdentifier() {
		return curriculumElementIdentifier;
	}

	public Long getParentCurriculumElementKey() {
		return parentCurriculumElementKey;
	}

	public String getParentCurriculumElementDisplayName() {
		return parentCurriculumElementDisplayName;
	}

	public String getParentCurriculumElementIdentifier() {
		return parentCurriculumElementIdentifier;
	}
	
	@Override
	public int hashCode() {
		return (repositoryEntryKey == null ? 7645925 : repositoryEntryKey.hashCode())
				+ (curriculumElementKey == null ? -4785 : curriculumElementKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementWebDAVInfos) {
			CurriculumElementWebDAVInfos infos = (CurriculumElementWebDAVInfos)obj;
			return repositoryEntryKey != null && repositoryEntryKey.equals(infos.repositoryEntryKey)
					&& curriculumElementKey != null && curriculumElementKey.equals(infos.curriculumElementKey);
		}
		return false;
	}
	
	
}
