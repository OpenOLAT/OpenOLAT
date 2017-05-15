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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	private final Link startButton, startWizardButton;
	
	private TeacherRollCallController rollCallCtrl;
	private TeacherRollCallWizardController rollCallWizardCtrl;
	
	private TeacherOverviewTableController currentLecturesBlockCtrl;
	private TeacherOverviewTableController pendingLecturesBlockCtrl;
	private TeacherOverviewTableController nextLecturesBlockCtrl;
	private TeacherOverviewTableController closedLecturesBlockCtrl;
	
	
	private final boolean admin;
	private final RepositoryEntry entry;
	private final RepositoryEntryLectureConfiguration entryConfig;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public TeacherOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry entry, boolean admin) {
		super(ureq, wControl);
		this.entry = entry;
		this.admin = admin;
		this.toolbarPanel = toolbarPanel;
		entryConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		
		mainVC = createVelocityContainer("teacher_view");
		
		startButton = LinkFactory.createButton("start", mainVC, this);
		startButton.setVisible(false);
		startWizardButton = LinkFactory.createButton("start.wizard", mainVC, this);
		startWizardButton.setVisible(false);
		
		currentLecturesBlockCtrl = new TeacherOverviewTableController(ureq, getWindowControl(), toolbarPanel, admin);
		listenTo(currentLecturesBlockCtrl);
		mainVC.put("currentLectures", currentLecturesBlockCtrl.getInitialComponent());
		pendingLecturesBlockCtrl = new TeacherOverviewTableController(ureq, getWindowControl(), toolbarPanel, admin);
		listenTo(pendingLecturesBlockCtrl);
		mainVC.put("pendingLectures", pendingLecturesBlockCtrl.getInitialComponent());
		nextLecturesBlockCtrl = new TeacherOverviewTableController(ureq, getWindowControl(), toolbarPanel, admin);
		listenTo(nextLecturesBlockCtrl);
		mainVC.put("nextLectures", nextLecturesBlockCtrl.getInitialComponent());
		closedLecturesBlockCtrl = new TeacherOverviewTableController(ureq, getWindowControl(), toolbarPanel, admin);
		listenTo(closedLecturesBlockCtrl);
		mainVC.put("closedLectures", closedLecturesBlockCtrl.getInitialComponent());

		loadModel();
		putInitialPanel(mainVC);
	}
	
	private void loadModel() {
		List<LectureBlock> blocks = lectureService.getLectureBlocks(entry, getIdentity());
		//reset
		startButton.setVisible(false);
		startButton.setUserObject(null);
		startWizardButton.setVisible(false);
		startWizardButton.setUserObject(null);
		
		List<LectureBlock> currentBlocks = new ArrayList<>();
		List<LectureBlock> pendingBlocks = new ArrayList<>();
		List<LectureBlock> nextBlocks = new ArrayList<>();
		List<LectureBlock> closedBlocks = new ArrayList<>();

		// only show the start button if 
		Date now = new Date();
		if(ConfigurationHelper.isRollCallEnabled(entryConfig, lectureModule)) {
			for(LectureBlock block:blocks) {
				if(canStartRollCall(block)) {
					startButton.setVisible(true);
					startButton.setUserObject(block);
					startButton.setPrimary(true);
					
					startWizardButton.setVisible(true);
					startWizardButton.setUserObject(block);
					
					currentBlocks.add(block);
				} else if(block.getRollCallStatus() == LectureRollCallStatus.closed || block.getRollCallStatus() == LectureRollCallStatus.autoclosed) {
					closedBlocks.add(block);
				} else if(block.getStartDate() != null && block.getStartDate().after(now)) {
					nextBlocks.add(block);
				} else {
					pendingBlocks.add(block);
				}
			}
		}
		
		currentLecturesBlockCtrl.loadModel(currentBlocks);
		pendingLecturesBlockCtrl.loadModel(pendingBlocks);
		nextLecturesBlockCtrl.loadModel(nextBlocks);
		closedLecturesBlockCtrl.loadModel(closedBlocks);
	}
	
	private boolean canStartRollCall(LectureBlock block) {
		Date start = block.getStartDate();
		Date end = block.getEndDate();
		Date now = new Date();
		if(start.compareTo(now) <= 0 && end.compareTo(now) >= 0) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rollCallCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolbarPanel.popController(rollCallCtrl);
				loadModel();
			}
		} else if(rollCallWizardCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			getWindowControl().pop();
			getWindowControl().getWindowBackOffice()
				.getChiefController().getScreenMode().setMode(Mode.standard);
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rollCallWizardCtrl);
		rollCallWizardCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == startButton) {
			LectureBlock block = (LectureBlock)startButton.getUserObject();
			doStartRollCall(ureq, block);
		} else if(source == startWizardButton) {
			LectureBlock block = (LectureBlock)startWizardButton.getUserObject();
			doStartWizardRollCall(ureq, block);
		}
	}
	
	//same as above???
	private void doStartRollCall(UserRequest ureq, LectureBlock block) {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), reloadedBlock, participants, getRollCallSecurityCallback(reloadedBlock));
		listenTo(rollCallCtrl);
		toolbarPanel.pushController(reloadedBlock.getTitle(), rollCallCtrl);
	}
	
	@SuppressWarnings("deprecation")
	private void doStartWizardRollCall(UserRequest ureq, LectureBlock block) {
		if(rollCallWizardCtrl != null) return;
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		rollCallWizardCtrl = new TeacherRollCallWizardController(ureq, getWindowControl(), reloadedBlock, participants);
		listenTo(rollCallWizardCtrl);
		
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		cc.getScreenMode().setMode(Mode.full);
		getWindowControl().pushToMainArea(rollCallWizardCtrl.getInitialComponent());
	}
	
	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block) {
		return new RollCallSecurityCallbackImpl(admin, true, block, lectureModule);
	}
}
