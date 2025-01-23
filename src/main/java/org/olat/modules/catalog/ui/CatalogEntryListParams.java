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
package org.olat.modules.catalog.ui;

import java.util.Set;

/**
 * Initial date: 2025-01-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CatalogEntryListParams {
	private boolean withSearch;
	private boolean fireBookedEvent;
	private boolean excludeMembers;
	private Set<String> excludedAccessMethodTypes;
	private boolean excludeRepositoryEntries;
	private Set<Long> excludedCurriculumElementKeys;

	public boolean isWithSearch() {
		return withSearch;
	}

	public void setWithSearch(boolean withSearch) {
		this.withSearch = withSearch;
	}

	public boolean isFireBookedEvent() {
		return fireBookedEvent;
	}

	public void setFireBookedEvent(boolean fireBookedEvent) {
		this.fireBookedEvent = fireBookedEvent;
	}

	public boolean isExcludeMembers() {
		return excludeMembers;
	}

	public void setExcludeMembers(boolean excludeMembers) {
		this.excludeMembers = excludeMembers;
	}

	public Set<String> getExcludedAccessMethodTypes() {
		return excludedAccessMethodTypes;
	}

	public void setExcludedAccessMethodTypes(Set<String> excludedAccessMethodTypes) {
		this.excludedAccessMethodTypes = excludedAccessMethodTypes;
	}

	public boolean isExcludeRepositoryEntries() {
		return excludeRepositoryEntries;
	}

	public void setExcludeRepositoryEntries(boolean excludeRepositoryEntries) {
		this.excludeRepositoryEntries = excludeRepositoryEntries;
	}

	public Set<Long> getExcludedCurriculumElementKeys() {
		return excludedCurriculumElementKeys;
	}

	public void setExcludedCurriculumElementKeys(Set<Long> excludedCurriculumElementKeys) {
		this.excludedCurriculumElementKeys = excludedCurriculumElementKeys;
	}
}
