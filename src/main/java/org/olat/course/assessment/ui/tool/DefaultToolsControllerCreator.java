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
package org.olat.course.assessment.ui.tool;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentToolOptions;

/**
 * 
 * Default implementation without any tools.
 * 
 * Initial date: 4 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultToolsControllerCreator implements ToolsControllerCreator {

	@Override
	public boolean hasCalloutTools() {
		return false;
	}

	@Override
	public Controller createCalloutController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, Identity assessedIdentity) {
		return null;
	}

	@Override
	public List<Controller> createAssessmentTools(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, UserCourseEnvironment coachCourseEnv, AssessmentToolOptions options) {
		return null;
	}

	@Override
	public List<Controller> createMultiSelectionTools(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, IdentityListCourseNodeProvider provider) {
		return Collections.emptyList();
	}
}
