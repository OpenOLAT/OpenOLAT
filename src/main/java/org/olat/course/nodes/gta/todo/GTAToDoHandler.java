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
package org.olat.course.nodes.gta.todo;

import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.duedate.DueDateService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.todo.CourseNodeToDoHandler;
import org.olat.course.todo.CourseNodeToDoSyncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTAToDoHandler implements CourseNodeToDoHandler {
	
	public static final Set<String> TO_DO_TASK_TYPES = Set.of(GTAAssignmentToDoProvider.TYPE, GTASubmitToDoProvider.TYPE, GTARevisionToDoProvider.TYPE);
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private DueDateService dueDateService;
	@Autowired
	private I18nManager i18nManager;

	@Override
	public String acceptCourseNodeType() {
		return GTACourseNode.TYPE_INDIVIDUAL;
	}

	@Override
	public Set<String> getToDoTaskTypes() {
		return TO_DO_TASK_TYPES;
	}
	
	@Override
	public CourseNodeToDoSyncher getCourseNodeToDoSyncher(CourseNode courseNode, Set<Identity> identities) {
		return new GTAToDoSyncher(gtaManager, dueDateService, i18nManager, identities);
	}



}
