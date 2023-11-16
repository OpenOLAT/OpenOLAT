/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.todo;

import java.util.Date;

import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: 1 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CourseToDoEnvironment {
	
	public void reset();

	public ToDoTask getToDoTask(UserCourseEnvironment userCourseEnv, CourseNode courseNode, String toDoTaskType);

	public ToDoTask createToDoTask(UserCourseEnvironment userCourseEnv, CourseNode courseNode, String toDoTaskType);
	
	public void updateToDoTask(ToDoTask toDoTask, String title, String description, ToDoStatus status, Date dueDate,
			String originTitle, String originSubTitle);

	public void updateOriginDeleted(ToDoTask toDoTask, boolean deleted);
	
	public Date getCourseLaunchDate(UserCourseEnvironment userCourseEnv);

	public boolean isCourseParticipantMember(UserCourseEnvironment userCourseEnv);

}
