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

import org.olat.core.gui.translator.Translator;
import org.olat.course.core.CourseElement;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-11-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CompletionCriterionMetCondition implements BadgeCondition {
	public static final String KEY = "completionCriterionMet";
	
	private String subIdent;
	
	public CompletionCriterionMetCondition(String subIdent) {
		this.subIdent = subIdent;
	}


	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String toString(Translator translator, RepositoryEntry courseEntry) {
		CourseElement courseElement = BadgeCondition.loadCourseElement(courseEntry, subIdent);
		return translator.translate("badgeCondition." + KEY,
				courseElement != null ? courseElement.getShortTitle() : subIdent);
	}
	
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}
}
