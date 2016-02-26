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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.course.assessment.AssessmentEditController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 18.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachedParticipantGradingController extends BasicController {
	
	private final Link assessmentFormButton;
	private final VelocityContainer mainVC;
	
	private AssessmentEditController assessmentForm;
	private CloseableModalController cmc;
	private MSCourseNodeRunController msCtrl;
	
	private final GTACourseNode gtaNode;
	private final Identity assessedIdentity;
	private final OLATResourceable courseOres;
	
	public GTACoachedParticipantGradingController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, GTACourseNode gtaNode, Identity assessedIdentity) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.courseOres = OresHelper.clone(courseOres);
		this.assessedIdentity = assessedIdentity;
		
		mainVC = createVelocityContainer("coach_grading");
		
		assessmentFormButton = LinkFactory.createCustomLink("coach.assessment", "assessment", "coach.assessment", Link.BUTTON, mainVC, this);
		assessmentFormButton.setCustomEnabledLinkCSS("btn btn-primary");
		assessmentFormButton.setIconLeftCSS("o_icon o_icon o_icon_submit");
		assessmentFormButton.setElementCssClass("o_sel_course_gta_assessment_button");

		putInitialPanel(mainVC);
		setAssessmentDatas(ureq);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentForm == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setAssessmentDatas(ureq);
				doGraded();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(assessmentForm);
		removeAsListenerAndDispose(cmc);
		assessmentForm = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(assessmentFormButton == source) {
			doOpenAssessmentForm(ureq);
		}
	}
	
	private void setAssessmentDatas(UserRequest ureq) {
		removeAsListenerAndDispose(msCtrl);
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		UserCourseEnvironment uce = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
		msCtrl = new MSCourseNodeRunController(ureq, getWindowControl(), uce, gtaNode, false, false);
		listenTo(msCtrl);
		mainVC.put("msrun", msCtrl.getInitialComponent());
	}
	
	private void doGraded() {
		//assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.grading);
	}

	private void doOpenAssessmentForm(UserRequest ureq) {
		if(assessmentForm != null) return;//already open
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		UserCourseEnvironment uce = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
		AssessedIdentityWrapper assessedIdentityWrapper = AssessmentHelper.wrapIdentity(uce, null, gtaNode);
		
		assessmentForm = new AssessmentEditController(ureq, getWindowControl(), null, course, gtaNode,
				assessedIdentityWrapper, false, true, true);
		listenTo(assessmentForm);
		
		String title = translate("grading");
		cmc = new CloseableModalController(getWindowControl(), "close", assessmentForm.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
}
