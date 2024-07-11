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
package org.olat.course.nodes.gta.ui.workflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.GTACoachedGroupGradingController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractWorkflowListController extends FormBasicController {
	
	protected static final String USER_PROPS_ID = GTACoachedGroupGradingController.USER_PROPS_ID;
	protected static final int USER_PROPS_OFFSET = GTACoachedGroupGradingController.USER_PROPS_OFFSET;

	protected Date now;
	protected int count = 0;
	protected final Roles roles;
	protected final GTACourseNode gtaNode;
	protected final CourseEnvironment courseEnv;
	protected final AssessmentConfig assessmentConfig;
	protected final UserCourseEnvironment coachCourseEnv;
	protected final OLATResourceable taskListEventResource;
	protected final AssessmentToolSecurityCallback assessmentCallback;
	
	protected DueDateConfigFormatter dueDateConfigFormatter;
	
	protected final List<Identity> assessedIdentities;
	protected final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	protected DB dbInstance;
	@Autowired
	protected GTAManager gtaManager;
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	protected BaseSecurityModule securityModule;
	@Autowired
	protected AssessmentService assessmentService;
	@Autowired
	protected CourseAssessmentService courseAssessmentService;
	
	AbstractWorkflowListController(UserRequest ureq, WindowControl wControl, String pageName,
			UserCourseEnvironment coachCourseEnv, List<Identity> identities, GTACourseNode gtaNode) {
		super(ureq, wControl, pageName, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale(),
				Util.createPackageTranslator(IdentityListCourseNodeController.class, ureq.getLocale())));

		roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.now = new Date();
		this.gtaNode = gtaNode;
		this.coachCourseEnv = coachCourseEnv;
		
		assessedIdentities = new ArrayList<>(identities);
		courseEnv = coachCourseEnv.getCourseEnvironment();
		assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(coachCourseEnv), gtaNode);
		assessmentCallback = courseAssessmentService.createCourseNodeRunSecurityCallback(ureq, coachCourseEnv);
		
		TaskList taskList = gtaManager.getTaskList(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode);
		taskListEventResource = OresHelper.createOLATResourceableInstance("GTaskList", taskList.getKey());
		
		dueDateConfigFormatter = DueDateConfigFormatter.create(getLocale());
	}
	
	protected final boolean isDateOnly(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0);
	}
	
	protected final String dateToString(Date date) {
		Formatter formatter = Formatter.getInstance(getLocale());
		return isDateOnly(date) ? formatter.formatDate(date) : formatter.formatDateAndTime(date);
	}
	
	protected final String dueDateConfigToString(DueDateConfig dueDateConfig) {
		String val = "";
		if(dueDateConfig == null) {
			val = "";
		} else if(dueDateConfig.getAbsoluteStartDate() != null && dueDateConfig.getAbsoluteDate() != null) {
			String start = dateToString(dueDateConfig.getAbsoluteStartDate());
			String end = dateToString(dueDateConfig.getAbsoluteDate());
			val = translate("workflow.infos.period", start, end);
		} else if(dueDateConfig.getAbsoluteDate() != null) {
			val = dateToString(dueDateConfig.getAbsoluteDate());
		} else if(dueDateConfig.getRelativeToType() != null) {
			val = dueDateConfigFormatter.getRelativeToTypeName(dueDateConfig.getRelativeToType());
			val = dueDateConfigFormatter.concatRelativeDateConfig(dueDateConfig.getNumOfDays(), val);
		}
		return val;
	}
	
	protected final String numberOfDocuments(int min, int max) {
		String val = "";
		if(min >= 1 && max > 0) {
			if(min == max) {
				val = translate("workflow.infos.num.documents", Integer.toString(max));
			} else {
				val = translate("workflow.infos.range.documents", Integer.toString(min), Integer.toString(max));
			}
		} else if(min >= 1 && max < 0) {
			val = translate("workflow.infos.num.documents", ">" + Integer.toString(min));
		}
		return val;
	}
	
	protected final String getFullNames(List<CoachedParticipantRow> rows) {
		StringBuilder fullNames = new StringBuilder();
		for(CoachedParticipantRow row:rows) {
			if(!fullNames.isEmpty()) {
				fullNames.append(", ");
			}
			String fullName = userManager.getUserDisplayName(row.getAssessedIdentity());
			fullNames.append(fullName);
		}
		return fullNames.toString();
	}
	
	protected final void doSetStatus(Identity assessedIdentity, AssessmentEntryStatus status, CourseNode cNode, TaskList taskList, ICourse course) {
		Roles roles = securityManager.getRoles(assessedIdentity);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(cNode, assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getWeightedScore(),
				scoreEval.getScoreScale(), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), status,
				scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(cNode, doneEval, assessedUserCourseEnv,
				getIdentity(), false, Role.coach);
		
		Task assignedTask = gtaManager.getTask(assessedIdentity, taskList);
		if(assignedTask == null) {
			gtaManager.createTask(null, taskList, TaskProcess.graded, null, assessedIdentity, gtaNode);
		} else {
			gtaManager.updateTask(assignedTask, TaskProcess.graded, gtaNode, false, getIdentity(), Role.coach);
		}
	}
}
