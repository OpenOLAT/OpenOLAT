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
package org.olat.modules.quality.generator.provider.course;

import java.util.Date;
import java.util.function.Predicate;

import org.olat.modules.quality.generator.ProviderHelper;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class PlusDurationIsInPast implements Predicate<RepositoryEntry> {
	
	private final boolean beginTrigger;
	private final boolean endTrigger;
	private final int dueDateDays;
	private final int durationHours;
	private final Date toDate;
	
	PlusDurationIsInPast(QualityGeneratorConfigs configs, Date toDate) {
		String trigger = configs.getValue(CourseProvider.CONFIG_KEY_TRIGGER);
		this.beginTrigger = CourseProvider.CONFIG_KEY_TRIGGER_BEGIN.equals(trigger);
		this.endTrigger = CourseProvider.CONFIG_KEY_TRIGGER_END.equals(trigger);
		this.dueDateDays = ProviderHelper.toIntOrZero(configs.getValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS));
		this.durationHours = ProviderHelper.toIntOrZero(configs.getValue(CourseProvider.CONFIG_KEY_DURATION_HOURS));
		this.toDate = toDate;
	}

	@Override
	public boolean test(RepositoryEntry re) {
		Date dcStart = null;
		Date dcEnd = null;
		if (beginTrigger && re.getLifecycle() != null && re.getLifecycle().getValidFrom() != null) {
			dcStart = ProviderHelper.addDays(re.getLifecycle().getValidFrom(), dueDateDays);
		} else if (endTrigger && re.getLifecycle() != null && re.getLifecycle().getValidTo() != null) {
			dcStart = ProviderHelper.addDays(re.getLifecycle().getValidTo(), dueDateDays);
		}
		
		if (dcStart != null) {
			dcEnd = ProviderHelper.addHours(dcStart, durationHours);
		}
		return dcEnd != null? dcEnd.before(toDate): false;
	}

}
