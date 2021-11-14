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

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserPropertiesRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmUserVisibilityController<U extends UserPropertiesRow> extends FormBasicController {
	
	private static final String[] visibilityKeys = new String[] { "visible", "hidden" };
	
	private SingleSelection visibilityEl;
	
	private final List<U> rows;
	private final CourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public ConfirmUserVisibilityController(UserRequest ureq, WindowControl wControl, List<U> rows,
			UserCourseEnvironment coachCourseEnv, CourseNode courseNode) {
		super(ureq, wControl);
		this.rows = rows;
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] visibilityValues = new String[]{
				translate("user.visibility.visible.select"), translate("user.visibility.hidden.select")
		};
		visibilityEl = uifactory.addRadiosVertical("user.visibility", "user.visibility", formLayout, visibilityKeys, visibilityValues);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("change.visibility", buttonsCont);
	}
	
	public Boolean getVisibility() {
		return visibilityEl.isSelected(0);
	}
	
	public List<U> getRows() {
		return rows;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		RepositoryEntry courseEntry = this.coachCourseEnv.getCourseEnvironment()
				.getCourseGroupManager().getCourseEntry();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Boolean visibility = getVisibility();
		
		for(UserPropertiesRow row:rows) {
			Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
			
			Roles roles = securityManager.getRoles(assessedIdentity);
			
			IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
			UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
					coachCourseEnv.getCourseReadOnlyDetails());
			assessedUserCourseEnv.getScoreAccounting().evaluateAll();

			ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getPassed(),
					scoreEval.getAssessmentStatus(), visibility,
					scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
					scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv, getIdentity(),
					false, Role.coach);
			dbInstance.commitAndCloseSession();
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
