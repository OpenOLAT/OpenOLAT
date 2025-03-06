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
package org.olat.modules.curriculum;

import java.util.Date;

import org.olat.core.util.DateUtils;

/**
 * 
 * Initial date: 4 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum AutomationUnit {
	
	DAYS,
	WEEKS,
	MONTHS,
	YEARS,
	SAME_DAY;
	
	public Date before(Date date, int val) {
		return after(date, -val);
	}
	
	public Date after(Date date, int val) {
		return switch(this) {
			case DAYS -> DateUtils.addDays(date, val);
			case WEEKS -> DateUtils.addWeeks(date, val);
			case MONTHS -> DateUtils.addMonth(date, val);
			case YEARS -> DateUtils.addYears(date, val);
			case SAME_DAY -> date;
		};
	}
}
