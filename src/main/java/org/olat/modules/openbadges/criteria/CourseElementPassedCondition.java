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
package org.olat.modules.openbadges.criteria;

import java.beans.Transient;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.course.core.CourseElement;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-06-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseElementPassedCondition implements BadgeCondition {
	public static final String KEY = "courseElementPassed";

	private String subIdent;
	private String displayName;

	public CourseElementPassedCondition(String subIdent, String displayName) {
		this.subIdent = subIdent;
		this.displayName = displayName;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String toString(Translator translator, RepositoryEntry courseEntry) {
		return translator.translate("badgeCondition." + KEY, courseElementName(courseEntry));
	}
	
	private String courseElementName(RepositoryEntry courseEntry) {
		String currentCourseElementName = courseElementName(courseEntry, subIdent);
		if (StringHelper.containsNonWhitespace(currentCourseElementName)) {
			return currentCourseElementName;
		}
		if (StringHelper.containsNonWhitespace(getDisplayName())) {
			return getDisplayName();
		}
		return subIdent;
	}

	private String courseElementName(RepositoryEntry courseEntry, String subIdent) {
		CourseElement courseElement = BadgeCondition.loadCourseElement(courseEntry, subIdent);
		if (courseElement != null) {
			return courseElement.getShortTitle();
		}
		return "";
	}
	
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Transient
	public void prepareForEntryReset(RepositoryEntry courseEntry) {
		String currentCourseElementName = courseElementName(courseEntry, subIdent);
		if (StringHelper.containsNonWhitespace(currentCourseElementName)) {
			setDisplayName(currentCourseElementName);
		}
	}
}
