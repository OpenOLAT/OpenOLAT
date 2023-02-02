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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.video.VideoTaskSession;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentDetailsController extends AbstractVideoTaskSessionListController {
	
	private final Identity assessedIdentity;
	private final UserCourseEnvironment assessedUserCourseEnv;

	public VideoTaskAssessmentDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			VideoTaskCourseNode courseNode, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "assessment_details", stackPanel, courseNode, assessedUserCourseEnv.getCourseEnvironment());
		
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		
		initForm(ureq);
		loadModel();

		int maxAttempts = courseNode.getModuleConfiguration()
				.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, tableModel.getMaxAttempts());
		initFilters(false, maxAttempts);
	}

	@Override
	protected void loadModel() {
		List<VideoTaskSession> taskSessions = videoAssessmentService.getTaskSessions(entry, courseNode.getIdent(), assessedIdentity);
		loadModel(taskSessions);
	}

	@Override
	protected void doInvalidate(UserRequest ureq, VideoTaskSessionRow taskSession) {
		super.doInvalidate(ureq, taskSession, assessedUserCourseEnv);
	}

	@Override
	protected void doRevalidate(UserRequest ureq, VideoTaskSessionRow taskSession) {
		super.doRevalidate(ureq, taskSession, assessedUserCourseEnv);
	}
}
