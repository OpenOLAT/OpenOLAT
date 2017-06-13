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
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewController extends BasicController implements TooledController {
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	private final Link startButton, startWizardButton, allTeachersSwitch;
	
	private TeacherRollCallController rollCallCtrl;
	private TeacherRollCallWizardController rollCallWizardCtrl;
	
	private TeacherLecturesTableController currentLecturesBlockCtrl;
	private TeacherLecturesTableController pendingLecturesBlockCtrl;
	private TeacherLecturesTableController nextLecturesBlockCtrl;
	private TeacherLecturesTableController closedLecturesBlockCtrl;
	
	
	private final boolean admin;
	private boolean dirtyTables = false;
	private final RepositoryEntry entry;
	private final RepositoryEntryLectureConfiguration entryConfig;
	
	@Autowired
	private UserManager userManager;
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
		toolbarPanel.addListener(this);
		entryConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);

		allTeachersSwitch = LinkFactory.createToolLink("all.teachers.switch", translate("all.teachers.switch"), this);
		boolean all = isAllTeachersSwitch(ureq, false);
		allTeachersSwitch.setUserObject(all);
		if(all) {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_on");
		} else {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_off");
		}
		
		mainVC = createVelocityContainer("teacher_view");
		
		startButton = LinkFactory.createButton("start", mainVC, this);
		startButton.setVisible(false);
		startWizardButton = LinkFactory.createButton("start.wizard", mainVC, this);
		startWizardButton.setVisible(false);
		
		currentLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(), toolbarPanel,
				admin, "empty.table.current.lectures.blocks");
		listenTo(currentLecturesBlockCtrl);
		mainVC.put("currentLectures", currentLecturesBlockCtrl.getInitialComponent());
		pendingLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(), toolbarPanel,
				admin, "empty.table.lectures.blocks");
		listenTo(pendingLecturesBlockCtrl);
		mainVC.put("pendingLectures", pendingLecturesBlockCtrl.getInitialComponent());
		nextLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(), toolbarPanel,
				admin, "empty.table.lectures.blocks");
		listenTo(nextLecturesBlockCtrl);
		mainVC.put("nextLectures", nextLecturesBlockCtrl.getInitialComponent());
		closedLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(), toolbarPanel,
				admin, "empty.table.lectures.blocks");
		listenTo(closedLecturesBlockCtrl);
		mainVC.put("closedLectures", closedLecturesBlockCtrl.getInitialComponent());

		loadModel();
		putInitialPanel(mainVC);
	}

	@Override
	public void initTools() {
		toolbarPanel.addTool(allTeachersSwitch, Align.right);
	}
	
	private void loadModel() {
		Identity filterByTeacher = ((Boolean)allTeachersSwitch.getUserObject()).booleanValue() ? null : getIdentity();
		List<LectureBlockWithTeachers> blocksWithTeachers = lectureService.getLectureBlocksWithTeachers(entry, filterByTeacher);

		//reset
		startButton.setVisible(false);
		startButton.setUserObject(null);
		startWizardButton.setVisible(false);
		startWizardButton.setUserObject(null);
		
		List<LectureBlockRow> currentBlocks = new ArrayList<>();
		List<LectureBlockRow> pendingBlocks = new ArrayList<>();
		List<LectureBlockRow> nextBlocks = new ArrayList<>();
		List<LectureBlockRow> closedBlocks = new ArrayList<>();

		// only show the start button if 
		Date now = new Date();
		if(ConfigurationHelper.isRollCallEnabled(entryConfig, lectureModule)) {
			for(LectureBlockWithTeachers blockWithTeachers:blocksWithTeachers) {
				LectureBlock block = blockWithTeachers.getLectureBlock();
				
				StringBuilder teachers = new StringBuilder();
				List<Identity> teacherList = blockWithTeachers.getTeachers();
				
				for(Identity teacher:blockWithTeachers.getTeachers()) {
					if(teachers.length() > 0) teachers.append(", ");
					teachers.append(userManager.getUserDisplayName(teacher));
				}
				
				LectureBlockRow row = new LectureBlockRow(block, teachers.toString(), teacherList.contains(getIdentity()));
				if(canStartRollCall(blockWithTeachers)) {
					startButton.setVisible(true);
					startButton.setUserObject(block);
					startButton.setPrimary(true);
					
					startWizardButton.setVisible(true);
					startWizardButton.setUserObject(block);
					
					currentBlocks.add(row);
				} else if(block.getRollCallStatus() == LectureRollCallStatus.closed || block.getRollCallStatus() == LectureRollCallStatus.autoclosed) {
					closedBlocks.add(row);
				} else if(block.getStartDate() != null && block.getStartDate().after(now)) {
					nextBlocks.add(row);
				} else {
					pendingBlocks.add(row);
				}
			}
		}
		
		currentLecturesBlockCtrl.loadModel(currentBlocks);
		mainVC.contextPut("currentBlockSize", currentBlocks.size());
		pendingLecturesBlockCtrl.loadModel(pendingBlocks);
		mainVC.contextPut("pendingBlockSize", pendingBlocks.size());
		nextLecturesBlockCtrl.loadModel(nextBlocks);
		mainVC.contextPut("nextBlockSize", nextBlocks.size());
		closedLecturesBlockCtrl.loadModel(closedBlocks);
		mainVC.contextPut("closedBlockSize", closedBlocks.size());
		dirtyTables = false;
	}
	
	private boolean canStartRollCall(LectureBlockWithTeachers blockWithTeachers) {
		LectureBlock lectureBlock = blockWithTeachers.getLectureBlock();
		if(blockWithTeachers.getTeachers().contains(getIdentity())) {
			Date start = lectureBlock.getStartDate();
			Date end = lectureBlock.getEndDate();
			Date now = new Date();
			if(start.compareTo(now) <= 0 && end.compareTo(now) >= 0) {
				return true;
			}
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
		} else if(currentLecturesBlockCtrl == source || pendingLecturesBlockCtrl == source
				|| nextLecturesBlockCtrl == source ||  closedLecturesBlockCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				loadModel();
				toolbarPanel.popUpToController(this);
			} else if(event == Event.CHANGED_EVENT) {
				dirtyTables = true;
			}
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
		} else if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent)event;
				if(popEvent.getController() instanceof TeacherRollCallController && dirtyTables) {
					loadModel();
				}
			}
		} else if(source == allTeachersSwitch) {
			Boolean val = (Boolean)allTeachersSwitch.getUserObject();
			doToggleAllTeachersSwitch(ureq, val);
		}
	}
	
	//same as above???
	private void doStartRollCall(UserRequest ureq, LectureBlock block) {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> teachers = lectureService.getTeachers(reloadedBlock);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		RollCallSecurityCallback secCallback = getRollCallSecurityCallback(reloadedBlock, teachers.contains(getIdentity()));
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), reloadedBlock, participants, secCallback);
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
	
	private void doToggleAllTeachersSwitch(UserRequest ureq, Boolean value) {
		saveAllTeachersSwitch(ureq, !value.booleanValue());
		loadModel();
	}
	
	private boolean isAllTeachersSwitch(UserRequest ureq, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(TeacherOverviewController.class, getAllTeachersSwitchPrefsId());
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveAllTeachersSwitch(UserRequest ureq, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(TeacherOverviewController.class, getAllTeachersSwitchPrefsId(), new Boolean(newValue));
		}
		if(newValue) {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_on");
		} else {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_off");
		}
		allTeachersSwitch.setUserObject(newValue);
	}
	
	private String getAllTeachersSwitchPrefsId() {
		return "Lectures::" + entry.getKey();
	}
	
	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block, boolean iamTeacher) {
		return new RollCallSecurityCallbackImpl(admin, iamTeacher, block, lectureModule);
	}
}
