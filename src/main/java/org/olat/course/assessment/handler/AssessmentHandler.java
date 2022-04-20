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
package org.olat.course.assessment.handler;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeOverviewController;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeProvider;
import org.olat.course.run.scoring.AccountingEvaluators;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * The course nodes can implement some details individually. Have a look at the
 * CourseAssessmentService for the descriptions of the methods.
 * 
 * Initial date: 20 Aug 2019<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentHandler extends CourseNodeProvider {
	
	public AssessmentConfig getAssessmentConfig(RepositoryEntryRef courseEntry, CourseNode courseNode);
	
	/**
	 * This method has to be implemented if the AssessmentConfig.isEvaluationPersisted() return true.
	 *
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return
	 */
	public AssessmentEntry getAssessmentEntry(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);
	
	/**
	 * Provides some evaluators to calculate some values automatically.
	 *
	 * @param courseNode
	 * @param courseConfig
	 * @return
	 */
	public AccountingEvaluators getEvaluators(CourseNode courseNode, CourseConfig courseConfig);
	
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CourseNode courseNode, UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnvironment);
	
	public boolean hasCustomIdentityList();
	
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback, boolean showTitle);

	public default boolean hasCustomOverviewController() {
		return false;
	}

	@SuppressWarnings("unused")
	public default AssessmentCourseNodeOverviewController getCustomOverviewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, CourseNode courseNode,
			AssessmentToolSecurityCallback assessmentCallback, boolean courseInfoLaunch, boolean readOnly) {
		return null;
	}

}
