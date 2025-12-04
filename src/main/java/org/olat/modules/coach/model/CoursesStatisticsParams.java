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
 * Initial date: 4 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record CoursesStatisticsParams(boolean withStatistics, boolean withCertificates, boolean withStatements, boolean withCompletions,
			boolean withReferences, List<RepositoryEntryRuntimeType> runtimeTypes) {

	public static CoursesStatisticsParams valueOf(CoursesStatisticsRuntimeTypesGroup runtimeTypesGroup) {
		boolean withStatistics = runtimeTypesGroup.loadStatistics();
		return new CoursesStatisticsParams(withStatistics, withStatistics, withStatistics, withStatistics, withStatistics, runtimeTypesGroup.runtimeTypes());
	}
	
	public static CoursesStatisticsParams withoutStatistics(CoursesStatisticsRuntimeTypesGroup runtimeTypesGroup) {
		return new CoursesStatisticsParams(false, false, false, false, false, runtimeTypesGroup.runtimeTypes());
	}

}
