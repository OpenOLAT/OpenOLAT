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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherToolOverviewController extends AbstractTeacherOverviewController {
	
	private final LecturesSecurityCallback secCallback;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public TeacherToolOverviewController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, secCallback.viewAs() == LectureRoles.lecturemanager, "Lectures::UserTools", true, false);
		this.secCallback = secCallback;
		initTables(ureq, true, false);
		
		LecturesBlockSearchParameters searchParams = getDefaultSearchParameters();
		searchCtrl.setDefaultDates(searchParams.getStartDate(), searchParams.getEndDate());
		loadModel(searchParams);
	}
	
	private LecturesBlockSearchParameters getDefaultSearchParameters() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -14);
		Date start = cal.getTime();
		searchParams.setStartDate(start);
		cal.add(Calendar.DATE, 28);
		Date end = cal.getTime();
		searchParams.setEndDate(end);
		return searchParams;
	}

	@Override
	protected List<LectureBlockRow> getRows(LecturesBlockSearchParameters searchParams) {
		if(searchParams == null) {
			searchParams = new LecturesBlockSearchParameters();
		}
		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		
		List<LectureBlockWithTeachers> blocksWithTeachers = lectureService
				.getLectureBlocksWithTeachers(searchParams);
		
		List<LectureBlockRow> rows = new ArrayList<>(blocksWithTeachers.size());
		for(LectureBlockWithTeachers blockWithTeachers:blocksWithTeachers) {
			LectureBlock block = blockWithTeachers.getLectureBlock();
			RepositoryEntry entry = block.getEntry();
			
			StringBuilder teachers = new StringBuilder(32);
			String separator = translate("user.fullname.separator");
			for(Identity teacher:blockWithTeachers.getTeachers()) {
				if(teachers.length() > 0) teachers.append(" ").append(separator).append(" ");
				teachers.append(userManager.getUserDisplayName(teacher));
			}
			
			rows.add(new LectureBlockRow(block, entry.getDisplayname(), entry.getExternalRef(),
					teachers.toString(), true, blockWithTeachers.isAssessmentMode()));
		}
		return rows;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//reload table... first
		super.event(ureq, source, event);
		
		if(source instanceof TeacherLecturesTableController || source instanceof TeacherRollCallController) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				stackPanel.popUpToRootController(ureq);
			}
		}
	}
}