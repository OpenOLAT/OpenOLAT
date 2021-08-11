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

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.iq.QTI21IdentityListCourseNodeToolsController.AssessmentTestSessionDetailsComparator;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
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
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("delete", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(DisadvantageCompensation compensation:compensationToDelete) {
			if(lastSession != null) {
				qtiService.compensationExtraTimeAssessmentTestSession(lastSession, 0, getIdentity());
			} else {
				List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
						.getAssessmentTestSessionsStatistics(compensation.getEntry(), compensation.getSubIdent(), compensation.getIdentity(), true);
				if(!sessionsStatistics.isEmpty()) {
					Collections.sort(sessionsStatistics, new AssessmentTestSessionDetailsComparator());
					AssessmentTestSession oneLastSession = sessionsStatistics.get(0).getTestSession();
					qtiService.compensationExtraTimeAssessmentTestSession(oneLastSession, 0, getIdentity());
				}
			}
			compensation.setStatusEnum(DisadvantageCompensationStatusEnum.deleted);
			disadvantageCompensationService.updateDisadvantageCompensation(compensation);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
