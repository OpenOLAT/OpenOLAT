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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodeaccess.ui.NodeAccessSettingsController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LectureRepositorySettingsController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.settings.RepositoryEntryLifecycleController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseExecutionSettingsController extends BasicController {
	
	private RepositoryEntryLifecycleController lifecycleCtrl;
	private LectureRepositorySettingsController lectureSettingsCtrl;
	private NodeAccessSettingsController nodeAccessSettingsCtrl;
	
	@Autowired
	private LectureModule lectureModule;
	
	public CourseExecutionSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("execution_settings");
		
		lifecycleCtrl = new RepositoryEntryLifecycleController(ureq, getWindowControl(), entry, readOnly);
		listenTo(lifecycleCtrl);
		mainVC.put("lifecycle", lifecycleCtrl.getInitialComponent());
		
		if(lectureModule.isEnabled()) {
			lectureSettingsCtrl = new LectureRepositorySettingsController(ureq, getWindowControl(), entry, readOnly);
			listenTo(lectureSettingsCtrl);
			mainVC.put("lectures", lectureSettingsCtrl.getInitialComponent());
		}
		
		nodeAccessSettingsCtrl = new NodeAccessSettingsController(ureq, wControl, entry, readOnly);
		listenTo(nodeAccessSettingsCtrl);
		mainVC.put("nodeAccess", nodeAccessSettingsCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}
}
