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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;

/**
 * 
 * Initial date: 30.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAGroupAssessmentToolController extends BasicController {

	private final Link statsButton;

	private CloseableModalController cmc;
	private GroupAssessmentController assessmentCtrl;

	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	private final BusinessGroup assessedGroup;
	
	public GTAGroupAssessmentToolController(UserRequest ureq, WindowControl wControl, 
			CourseEnvironment courseEnv, BusinessGroup assessedGroup, GTACourseNode gtaNode) {
		super(ureq, wControl);
		
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		this.assessedGroup = assessedGroup;

		statsButton = LinkFactory.createButton("assessment.group.tool", null, this);
		statsButton.setTranslator(getTranslator());
		putInitialPanel(statsButton);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doGrading();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(assessmentCtrl);
		removeAsListenerAndDispose(cmc);
		assessmentCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(statsButton == source) {
			doOpenAssessmentForm(ureq);
		}
	}
	
	private void doGrading() {
		//assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.graded);
	}
	
	private void doOpenAssessmentForm(UserRequest ureq) {
		removeAsListenerAndDispose(assessmentCtrl);
		
		assessmentCtrl = new GroupAssessmentController(ureq, getWindowControl(), courseEnv, gtaNode, assessedGroup);
		listenTo(assessmentCtrl);
		
		String title = translate("grading");
		cmc = new CloseableModalController(getWindowControl(), "close", assessmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}