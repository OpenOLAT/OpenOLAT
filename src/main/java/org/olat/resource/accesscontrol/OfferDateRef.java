/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol;

import java.util.Date;

/**
 * Reference point for relative offer date computation.
 *
 * Initial date: 27.04.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public enum OfferDateRef {

	BEFORE_BEGIN("before.begin") {
		@Override
		public Date computeDate(Date beginDate, Date endDate, OfferDateUnit unit, int value) {
			return unit.before(beginDate, value);
		}
	},
	AFTER_BEGIN("after.begin") {
		@Override
		public Date computeDate(Date beginDate, Date endDate, OfferDateUnit unit, int value) {
			return unit.after(beginDate, value);
		}
	},
	BEFORE_END("before.end") {
		@Override
		public Date computeDate(Date beginDate, Date endDate, OfferDateUnit unit, int value) {
			return unit.before(endDate, value);
		}
	},
	AFTER_END("after.end") {
		@Override
		public Date computeDate(Date beginDate, Date endDate, OfferDateUnit unit, int value) {
			return unit.after(endDate, value);
		}
	};

	private final String i18nSuffix;

	OfferDateRef(String i18nSuffix) {
		this.i18nSuffix = i18nSuffix;
	}

	public String i18nSuffix() {
		return i18nSuffix;
	}

	public abstract Date computeDate(Date beginDate, Date endDate, OfferDateUnit unit, int value);

}
