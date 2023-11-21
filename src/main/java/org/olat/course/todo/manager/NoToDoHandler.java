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
package org.olat.course.todo.manager;

import java.util.Locale;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.todo.CourseNodeToDoHandler;
import org.olat.course.todo.CourseNodeToDoSyncher;
import org.olat.course.todo.CourseToDoEnvironment;
import org.olat.modules.todo.ui.ToDoTaskRowGrouping;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class NoToDoHandler implements CourseNodeToDoHandler {
	
	private static final NoToDoSyncher NO_TO_DO_SYNCHER = new NoToDoSyncher();

	@Override
	public String acceptCourseNodeType() {
		return null;
	}
	
	@Override
	public Set<String> getToDoTaskTypes() {
		return Set.of();
	}

	@Override
	public CourseNodeToDoSyncher getCourseNodeToDoSyncher(CourseNode courseNode, Set<Identity> identities) {
		return NO_TO_DO_SYNCHER;
	}

	@Override
	public ToDoTaskRowGrouping getToDoTaskRowGrouping(Locale locale, CourseNode courseNode) {
		return ToDoTaskRowGrouping.NO_GROUPING;
	}
	
	private static final class NoToDoSyncher implements CourseNodeToDoSyncher {
		
		@Override
		public void reset() {
			//
		}
		
		@Override
		public void synch(CourseNode courseNode, UserCourseEnvironment userCourseEnv, CourseToDoEnvironment toDoEnv) {
			//
		}

	}

}
