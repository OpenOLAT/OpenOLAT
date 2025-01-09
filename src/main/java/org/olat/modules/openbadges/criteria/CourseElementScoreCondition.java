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
 * Initial date: 2024-06-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseElementScoreCondition implements BadgeCondition {
	public static final String KEY = "courseElementScore";

	private String subIdent;
	private Symbol symbol;
	private double value;
	private String displayName;

	public CourseElementScoreCondition(String subIdent, Symbol symbol, double value, String displayName) {
		this.subIdent = subIdent;
		this.symbol = symbol;
		this.value = value;
		this.displayName = displayName;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String toString(Translator translator, RepositoryEntry courseEntry) {
		return translator.translate("badgeCondition." + KEY,
				courseElementName(courseEntry), 
				getSymbol().getSymbolString(),
				Double.toString(getValue())
		);
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

	public boolean satisfiesCondition(double score) {
		return Symbol.compare(score, symbol, value);
	}

	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
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
