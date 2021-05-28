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
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.event.SearchLecturesBlockEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractTeacherOverviewController extends BasicController implements BreadcrumbPanelAware, Activateable2 {
	
	protected BreadcrumbPanel stackPanel;
	protected final VelocityContainer mainVC;
	private final Link startButton;
	private final Link startWizardButton;
	protected final Link allTeachersSwitch;
	
	private TeacherRollCallController rollCallCtrl;
	private TeacherRollCallWizardController rollCallWizardCtrl;
	
	protected TeacherOverviewSearchController searchCtrl;
	private TeacherLecturesTableController currentLecturesBlockCtrl;
	private TeacherLecturesTableController pendingLecturesBlockCtrl;
	private TeacherLecturesTableController nextLecturesBlockCtrl;
	private TeacherLecturesTableController closedLecturesBlockCtrl;
	
	private final boolean admin;
	private final String switchPrefsId;
	protected boolean dirtyTables = false;
	private LecturesBlockSearchParameters currentSearchParams;
	private final boolean withRepositoryEntry;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;

	AbstractTeacherOverviewController(UserRequest ureq, WindowControl wControl, boolean admin,
			String switchPrefsId, boolean withRepositoryEntry, boolean defaultShowAllLectures) {
		super(ureq, wControl);
		this.admin = admin;
		this.switchPrefsId = switchPrefsId;
		this.withRepositoryEntry = withRepositoryEntry;
		
		mainVC = createVelocityContainer("teacher_view");
		
		startButton = LinkFactory.createButton("start.desktop", mainVC, this);
		startButton.setVisible(false);
		startWizardButton = LinkFactory.createButton("start.mobile", mainVC, this);
		startWizardButton.setVisible(false);
		
		allTeachersSwitch = LinkFactory.createToolLink("all.teachers.switch", translate("all.teachers.switch"), this);
		boolean all = isAllTeachersSwitch(ureq, defaultShowAllLectures);
		allTeachersSwitch.setUserObject(all);
		if(all) {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_on");
			allTeachersSwitch.setTooltip(translate("all.teachers.switch.tooltip.on"));
		} else {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_off");
			allTeachersSwitch.setTooltip(translate("all.teachers.switch.tooltip.off"));
		}
		putInitialPanel(mainVC);
	}
	
	protected void initTables(UserRequest ureq, boolean withTeachers, boolean withAssessment) {
		searchCtrl = new TeacherOverviewSearchController(ureq, getWindowControl(), withRepositoryEntry, withRepositoryEntry);
		listenTo(searchCtrl);
		mainVC.put("search", searchCtrl.getInitialComponent());
		
		currentLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(),
				admin, "empty.table.current.lectures.blocks", false, "current", 20, withRepositoryEntry, withTeachers, withAssessment);
		listenTo(currentLecturesBlockCtrl);
		mainVC.put("currentLectures", currentLecturesBlockCtrl.getInitialComponent());
		
		pendingLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(),
				admin, "empty.table.lectures.blocks", false, "pending", 20, withRepositoryEntry, withTeachers, withAssessment);
		listenTo(pendingLecturesBlockCtrl);
		mainVC.put("pendingLectures", pendingLecturesBlockCtrl.getInitialComponent());
		
		nextLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(),
				admin, "empty.table.lectures.blocks", true, "next", 5, withRepositoryEntry, withTeachers, withAssessment);
		listenTo(nextLecturesBlockCtrl);
		mainVC.put("nextLectures", nextLecturesBlockCtrl.getInitialComponent());
		
		closedLecturesBlockCtrl = new TeacherLecturesTableController(ureq, getWindowControl(),
				admin, "empty.table.lectures.blocks", false, "closed", 10, withRepositoryEntry, withTeachers, false);
		listenTo(closedLecturesBlockCtrl);
		mainVC.put("closedLectures", closedLecturesBlockCtrl.getInitialComponent());
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		currentLecturesBlockCtrl.setBreadcrumbPanel(stackPanel);
		pendingLecturesBlockCtrl.setBreadcrumbPanel(stackPanel);
		nextLecturesBlockCtrl.setBreadcrumbPanel(stackPanel);
		closedLecturesBlockCtrl.setBreadcrumbPanel(stackPanel);
	}

	public int getRowCount() {
		return currentLecturesBlockCtrl.getRowCount() + pendingLecturesBlockCtrl.getRowCount()
			+ nextLecturesBlockCtrl.getRowCount() + closedLecturesBlockCtrl.getRowCount();
	}
	
	protected abstract List<LectureBlockRow> getRows(LecturesBlockSearchParameters searchParams);

	protected void loadModel(LecturesBlockSearchParameters searchParams) {
		currentSearchParams = searchParams;
		
		List<LectureBlockRow> rows = getRows(searchParams);

		//reset
		List<LectureBlockRow> currentBlocks = new ArrayList<>();
		List<LectureBlockRow> pendingBlocks = new ArrayList<>();
		List<LectureBlockRow> nextBlocks = new ArrayList<>();
		List<LectureBlockRow> closedBlocks = new ArrayList<>();

		// only show the start button if 
		Date now = new Date();
		for(LectureBlockRow row:rows) {
			LectureBlock block = row.getLectureBlock();
			
			if(canStartRollCall(row)) {
				startButton.setVisible(true);
				startButton.setUserObject(block);
				startButton.setPrimary(true);
				
				startWizardButton.setVisible(true);
				startWizardButton.setUserObject(block);
				
				currentBlocks.add(row);
			} else if(block.getStatus() == LectureBlockStatus.cancelled
					|| block.getRollCallStatus() == LectureRollCallStatus.closed
					|| block.getRollCallStatus() == LectureRollCallStatus.autoclosed) {
				closedBlocks.add(row);
			} else if(block.getStartDate() != null && block.getStartDate().after(now)) {
				nextBlocks.add(row);
			} else {
				pendingBlocks.add(row);
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
		mainVC.contextPut("totalBlockSize", getRowCount());

		dirtyTables = false;
	}
	
	private boolean canStartRollCall(LectureBlockRow blockWithTeachers) {
		LectureBlock lectureBlock = blockWithTeachers.getLectureBlock();
		if(blockWithTeachers.isIamTeacher()
				&& lectureBlock.getStatus() != LectureBlockStatus.done
				&& lectureBlock.getStatus() != LectureBlockStatus.cancelled) {
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		Long id = entries.get(0).getOLATResourceable().getResourceableId();
		if("LectureBlock".equalsIgnoreCase(name)) {
			boolean started = false;
			if(entries.size() > 1) {
				String action = entries.get(1).getOLATResourceable().getResourceableTypeName();
				if("Start".equalsIgnoreCase(action)) {
					LectureBlockRow row = currentLecturesBlockCtrl.getRow(id);
					if(row != null && canStartRollCall(row)) {
						doStartRollCall(ureq, row.getLectureBlock());
						started = true;
					}
				} else if("StartWizard".equalsIgnoreCase(action)) {
					LectureBlockRow row = currentLecturesBlockCtrl.getRow(id);
					if(row != null && canStartRollCall(row)) {
						doStartWizardRollCall(ureq, row.getLectureBlock());
						started = true;
					}
				}
			} 
			if(!started) {
				activateLectureBlockInTable(ureq, entries, state);
			}
		} else if("RepositoryEntry".equals(name)) {
			searchCtrl.activate(ureq, entries, state);
		}
	}
	
	private void activateLectureBlockInTable(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		currentLecturesBlockCtrl.activate(ureq, entries, state);
		pendingLecturesBlockCtrl.activate(ureq, entries, state);
		nextLecturesBlockCtrl.activate(ureq, entries, state);
		closedLecturesBlockCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rollCallCtrl == source) {
			if(event == Event.DONE_EVENT) {
				stackPanel.popController(rollCallCtrl);
				loadModel(currentSearchParams);
			}
		} else if(rollCallWizardCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(currentSearchParams);
			}
			getWindowControl().pop();
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			getWindowControl().getWindowBackOffice()
				.getChiefController().getScreenMode().setMode(Mode.standard, businessPath);
			cleanUp();
		} else if(currentLecturesBlockCtrl == source || pendingLecturesBlockCtrl == source
				|| nextLecturesBlockCtrl == source ||  closedLecturesBlockCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				loadModel(currentSearchParams);
				stackPanel.popUpToController(this);
			} else if(event == Event.CHANGED_EVENT) {
				dirtyTables = true;
			}
		} else if(searchCtrl == source) {
			if(event instanceof SearchLecturesBlockEvent) {
				doSearch((SearchLecturesBlockEvent)event);
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
		} else if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent)event;
				if(popEvent.getController() instanceof TeacherRollCallController && dirtyTables) {
					loadModel(currentSearchParams);
				}
			}
		} else if(source == allTeachersSwitch) {
			Boolean val = (Boolean)allTeachersSwitch.getUserObject();
			doToggleAllTeachersSwitch(ureq, val);
		}
	}
	
	private void doSearch(SearchLecturesBlockEvent event) {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setSearchString(event.getSearchString());
		searchParams.setStartDate(event.getStartDate());
		searchParams.setEndDate(event.getEndDate());
		loadModel(searchParams);
	}
	
	//same as above???
	private void doStartRollCall(UserRequest ureq, LectureBlock block) {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> teachers = lectureService.getTeachers(reloadedBlock);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		RollCallSecurityCallback secCallback = getRollCallSecurityCallback(reloadedBlock, teachers.contains(getIdentity()));
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), reloadedBlock, participants, secCallback, false);
		if(withRepositoryEntry) {
			rollCallCtrl.addLoggingResourceable(CoreLoggingResourceable.wrap(reloadedBlock.getEntry().getOlatResource(),
					OlatResourceableType.course, reloadedBlock.getEntry().getDisplayname()));
		}
		listenTo(rollCallCtrl);
		stackPanel.pushController(reloadedBlock.getTitle(), rollCallCtrl);

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_STARTED, getClass(),
				CoreLoggingResourceable.wrap(block, OlatResourceableType.lectureBlock, block.getTitle()));
	}
	
	@SuppressWarnings("deprecation")
	private void doStartWizardRollCall(UserRequest ureq, LectureBlock block) {
		if(rollCallWizardCtrl != null) return;
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> teachers = lectureService.getTeachers(reloadedBlock);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		RollCallSecurityCallback secCallback = getRollCallSecurityCallback(reloadedBlock, teachers.contains(getIdentity()));
		rollCallWizardCtrl = new TeacherRollCallWizardController(ureq, getWindowControl(), reloadedBlock, participants, secCallback);
		if(withRepositoryEntry) {
			rollCallWizardCtrl.addLoggingResourceable(CoreLoggingResourceable.wrap(reloadedBlock.getEntry().getOlatResource(),
					OlatResourceableType.course, reloadedBlock.getEntry().getDisplayname()));
		}
		listenTo(rollCallWizardCtrl);
		
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		cc.getScreenMode().setMode(Mode.full, null);
		getWindowControl().pushToMainArea(rollCallWizardCtrl.getInitialComponent());
		
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_STARTED, getClass(),
				CoreLoggingResourceable.wrap(block, OlatResourceableType.lectureBlock, block.getTitle()));
	}
	
	private void doToggleAllTeachersSwitch(UserRequest ureq, Boolean value) {
		saveAllTeachersSwitch(ureq, !value.booleanValue());
		loadModel(currentSearchParams);
	}
	
	private boolean isAllTeachersSwitch(UserRequest ureq, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(AbstractTeacherOverviewController.class, switchPrefsId);
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveAllTeachersSwitch(UserRequest ureq, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(AbstractTeacherOverviewController.class, switchPrefsId, Boolean.valueOf(newValue));
		}
		if(newValue) {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_on");
			allTeachersSwitch.setTooltip(translate("all.teachers.switch.tooltip.on"));
		} else {
			allTeachersSwitch.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_off");
			allTeachersSwitch.setTooltip(translate("all.teachers.switch.tooltip.off"));
		}
		allTeachersSwitch.setUserObject(newValue);
	}
	
	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block, boolean iamTeacher) {
		boolean masterCoach = false;
		if(!admin) {
			masterCoach = lectureService.isMasterCoach(block, getIdentity());
		}
		return new RollCallSecurityCallbackImpl(admin, masterCoach, iamTeacher, block, lectureModule);
	}
}
