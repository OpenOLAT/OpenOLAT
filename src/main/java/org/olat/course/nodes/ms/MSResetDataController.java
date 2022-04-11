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
package org.olat.course.nodes.ms;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.06.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSResetDataController extends FormBasicController {
	
	private static final String[] confirmationKeys = new String[] { "confirm.delete" };

	private MultipleSelectionElement confirmationEl;

	private final CourseEnvironment courseEnv;
	private final MSCourseNode courseNode;
	private final List<Identity> identities;
	
	@Autowired
	private MSService msService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public MSResetDataController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, MSCourseNode courseNode) {
		super(ureq, wControl, "delete_data_confirmation");
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		
		if(asOptions.getGroup() == null && asOptions.getIdentities() == null) {
			identities = ScoreAccountingHelper.loadUsers(courseEnv);
		} else if (asOptions.getIdentities() != null) {
			identities = asOptions.getIdentities();
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer confirmCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
		formLayout.add("confirm", confirmCont);
		confirmCont.setRootForm(mainForm);
		
		flc.contextPut("numIdentities", String.valueOf(identities.size()));
		
		String[] conformationValues = new String[] { translate("tool.reset.data.confirm.message") };
		confirmationEl = uifactory.addCheckboxesHorizontal("confirm.delete", "", confirmCont, confirmationKeys, conformationValues);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		confirmCont.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("tool.reset.data.confirm.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmationEl.clearError();
		if(!confirmationEl.isAtLeastSelected(1)) {
			confirmationEl.setErrorKey("tool.reset.data.confirm.error", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		UserNodeAuditManager auditManager = courseEnv.getAuditManager();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		ScoreEvaluation scoreEval = new ScoreEvaluation(null, null, null, null, null, AssessmentEntryStatus.notStarted,
				null, null, 0.0d, AssessmentRunStatus.notStarted, null);
		for(Identity identity:identities) {
			IdentityEnvironment ienv = new IdentityEnvironment(identity, Roles.userRoles());
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
			courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, uce, getIdentity(), false,
					Role.coach);
			AuditEnv auditEnv = AuditEnv.of(auditManager, courseNode, identity, getIdentity(), Role.coach);
			msService.deleteSession(courseEntry, courseNode.getIdent(), identity, auditEnv);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
