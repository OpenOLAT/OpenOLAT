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
package org.olat.course.nodes.survey;

import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;

/**
 * 
 * Initial date: 02.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyRunSecurityCallback {
	
	private final boolean admin;
	private final boolean courseReadOnly;
	private final boolean guestOnly;
	private final boolean executor;
	private final boolean reportViewer;
	
	public SurveyRunSecurityCallback(ModuleConfiguration moduleConfiguration, UserCourseEnvironment userCourseEnv) {
		this.courseReadOnly = userCourseEnv.isCourseReadOnly();
		this.guestOnly = userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly();
		this.executor = hasExecutionRole(moduleConfiguration, userCourseEnv);
		this.reportViewer = hasReportRole(moduleConfiguration, userCourseEnv);
		this.admin = userCourseEnv.isAdmin();
	}

	public boolean isGuestOnly() {
		return guestOnly;
	}

	public boolean isExecutor() {
		return executor;
	}

	public boolean isReportViewer() {
		return reportViewer;
	}

	private boolean hasExecutionRole(ModuleConfiguration moduleConfiguration, UserCourseEnvironment userCourseEnv) {
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_EXECUTION_BY_GUEST)
				&& guestOnly) {
			return true;
		}
		
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_EXECUTION_BY_PARTICIPANT)
				&& userCourseEnv.isParticipant()) {
			return true;
		}
		
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_EXECUTION_BY_COACH)
				&& userCourseEnv.isCoach()) {
			return true;
		}
		
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_EXECUTION_BY_OWNER)
				&& userCourseEnv.isAdmin()) {
			return true;
		}
		
		return false;
	}
	
	private boolean hasReportRole(ModuleConfiguration moduleConfiguration, UserCourseEnvironment userCourseEnv) {
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_REPORT_FOR_GUEST)
				&& guestOnly) {
			return true;
		}
		
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_REPORT_FOR_PARTICIPANT)
				&& userCourseEnv.isParticipant()) {
			return true;
		}
		
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_REPORT_FOR_COACH)
				&& userCourseEnv.isCoach()) {
			return true;
		}
		
		if (moduleConfiguration.getBooleanSafe(SurveyCourseNode.CONFIG_KEY_REPORT_FOR_OWNER)
				&& userCourseEnv.isAdmin()) {
			return true;
		}
		
		return false;
	}
	
	public boolean canParticipate() {
		return isExecutor() && !courseReadOnly;
	}
	
	public boolean hasParticipated(EvaluationFormParticipation participation) {
		return participation != null && EvaluationFormParticipationStatus.done.equals(participation.getStatus());
	}

	public boolean canExecute(EvaluationFormParticipation participation) {
		return participation != null && isExecutor() && !hasParticipated(participation);
	}
	
	public boolean canViewReporting(EvaluationFormParticipation participation) {
		if (isReportViewer()) {
			if (!isExecutor()) {
				return true;
			}
			if (hasParticipated(participation)) {
				return true;
			}
			if (isReadOnly()) {
				return true;
			}
		}
		return false;
	}

	public boolean isReadOnly() {
		return courseReadOnly && isExecutor();
	}

	public boolean canResetAll() {
		return !courseReadOnly && admin;
	}

}
