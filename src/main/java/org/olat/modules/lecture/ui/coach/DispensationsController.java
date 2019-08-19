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
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
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
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SearchAbsenceNoticeEvent;
import org.olat.modules.lecture.ui.wizard.AbsenceNotice1UserSearchStep;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DispensationsController extends BasicController {

	private Link addDispensationButton;
	private Link addNoticeOfAbsenceButton;
	private final VelocityContainer mainVC;

	private final boolean withAddAbsence;
	private final LecturesSecurityCallback secCallback;
	private final AbsenceNoticeSearchParameters searchParams = new AbsenceNoticeSearchParameters();
	
	private StepsMainRunController addNoticeCtrl;
	private AbsenceNoticeSearchController searchCtrl;
	private AbsenceNoticesListController noticesListCtlr;

	@Autowired
	private MailManager mailService;
	@Autowired
	private LectureService lectureService;
	
	public DispensationsController(UserRequest ureq, WindowControl wControl, Date currentDate,
			LecturesSecurityCallback secCallback, boolean withSearch, boolean withAddAbsence) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		this.secCallback = secCallback;
		this.withAddAbsence = withAddAbsence;
		
		searchParams.addTypes(AbsenceNoticeType.notified, AbsenceNoticeType.dispensation);
		searchParams.setViewAs(getIdentity(), ureq.getUserSession().getRoles(), secCallback.viewAs());
		searchParams.setLinkedToRollCall(false);
		searchParams.setStartDate(CalendarUtils.startOfDay(currentDate));
		searchParams.setEndDate(CalendarUtils.endOfDay(currentDate));
	
		searchCtrl = new AbsenceNoticeSearchController(ureq, getWindowControl(), currentDate);
		listenTo(searchCtrl);
		noticesListCtlr = new AbsenceNoticesListController(ureq, getWindowControl(),
				null, false, secCallback, "notices");
		listenTo(noticesListCtlr);
		
		mainVC = createVelocityContainer("dispensations");
		if(withSearch) {
			mainVC.put("search", searchCtrl.getInitialComponent());
		}
		mainVC.put("noticesList", noticesListCtlr.getInitialComponent());

		addDispensationButton = LinkFactory.createButton("add.dispensation", mainVC, this);
		addDispensationButton.setIconLeftCSS("o_icon o_icon_add");
		addDispensationButton.setVisible(withAddAbsence && secCallback.canAddDispensations());
		addNoticeOfAbsenceButton = LinkFactory.createButton("add.notice.absence", mainVC, this);
		addNoticeOfAbsenceButton.setIconLeftCSS("o_icon o_icon_add");
		addNoticeOfAbsenceButton.setVisible(withAddAbsence && secCallback.canAddNoticeOfAbsences());
		
		putInitialPanel(mainVC);
		noticesListCtlr.loadModel(searchParams);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchCtrl == source) {
			if(event instanceof SearchAbsenceNoticeEvent) {
				doSearch((SearchAbsenceNoticeEvent)event);
			}
		} else if(addNoticeCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					noticesListCtlr.loadModel(searchParams);
				}
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addNoticeCtrl);
		addNoticeCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == addDispensationButton) {
			doAddNotice(ureq, AbsenceNoticeType.dispensation);
		} else if(source == addNoticeOfAbsenceButton) {
			doAddNotice(ureq, AbsenceNoticeType.notified);
		}
	}
	
	public void reloadModel() {
		noticesListCtlr.reloadModel();
	}
	
	private void doSearch(SearchAbsenceNoticeEvent event) {
		searchParams.setStartDate(event.getStartDate());
		searchParams.setEndDate(event.getEndDate());
		searchParams.setSearchString(event.getSearchString());
		searchParams.setAuthorized(event.getAuthorized());
		searchParams.setAbsenceCategory(event.getAbsenceCategory());
		searchParams.setTypes(event.getTypes());
		noticesListCtlr.loadModel(searchParams);
	}
	
	private void doAddNotice(UserRequest ureq, AbsenceNoticeType type) {
		if(!withAddAbsence) return;
		
		final EditAbsenceNoticeWrapper noticeWrapper = new EditAbsenceNoticeWrapper(type);
		AbsenceNotice1UserSearchStep step = new AbsenceNotice1UserSearchStep(ureq, noticeWrapper, secCallback);
		StepRunnerCallback stop = (uureq, swControl, runContext) -> {
			if(noticeWrapper.getAbsenceNotice() == null) {
				Identity absentIdentity = noticeWrapper.getIdentity();
				lectureService.createAbsenceNotice(absentIdentity, noticeWrapper.getAbsenceNoticeType(), noticeWrapper.getAbsenceNoticeTarget(),
						noticeWrapper.getStartDate(), noticeWrapper.getEndDate(),
						noticeWrapper.getAbsenceCategory(), noticeWrapper.getAbsenceReason(), noticeWrapper.getAuthorized(),
						noticeWrapper.getEntries(), noticeWrapper.getLectureBlocks(), getIdentity());
			}
			
			if(noticeWrapper.getIdentitiesToContact() != null && !noticeWrapper.getIdentitiesToContact().isEmpty()) {
				inform(ureq, noticeWrapper);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		String title = translate("add.dispensation.title");
		if(type == AbsenceNoticeType.notified) {
			title = translate("add.notice.absence.title");
		}
		
		removeAsListenerAndDispose(addNoticeCtrl);
		addNoticeCtrl = new StepsMainRunController(ureq, getWindowControl(), step, stop, null, title, "");
		listenTo(addNoticeCtrl);
		getWindowControl().pushAsModalDialog(addNoticeCtrl.getInitialComponent());
	}
	
	private void inform(UserRequest ureq, EditAbsenceNoticeWrapper noticeWrapper) {
		boolean success = false;
		try {
			List<ContactList> contactList = new ArrayList<>();
			ContactList memberList = new ContactList(translate("contact.teachers.list.name"));
			memberList.addAllIdentites(noticeWrapper.getIdentitiesToContact());
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(getIdentity());						
			bundle.setContactLists(contactList);
			bundle.setContent(noticeWrapper.getContactSubject(), noticeWrapper.getContactSubject());
			MailerResult result = mailService.sendMessage(bundle);
			success = result.isSuccessful();
			if (success) {
				showInfo("msg.send.ok");
				// do logging
				ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				Roles roles = ureq.getUserSession().getRoles();
				boolean admin = roles.isAdministrator() || roles.isSystemAdmin();
				MailHelper.printErrorsAndWarnings(result, getWindowControl(), admin, getLocale());
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		} catch (Exception e) {
			logError("", e);
			showWarning("error.msg.send.nok");
		}
	}
}
