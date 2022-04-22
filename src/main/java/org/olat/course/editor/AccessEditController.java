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
package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.condition.additionalconditions.AdditionalCondition;
import org.olat.course.condition.additionalconditions.PasswordCondition;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * 
 * Initial date: 10 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessEditController extends BasicController {
	
	private final VelocityContainer mainVc;
	private ConditionEditController accessConditionCtrl;
	private Controller passwordConditionEditController;

	private final AbstractAccessableCourseNode courseNode;

	public AccessEditController(UserRequest ureq, WindowControl wControl, AbstractAccessableCourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment, CourseEditorTreeModel editorModel,
			ConditionAccessEditConfig accessEditConfig) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		mainVc = createVelocityContainer("accessedit");

		// access
		Condition accessCondition = courseNode.getPreConditionAccess();
		accessConditionCtrl = new ConditionEditController(ureq, wControl, userCourseEnvironment, accessCondition,
				AssessmentHelper.getAssessableNodes(new CourseEntryRef(userCourseEnvironment), editorModel, courseNode));
		listenTo(accessConditionCtrl);
		mainVc.put("accessCondition", accessConditionCtrl.getInitialComponent());
		
		// password
		CourseEditorEnv courseEnv = userCourseEnvironment.getCourseEditorEnv();
		boolean isRootNode = courseEnv.getRootNodeId().equals(courseNode.getIdent());
		if(accessEditConfig.isShowPassword()){
			PasswordCondition passwordCondition = null;
			for(AdditionalCondition addCond : courseNode.getAdditionalConditions()){
				if(addCond instanceof PasswordCondition){
					passwordCondition = (PasswordCondition) addCond;
				}
			}
			if ((passwordCondition == null) && (!isRootNode)) {
				passwordCondition = new PasswordCondition();
				courseNode.getAdditionalConditions().add(passwordCondition);
			}
			if ((passwordCondition != null) && (isRootNode)) {
				String pass = passwordCondition.getPassword();
				if ((pass == null) || (pass.length() == 0)) {
					courseNode.getAdditionalConditions().remove(passwordCondition);
					passwordCondition = null;
				}
			}
			if (passwordCondition != null) {
				passwordConditionEditController = passwordCondition.getEditorComponent(ureq, wControl);
				listenTo(passwordConditionEditController);
			}
			if(passwordConditionEditController != null) {
				mainVc.put("passwordCondition", passwordConditionEditController.getInitialComponent());
				mainVc.contextPut("renderPW", true);
			}
		}

		putInitialPanel(mainVc);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessConditionCtrl && event == Event.CHANGED_EVENT) {
			Condition cond = accessConditionCtrl.getCondition();
			courseNode.setPreConditionAccess(cond);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if (source == passwordConditionEditController  && event == Event.CHANGED_EVENT){
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
