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
package org.olat.modules.lecture.ui.profile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
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
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.ui.AppealListRepositoryController;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController;
import org.olat.modules.lecture.ui.coach.DispensationsController;
import org.olat.modules.lecture.ui.wizard.AbsenceNotice3LecturesEntriesStep;
import org.olat.user.DisplayPortraitController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityProfileController extends BasicController implements Activateable2 {
	
	private Link backLink;
	private Link addAbsence;
	private Link addDispensation;
	private Link addNoticeOfAbsence;
	
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	
	private final Identity profiledIdentity;
	private final LecturesSecurityCallback secCallback;
	
	private StepsMainRunController addNoticeCtrl;
	private DispensationsController dispensationsCtrl;
	private AppealListRepositoryController appealsCtrl;
	private DailyOverviewProfilController dailyOverviewCtrl;
	private ParticipantLecturesOverviewController lecturesCtrl;
	
	@Autowired
	private MailManager mailService;
	@Autowired
	private LectureService lectureService;
	
	public IdentityProfileController(UserRequest ureq, WindowControl wControl, Identity profiledIdentity,
			LecturesSecurityCallback secCallback, boolean withBack) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.profiledIdentity = profiledIdentity;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("profile");
		if(withBack) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
			mainVC.put("back", backLink);
		}
		
		DisplayPortraitController portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), profiledIdentity, true, false);
		listenTo(portraitCtr);
		mainVC.put("portrait", portraitCtr.getInitialComponent());
		UserShortDescription userDescr = new UserShortDescription(ureq, getWindowControl(), profiledIdentity);
		listenTo(userDescr);
		mainVC.put("userDescr", userDescr.getInitialComponent());
		
		//new absence, new notice of absence, new dispensation
		addAbsence = LinkFactory.createButton("add.absence", mainVC, this);
		addAbsence.setIconLeftCSS("o_icon o_icon_add");
		addAbsence.setVisible(secCallback.canAddAbsences());

		addNoticeOfAbsence = LinkFactory.createButton("add.notice.absence", mainVC, this);
		addNoticeOfAbsence.setIconLeftCSS("o_icon o_icon_add");
		addNoticeOfAbsence.setVisible(secCallback.canAddNoticeOfAbsences());

		addDispensation = LinkFactory.createButton("add.dispensation", mainVC, this);
		addDispensation.setIconLeftCSS("o_icon o_icon_add");
		addDispensation.setVisible(secCallback.canAddDispensations());

		tabPane = new TabbedPane("tabPane", getLocale());
		tabPane.setElementCssClass("o_sel_lectures_profile");
		tabPane.addListener(this);
		mainVC.put("tabPane", tabPane);
		
		// day overview
		dailyOverviewCtrl = new DailyOverviewProfilController(ureq, getWindowControl(), profiledIdentity, secCallback);
		listenTo(dailyOverviewCtrl);
		tabPane.addTab(translate("cockpit.day.overview"), dailyOverviewCtrl);
		
		// list of lectures
		tabPane.addTab(translate("user.overview.lectures"), uureq -> {
			lecturesCtrl = new ParticipantLecturesOverviewController(uureq, getWindowControl(), profiledIdentity, null,
					true, true, true, true, true, false);
			listenTo(lecturesCtrl);
			BreadcrumbedStackedPanel lecturesPanel = new BreadcrumbedStackedPanel("lectures", getTranslator(), lecturesCtrl);
			lecturesPanel.pushController(translate("user.overview.lectures"), lecturesCtrl);
			lecturesCtrl.setBreadcrumbPanel(lecturesPanel);
			lecturesPanel.setInvisibleCrumb(1);
			return lecturesPanel;
		});

		// dispensation
		tabPane.addTab(translate("user.overview.dispensation"), uureq -> {
			dispensationsCtrl = new DispensationsController(uureq, getWindowControl(), null, secCallback, false, false);
			listenTo(dispensationsCtrl);
			return dispensationsCtrl.getInitialComponent();
		});

		// appeals
		tabPane.addTab(translate("user.overview.appeals"), uureq -> {
			appealsCtrl = new AppealListRepositoryController(uureq, getWindowControl(), profiledIdentity, secCallback);
			listenTo(appealsCtrl);
			return appealsCtrl.getInitialComponent();
		});
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(addAbsence == source) {
			doAddNotice(ureq, AbsenceNoticeType.absence);
		} else if(addNoticeOfAbsence == source) {
			doAddNotice(ureq, AbsenceNoticeType.notified);
		} else if(addDispensation == source) {
			doAddNotice(ureq, AbsenceNoticeType.dispensation);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addNoticeCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					updateModels();
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
	
	private void updateModels() {
		if(dispensationsCtrl != null) {
			dispensationsCtrl.reloadModel();
		}
		if(dailyOverviewCtrl != null) {
			dailyOverviewCtrl.reloadModel();
		}
	}
	
	private void doAddNotice(UserRequest ureq, AbsenceNoticeType type) {
		final EditAbsenceNoticeWrapper noticeWrapper = new EditAbsenceNoticeWrapper(type);
		noticeWrapper.setIdentity(profiledIdentity);
		noticeWrapper.setCurrentDate(new Date());
		
		AbsenceNotice3LecturesEntriesStep step = new AbsenceNotice3LecturesEntriesStep(ureq, noticeWrapper, secCallback, true);
		StepRunnerCallback stop = (uureq, swControl, runContext) -> {
			if(noticeWrapper.getAbsenceNotice() == null) {
				Identity absentIdentity = noticeWrapper.getIdentity();
				lectureService.createAbsenceNotice(absentIdentity, noticeWrapper.getAbsenceNoticeType(), noticeWrapper.getAbsenceNoticeTarget(),
						noticeWrapper.getStartDate(), noticeWrapper.getEndDate(),
						noticeWrapper.getAbsenceCategory(), noticeWrapper.getAbsenceReason(), noticeWrapper.getAuthorized(),
						noticeWrapper.getEntries(), noticeWrapper.getLectureBlocks());
			}
			
			if(noticeWrapper.getIdentitiesToContact() != null && !noticeWrapper.getIdentitiesToContact().isEmpty()) {
				inform(ureq, noticeWrapper);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		String title = translate("add.dispensation.title");
		if(type == AbsenceNoticeType.notified) {
			title = translate("add.notice.absence.title");
		} else if(type == AbsenceNoticeType.absence) {
			title = translate("add.absence.title");
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
