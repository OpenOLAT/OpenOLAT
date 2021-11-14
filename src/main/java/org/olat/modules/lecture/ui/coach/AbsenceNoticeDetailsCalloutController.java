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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockWithNotice;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeDetailsCalloutController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public AbsenceNoticeDetailsCalloutController(UserRequest ureq, WindowControl wControl, AbsenceNoticeRow row) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		AbsenceNotice notice = lectureService.getAbsenceNotice(row);
		mainVC = createVelocityContainer("notice_details");
		init(notice, row.getLectureBlocks(), row.getEntriesList(), row.getTeachers());
		putInitialPanel(mainVC);
	}
	
	public AbsenceNoticeDetailsCalloutController(UserRequest ureq, WindowControl wControl, AbsenceNotice notice) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		notice = lectureService.getAbsenceNotice(notice);
		mainVC = createVelocityContainer("notice_details");
		
		List<LectureBlockWithNotice> lectureBlocksWith = lectureService.getLectureBlocksWithAbsenceNotices(Collections.singletonList(notice));
		Set<RepositoryEntry> entries = lectureBlocksWith.stream()
				.filter(block -> block.getEntry() != null)
				.map(LectureBlockWithNotice::getEntry)
				.collect(Collectors.toSet());
		List<LectureBlock> lectureBlocks = lectureBlocksWith.stream()
				.map(LectureBlockWithNotice::getLectureBlock)
				.collect(Collectors.toList());

		List<Identity> teachers = lectureService.getTeachers(lectureBlocks);
		init(notice, lectureBlocks, entries, teachers);
		putInitialPanel(mainVC);
	}
	
	private void init(AbsenceNotice notice, List<LectureBlock> lectureBlocks, Collection<RepositoryEntry> entries, List<Identity> teachers) {
		// info absences, reason, status, type
		AbsenceCategory category = notice.getAbsenceCategory();
		if(category != null) {
			mainVC.contextPut("category", StringHelper.escapeHtml(category.getTitle()));
		}
		String reason = notice.getAbsenceReason();
		if(StringHelper.containsNonWhitespace(reason)) {
			mainVC.contextPut("category", StringHelper.escapeHtml(reason));
		}
		
		// lectures blocks
		List<String> blocks = new ArrayList<>();
		for(LectureBlock lectureBlock:lectureBlocks) {
			blocks.add(lectureBlock.getTitle());
		}
		mainVC.contextPut("lectureBlocks", blocks);

		// courses
		List<String> entriesInfo = new ArrayList<>();
		for(RepositoryEntry entry:entries) {
			entriesInfo.add(entry.getDisplayname());
		}
		Collections.sort(entriesInfo);
		mainVC.contextPut("entries", entriesInfo);

		// teachers
		List<String> teacherList = new ArrayList<>();
		for(Identity teacher:teachers) {
			String teacherFullName = userManager.getUserDisplayName(teacher);
			teacherList.add(teacherFullName);
		}
		mainVC.contextPut("teachers", teacherList);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
