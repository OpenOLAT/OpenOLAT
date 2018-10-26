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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.ui.tool.tools.AbstractToolsController;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAIdentityListCourseNodeToolsController extends AbstractToolsController {
	
	@Autowired
	private GTAManager gtaManager;
	
	public GTAIdentityListCourseNodeToolsController(UserRequest ureq, WindowControl wControl,
			AssessableCourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, courseNode, assessedIdentity, coachCourseEnv);
		
		initTools();
	}
	
	

	@Override
	protected void reopenEvaluation() {
		super.reopenEvaluation();

		TaskList taskList = gtaManager.getTaskList(getCourseRepositoryEntry(), (GTACourseNode)courseNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null && task.getTaskStatus() == TaskProcess.graded) {
			gtaManager.updateTask(task, TaskProcess.grading, (GTACourseNode)courseNode, Role.coach);
		}
	}

	@Override
	protected void doneEvalution() {
		super.doneEvalution();
		
		TaskList taskList = gtaManager.getTaskList(getCourseRepositoryEntry(), (GTACourseNode)courseNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null) {
			gtaManager.updateTask(task, TaskProcess.graded, (GTACourseNode)courseNode, Role.coach);
		}
	}
}
