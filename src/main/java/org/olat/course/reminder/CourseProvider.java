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
package org.olat.course.reminder;

import java.util.Collection;
import java.util.List;

import org.olat.course.reminder.rule.PassedRuleSPI;
import org.olat.modules.reminder.rule.DateRuleSPI;

/**
 * 
 * Initial date: 13 Aug 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseProvider implements CourseNodeReminderProvider {
	
	private final static CourseProvider COURSE_REMINDER_PROVIDER = new CourseProvider();
	
	public static final CourseProvider create() {
		return COURSE_REMINDER_PROVIDER;
	}
	
	private CourseProvider() {
		//
	}
	
	@Override
	public String getCourseNodeIdent() {
		return null;
	}
	
	@Override
	public boolean filter(Collection<String> nodeIdents, Collection<String> ruleTypes) {
		return true;
	}
	
	@Override
	public Collection<String> getMainRuleSPITypes() {
		return null;
	}

	@Override
	public String getDefaultMainRuleSPIType(List<String> availableRuleTypes) {
		if (availableRuleTypes.contains(PassedRuleSPI.class.getSimpleName())) {
			return DateRuleSPI.class.getSimpleName();
		}
		return null;
	}

	@Override
	public void refresh() {
		//
	}

}
