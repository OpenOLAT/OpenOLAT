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
package org.olat.course.nodes.video;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */

public class VideoRunController extends BasicController {

	private Panel main;
	
	private VideoDisplayController videoDispCtr;

	private VideoCourseNode videoNode;
	private ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	private double currentProgress = 0d;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	/**
	 * single page run controller 
	 * @param wControl
	 * @param ureq
	 * @param userCourseEnv
	 * @param videoNode
	 */
	public VideoRunController(ModuleConfiguration config, WindowControl wControl, UserRequest ureq, UserCourseEnvironment userCourseEnv, VideoCourseNode videoNode) {
		super(ureq,wControl);
		
		this.config = config;
		this.videoNode = videoNode;
		this.userCourseEnv = userCourseEnv;
		addLoggingResourceable(LoggingResourceable.wrap(videoNode));
		
		main = new Panel("videorunmain");
		doLaunch(ureq);
		putInitialPanel(main);
	}
	
	@Override
	protected void doDispose() {
		// controllers auto-disposed
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// Update last position if user leaves controller somehow uncontrolled
		if (this.currentProgress > 0d && this.currentProgress < 1d) {
			doUpdateAssessmentStatus(ureq, this.currentProgress, true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == videoDispCtr) {
			if (event instanceof VideoEvent	) {
				VideoEvent videoEvent = (VideoEvent) event;
				if (videoEvent.getCommand().equals(VideoEvent.ENDED)) {
					doUpdateAssessmentStatus(ureq, 1d, true);					
				} else if (videoEvent.getCommand().equals(VideoEvent.PAUSE)) {
					doUpdateAssessmentStatus(ureq, videoEvent.getProgress(), true);
				} else if (videoEvent.getCommand().equals(VideoEvent.PROGRESS)) {
					doUpdateAssessmentStatus(ureq, videoEvent.getProgress(), false);
				}				
			}
		}
	}
	
	private void doUpdateAssessmentStatus(UserRequest ureq, double progress, boolean forceSave) {
		if (!this.userCourseEnv.isCourseReadOnly() && this.userCourseEnv.isParticipant()) {						
			boolean update = false;
			// Update video progress as assessment completion if not already in status DONE
			AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(videoNode, userCourseEnv);
			if (!AssessmentEntryStatus.done.equals(assessmentEvaluation.getAssessmentStatus())) {
				// Update watch progress
				Double newProgress = assessmentEvaluation.getCompletion();
				if (newProgress ==  null ||  newProgress.floatValue() < progress) { 
					newProgress = Double.valueOf(progress);
					// Save only in 10% steps to reduce save and assessment recalcualtion cycles					
					if (!forceSave && (Math.round(currentProgress * 10) + 1 <= Math.round(progress * 10))) {						
						update = true;
					}
					this.currentProgress = newProgress;
				}		
				// Update status, only if configured to be done by node
				AssessmentEntryStatus assessmentEntryStatus = assessmentEvaluation.getAssessmentStatus();
				if (Mode.setByNode.equals(courseAssessmentService.getAssessmentConfig(videoNode).getCompletionMode())) {					
					// 95% is considered as "fully watched", set as done
					if (newProgress.floatValue() >= 0.95d) {
						assessmentEntryStatus = AssessmentEntryStatus.done;
						newProgress = 1d;
						update = true;
					} else {
						assessmentEntryStatus = AssessmentEntryStatus.inProgress;				
					}
				}
	
				if (update) {
					courseAssessmentService.updateCompletion(videoNode, userCourseEnv, newProgress, assessmentEntryStatus, Role.user);				
					if (assessmentEntryStatus == AssessmentEntryStatus.done) {			
						courseAssessmentService.updateFullyAssessed(videoNode, userCourseEnv, Boolean.TRUE, assessmentEntryStatus);	
						// TODO: Update menu tree to indicate DONE state
					}
				}
			}
		}
	}
	
	
	private void doLaunch(UserRequest ureq){
		VelocityContainer myContent = createVelocityContainer("run");
		RepositoryEntry videoEntry = VideoEditController.getVideoReference(config, false);
		if (videoEntry == null) {
			showError(VideoEditController.NLS_ERROR_VIDEOREPOENTRYMISSING);
			return;
		}
		
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		VideoDisplayOptions displayOptions = videoNode.getVideoDisplay(videoEntry, userCourseEnv.isCourseReadOnly());
		
		// Read current status
		AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(videoNode, userCourseEnv);
		double completion = (assessmentEvaluation.getCompletion() == null ? 0d : assessmentEvaluation.getCompletion());
		// Override forwardSeeking configuration
		if (this.userCourseEnv.isParticipant()) {
			if (this.userCourseEnv.isCourseReadOnly() || completion == 1d) {			
				displayOptions.setForwardSeekingRestricted(false);
			} // else use as configured in course element
		} else {
			// don't restrict it for owner, coaches etc. 
			displayOptions.setForwardSeekingRestricted(false);			
		}
		videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, courseEntry, videoNode, displayOptions);
		// Enable progress tracking for participants in learning path courses
		//TODO: only for learning path courses?
		if (!this.userCourseEnv.isCourseReadOnly() && this.userCourseEnv.isParticipant()) {
			videoDispCtr.setProgressListener(true);
			// Init with last position
			if (completion > 0.05d && completion < 0.95d) {
				videoDispCtr.setPlayProgress(completion);			
			}
		}
		
		listenTo(videoDispCtr);
		
		myContent.put("videoDisplay", videoDispCtr.getInitialComponent());
		main.setContent(myContent);
		
		// Update launch counter
		repositoryService.incrementLaunchCounter(videoEntry);
	}

	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq) {
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, userCourseEnv, videoNode, "o_icon_video");
		return new NodeRunConstructionResult(ctrl);
	}
}
