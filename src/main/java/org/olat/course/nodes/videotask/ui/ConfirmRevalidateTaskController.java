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
package org.olat.course.nodes.videotask.ui;

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
import org.olat.core.id.Identity;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.videotask.ui.components.VideoTaskSessionComparator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoTaskSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRevalidateTaskController extends FormBasicController {
	
	private FormLink revalidateButton;
	
	private VideoTaskSession taskSession;
	private final Identity assessedIdentity;
	private final VideoTaskCourseNode courseNode;
	private final boolean canUpdateAssessmentEntry;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public ConfirmRevalidateTaskController(UserRequest ureq, WindowControl wControl, VideoTaskSession taskSession,
			VideoTaskCourseNode courseNode, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "confirm_revalidate");
		this.taskSession = taskSession;
		this.courseNode = courseNode;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		canUpdateAssessmentEntry = isNextLastSession(taskSession);
		
		initForm(ureq);
	}
	
	private boolean isNextLastSession(VideoTaskSession session) {
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<VideoTaskSession> sessions = videoAssessmentService.getTaskSessions(courseEntry, courseNode.getIdent(), assessedIdentity);
		if(sessions.isEmpty()) {
			return true;
		}
		
		sessions.add(session);
		Collections.sort(sessions, new VideoTaskSessionComparator(false));
		return session.equals(sessions.get(0));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("msg", translate("confirm.revalidate.text"));
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		if(canUpdateAssessmentEntry) {
			uifactory.addFormSubmitButton("revalidate.overwrite", formLayout);
			revalidateButton = uifactory.addFormLink("revalidate", formLayout, Link.BUTTON);
		} else {
			uifactory.addFormSubmitButton("revalidate", formLayout);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(revalidateButton == source) {
			doValidateSession(ureq, false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doValidateSession(ureq, canUpdateAssessmentEntry);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doValidateSession(UserRequest ureq, boolean updateEntryResults) {
		taskSession.setCancelled(false);
		taskSession = videoAssessmentService.updateTaskSession(taskSession);
		dbInstance.commit();
		
		if(canUpdateAssessmentEntry) {
			if(courseNode != null) {
				courseNode.promoteTaskSession(taskSession, assessedUserCourseEnv, updateEntryResults, getIdentity(), Role.coach, getLocale());
			}
		}
			
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
