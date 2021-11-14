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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentIdentiesPrintController extends BasicController {
	
	private VelocityContainer mainVC;
	
	@Autowired
	private BaseSecurityManager securityManager;

	public AssessmentIdentiesPrintController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			UserCourseEnvironment coachCourseEnv, List<Long> assessesIdentityKeys) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("identities_print");
		
		List<Identity> assessedIdentities = securityManager.loadIdentityByKeys(assessesIdentityKeys);
		List<String> names = new ArrayList<>(assessedIdentities.size());
		int counter = 0;
		for (Identity assessedIdentity : assessedIdentities) {
			AssessmentIdentityCourseController controller = new AssessmentIdentityCourseController(ureq, wControl,
					null, courseEntry, coachCourseEnv, assessedIdentity, false);
			listenTo(controller);
			String name = "ass" + counter++;
			mainVC.put(name, controller.getInitialComponent());
			names.add(name);
		}
		mainVC.contextPut("names", names);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
