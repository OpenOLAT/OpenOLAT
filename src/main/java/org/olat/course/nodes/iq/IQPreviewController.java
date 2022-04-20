/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * Displays a small form where in preview mode the users results can be
 * simulated Initial Date: 13.01.2005 <br>
 * 
 * @author Felix Jost
 */
public class IQPreviewController extends BasicController {

	private PreviewForm pf;
	private final UserCourseEnvironment userCourseEnv;
	private IQTESTCourseNode cn;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	/**
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param cn
	 * @param ne
	 */
	public IQPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, IQTESTCourseNode cn) {
		super(ureq, wControl);

		this.userCourseEnv = userCourseEnv;
		this.cn = cn;
		pf = new PreviewForm(ureq, wControl);
		listenTo(pf);
		putInitialPanel(pf.getInitialComponent());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == pf) {
			if (event == Event.DONE_EVENT) {
				int score = pf.getPointValue();
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(userCourseEnv), cn);
				Float cutValue = assessmentConfig.getCutValue();
				boolean passed = score >= (cutValue == null ? 0 : cutValue.floatValue());
				ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), null, null, null,
						Boolean.valueOf(passed), null, null, null, null, null, null);
				
				boolean incrementUserAttempts = true;
				courseAssessmentService.updateScoreEvaluation(cn, sceval, userCourseEnv, ureq.getIdentity(),
						incrementUserAttempts, Role.user);
				getWindowControl().setInfo(translate("preview.points.set"));
			}
		}
	}
}
