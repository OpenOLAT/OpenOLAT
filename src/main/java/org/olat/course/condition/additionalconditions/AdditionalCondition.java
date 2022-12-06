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
import org.olat.course.condition.Condition;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

public abstract class AdditionalCondition extends Condition {

	private static final long serialVersionUID = -65033606683343812L;
	
	protected AbstractAccessableCourseNode node;
	protected Long courseId;
	
	// The field answers must not be used, it's only there for XStream's compatibility purpose
	@Deprecated
	protected AdditionalConditionAnswerContainer answers;
	
	/**
	 * 
	 * @return the controller that is used to input the condition
	 */
	public abstract Controller getEditorComponent(UserRequest ureq, WindowControl wControl);
	
	/**
	 * use this Method to evaluate the additional condition
	 * could internally call a controller / gui-input-element / db-check / ws-request / whatever is needed to evaluate this condition  
	 * @return NULL if something went wrong, true if the condition was met or false if it hadn't been fulfilled
	 * 
	 */
	public abstract boolean evaluate(Object userObject);
	
	/**
	 * used to get an optional controller if this additionalcondition could be fulfilled with userinteraction
	 * @param ureq
	 * @param wControl
	 * @return the controller, null if this condition could not be fulfilled by the user and no specific message i.e webservice not reachable should be shown
	 */
	public abstract Controller getUserInputController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv);
	
	public String getNodeIdentifier() {
		return node != null ? node.getIdent() : null;
	}
	
	public Long getCourseId() {
		return courseId;
	}
	
	public void setAnswers(AdditionalConditionAnswerContainer answers) {
		this.answers = answers;
	}
	
	protected void setNode(AbstractAccessableCourseNode node) {
		this.node = node;
	}
	
	protected void setCourseId(Long courseId) {
		this.courseId = courseId;
	}
}