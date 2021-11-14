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
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * 
 * Initial date: 4 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VisibilityEditController extends BasicController {

	private final VelocityContainer mainVc;
	private ConditionEditController visibilityConditionCtrl;
	private NoAccessExplEditController noAccessContr;

	private final CourseNode courseNode;

	public VisibilityEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment, CourseEditorTreeModel editorModel) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		mainVc = createVelocityContainer("visibilityedit");
		
		Condition visibilityCondition = courseNode.getPreConditionVisibility();
		visibilityConditionCtrl = new ConditionEditController(ureq, getWindowControl(), userCourseEnvironment, visibilityCondition,
				AssessmentHelper.getAssessableNodes(editorModel, courseNode));
		listenTo(visibilityConditionCtrl);
		mainVc.put("visibilityCondition", visibilityConditionCtrl.getInitialComponent());
		
		String noAccessExplanation = courseNode.getNoAccessExplanation();
		noAccessContr = new NoAccessExplEditController(ureq, getWindowControl(), noAccessExplanation);
		listenTo(noAccessContr);
		mainVc.put("noAccessExplanationComp", noAccessContr.getInitialComponent());
		
		putInitialPanel(mainVc);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == visibilityConditionCtrl && event == Event.CHANGED_EVENT) {
			Condition cond = visibilityConditionCtrl.getCondition();
			courseNode.setPreConditionVisibility(cond);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if (source == noAccessContr && event == Event.CHANGED_EVENT) {
			String noAccessExplanation = noAccessContr.getNoAccessExplanation();
			courseNode.setNoAccessExplanation(noAccessExplanation);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
