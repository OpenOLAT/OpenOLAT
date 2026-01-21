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
package org.olat.modules.coach.ui.dashboard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dashboard.BentoBoxSize;
import org.olat.core.gui.control.generic.dashboard.DashbordController;
import org.olat.modules.coach.model.CoachingSecurity;

/**
 * 
 * Initial date: Oct 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CoachDashboardController extends BasicController {

	private DashbordController dashbordCtrl;
	private CourseWidgetController courseCoachCtrl;
	private CoachLectureBlocksWidgetController lectureBlocksCtrl;

	public CoachDashboardController(UserRequest ureq, WindowControl wControl, CoachingSecurity coachingSec) {
		super(ureq, wControl);
		
		dashbordCtrl = new DashbordController(ureq, wControl);
		listenTo(dashbordCtrl);
		putInitialPanel(dashbordCtrl.getInitialComponent());
		
		if (coachingSec.coach()) {
			courseCoachCtrl = new CourseWidgetController(ureq, wControl);
			listenTo(courseCoachCtrl);
			dashbordCtrl.addWidget("courseCoach", courseCoachCtrl, BentoBoxSize.box_4_1);
			
			lectureBlocksCtrl = new CoachLectureBlocksWidgetController(ureq, wControl);
			listenTo(lectureBlocksCtrl);
			lectureBlocksCtrl.reload();
			dashbordCtrl.addWidget("lectureBlocks", lectureBlocksCtrl, BentoBoxSize.box_4_1);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public void reload() {
		if (courseCoachCtrl != null) {
			courseCoachCtrl.reload();
		}
		if (lectureBlocksCtrl != null) {
			lectureBlocksCtrl.reload();
		}
	}

}
