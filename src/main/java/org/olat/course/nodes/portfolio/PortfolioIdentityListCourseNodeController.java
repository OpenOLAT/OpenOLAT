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
package org.olat.course.nodes.portfolio;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioIdentityListCourseNodeController extends IdentityListCourseNodeController {

	public PortfolioIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, RepositoryEntry courseEntry, CourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, stackPanel, courseEntry, courseNode, coachCourseEnv, toolContainer, assessmentCallback,
				showTitle);
	}
	
	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		super.initGradeScaleEditButton(formLayout);
		super.initMultiSelectionTools(ureq, formLayout);
	}

}
