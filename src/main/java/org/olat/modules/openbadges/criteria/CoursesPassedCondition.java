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

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;

/**
 * Initial date: 2024-06-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CoursesPassedCondition implements BadgeCondition {
	public static final String KEY = "coursesPassed";

	private List<Long> courseResourceKeys;
	private List<Long> courseRepositoryEntryKeys;

	public CoursesPassedCondition() {
	}

	public CoursesPassedCondition(List<Long> courseRepositoryEntryKeys) {
		this.courseRepositoryEntryKeys = new ArrayList<>();
		this.courseRepositoryEntryKeys.addAll(courseRepositoryEntryKeys);
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String toString(Translator translator, RepositoryEntry courseEntry) {
		List<RepositoryEntry> repositoryEntries = new ArrayList<>();
		if (courseRepositoryEntryKeys != null && !courseRepositoryEntryKeys.isEmpty()) {
			repositoryEntries = CoreSpringFactory.getImpl(RepositoryEntryDAO.class).loadByKeys(courseRepositoryEntryKeys);
		}

		StringBuilder sb = new StringBuilder();
		String keySuffix = repositoryEntries.size() == 1 ? "singular" : "plural";
		if (repositoryEntries.size() != 1) {
			sb.append("(");
		}

		String str = repositoryEntries.stream()
				.map(c -> StringHelper.escapeHtml(c.getDisplayname()))
				.map(s -> "\"" + s + "\"")
				.sorted(Collator.getInstance(translator.getLocale()))
				.collect(Collectors.joining(", "));
		if (StringHelper.containsNonWhitespace(str)) {
			sb.append(str);
		}

		if (repositoryEntries.size() != 1) {
			sb.append(")");
		}
		return translator.translate("badgeCondition." + KEY + "." + keySuffix, sb.toString());
	}

	public List<Long> getCourseRepositoryEntryKeys() {
		if (courseRepositoryEntryKeys == null) {
			courseRepositoryEntryKeys = new ArrayList<>();
		}
		return courseRepositoryEntryKeys;
	}
}
