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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;

/**
 * Initial date: 2024-06-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CoursesPassedCondition implements BadgeCondition {
	public static final String KEY = "coursesPassed";

	private final List<Long> courseResourceKeys = new ArrayList<>();

	public CoursesPassedCondition() {
	}

	public CoursesPassedCondition(List<Long> courseResourceKeys) {
		this.courseResourceKeys.addAll(courseResourceKeys);
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String toString(Translator translator, RepositoryEntry courseEntry) {
		List<RepositoryEntry> repositoryEntries = CoreSpringFactory.getImpl(RepositoryEntryDAO.class).loadByResourceKeys(courseResourceKeys);

		StringBuilder sb = new StringBuilder();
		String keySuffix = repositoryEntries.size() == 1 ? "singular" : "plural";
		if (repositoryEntries.size() == 1) {
			sb.append("\"").append(repositoryEntries.get(0).getDisplayname()).append("\"");
		} else {
			sb.append("(");
			boolean first = true;
			for (RepositoryEntry repositoryEntry : repositoryEntries) {
				if (!first) {
					sb.append(", ");
				} else {
					first = false;
				}
				sb.append("\"");
				sb.append(repositoryEntry.getDisplayname());
				sb.append("\"");
			}
			sb.append(")");
		}
		return translator.translate("badgeCondition." + KEY + "." + keySuffix, sb.toString());
	}

	public List<Long> getCourseResourceKeys() {
		return courseResourceKeys;
	}
}
