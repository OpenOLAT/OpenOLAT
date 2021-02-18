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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog.Action;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditNoticeController extends FormBasicController {
	
	private EditReasonController editReasonCtrl;
	private EditDatesLecturesEntriesController datesAndLecturesCtrl;
	
	private AbsenceNotice absenceNotice;
	private final EditAbsenceNoticeWrapper noticeWrapper;
	
	@Autowired
	private LectureService lectureService;
	
	public EditNoticeController(UserRequest ureq, WindowControl wControl, AbsenceNotice notice, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "edit_notice");
		
		absenceNotice = notice;
		noticeWrapper = EditAbsenceNoticeWrapper.valueOf(absenceNotice);
		
		if(notice.getNoticeTarget() == AbsenceNoticeTarget.lectureblocks) {
			List<AbsenceNoticeToLectureBlock> relations = lectureService.getAbsenceNoticeToLectureBlocks(notice);
			List<LectureBlock> lectureBlocks = relations.stream()
					.map(AbsenceNoticeToLectureBlock::getLectureBlock).collect(Collectors.toList());
			noticeWrapper.setLectureBlocks(lectureBlocks);
		} else if(notice.getNoticeTarget() == AbsenceNoticeTarget.entries) {
			List<AbsenceNoticeToRepositoryEntry> relations = lectureService.getAbsenceNoticeToRepositoryEntries(notice);
			List<RepositoryEntry> entries = relations.stream()
					.map(AbsenceNoticeToRepositoryEntry::getEntry).collect(Collectors.toList());
			noticeWrapper.setEntries(entries);
		}
		
		datesAndLecturesCtrl = new EditDatesLecturesEntriesController(ureq, wControl, mainForm, noticeWrapper, secCallback, false);
		listenTo(datesAndLecturesCtrl);

		editReasonCtrl = new EditReasonController(ureq, wControl, mainForm, noticeWrapper, secCallback, false);
		listenTo(editReasonCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("reason", editReasonCtrl.getInitialFormItem());
		formLayout.add("datesAndLectures", datesAndLecturesCtrl.getInitialFormItem());
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void doDispose() {
		editReasonCtrl.deleteTempStorage();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		editReasonCtrl.formOK(ureq);
		datesAndLecturesCtrl.formOK(ureq);
		
		absenceNotice = lectureService.getAbsenceNotice(absenceNotice);
		String before = lectureService.toAuditXml(absenceNotice);
		
		Boolean authorized = absenceNotice.getAbsenceAuthorized();
		
		absenceNotice.setStartDate(noticeWrapper.getStartDate());
		absenceNotice.setEndDate(noticeWrapper.getEndDate());
		absenceNotice.setAbsenceAuthorized(noticeWrapper.getAuthorized());
		absenceNotice.setAbsenceCategory(noticeWrapper.getAbsenceCategory());
		absenceNotice.setAbsenceReason(noticeWrapper.getAbsenceReason());
		absenceNotice.setNoticeTarget(noticeWrapper.getAbsenceNoticeTarget());
		
		List<RepositoryEntry> entries = null;
		List<LectureBlock> lectureBlocks = null;
		if(noticeWrapper.getAbsenceNoticeTarget() == AbsenceNoticeTarget.entries) {
			entries = noticeWrapper.getEntries();
		} else if(noticeWrapper.getAbsenceNoticeTarget() == AbsenceNoticeTarget.lectureblocks) {
			lectureBlocks = noticeWrapper.getLectureBlocks();
		}
		
		Identity authorizer = null;
		if(absenceNotice.getAbsenceAuthorized() != null && absenceNotice.getAbsenceAuthorized().booleanValue()
				&& (authorized == null || !authorized.booleanValue())) {
			authorizer = getIdentity();
		}
		
		absenceNotice = lectureService.updateAbsenceNotice(absenceNotice, authorizer, entries, lectureBlocks, getIdentity());
		List<VFSItem> newFiles = new ArrayList<>();
		if(noticeWrapper.getTempUploadFolder() != null) {
			newFiles.addAll(noticeWrapper.getTempUploadFolder().getItems(new VFSSystemItemFilter()));
		}
		absenceNotice = lectureService.updateAbsenceNoticeAttachments(absenceNotice, newFiles, noticeWrapper.getAttachmentsToDelete(), getIdentity());
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		String after = lectureService.toAuditXml(absenceNotice);
		lectureService.auditLog(Action.updateAbsenceNotice, before, after, null, absenceNotice, noticeWrapper.getIdentity(), getIdentity());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
