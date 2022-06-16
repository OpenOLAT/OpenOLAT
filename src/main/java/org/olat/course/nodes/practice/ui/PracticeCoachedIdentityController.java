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
package org.olat.course.nodes.practice.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.ui.tool.AssessedIdentityLargeInfosController;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeCoachedIdentityController extends BasicController {
	
	private Link resetDataButton;
	private final VelocityContainer mainVC;
	
	private final Identity practicingIdentity;
	private final RepositoryEntry courseEntry;
	private final PracticeCourseNode courseNode;
	
	private CloseableModalController cmc;
	private PracticeParticipantController statisticsCtrl;
	private ConfirmResetPracticeDataController confirmCtrl;
	
	public PracticeCoachedIdentityController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, PracticeCourseNode courseNode, Identity practicingIdentity) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.practicingIdentity = practicingIdentity;
		
		mainVC = createVelocityContainer("practice_coached_identity");

		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		AssessedIdentityLargeInfosController identityInfosCtrl
			= new AssessedIdentityLargeInfosController(ureq, wControl, practicingIdentity, course, courseNode);
		listenTo(identityInfosCtrl);
		mainVC.put("identityInfos", identityInfosCtrl.getInitialComponent());
		
		loadStatistics(ureq);
		
		if(!coachCourseEnv.isCourseReadOnly()) {
			resetDataButton = LinkFactory.createButton("reset.user.data", mainVC, this);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(resetDataButton == source) {
			doConfirmReset(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadStatistics(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCtrl = null;
		cmc = null;
	}
	
	private void doConfirmReset(UserRequest ureq) {
		confirmCtrl = new ConfirmResetPracticeDataController(ureq, getWindowControl(),
				courseEntry, courseNode, practicingIdentity);
		listenTo(confirmCtrl);
		
		String title = translate("confirm.reset.data.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void loadStatistics(UserRequest ureq) {
		List<PracticeResource> resources = statisticsCtrl == null ? null : statisticsCtrl.getResources();
		List<PracticeItem> items = statisticsCtrl == null ? null : statisticsCtrl.getPracticeItems();
		removeAsListenerAndDispose(statisticsCtrl);
		
		statisticsCtrl = new PracticeParticipantController(ureq, getWindowControl(),
				courseEntry, courseNode, null, practicingIdentity, resources, items);
		listenTo(statisticsCtrl);
		mainVC.put("statistics", statisticsCtrl.getInitialComponent());
	}
}
