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
package org.olat.modules.reminder.rule;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.modules.reminder.FilterRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.CourseLaunchRuleEditor;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractLaunchDateRuleSPI  implements FilterRuleSPI {

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new CourseLaunchRuleEditor(rule, this.getClass().getSimpleName());
	}
	
	protected abstract Map<Long,Date> getLaunchDates(ReminderRule rule, RepositoryEntry entry, List<Identity> identities);

	@Override
	public void filter(RepositoryEntry entry, List<Identity> identities, ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			
			Date now = new Date();
			int distance = Integer.parseInt(r.getRightOperand());
			LaunchUnit unit = LaunchUnit.valueOf(r.getRightUnit());

			Map<Long,Date> initialLaunchDates = getLaunchDates(rule, entry, identities);

			for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				Identity identity = identityIt.next();
				Date initialLaunchDate = initialLaunchDates.get(identity.getKey());
				if(initialLaunchDate == null) {
					identityIt.remove();
				} else if(!after(initialLaunchDate, now, distance, unit)) {
					identityIt.remove();
				}	
			}
		}
	}
	
	private boolean after(Date date, Date now, int distance, LaunchUnit unit) {
		double between = -1;
		switch(unit) {
			case day:
				between = daysBetween(date, now);
				break;
			case week:
				between = weeksBetween(date, now);
				break;
			case month:
				between = monthsBetween(date, now);
				break;
			case year:
				between = yearsBetween(date, now);
				break;
		}
		return between > distance;
	}
	
	public double daysBetween(Date d1, Date d2) {
        return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d));
	}
	
	public double weeksBetween(Date d1, Date d2) {
        return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 7d));
	}
	
	public double monthsBetween(Date d1, Date d2) {
        return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 30d));
	}
	
	public double yearsBetween(Date d1, Date d2) {
        return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 365d));
	}
}