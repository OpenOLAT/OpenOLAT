/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus;

import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.ui.committee.assignment.AssignmentsData.Spreading;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssignmentService {
	
	/**
	 * Assign all applications to all assignee
	 * @param application
	 * @param assigneeList
	 */
	public void assignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assigneeList,
			Identity doer, Translator translator);
	
	/**
	 * 
	 * @param position
	 * @param applications
	 * @param assigneeList
	 * @param maximumAssignments
	 * @param additionalAssignments
	 * @param spreading
	 * @param doer
	 * @param translator
	 */
	public void assignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assigneeList,
			Integer maximumAssignments, Integer additionalAssignments, Spreading spreading,
			Identity doer, Translator translator);
	
	/**
	 * Remove the following committee member's assignments from the pseicified list of applications.
	 * 
	 * @param position The position
	 * @param applications The applications to manage
	 * @param assigneeList A list of assignees to remove
	 * @param doer 
	 * @param translator
	 */
	public void removeAssignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assigneeList,
			Identity doer, Translator translator);
	
	public List<ApplicationAssignmentLight> getAssignments(PositionRef position);
	
	public List<Identity> getAssignees(ApplicationRef application);

}
