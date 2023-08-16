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
package org.olat.course.learningpath.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentityController extends BasicController implements TooledController {

	private CoachedIdentityLargeInfosController coachedIdentityLargeInfosCtrl;
	private LearningPathListController learningPathListCtrl;

	public LearningPathIdentityController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			UserCourseEnvironment userCourseEnv, Identity coachedIdentity) {
		super(ureq, wControl);

		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		VelocityContainer mainVC = createVelocityContainer("identity");
		mainVC.contextPut("courseTitle", courseEnv.getCourseTitle());
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(coachedIdentity);
		UserCourseEnvironmentImpl coachedCourseEnv = new UserCourseEnvironmentImpl(identityEnv, courseEnv);
		// Viewed identity must act as participant only, to get the right assigned / excluded course nodes.
		coachedCourseEnv.setUserRoles(false, false, true);
		
		coachedIdentityLargeInfosCtrl = new CoachedIdentityLargeInfosController(ureq, wControl, coachedCourseEnv);
		listenTo(coachedIdentityLargeInfosCtrl);
		mainVC.put("user", coachedIdentityLargeInfosCtrl.getInitialComponent());

		learningPathListCtrl = new LearningPathListController(ureq, wControl, stackPanel, coachedCourseEnv, getCanEdit(userCourseEnv), getCanReset(userCourseEnv));
		listenTo(learningPathListCtrl);
		mainVC.put("list", learningPathListCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	private boolean getCanEdit(UserCourseEnvironment myCourseEnv) {
		return !myCourseEnv.isCourseReadOnly() && (myCourseEnv.isAdmin() || myCourseEnv.isCoach());
	}
	
	private boolean getCanReset(UserCourseEnvironment myCourseEnv) {
		return !myCourseEnv.isCourseReadOnly() && (myCourseEnv.isAdmin()
				|| (myCourseEnv.isCoach() && myCourseEnv.getCourseEnvironment().getRunStructure().getRootNode()
						.getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_RESET_DATA)));
	}

	@Override
	public void initTools() {
		learningPathListCtrl.initTools();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
