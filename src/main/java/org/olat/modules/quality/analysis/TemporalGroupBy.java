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
package org.olat.modules.quality.analysis;

/**
 * 
 * Initial date: 19 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum TemporalGroupBy {
	
	DATA_COLLECTION_DEADLINE_YEAR("trend.group.data.collection.deadline.year") {
		@Override
		public TemporalKey getNextKey(TemporalKey currentKey) {
			return TemporalKey.of(currentKey.getYear() + 1);
		}
	},
	DATA_COLLECTION_DEADLINE_HALF_YEAR("trend.group.data.collection.deadline.half.year") {
		@Override
		public TemporalKey getNextKey(TemporalKey currentKey) {
			return TemporalGroupBy.getNextKey(currentKey, 2);
		}
	},
	DATA_COLLECTION_DEADLINE_QUARTER("trend.group.data.collection.deadline.quarter") {
		@Override
		public TemporalKey getNextKey(TemporalKey currentKey) {
			return TemporalGroupBy.getNextKey(currentKey, 4);
		}
	},
	DATA_COLLECTION_DEADLINE_MONTH("trend.group.data.collection.deadline.month") {
		@Override
		public TemporalKey getNextKey(TemporalKey currentKey) {
			return TemporalGroupBy.getNextKey(currentKey, 12);
		}
	};

	private String i18nKey;

	private TemporalGroupBy(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	public String i18nKey() {
		return i18nKey;
	}

	public abstract TemporalKey getNextKey(TemporalKey currentKey);

	private static TemporalKey getNextKey(TemporalKey currentKey, int max) {
		if (currentKey.getYearPart() < max) {
			return TemporalKey.of(currentKey.getYear(), currentKey.getYearPart() + 1);
		}
		return TemporalKey.of(currentKey.getYear() + 1, 1);
	}

}
