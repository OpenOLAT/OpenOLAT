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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.course.condition.additionalconditions;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.userview.UserCourseEnvironment;

public class AdditionalConditionManager {
	
	private AbstractAccessableCourseNode node ;
	private final IdentityEnvironment identityEnv;
	private Long courseId;
	
	public AdditionalConditionManager(AbstractAccessableCourseNode node, Long courseId, IdentityEnvironment identityEnv) {
		this.node = node;
		this.identityEnv = identityEnv;
		this.courseId = courseId;
	}
	
	public Boolean evaluateConditions(){
		boolean retVal = true;
		for(AdditionalCondition cond : node.getAdditionalConditions()) {
			cond.setNode(node);
			cond.setCourseId(courseId);
			retVal = cond.evaluate(identityEnv);
			//otherwise all users on this node can enter the course if one user had known the correct answer 
			if(!retVal) {
				break;
			}
		}
		return retVal;
	}
	
	
	/**
	 * used to get the gui element for the next condition that a user can influence or sees a more detailed error-message in
	 * call only if the evaluateCondtions() call answered with false or null
	 * @param ureq
	 * @param wControl
	 * @return null if either nothing is wrong or the user is unable to influence the condition in olat (and won't get a more detailed error-message) 
	 */
	public Controller nextUserInputController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		for(AdditionalCondition cond : node.getAdditionalConditions()){
			cond.setNode(node);
			cond.setCourseId(courseId);
			boolean retVal = cond.evaluate(identityEnv);
			if(!retVal) {
				Controller ctrl = cond.getUserInputController(ureq, wControl, userCourseEnv);
				CourseNodeConfiguration config = CourseNodeFactory.getInstance().getCourseNodeConfiguration(node.getType());
				return TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, userCourseEnv, node, config.getIconCSSClass());
			}
		}		
		
		return null;
	}
}
