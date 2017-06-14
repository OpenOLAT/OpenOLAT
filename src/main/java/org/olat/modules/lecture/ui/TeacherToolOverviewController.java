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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherToolOverviewController extends BasicController {
	
	private final VelocityContainer mainVC;

	private TeacherToolLecturesTableController currentLecturesBlockCtrl;
	private TeacherToolLecturesTableController pendingLecturesBlockCtrl;
	private TeacherToolLecturesTableController nextLecturesBlockCtrl;
	private TeacherToolLecturesTableController closedLecturesBlockCtrl;

	@Autowired
	private LectureService lectureService;
	
	public TeacherToolOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("teacher_view");

		currentLecturesBlockCtrl = new TeacherToolLecturesTableController(ureq, getWindowControl(), "empty.table.current.lectures.blocks");
		listenTo(currentLecturesBlockCtrl);
		mainVC.put("currentLectures", currentLecturesBlockCtrl.getInitialComponent());
		pendingLecturesBlockCtrl = new TeacherToolLecturesTableController(ureq, getWindowControl(), "empty.table.lectures.blocks");
		listenTo(pendingLecturesBlockCtrl);
		mainVC.put("pendingLectures", pendingLecturesBlockCtrl.getInitialComponent());
		nextLecturesBlockCtrl = new TeacherToolLecturesTableController(ureq, getWindowControl(), "empty.table.lectures.blocks");
		listenTo(nextLecturesBlockCtrl);
		mainVC.put("nextLectures", nextLecturesBlockCtrl.getInitialComponent());
		closedLecturesBlockCtrl = new TeacherToolLecturesTableController(ureq, getWindowControl(), "empty.table.lectures.blocks");
		listenTo(closedLecturesBlockCtrl);
		mainVC.put("closedLectures", closedLecturesBlockCtrl.getInitialComponent());

		loadModel();
		putInitialPanel(mainVC);
	}
	
	public int getRowCount() {
		return currentLecturesBlockCtrl.getRowCount() + pendingLecturesBlockCtrl.getRowCount()
			+ nextLecturesBlockCtrl.getRowCount() + closedLecturesBlockCtrl.getRowCount();
	}

	private void loadModel() {
		List<LectureBlock> blocksWithTeachers = lectureService.getLectureBlocks(getIdentity());

		//reset
		List<LectureBlockRow> currentBlocks = new ArrayList<>();
		List<LectureBlockRow> pendingBlocks = new ArrayList<>();
		List<LectureBlockRow> nextBlocks = new ArrayList<>();
		List<LectureBlockRow> closedBlocks = new ArrayList<>();

		// only show the start button if 
		Date now = new Date();
		for(LectureBlock block:blocksWithTeachers) {
			RepositoryEntry entry = block.getEntry();
			LectureBlockRow row = new LectureBlockRow(block, entry.getDisplayname(), "", true);
			if(canStartRollCall(block)) {
				currentBlocks.add(row);
			} else if(block.getRollCallStatus() == LectureRollCallStatus.closed || block.getRollCallStatus() == LectureRollCallStatus.autoclosed) {
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
	}
	
	private boolean canStartRollCall(LectureBlock lectureBlock) {
		Date start = lectureBlock.getStartDate();
		Date end = lectureBlock.getEndDate();
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
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
