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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.ui.widgets.LectureBlocksWidgetController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumOverviewController extends BasicController {

	private LectureBlocksWidgetController lectureBlocksCtrl;
	
	@Autowired
	private LectureModule lectureModule;
	
	public CurriculumOverviewController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, LecturesSecurityCallback lecturesSecCallback) {
		super(ureq, wControl);

		List<String> widgets = new ArrayList<>();
		VelocityContainer mainVC = createVelocityContainer("curriculum_overview");
		
		if(lectureModule.isEnabled()) {
			lectureBlocksCtrl = new LectureBlocksWidgetController(ureq, getWindowControl(), curriculum, lecturesSecCallback);
			listenTo(lectureBlocksCtrl);
			mainVC.put("lectures", lectureBlocksCtrl.getInitialComponent());
			widgets.add("lectures");
		}

		mainVC.contextPut("widgets", widgets);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(lectureBlocksCtrl == source) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
