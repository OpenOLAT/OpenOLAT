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
package org.olat.modules.quality.generator.provider.courselectures;

import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * Example: Check isFailed() if value LOWER than limit.
 * 
 * Initial date: 31.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum LimitCheck {
	
	LOWER("limit.check.lower", "limit.check.lower", (d1, d2) -> d1 < d2),
	LOWER_EQUAL("limit.check.lower.equal", "limit.check.lower.equal", (d1, d2) -> d1 <= d2),
	HIGHER("limit.check.higher", "limit.check.higher", (d1, d2) -> d1 > d2),
	HIGHER_EQUAL("limit.check.higher.equal", "limit.check.higher.equal", (d1, d2) -> d1 >= d2);
	
	private final String key;
	private final String i18nKey;
	private final BiPredicate<Double, Double> faildBiPredicate;

	private LimitCheck(String key, String i18nKey, BiPredicate<Double, Double> faildBiPredicate) {
		this.key = key;
		this.i18nKey = i18nKey;
		this.faildBiPredicate = faildBiPredicate;
		
	}
	
	public static LimitCheck getEnum(String key) {
		for (LimitCheck limitCheck: LimitCheck.values()) {
			if (limitCheck.key.equals(key)) {
				return limitCheck;
			}
		}
		return null;
	}
	

	public String getKey() {
		return key;
	}

	static public String[] getKeys() {
		return Arrays.stream(LimitCheck.values()).map(LimitCheck::getKey).toArray(String[]::new);
	}
	
	public String getI18nKey() {
		return i18nKey;
	}

	static public String[] getI18nKeys() {
		return Arrays.stream(LimitCheck.values()).map(LimitCheck::getI18nKey).toArray(String[]::new);
	}

	public boolean isTrue(Double value, Double limit) {
		return faildBiPredicate.test(value, limit);
	}

}
