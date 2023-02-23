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
package org.olat.course.assessment.ui.tool;

import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCoachingListOptions {
	
	private final String translatedFormTitle;
	private final RepositoryEntry courseEntry;
	private final boolean withCoachAssignment;
	
	public AssessmentCoachingListOptions(String translatedFormTitle) {
		this(null, false, translatedFormTitle);
	}
	
	public AssessmentCoachingListOptions(RepositoryEntry courseEntry, boolean withCoachAssignment, String translatedFormTitle) {
		this.translatedFormTitle = translatedFormTitle;
		this.courseEntry = courseEntry;
		this.withCoachAssignment = withCoachAssignment;
	}

	public String getTranslatedFormTitle() {
		return translatedFormTitle;
	}

	public RepositoryEntry getCourseEntry() {
		return courseEntry;
	}

	public boolean withCoachAssignment() {
		return withCoachAssignment;
	}

}
