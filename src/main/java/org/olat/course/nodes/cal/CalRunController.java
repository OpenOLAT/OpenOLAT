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

package org.olat.course.nodes.cal;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * <h3>Description:</h3>
 * Run controller for calendar
 * <p>
 * Initial Date:  4 nov. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CalRunController extends BasicController implements Activateable2 {

	private final CourseCalendarController calCtr;
	private final ModuleConfiguration config;
	
	public CalRunController(WindowControl wControl, UserRequest ureq, CalCourseNode calCourseNode,
			UserCourseEnvironment courseEnv, CalSecurityCallback secCallback) {
		super(ureq, wControl);
		config = calCourseNode.getModuleConfiguration();
		VelocityContainer mainVC = createVelocityContainer("run");

		CourseCalendars myCal = CourseCalendars.createCourseCalendarsWrapper(ureq, wControl, courseEnv, secCallback);
		calCtr = new CourseCalendarController(ureq, wControl, myCal, courseEnv, secCallback);
		listenTo(calCtr);
		
		Date startDate = null;
		if(CalEditController.getAutoDate(config)) {
			startDate = new Date();
		} else {
			startDate = CalEditController.getStartDate(config);
			if(startDate == null) startDate = new Date();
		}
		calCtr.setFocus(startDate);
		mainVC.put("cal", calCtr.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		String eventId = BusinessControlFactory.getInstance().getPath(ce);
		if(StringHelper.containsNonWhitespace(eventId)) {
			calCtr.setFocusOnEvent(eventId, null);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//no events yet
	}

	@Override
	protected void doDispose() {
		if(calCtr != null){
			calCtr.dispose();
		}
        super.doDispose();
	}
}