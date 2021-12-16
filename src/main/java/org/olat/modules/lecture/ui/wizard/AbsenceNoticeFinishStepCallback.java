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
package org.olat.modules.lecture.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlockAuditLog.Action;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeFinishStepCallback implements StepRunnerCallback {
	
	private static final Logger log = Tracing.createLoggerFor(AbsenceNoticeFinishStepCallback.class);
	
	private final Translator translator;
	private final EditAbsenceNoticeWrapper noticeWrapper;
	
	@Autowired
	private MailManager mailService;
	@Autowired
	private LectureService lectureService;
	
	public AbsenceNoticeFinishStepCallback(EditAbsenceNoticeWrapper noticeWrapper, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.translator = Util.createPackageTranslator(ContactFormController.class, translator.getLocale(), translator);
		this.noticeWrapper = noticeWrapper;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		if(noticeWrapper.getAbsenceNotice() == null) {
			Identity absentIdentity = noticeWrapper.getIdentity();
			AbsenceNotice notice = lectureService.createAbsenceNotice(absentIdentity, noticeWrapper.getAbsenceNoticeType(),
					noticeWrapper.getAbsenceNoticeTarget(), noticeWrapper.getStartDate(), noticeWrapper.getEndDate(),
					noticeWrapper.getAbsenceCategory(), noticeWrapper.getAbsenceReason(), noticeWrapper.getAuthorized(),
					noticeWrapper.getEntries(), noticeWrapper.getLectureBlocks(), ureq.getIdentity());
			
			List<VFSItem> newFiles = new ArrayList<>();
			if(noticeWrapper.getTempUploadFolder() != null) {
				newFiles.addAll(noticeWrapper.getTempUploadFolder().getItems(new VFSSystemItemFilter()));
			}
			notice = lectureService.updateAbsenceNoticeAttachments(notice, newFiles, noticeWrapper.getAttachmentsToDelete(), ureq.getIdentity());
			
			String after = lectureService.toAuditXml(notice);
			lectureService.auditLog(Action.createAbsenceNotice, null, after, null, notice, absentIdentity, ureq.getIdentity());
		} else {
			AbsenceNotice absenceNotice = lectureService.getAbsenceNotice(noticeWrapper.getAbsenceNotice());
			String before = lectureService.toAuditXml(absenceNotice);
			
			absenceNotice.setNoticeType(noticeWrapper.getAbsenceNoticeType());
			absenceNotice.setNoticeTarget(noticeWrapper.getAbsenceNoticeTarget());
			absenceNotice.setStartDate(noticeWrapper.getStartDate());
			absenceNotice.setEndDate(noticeWrapper.getEndDate());
			absenceNotice.setAbsenceCategory(noticeWrapper.getAbsenceCategory());
			absenceNotice.setAbsenceReason(noticeWrapper.getAbsenceReason());
			absenceNotice.setAbsenceAuthorized(noticeWrapper.getAuthorized());
			
			lectureService.updateAbsenceNotice(absenceNotice, null,
					noticeWrapper.getEntries(), noticeWrapper.getLectureBlocks(), ureq.getIdentity());
			
			String after = lectureService.toAuditXml(absenceNotice);
			lectureService.auditLog(Action.updateAbsenceNotice, before, after, null, absenceNotice, noticeWrapper.getIdentity(), ureq.getIdentity());
		}
		
		if(noticeWrapper.getTempUploadFolder() != null) {
			noticeWrapper.getTempUploadFolder().deleteSilently();
		}

		if(noticeWrapper.getIdentitiesToContact() != null && !noticeWrapper.getIdentitiesToContact().isEmpty()) {
			inform(ureq, wControl);
		}
		return StepsMainRunController.DONE_MODIFIED;
	}

	private void inform(UserRequest ureq, WindowControl wControl) {
		boolean success = false;
		try {
			ContactList memberList = new ContactList(translator.translate("contact.teachers.list.name"));
			memberList.addAllIdentites(noticeWrapper.getIdentitiesToContact());
			MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(ureq.getIdentity());						
			bundle.setContactList(memberList);
			bundle.setContent(noticeWrapper.getContactSubject(), noticeWrapper.getContactBody());
			MailerResult result = mailService.sendMessage(bundle);
			success = result.isSuccessful();
			if (success) {
				wControl.setInfo(translator.translate("msg.send.ok"));
				// do logging
				ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
			} else {
				Roles roles = ureq.getUserSession().getRoles();
				boolean admin = roles.isAdministrator() || roles.isSystemAdmin();
				MailHelper.printErrorsAndWarnings(result, wControl, admin, ureq.getLocale());
			}
		} catch (Exception e) {
			log.error("", e);
			wControl.setWarning(translator.translate("error.msg.send.nok"));
		}
	}
}
