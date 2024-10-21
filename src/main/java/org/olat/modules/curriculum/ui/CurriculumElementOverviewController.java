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
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.widgets.CoursesWidgetController;

/**
 * 
 * Initial date: 16 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementOverviewController extends BasicController {
	
	private CoursesWidgetController coursesCtrl;
	
	public CurriculumElementOverviewController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl);

		List<String> widgets = new ArrayList<>();
		VelocityContainer mainVC = createVelocityContainer("curriculum_element_overview");
		if(curriculumElement.getParent() != null) {
			coursesCtrl = new CoursesWidgetController(ureq, getWindowControl(), curriculumElement);
			listenTo(coursesCtrl);
			mainVC.put("courses", coursesCtrl.getInitialComponent());
			widgets.add("courses");
		}
		
		mainVC.contextPut("widgets", widgets);
		putInitialPanel(mainVC);
	}
	

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(coursesCtrl == source) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
