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
package org.olat.course.nodeaccess;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.nodes.INode;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.CoursePaginationController;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface NodeAccessProvider extends NodeAccessProviderIdentifier {

	public boolean isSupported(String courseNodeType);

	public boolean isGuestSupported();

	public boolean isConditionExpressionSupported();
	
	public boolean isScoreCalculatorSupported();
	
	public boolean isEditPreviewSupported();

	public boolean isUpdateEvaluationOnPublish();

	public void updateConfigDefaults(CourseNode courseNode, boolean newNode, INode parent);

	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnv, CourseEditorTreeModel editorModel);

	public String getCourseTreeCssClass(CourseConfig courseConfig);

	public CoursePaginationController getCoursePaginationController(UserRequest ureq, WindowControl wControl);

	public CourseTreeModelBuilder getCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv);
	
	public NoAccessResolver getNoAccessResolver(UserCourseEnvironment userCourseEnv);

	public boolean onNodeVisited(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);

	public boolean isAssessmentConfirmationEnabled(CourseNode courseNode, UserCourseEnvironment userCourseEnv);

	public void onAssessmentConfirmed(CourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean confirmed);
	
	public void onScoreUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Float score,
			Boolean userVisibility);

	public void onPassedUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Boolean passed, Boolean userVisibility);

	public void onStatusUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			AssessmentEntryStatus status);

}
