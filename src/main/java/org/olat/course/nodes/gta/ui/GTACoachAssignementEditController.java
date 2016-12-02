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
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * 
 * Initial date: 27.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachAssignementEditController extends AbstractAssignmentEditController {
	
	public GTACoachAssignementEditController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, CourseEnvironment courseEnv, boolean readOnly) {
		super(ureq, wControl, gtaNode, null, courseEnv, readOnly);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
