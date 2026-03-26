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
package org.olat.modules.opencast;

import java.util.function.Function;
import java.util.function.Predicate;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 19 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class WildcardFilter implements Predicate<Object> {

	private final String searchValue;
	private final Function<Object, String> stringifier;
	private final String[] segments;

	WildcardFilter(String searchValue, Function<Object, String> stringifier) {
		this.stringifier = stringifier;
		if (searchValue.contains("*")) {
			this.searchValue = null;
			this.segments = searchValue.toLowerCase().split("\\*", -1);
		} else {
			this.searchValue = searchValue.toLowerCase();
			this.segments = null;
		}
	}

	@Override
	public boolean test(Object object) {
		if (segments == null && !StringHelper.containsNonWhitespace(searchValue)) return true;

		String value = stringifier.apply(object);
		if (value != null) {
			String lowerCaseValue = value.toLowerCase();
			return segments != null ? matchesWildcard(lowerCaseValue) : lowerCaseValue.indexOf(searchValue) > -1;
		}
		return false;
	}

	private boolean matchesWildcard(String lowerCaseValue) {
		int pos = 0;
		for (String segment : segments) {
			int idx = lowerCaseValue.indexOf(segment, pos);
			if (idx < 0) return false;
			pos = idx + segment.length();
		}
		return true;
	}

}
