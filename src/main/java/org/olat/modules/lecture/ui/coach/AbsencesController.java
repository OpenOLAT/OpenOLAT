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
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SearchAbsenceNoticeEvent;
import org.olat.modules.lecture.ui.wizard.AbsenceNotice1UserSearchStep;
import org.olat.modules.lecture.ui.wizard.AbsenceNoticeCancelStepCallback;
import org.olat.modules.lecture.ui.wizard.AbsenceNoticeFinishStepCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsencesController extends BasicController {
	
	private Link addAbsenceButton;
	private VelocityContainer mainVC;

	private final Roles roles;
	private final LecturesSecurityCallback secCallback;
	private final AbsenceNoticeSearchParameters searchParams = new AbsenceNoticeSearchParameters();
	
	private StepsMainRunController addNoticeCtrl;
	private AbsenceNoticeSearchController searchCtrl;
	private AbsenceNoticesListController noticesListCtlr;

	@Autowired
	private LectureService lectureService;
	
	public AbsencesController(UserRequest ureq, WindowControl wControl, Date currentDate, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		this.secCallback = secCallback;
		this.roles = ureq.getUserSession().getRoles();
		searchParams.addTypes(AbsenceNoticeType.absence);
		searchParams.setViewAs(getIdentity(), ureq.getUserSession().getRoles(), secCallback.viewAs());
		searchParams.setLinkedToRollCall(true);
		searchParams.setStartDate(CalendarUtils.startOfDay(currentDate));
		searchParams.setEndDate(CalendarUtils.endOfDay(currentDate));
		
		searchCtrl = new AbsenceNoticeSearchController(ureq, getWindowControl(), currentDate);
		listenTo(searchCtrl);
		noticesListCtlr = new AbsenceNoticesListController(ureq, getWindowControl(),
				null, true, secCallback, "absences");
		listenTo(noticesListCtlr);
		
		mainVC = createVelocityContainer("absences");
		mainVC.put("search", searchCtrl.getInitialComponent());
		mainVC.put("noticesList", noticesListCtlr.getInitialComponent());
		
		addAbsenceButton = LinkFactory.createButton("add.absence", mainVC, this);
		addAbsenceButton.setIconLeftCSS("o_icon o_icon_add");
		addAbsenceButton.setVisible(secCallback.canAddAbsences());
		
		putInitialPanel(mainVC);
		noticesListCtlr.loadModel(searchParams);
		loadUnauthorizedAbsences();
	}
	
	private void loadUnauthorizedAbsences() {
		AbsenceNoticeSearchParameters unauthorizedSearchParams = new AbsenceNoticeSearchParameters();
		unauthorizedSearchParams.addTypes(AbsenceNoticeType.absence);
		unauthorizedSearchParams.setLinkedToRollCall(true);
		unauthorizedSearchParams.setViewAs(getIdentity(), roles, secCallback.viewAs());

		List<AbsenceNoticeInfos> unauthorizedAbsences = lectureService.searchAbsenceNotices(unauthorizedSearchParams);
		if(unauthorizedAbsences.isEmpty()) {
			mainVC.contextRemove("unauthorizedAbsencesMsg");
		} else if (unauthorizedAbsences.size() == 1) {
			String msg = translate("error.unauthorized.absence.msg");
			mainVC.contextPut("unauthorizedAbsencesMsg", msg);
		} else {
			String msg = translate("error.unauthorized.absences.msg", new String[] { Integer.toString(unauthorizedAbsences.size()) });
			mainVC.contextPut("unauthorizedAbsencesMsg", msg);
		}
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
		if(source == addAbsenceButton) {
			doAddNotice(ureq);
		}
	}
	
	protected void reloadModels() {
		noticesListCtlr.reloadModel();
		loadUnauthorizedAbsences();
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
	
	private void doAddNotice(UserRequest ureq) {
		final EditAbsenceNoticeWrapper noticeWrapper = new EditAbsenceNoticeWrapper(AbsenceNoticeType.absence);
		AbsenceNotice1UserSearchStep step = new AbsenceNotice1UserSearchStep(ureq, noticeWrapper, secCallback);
		StepRunnerCallback stop = new AbsenceNoticeFinishStepCallback(noticeWrapper, getTranslator());
		StepRunnerCallback cancel = new AbsenceNoticeCancelStepCallback(noticeWrapper);

		removeAsListenerAndDispose(addNoticeCtrl);
		String title = translate("add.absence.title");
		addNoticeCtrl = new StepsMainRunController(ureq, getWindowControl(), step, stop, cancel, title, "");
		listenTo(addNoticeCtrl);
		getWindowControl().pushAsModalDialog(addNoticeCtrl.getInitialComponent());
	}

}
