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
package org.olat.modules.coach.model;

import java.util.List;

import org.olat.repository.RepositoryEntryRuntimeType;

/**
 * 
 * Initial date: 5 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum CoursesStatisticsRuntimeTypesGroup {
	
	standaloneAndCurricular(true, List.of(RepositoryEntryRuntimeType.standalone, RepositoryEntryRuntimeType.curricular)),
	embeddedAndTemplate(false, List.of(RepositoryEntryRuntimeType.embedded, RepositoryEntryRuntimeType.template));
	
	private final boolean loadStatistics;
	private final List<RepositoryEntryRuntimeType> runtimeTypes;
	
	private CoursesStatisticsRuntimeTypesGroup(boolean loadStatistics, List<RepositoryEntryRuntimeType> runtimeTypes) {
		this.runtimeTypes = runtimeTypes;
		this.loadStatistics = loadStatistics;
	}
	
	public boolean loadStatistics() {
		return loadStatistics;
	}
	
	public List<RepositoryEntryRuntimeType> runtimeTypes() {
		return runtimeTypes;
	}

}
