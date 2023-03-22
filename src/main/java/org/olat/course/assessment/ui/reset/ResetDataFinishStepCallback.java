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
package org.olat.course.assessment.ui.reset;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.run.scoring.ResetCourseDataHelper;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetDataFinishStepCallback implements StepRunnerCallback {
	
	private final ResetDataContext dataContext;
	private final AssessmentToolSecurityCallback secCallback;
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public ResetDataFinishStepCallback(ResetDataContext dataContext, AssessmentToolSecurityCallback secCallback) {
		CoreSpringFactory.autowireObject(this);
		this.dataContext = dataContext;
		this.secCallback = secCallback;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		ICourse course = CourseFactory.loadCourse(dataContext.getRepositoryEntry());
		
		List<Identity> participants;
		if(dataContext.getResetParticipants() == ResetParticipants.all) {
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null,  secCallback);
			participants = assessmentToolManager.getAssessedIdentities(ureq.getIdentity(), params);
		} else {
			participants = dataContext.getSelectedParticipants();
		}
		
		ResetCourseDataHelper resetHelper = new ResetCourseDataHelper(course.getCourseEnvironment());
		MediaResource archiveResource = null;
		if(dataContext.getResetCourse() == ResetCourse.all) {
			archiveResource = resetHelper.resetCourse(participants, ureq.getIdentity(), Role.coach);
		} else if(!dataContext.getCourseNodes().isEmpty()) {
			archiveResource = resetHelper.resetCourseNodes(participants, dataContext.getCourseNodes(), false, ureq.getIdentity(), Role.coach);
		}
		if(archiveResource != null) {
			Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, archiveResource);
			wControl.getWindowBackOffice().sendCommandTo(downloadCmd);
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
}
