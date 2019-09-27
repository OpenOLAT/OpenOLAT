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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface NodeAccessService {
	
	public List<? extends NodeAccessProviderIdentifier> getNodeAccessProviderIdentifer();
	
	public boolean isSupported(NodeAccessType type, String courseNodeType);

	public boolean isSupported(NodeAccessType type, CourseNode courseNode);

	/**
	 * Creates the controller to edit the access configurations of the node.
	 * 
	 * @param ureq
	 * @param windowControl
	 * @param type
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @param editorModel 
	 * @return
	 */
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, NodeAccessType type,
			CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, CourseEditorTreeModel editorModel);

	/**
	 * Builder to build the TreeModel of the complete course run structure.
	 * The builded TreeModel holds only TreeNodes of the (sub) type CourseTreeNode
	 *
	 * @param userCourseEnv
	 * @return
	 */
	public CourseTreeModelBuilder getCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv);

	/**
	 * Hook after the completion and the run status is updated.
	 *
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @param completion
	 * @param status
	 * @param by
	 */
	public void onCompletionUpdate(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Double completion, AssessmentEntryStatus status, Role by);

}
