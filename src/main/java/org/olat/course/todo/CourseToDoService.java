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

import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 19 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CourseToDoService {

	public CourseNodeToDoHandler getToDoHandler(CourseNode courseNode);

	public CourseNodesToDoSyncher getCourseNodesToDoSyncher(CourseEnvironment courseEnv, Set<Identity> identities);

	public void deleteToDoTasks(ICourse course, CourseNode courseNode);

	public void resetToDoTasks(UserCourseEnvironment userCourseEnv, CourseNode courseNode);

	public void updateOriginTitle(RepositoryEntry courseEntry);

	public void updateOriginDeletedFalse(RepositoryEntry courseEntry, IdentityRef identity);

}
