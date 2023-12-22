/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.generator;

import java.util.Collection;
import java.util.List;

import org.olat.core.id.OrganisationRef;
import org.olat.core.util.DateRange;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 1 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorPreviewSearchParams {
	
	private DateRange dateRange;
	private List<Long> formEntryKeys;
	private List<Long> generatorKeys;
	private List<QualityDataCollectionTopicType> topicTypes;
	private Collection<? extends OrganisationRef> generatorOrganisationRefs;
	private Collection<Long> repositoryEntryKeys;
	private Collection<Long> curriculumElementKeys;

	public DateRange getDateRange() {
		return dateRange;
	}

	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}

	public List<Long> getFormEntryKeys() {
		return formEntryKeys;
	}

	public void setFormEntryKeys(List<Long> formEntryKeys) {
		this.formEntryKeys = formEntryKeys;
	}

	public List<Long> getGeneratorKeys() {
		return generatorKeys;
	}

	public void setGeneratorKeys(List<Long> generatorKeys) {
		this.generatorKeys = generatorKeys;
	}

	public List<QualityDataCollectionTopicType> getTopicTypes() {
		return topicTypes;
	}

	public void setTopicTypes(List<QualityDataCollectionTopicType> topicTypes) {
		this.topicTypes = topicTypes;
	}

	public Collection<? extends OrganisationRef> getGeneratorOrganisationRefs() {
		return generatorOrganisationRefs;
	}

	public void setGeneratorOrganisationRefs(Collection<? extends OrganisationRef> generatorOrganisationRefs) {
		this.generatorOrganisationRefs = generatorOrganisationRefs;
	}
	
	// empty collection should return no previews
	// null should not filter by repositoryEntryKeys
	public Collection<Long> getRepositoryEntryKeys() {
		return repositoryEntryKeys;
	}

	public void setRepositoryEntryKeys(Collection<Long> repositoryEntryKeys) {
		this.repositoryEntryKeys = repositoryEntryKeys;
	}

	public void setRepositoryEntries(Collection<? extends RepositoryEntryRef> repositoryEntryKeys) {
		this.repositoryEntryKeys = repositoryEntryKeys != null
				? repositoryEntryKeys.stream().map(RepositoryEntryRef::getKey).toList()
				: null;
	}

	public void setRepositoryEntry(RepositoryEntryRef repositoryEntry) {
		this.repositoryEntryKeys = repositoryEntry != null? List.of(repositoryEntry.getKey()): null;
	}

	// empty collection should return no previews
	// null should not filter by repositoryEntryKeys
	public Collection<Long> getCurriculumElementKeys() {
		return curriculumElementKeys;
	}

	public void setCurriculumElementKeys(Collection<Long> curriculumElementKeys) {
		this.curriculumElementKeys = curriculumElementKeys;
	}
	
	public void setCurriculumElements(Collection<? extends CurriculumElementRef> curriculumElements) {
		this.curriculumElementKeys = curriculumElements != null
				? curriculumElements.stream().map(CurriculumElementRef::getKey).toList()
				: null;
	}
}
