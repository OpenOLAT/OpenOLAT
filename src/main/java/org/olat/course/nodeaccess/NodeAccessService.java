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

import java.util.List;
import java.util.Locale;

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
public interface NodeAccessService {
	
	public List<? extends NodeAccessProviderIdentifier> getNodeAccessProviderIdentifer();
	
	public String getNodeAccessTypeName(NodeAccessType type, Locale locale);
	
	public boolean isSupported(NodeAccessType type, String courseNodeType);

	public boolean isSupported(NodeAccessType type, CourseNode courseNode);
	
	/**
	 * @param type
	 * @return if this type supports guest access
	 */
	public boolean isGuestSupported(NodeAccessType type);
	
	public boolean isConditionExpressionSupported(NodeAccessType type);
	
	public boolean isScoreCalculatorSupported(NodeAccessType type);

	/**
	 * Returns whether the evaluation after the course has published has to update
	 * the assessment entries or not.
	 *
	 * @param type
	 * @return
	 */
	public boolean isUpdateEvaluationOnPublish(NodeAccessType type);
	
	/**
	 * Update configs if a node is created or updated.
	 *
	 * @param type
	 * @param courseNode
	 * @param newNode
	 * @param parent
	 */
	public void updateConfigDefaults(NodeAccessType type, CourseNode courseNode, boolean newNode, INode parent);

	/**
	 * Creates the controller to edit the access configurations of the node.
	 * 
	 * @param ureq
	 * @param windowControl
	 * @param type
	 * @param courseNode
	 * @param userCourseEnv
	 * @param editorModel 
	 * @return
	 */
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, NodeAccessType type,
			CourseNode courseNode, UserCourseEnvironment userCourseEnv, CourseEditorTreeModel editorModel);

	/**
	 * CSS class to inject in the course menu tree.
	 *
	 * @param courseConfig
	 * @return
	 */
	public String getCourseTreeCssClass(CourseConfig courseConfig);

	/**
	 * Controller to navigate to the next and the previous course node.
	 *
	 * @param ureq
	 * @param wControl
	 * @param type 
	 * @return
	 */
	public CoursePaginationController getCoursePaginationController(UserRequest ureq, WindowControl wControl, NodeAccessType type);
	
	/**
	 * Builder to build the TreeModel of the complete course run structure.
	 * The builded TreeModel holds only TreeNodes of the (sub) type CourseTreeNode
	 *
	 * @param userCourseEnv
	 * @return
	 */
	public CourseTreeModelBuilder getCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv);
	
	public NoAccessResolver getNoAccessResolver(UserCourseEnvironment userCourseEnv);
	
	/**
	 * Returns if a user can confirm the execution of an assessment.
	 *
	 * @param courseNode
	 * @param userCourseEnv
	 * @return
	 */
	public boolean isAssessmentConfirmationEnabled(CourseNode courseNode, UserCourseEnvironment userCourseEnv);

	/**
	 * Hook after the participant has confirmed the execution of a course node.
	 * 
	 * @param courseNode
	 * @param userCourseEnv
	 * @param confirmed 
	 */
	public void onAssessmentConfirmed(CourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean confirmed);

	/**
	 * Hook after the update of the score value of an assessment changed.
	 *
	 * @param courseNode
	 * @param userCourseEnv
	 * @param score
	 * @param userVisibility
	 */
	public void onScoreUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Float score,
			Boolean userVisibility);
	
	/**
	 * Hook after the update of the passed value of an assessment changed.
	 *
	 * @param courseNode
	 * @param userCourseEnv
	 * @param passed 
	 * @param userVisibility 
	 */
	public void onPassedUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Boolean passed,
			Boolean userVisibility);

	/**
	 * Hook after the assessment status is updated.
	 *
	 * @param courseNode
	 * @param userCourseEnv
	 * @param status
	 */
	public void onStatusUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			AssessmentEntryStatus status);
	
}
