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
package org.olat.modules.quality;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 29.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityDataCollectionSearchParams {
	
	private Date startDateAfter;
	private Date startDateBefore;
	private List<Long> formEntryKeys;
	private List<QualityDataCollectionTopicType> topicTypes;
	private IdentityRef topicIdentityRef;
	private RepositoryEntryRef topicRepositoryRef;
	private List<Long> generatorKeys;
	private Long generatorProviderKey;
	private boolean fetchGenerator;
	private Boolean generatorOverrideAvailable;
	
	public Date getStartDateAfter() {
		return startDateAfter;
	}

	public void setStartDateAfter(Date startDateAfter) {
		this.startDateAfter = startDateAfter;
	}

	public Date getStartDateBefore() {
		return startDateBefore;
	}

	public void setStartDateBefore(Date startDateBefore) {
		this.startDateBefore = startDateBefore;
	}

	public List<Long> getFormEntryKeys() {
		return formEntryKeys;
	}

	public void setFormEntryKeys(List<Long> formEntryKeys) {
		this.formEntryKeys = formEntryKeys;
	}

	public List<QualityDataCollectionTopicType> getTopicTypes() {
		return topicTypes;
	}

	public void setTopicTypes(List<QualityDataCollectionTopicType> topicTypes) {
		this.topicTypes = topicTypes;
	}

	public IdentityRef getTopicIdentityRef() {
		return topicIdentityRef;
	}

	public void setTopicIdentityRef(IdentityRef topicIdentityRef) {
		this.topicIdentityRef = topicIdentityRef;
	}

	public RepositoryEntryRef getTopicRepositoryRef() {
		return topicRepositoryRef;
	}

	public void setTopicRepositoryRef(RepositoryEntryRef topicRepositoryRef) {
		this.topicRepositoryRef = topicRepositoryRef;
	}

	public List<Long> getGeneratorKeys() {
		return generatorKeys;
	}

	public void setGeneratorKeys(List<Long> generatorKeys) {
		this.generatorKeys = generatorKeys;
	}

	public void setGeneratorRef(QualityGeneratorRef generatorRef) {
		this.generatorKeys = List.of(generatorRef.getKey());
	}

	public Long getGeneratorProviderKey() {
		return generatorProviderKey;
	}

	public void setGeneratorProviderKey(Long generatorProviderKey) {
		this.generatorProviderKey = generatorProviderKey;
	}

	public boolean isFetchGenerator() {
		return fetchGenerator;
	}

	public void setFetchGenerator(boolean fetchGenerator) {
		this.fetchGenerator = fetchGenerator;
	}

	public Boolean getGeneratorOverrideAvailable() {
		return generatorOverrideAvailable;
	}

	public void setGeneratorOverrideAvailable(Boolean generatorOverrideAvailable) {
		this.generatorOverrideAvailable = generatorOverrideAvailable;
	}

}
