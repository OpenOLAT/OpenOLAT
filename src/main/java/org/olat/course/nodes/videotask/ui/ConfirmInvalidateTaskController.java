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

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.videotask.ui.components.VideoTaskSessionComparator;
import org.olat.course.run.scoring.ResetCourseDataHelper;
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
public class ConfirmInvalidateTaskController extends FormBasicController {
	
	private VideoTaskSession taskSession;
	private final Identity assessedIdentity;
	private final VideoTaskCourseNode courseNode;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final String text;
	private final String buttonKey;
	private final boolean singleSession;
	private final boolean latestSession;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public ConfirmInvalidateTaskController(UserRequest ureq, WindowControl wControl, VideoTaskSession taskSession,
										   String text, String buttonKey, boolean singleSession, boolean latestSession,
										   VideoTaskCourseNode courseNode, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "confirm_invalidate");
		this.text = text;
		this.buttonKey = buttonKey;
		this.singleSession = singleSession;
		this.latestSession = latestSession;
		this.courseNode = courseNode;
		this.taskSession = taskSession;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("invalidate.text", "", text, formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("invalidate", buttonKey, formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doInvalidateSession(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doInvalidateSession(UserRequest ureq) {
		taskSession.setCancelled(true);
		videoAssessmentService.updateTaskSession(taskSession);
		dbInstance.commit();

		if (singleSession) {
			ResetCourseDataHelper resetCourseDataHelper = new ResetCourseDataHelper(assessedUserCourseEnv.getCourseEnvironment());
			MediaResource mediaResource = resetCourseDataHelper.resetCourseNodes(
					List.of(assessedIdentity),
					List.of(courseNode),
					false,
					getIdentity(),
					Role.coach
			);
			if (mediaResource != null) {
				Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, mediaResource);
				getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
			}
		} else if (latestSession) {
			VideoTaskSession promotedSession = getNextLatestSession();
			if (promotedSession != null) {
				courseNode.promoteTaskSession(promotedSession, assessedUserCourseEnv, true, getIdentity(), Role.coach, getLocale());
			}
		}

		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private VideoTaskSession getNextLatestSession() {
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<VideoTaskSession> sessions = videoAssessmentService.getTaskSessions(courseEntry, courseNode.getIdent(), assessedIdentity);
		
		sessions.remove(taskSession);
		if (!sessions.isEmpty()) {
			sessions.sort(new VideoTaskSessionComparator(true));
			return sessions.get(0);
		}
		return null;
	}
}
