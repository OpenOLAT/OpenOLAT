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
package org.olat.course.reminder.rule;

import java.util.Calendar;
import java.util.Date;

import org.olat.modules.reminder.IdentitiesProviderRuleSPI;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;

/**
 * 
 * Initial date: 30 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractDueDateRuleSPI implements IdentitiesProviderRuleSPI {

	protected Date now() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	protected boolean isNear(Date dueDate, Date now, ReminderRuleImpl r) {
		int value = Integer.parseInt(r.getRightOperand());
		String unit = r.getRightUnit();
		return near(dueDate, now, value, LaunchUnit.valueOf(unit));
	}

	private boolean near(Date date, Date now, int distance, LaunchUnit unit) {
		double between = -1;
		switch(unit) {
			case day:
				between = daysBetween(now, date);
				break;
			case week:
				between = weeksBetween(now, date);
				break;
			case month:
				between = monthsBetween(now, date);
				break;
			case year:
				between = yearsBetween(now, date);
				break;
		}
		// 0.1 to let +- 2 hours to match
		return  between <= distance || between - 0.1 <= distance || between < 0.0;
	}

	private double daysBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d));
	}

	private double weeksBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 7d));
	}

	private double monthsBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 30d));
	}
	
	double yearsBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 365d));
	}

}