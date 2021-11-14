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
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesMemberSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;
import org.olat.modules.lecture.ui.profile.IdentityProfileController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Search by participants with a search controller and the standard
 * member list.
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantsSearchController extends BasicController implements Activateable2 {
	
	private final BreadcrumbedStackedPanel panel;
	private IdentityProfileController profileCtrl;
	private final ParticipantsSearchListController searchCtrl;
	
	private final LecturesSecurityCallback secCallback;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityManager securityManager;
	
	public ParticipantsSearchController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		
		searchCtrl = new ParticipantsSearchListController(ureq, getWindowControl());
		listenTo(searchCtrl);
		panel = new BreadcrumbedStackedPanel("t-search", getTranslator(), this);
		putInitialPanel(panel);
		panel.pushController(translate("search.participants"), searchCtrl);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Identity".equalsIgnoreCase(type)) {
			Long identityKey = entries.get(0).getOLATResourceable().getResourceableId();
			searchCtrl.activateIdentityByKey(ureq, identityKey);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchCtrl) {
			if(event instanceof SelectLectureIdentityEvent) {
				SelectLectureIdentityEvent sie = (SelectLectureIdentityEvent)event;
				doSelect(ureq, sie.getIdentityKey());
			}
		} else if(source == profileCtrl) {
			if(event == Event.BACK_EVENT && panel.getContent() == profileCtrl.getInitialComponent()) {
				panel.popContent();
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(profileCtrl);
		profileCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void doSelect(UserRequest ureq, Long identityKey) {
		removeAsListenerAndDispose(profileCtrl);
		
		Identity profiledIdentity = securityManager.loadIdentityByKey(identityKey);
		profileCtrl = new IdentityProfileController(ureq, getWindowControl(), profiledIdentity, secCallback, false);
		listenTo(profileCtrl);
		
		String fullName = userManager.getUserDisplayName(profiledIdentity);
		panel.pushController(fullName, profileCtrl);
	}
	
	private class ParticipantsSearchListController extends LecturesMembersSearchController {
		
		public ParticipantsSearchListController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
		}
		
		protected void activateIdentityByKey(UserRequest ureq, Long identityKey) {
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			if(identity != null) {
				// make sure the user has the permission to see this identity
				tableEl.quickSearch(ureq, identity.getUser().getLastName());
				List<LecturesMemberRow> rows = tableModel.getObjects();
				LecturesMemberRow selectedRow = rows.stream()
						.filter(row -> row.getIdentityKey().equals(identityKey))
						.findFirst().orElse(null);
				if(selectedRow != null) {
					doSelect(ureq, identityKey);
				}
			}
		}
		
		@Override
		protected void doSearch(UserRequest ureq, String searchString) {
			LecturesMemberSearchParameters searchParams = new LecturesMemberSearchParameters();
			searchParams.setSearchString(searchString);
			searchParams.setViewAs(getIdentity(), secCallback.viewAs());
			List<Identity> participants = lectureService.searchParticipants(searchParams);
			List<LecturesMemberRow> rows = participants.stream()
					.map(id -> new LecturesMemberRow(id, userPropertyHandlers, getLocale())).collect(Collectors.toList());
			tableModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
}
