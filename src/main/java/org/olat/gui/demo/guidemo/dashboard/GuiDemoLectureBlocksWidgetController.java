/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.gui.demo.guidemo.dashboard;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.ui.LectureBlocksWidgetController;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;

/**
 * 
 * Initial date: Jan 8, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoLectureBlocksWidgetController extends LectureBlocksWidgetController {
	
	private final Date dueDate;
	private final List<LectureBlock> lectureBlocks;
	private LectureBlockRef nextScheduledBlock;
	private long counter = 0;

	public GuiDemoLectureBlocksWidgetController(UserRequest ureq, WindowControl wControl, Date dueDate) {
		super(ureq, wControl);
		this.dueDate = dueDate;
		
		lectureBlocks = initLectureBlocks();
		nextScheduledBlock = initNextScheduledBlock();
		
		initForm(ureq);
		dayNavEl.setSelectedDate(DateUtils.addDays(dueDate, 2));
		reload();
	}

	private List<LectureBlock> initLectureBlocks() {
		Date currentDate = DateUtils.getPreviousDay(DateUtils.addDays(dueDate, 1), DayOfWeek.MONDAY);
		
		List<LectureBlock> lectureBlocks = new ArrayList<>();
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 0, 0), "SCI-BAS-101", "The Basics of Scientific Inquiry", "Room B1.12", false, 30));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 30, 0), "MATH-1A", "Understanding Fractions and Decimals", null, true, 120));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 12, 0, 0), "GEO-100", "World Geography", "Earth", false, 60));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 14, 0, 0), "HIST-203 (provisional)", "The Causes of the American Revolution", null, false, 0));
		currentDate = DateUtils.addDays(currentDate, 1);
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 8, 15, 0), "BIO-110", "How Plants Make Their Own Food", "Room B2.15", false, 1200));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 14, 0, 0), "CHEM-101", "Structure of the Atom", "Santiago", false, 60));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 15, 10, 0), "LIT-204", "Short Stories", "Library 12", false, 0));
		currentDate = DateUtils.addDays(currentDate, 2);
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 0, 0), "MATH-102", "Fundamentals of Algebraic Expressions", "Santiago", false, 300));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 13, 0, 0), "GEO-115", "The Water Cycle and Its Importance", "Pacific ocean, small island", false, 120));
		currentDate = DateUtils.addDays(currentDate, 1);
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 8, 0, 0), null, "Introduction to Computer Programming", "Room G2.18", false, 45));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 9, 0, 0), "POL-120", "The Role of Government in Society", "S2.09", false, 50));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 0, 0), "BIO-210", "Photosynthesis Explained", "B1.19", false, 10));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 15, 0), "GEO-220", "Understanding Climate and Weather", "", false, 90));
		currentDate = DateUtils.addDays(currentDate, 3);
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 0, 0), "BIO-205", "Human Digestive System", "Room L1.05", false, 120));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 13, 0, 0), "LING-101", "Basic Grammar", null, true, 60));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 14, 30, 0), "HIST-110", "The History of Ancient Egypt", "", false, 0));
		currentDate = DateUtils.addDays(currentDate, 1);
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 0, 0), null, "Introduction to Statistics and Data", "Math Building", false, 60));
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 17, 0, 0), "PHYS-130", "Energy", "outdooors", false, 180));
		currentDate = DateUtils.addDays(currentDate, 7);
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 19, 0, 0), "PHIL-101", "Critical Thinking and Problem Solving", "A1", false, 45));
		currentDate = DateUtils.addDays(currentDate, 70);
		lectureBlocks.add(createLecture(DateUtils.setTime(currentDate, 10, 0, 0), "SPT-100", "Physical Fitness and Health", "Room SPT-Hall 1", false, 5));
		
		return lectureBlocks;
	}

	private LectureBlock createLecture(Date startDate, String externalRef, String title, String location, boolean onlineMeeting, int durationMinutes) {
		LectureBlockImpl lectureBlock = new LectureBlockImpl();
		lectureBlock.setKey(counter++);
		lectureBlock.setExternalRef(externalRef);
		lectureBlock.setTitle(title);
		lectureBlock.setLocation(location);
		if (onlineMeeting) {
			lectureBlock.setBBBMeeting(new BigBlueButtonMeetingImpl());
		}
		lectureBlock.setStartDate(startDate);
		if (durationMinutes > 0) {
			lectureBlock.setEndDate(DateUtils.addMinutes(startDate, durationMinutes));
		}
		
		return lectureBlock;
	}

	private LectureBlockRef initNextScheduledBlock() {
		for (LectureBlock lectureBlock : lectureBlocks) {
			LectureBlockVirtualStatus vStatus = LectureBlockStatusCellRenderer.calculateStatus(lectureBlock);
			if (LectureBlockVirtualStatus.PLANNED == vStatus) {
				return lectureBlock;
			}
		}
		return null;
	}

	@Override
	protected List<LectureBlock> loadLectureBlocks(Date fromDate, Date toDate) {
		return lectureBlocks.stream()
			.filter(lb -> lb.getStartDate().after(fromDate) && lb.getStartDate().before(toDate))
			.collect(Collectors.toList());
	}

	@Override
	protected LectureBlockRef loadNextScheduledBlock() {
		return nextScheduledBlock;
	}

	@Override
	protected Date getPrevLectureBlock(Date date) {
		Date prev = null;
		for (LectureBlock lectureBlock : lectureBlocks) {
			if (date.after(lectureBlock.getStartDate())) {
				prev = lectureBlock.getStartDate();
			} else {
				return prev;
			}
		}
		return prev;
	}

	@Override
	protected Date getNextLectureBlock(Date date) {
		for (LectureBlock lectureBlock : lectureBlocks) {
			if (date.before(lectureBlock.getStartDate())) {
				return lectureBlock.getStartDate();
			}
		}
		return null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == showAllLink) {
			doShowAll();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doShowAll() {
		showInfo("show.all.message");
	}
	
}
