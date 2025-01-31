/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Dec 4, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public enum TaughtBy {
	
	teachers,
	coaches,
	owners;
	
	public static final List<TaughtBy> ALL = Arrays.asList(TaughtBy.values());
	public static final List<String> ALL_NAMES = ALL.stream().map(TaughtBy::name).toList();
	
	public static final String join(Collection<TaughtBy> taughtBys) {
		if (taughtBys == null || taughtBys.isEmpty()) {
			return null;
		}
		return taughtBys.stream().map(TaughtBy::name).collect(Collectors.joining(","));
	}
	
	public static final Set<TaughtBy> split(String taughtBys) {
		if (StringHelper.containsNonWhitespace(taughtBys)) {
			return Arrays.stream(taughtBys.split(",")).filter(TaughtBy::isValid).map(TaughtBy::valueOf).collect(Collectors.toSet());
		}
		return Set.of();
	}
	
	public static boolean isValid(String string) {
		boolean allOk = false;
		if(StringHelper.containsNonWhitespace(string)) {
			for(String name : ALL_NAMES) {
				if(name.equals(string)) {
					allOk = true;
					break;
				}
			}
		}
		return allOk;
	}

}
