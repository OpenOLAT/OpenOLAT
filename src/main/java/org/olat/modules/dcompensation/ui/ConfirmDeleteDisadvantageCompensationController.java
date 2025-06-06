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
package org.olat.modules.dcompensation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.activity.ActivityLogService;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ILoggingResourceable;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentLoggingAction;
import org.olat.course.nodes.iq.QTI21IdentityListCourseNodeToolsController.AssessmentTestSessionDetailsComparator;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteDisadvantageCompensationController extends FormBasicController {
	
	private final AssessmentTestSession lastSession;
	private final List<DisadvantageCompensation> compensationToDelete;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private ActivityLogService activityLogService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public ConfirmDeleteDisadvantageCompensationController(UserRequest ureq, WindowControl wControl,
			List<DisadvantageCompensation> compensationToDelete) {
		this(ureq, wControl, compensationToDelete, null);
	}
	
	public ConfirmDeleteDisadvantageCompensationController(UserRequest ureq, WindowControl wControl,
			List<DisadvantageCompensation> compensationToDelete, AssessmentTestSession lastSession) {
		super(ureq, wControl, "confirm_delete");
		this.lastSession = lastSession;
		this.compensationToDelete = compensationToDelete;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String message;
			if(compensationToDelete.size() == 1) {
				message = translate("confirm.delete.compensation");
			} else {
				message = translate("confirm.delete.compensations");
			}
			layoutCont.contextPut("msg", message);
		}

		uifactory.addFormSubmitButton("delete", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(DisadvantageCompensation compensation:compensationToDelete) {
			compensation = disadvantageCompensationService.loadDisadvantageCompensation(compensation);
			if(compensation == null) {
				continue;
			}
			if(lastSession != null) {
				qtiService.compensationExtraTimeAssessmentTestSession(lastSession, 0, getIdentity());
			} else {
				List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
						.getAssessmentTestSessionsStatistics(compensation.getEntry(), compensation.getSubIdent(), compensation.getIdentity(), true);
				if(!sessionsStatistics.isEmpty()) {
					Collections.sort(sessionsStatistics, new AssessmentTestSessionDetailsComparator());
					AssessmentTestSession oneLastSession = sessionsStatistics.get(0).testSession();
					qtiService.compensationExtraTimeAssessmentTestSession(oneLastSession, 0, getIdentity());
				}
			}
			compensation.setStatusEnum(DisadvantageCompensationStatusEnum.deleted);
			disadvantageCompensationService.updateDisadvantageCompensation(compensation);
			logLockActivity(compensation, ureq, AssessmentLoggingAction.DISADVANTAGE_COMPENSATION_DELETE);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void logLockActivity(DisadvantageCompensation compensation, UserRequest ureq, ILoggingAction action) {
		RepositoryEntry entry = compensation.getEntry();
		Identity assessedIdentity = compensation.getIdentity();
		List<ContextEntry> bcContextEntries = BusinessControlFactory.getInstance().createCEListFromString(entry.getOlatResource());
	
		Long identityKey = getIdentity().getKey();
		List<ILoggingResourceable> loggingResourceableList = new ArrayList<>();
		loggingResourceableList.add(CoreLoggingResourceable.wrap(entry.getOlatResource(), OlatResourceableType.course, entry.getDisplayname()));
		loggingResourceableList.add(CoreLoggingResourceable.wrap(OresHelper
			.createOLATResourceableInstance("CourseNode", Long.valueOf(compensation.getSubIdent())), OlatResourceableType.node, compensation.getSubIdentName()));
		loggingResourceableList.add(LoggingResourceable.wrap(assessedIdentity));
		
		String sessionId = activityLogService.getSessionId(ureq.getUserSession());
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "][CourseNode:" + compensation.getSubIdent() + "]";
		activityLogService.log(action, action.getResourceActionType(), sessionId, identityKey, getClass(), false,
				businessPath, bcContextEntries, loggingResourceableList);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
