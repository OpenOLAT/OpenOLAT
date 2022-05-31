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

import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesMemberSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SelectLectureCurriculumElementEvent;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;
import org.olat.modules.lecture.ui.profile.IdentityProfileController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Search by curriculum elements, and cascade to participants.
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumsSearchController extends BasicController {

	private BreadcrumbedStackedPanel panel;
	
	private final LecturesSecurityCallback secCallback;
	
	private IdentityProfileController profileCtrl;
	private CurriculumElementLecturesController lecturesCtrl;
	private CurriculumElementsListController elementsSearchCtrl;
	private ParticipantsSearchListController participantsSearchCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurity securityManager;
	
	public CurriculumsSearchController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		
		elementsSearchCtrl = new CurriculumElementsListController(ureq, getWindowControl(), secCallback);
		listenTo(elementsSearchCtrl);
		panel = new BreadcrumbedStackedPanel("c-search", getTranslator(), this);
		panel.pushController(translate("search.curriculums"), elementsSearchCtrl);
		putInitialPanel(panel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(elementsSearchCtrl == source) {
			if(event instanceof SelectLectureCurriculumElementEvent) {
				SelectLectureCurriculumElementEvent sie = (SelectLectureCurriculumElementEvent)event;
				if(sie.isShowAbsences()) {
					doSelectLectures(ureq, sie.getCurriculumElement());
				} else {
					doSelectCurriculumElement(ureq, sie.getCurriculumElement());
				}
			}
		} else if(source == participantsSearchCtrl) {
			if(event instanceof SelectLectureIdentityEvent) {
				SelectLectureIdentityEvent sie = (SelectLectureIdentityEvent)event;
				doSelectParticipant(ureq, sie.getIdentityKey());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doSelectCurriculumElement(UserRequest ureq, CurriculumElement element) {
		participantsSearchCtrl = new ParticipantsSearchListController(ureq, getWindowControl(), element);
		listenTo(participantsSearchCtrl);
		panel.pushController(element.getDisplayName(), participantsSearchCtrl);
		participantsSearchCtrl.doSearch(ureq, null);
	}
	
	private void doSelectLectures(UserRequest ureq, CurriculumElement element) {
		LectureRoles viewAs = secCallback.viewAs();
		CurriculumSecurityCallback curriculumSecCallback;
		if(viewAs == LectureRoles.lecturemanager || viewAs ==LectureRoles.mastercoach) {
			curriculumSecCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
		} else {
			curriculumSecCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
		}
		lecturesCtrl = new CurriculumElementLecturesController(ureq, getWindowControl(), panel, element, false, curriculumSecCallback);
		listenTo(lecturesCtrl);
		panel.pushController(element.getDisplayName(), lecturesCtrl);
	}
	
	private void doSelectParticipant(UserRequest ureq, Long identityKey) {
		Identity profiledIdentity = securityManager.loadIdentityByKey(identityKey);
		profileCtrl = new IdentityProfileController(ureq, getWindowControl(), profiledIdentity, secCallback, false);
		listenTo(profileCtrl);
		String fullname = userManager.getUserDisplayName(profiledIdentity);
		panel.pushController(fullname, profileCtrl);
	}
	
	private class ParticipantsSearchListController extends LecturesMembersSearchController {
		
		private final CurriculumElement element;
		
		public ParticipantsSearchListController(UserRequest ureq, WindowControl wControl, CurriculumElement element) {
			super(ureq, wControl);
			this.element = element;
		}
		
		@Override
		protected void doSearch(UserRequest ureq, String searchString) {
			LecturesMemberSearchParameters searchParams = new LecturesMemberSearchParameters();
			searchParams.setSearchString(searchString);
			searchParams.setCurriculumElement(element);// restriction is on the element
			List<Identity> participants = lectureService.searchParticipants(searchParams);
			List<LecturesMemberRow> rows = participants.stream()
					.map(id -> new LecturesMemberRow(id, userPropertyHandlers, getLocale())).collect(Collectors.toList());
			tableModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
}
