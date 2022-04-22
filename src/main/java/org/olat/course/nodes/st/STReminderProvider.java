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
package org.olat.course.nodes.st;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.STAssessmentConfig;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.reminder.rule.LearningProgressRuleSPI;
import org.olat.modules.reminder.rule.BeforeDateRuleSPI;
import org.olat.modules.reminder.rule.BusinessGroupRoleRuleSPI;
import org.olat.modules.reminder.rule.CourseEnrollmentDateRuleSPI;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.modules.reminder.rule.InitialCourseLaunchRuleSPI;
import org.olat.modules.reminder.rule.RecentCourseLaunchRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryLifecycleAfterValidFromRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryLifecycleAfterValidToRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryRoleRuleSPI;
import org.olat.modules.reminder.rule.UserPropertyRuleSPI;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 10 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STReminderProvider implements CourseNodeReminderProvider {

	private final String nodeIdent;
	private final AssessmentReminderProvider assessmentReminderProvider;
	private List<String> mainTypes;
	
	public STReminderProvider(RepositoryEntryRef courseEntry, STCourseNode courseNode) {
		this.nodeIdent = courseNode.getIdent();
		this.assessmentReminderProvider = new AssessmentReminderProvider(nodeIdent,
				new STAssessmentConfig(courseEntry, courseNode, true, courseNode.getModuleConfiguration()));
	}

	@Override
	public String getCourseNodeIdent() {
		return nodeIdent;
	}

	@Override
	public boolean filter(Collection<String> ruleNodeIdents) {
		return ruleNodeIdents.contains(nodeIdent) || ruleNodeIdents.isEmpty();
	}

	@Override
	public Collection<String> getMainRuleSPITypes() {
		if (mainTypes == null) {
			mainTypes = new ArrayList<>();
			mainTypes.addAll(assessmentReminderProvider.getMainRuleSPITypes());
			mainTypes.add(BeforeDateRuleSPI.class.getSimpleName());
			mainTypes.add(DateRuleSPI.class.getSimpleName());
			mainTypes.add(UserPropertyRuleSPI.class.getSimpleName());
			mainTypes.add(CourseEnrollmentDateRuleSPI.class.getSimpleName());
			mainTypes.add(InitialCourseLaunchRuleSPI.class.getSimpleName());
			mainTypes.add(RecentCourseLaunchRuleSPI.class.getSimpleName());
			mainTypes.add(LearningProgressRuleSPI.class.getSimpleName());
			mainTypes.add(BusinessGroupRoleRuleSPI.class.getSimpleName());
			mainTypes.add(RepositoryEntryRoleRuleSPI.class.getSimpleName());
			mainTypes.add(RepositoryEntryLifecycleAfterValidFromRuleSPI.class.getSimpleName());
			mainTypes.add(RepositoryEntryLifecycleAfterValidToRuleSPI.class.getSimpleName());
		}
		
		return mainTypes;
	}

	@Override
	public String getDefaultMainRuleSPIType(List<String> availableRuleTypes) {
		String detaultType = assessmentReminderProvider.getDefaultMainRuleSPIType(availableRuleTypes);
		return detaultType != null? detaultType: DateRuleSPI.class.getSimpleName();
	}
	
	@Override
	public void refresh() {
		assessmentReminderProvider.refresh();
		mainTypes = null;
	}

}
