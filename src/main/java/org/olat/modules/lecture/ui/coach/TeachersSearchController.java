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

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesMemberSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;
import org.olat.modules.lecture.ui.event.SelectLectureRepositoryEntryEvent;
import org.olat.modules.lecture.ui.profile.IdentityProfileController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeachersSearchController extends BasicController {
	
	private BreadcrumbPanel panel;
	private IdentityProfileController profileCtrl;
	private RepositoryEntriesListController entriesSearchCtrl;
	private final TeachersSearchListController teachersSearchCtrl;
	private ParticipantsSearchListController participantsSearchCtrl;
	
	private final LecturesSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityManager securityManager;
	
	public TeachersSearchController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;

		teachersSearchCtrl = new TeachersSearchListController(ureq, getWindowControl());
		listenTo(teachersSearchCtrl);
		panel = new BreadcrumbedStackedPanel("t-search", getTranslator(), this);
		panel.pushController(translate("search.entries"), teachersSearchCtrl);
		putInitialPanel(panel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == teachersSearchCtrl) {
			if(event instanceof SelectLectureIdentityEvent) {
				SelectLectureIdentityEvent sie = (SelectLectureIdentityEvent)event;
				doSelectTeacher(ureq, sie.getIdentityKey());
			}
		} else if(source == entriesSearchCtrl) {
			if(event instanceof SelectLectureRepositoryEntryEvent) {
				SelectLectureRepositoryEntryEvent slree = (SelectLectureRepositoryEntryEvent)event;
				doSelectRepositoryEntry(ureq, slree.getEntry());
			}
		} else if(source == participantsSearchCtrl) {
			if(event instanceof SelectLectureIdentityEvent) {
				SelectLectureIdentityEvent sie = (SelectLectureIdentityEvent)event;
				doSelectParticipant(ureq, sie.getIdentityKey());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doSelectTeacher(UserRequest ureq, Long identityKey) {
		Identity teacher = securityManager.loadIdentityByKey(identityKey);
		entriesSearchCtrl = new RepositoryEntriesListController(ureq, getWindowControl(), teacher, secCallback);
		listenTo(entriesSearchCtrl);
		String fullname = userManager.getUserDisplayName(teacher);
		panel.pushController(fullname, entriesSearchCtrl);
	}
	
	private void doSelectRepositoryEntry(UserRequest ureq, RepositoryEntry entry) {
		participantsSearchCtrl = new ParticipantsSearchListController(ureq, getWindowControl(), entry);
		listenTo(participantsSearchCtrl);
		panel.pushController(entry.getDisplayname(), participantsSearchCtrl);
		participantsSearchCtrl.doSearch(ureq, null);
	}
	
	private void doSelectParticipant(UserRequest ureq, Long identityKey) {
		Identity profiledIdentity = securityManager.loadIdentityByKey(identityKey);
		profileCtrl = new IdentityProfileController(ureq, getWindowControl(), profiledIdentity, secCallback, false);
		listenTo(profileCtrl);
		String fullname = userManager.getUserDisplayName(profiledIdentity);
		panel.pushController(fullname, profileCtrl);
	}

	private class TeachersSearchListController extends LecturesMembersSearchController {
		
		public TeachersSearchListController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
		}
		
		@Override
		protected void doSearch(UserRequest ureq, String searchString) {
			LecturesMemberSearchParameters searchParams = new LecturesMemberSearchParameters();
			searchParams.setSearchString(searchString);
			searchParams.setViewAs(getIdentity(), secCallback.viewAs());
			List<Identity> teachers = lectureService.searchTeachers(searchParams);
			List<LecturesMemberRow> rows = teachers.stream()
					.map(id -> new LecturesMemberRow(id, userPropertyHandlers, getLocale())).collect(Collectors.toList());
			tableModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
	
	private class ParticipantsSearchListController extends LecturesMembersSearchController {
		
		private final RepositoryEntry restrictToEntry;
		
		public ParticipantsSearchListController(UserRequest ureq, WindowControl wControl, RepositoryEntry restrictToEntry) {
			super(ureq, wControl);
			this.restrictToEntry = restrictToEntry;
		}
		
		@Override
		protected void doSearch(UserRequest ureq, String searchString) {
			LecturesMemberSearchParameters searchParams = new LecturesMemberSearchParameters();
			searchParams.setSearchString(searchString);
			searchParams.setRepositoryEntry(restrictToEntry);
			searchParams.setViewAs(getIdentity(), secCallback.viewAs());
			List<Identity> participants = lectureService.searchParticipants(searchParams);
			List<LecturesMemberRow> rows = participants.stream()
					.map(id -> new LecturesMemberRow(id, userPropertyHandlers, getLocale())).collect(Collectors.toList());
			tableModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
}
