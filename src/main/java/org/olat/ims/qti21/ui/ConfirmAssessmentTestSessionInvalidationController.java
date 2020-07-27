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
package org.olat.ims.qti21.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.DB;
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
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A controller to confirm to set a test session as invalid and eventually
 * promote the results of the next last session to the assessment entry. 
 * 
 * Initial date: 27 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmAssessmentTestSessionInvalidationController extends FormBasicController {
	
	private FormLink invalidateButton;
	
	private AssessmentTestSession session;
	private final IQTESTCourseNode courseNode;
	private final boolean canUpdateAssessmentEntry;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	
	public ConfirmAssessmentTestSessionInvalidationController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session, boolean lastSession, IQTESTCourseNode courseNode,
			UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "confirm_inval_test_session");
		this.session = session;
		this.courseNode = courseNode;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		canUpdateAssessmentEntry = lastSession && (getNextLastSession() != null);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			String fullname = userManager.getUserDisplayName(session.getIdentity());
			String text = translate("invalidate.test.confirm.text", new String[]{ fullname });
			((FormLayoutContainer) formLayout).contextPut("msg", text);
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		if(canUpdateAssessmentEntry) {
			uifactory.addFormSubmitButton("invalidate.overwrite", formLayout);
			invalidateButton = uifactory.addFormLink("invalidate", formLayout, Link.BUTTON);
		} else {
			uifactory.addFormSubmitButton("invalidate", formLayout);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doInvalidateSession(ureq, canUpdateAssessmentEntry);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(invalidateButton == source) {
			doInvalidateSession(ureq, false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doInvalidateSession(UserRequest ureq, boolean updateEntryResults) {
		session.setCancelled(true);
		session = qtiService.updateAssessmentTestSession(session);
		dbInstance.commit();
		if(updateEntryResults) {
			AssessmentTestSession promotedSession = getNextLastSession();
			if(promotedSession != null) {
				courseNode.promoteAssessmentTestSession(promotedSession, assessedUserCourseEnv, getIdentity(), Role.coach);
			}
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private AssessmentTestSession getNextLastSession() {
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(courseEntry, courseNode.getIdent(),
				assessedUserCourseEnv.getIdentityEnvironment().getIdentity(), true);
		sessions.remove(session);
		if(!sessions.isEmpty()) {
			Collections.sort(sessions, new AssessmentTestSessionComparator());
			return sessions.get(0);
		}
		return null;
	}
}
