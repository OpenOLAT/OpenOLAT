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
package org.olat.modules.assessment.ui.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 10.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentFormEvent extends Event {

	private static final long serialVersionUID = -5578626308503719045L;
	
	public static final String ASSESSMENT_DONE = "assessment-entry-done";
	public static final String ASSESSMENT_CHANGED = "assessment-entry-CHANGED";
	public static final String ASSESSMENT_EVAL_CHANGED = "assessment-entry-evaluation-changed";
	public static final String ASSESSMENT_REOPEN = "assessment-entry-reopened";

	private boolean close;
	
	public AssessmentFormEvent(String cmd, boolean close) {
		super(cmd);
		this.close = close;
	}
	
	public boolean isClose() {
		return close;
	}
	
	public AssessmentFormEvent cloneNotClose() {
		AssessmentFormEvent afe = new AssessmentFormEvent(getCommand(), false);
		
		return afe;
	}

}
