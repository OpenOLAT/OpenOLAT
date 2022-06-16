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
package org.olat.course.nodes.practice.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathConfigs.FullyAssessedResult;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmResetPracticeDataController extends FormBasicController {
	
	private FormLink resetButton;
	
	private final PracticeCourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private Identity practicingIdentity;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private LearningPathNodeAccessProvider learningPathNodeAccessProvider;
	
	public ConfirmResetPracticeDataController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, PracticeCourseNode courseNode, Identity practicingIdentity) {
		super(ureq, wControl, "confirm_reset");
		this.courseNode = courseNode;
		this.courseEntry = courseEntry;
		this.practicingIdentity = practicingIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			long completedSeries = practiceService.countCompletedSeries(practicingIdentity, courseEntry, courseNode.getIdent());
			String i18nKey;
			if(completedSeries <= 0) {
				i18nKey = "confirm.reset.data.text.zero";
			} else if(completedSeries == 1) {
				i18nKey = "confirm.reset.data.text.singular";
			} else {
				i18nKey = "confirm.reset.data.text.plural";
			}

			String fullName = userManager.getUserDisplayName(practicingIdentity);
			String message = translate(i18nKey, fullName, Long.toString(completedSeries));
			layoutCont.contextPut("msg", message);
		}
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		resetButton = uifactory.addFormLink("reset.user.data", formLayout, Link.BUTTON);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(resetButton == source) {
			doResetData();
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doResetData() {
		practiceService.resetSeries(practicingIdentity, courseEntry, courseNode.getIdent());

		ICourse course = CourseFactory.loadCourse(courseEntry);
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(practicingIdentity, course);
		
		INode parentNode = courseNode.getParent();
		CourseNode parent = parentNode != null ? (CourseNode)parentNode : null;
		LearningPathConfigs configs = learningPathService.getConfigs(courseNode, parent);
		FullyAssessedResult result = configs.isFullyAssessedOnConfirmation(false);
		result = LearningPathConfigs.fullyAssessed(true, result.isFullyAssessed(), result.isDone());
		learningPathNodeAccessProvider.updateFullyAssessed(courseNode, assessedUserCourseEnv, result);
		
		getLogger().info(Tracing.M_AUDIT, "Practice data deleted: {}, course node {}, participant {}", 
				courseEntry, courseNode.getIdent(), practicingIdentity);
	}
}
