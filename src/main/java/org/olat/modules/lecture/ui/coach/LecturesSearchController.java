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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesSearchController extends BasicController implements Activateable2 {
	
	private final TooledStackedPanel stackPanel;
	
	private LecturesListController listCtrl;
	private LecturesSearchFormController searchForm;
	private LecturesListSegmentController multipleUsersCtrl;
	
	private final boolean admin;
	
	@Autowired
	private LectureService lectureService;
	
	public LecturesSearchController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.stackPanel = stackPanel;
		Roles roles = ureq.getUserSession().getRoles();
		admin = roles.isUserManager() || roles.isRolesManager()
				|| roles.isAdministrator() || roles.isLectureManager();//TODO roles
		
		searchForm = new LecturesSearchFormController(ureq, getWindowControl(), admin);
		listenTo(searchForm);
		putInitialPanel(searchForm.getInitialComponent());
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
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchForm == source) {
			if(event == Event.DONE_EVENT) {
				cleanUp();
				doSearch(ureq);
			}	
		} 
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(multipleUsersCtrl);
		removeAsListenerAndDispose(listCtrl);
		multipleUsersCtrl = null;
		listCtrl = null;
	}
	
	private void doSearch(UserRequest ureq) {
		
		LectureStatisticsSearchParameters params = searchForm.getSearchParameters();
		List<UserPropertyHandler> userPropertyHandlers = searchForm.getUserPropertyHandlers();
		List<LectureBlockIdentityStatistics> statistics = lectureService
				.getLecturesStatistics(params, userPropertyHandlers, getIdentity(), admin);
		
		Set<Long> identities = statistics.stream().map(LectureBlockIdentityStatistics::getIdentityKey)
			     .collect(Collectors.toSet());
		
		Controller ctrl;
		if(identities.size() <= 1) {
			listCtrl = new LecturesListController(ureq, getWindowControl(), statistics,
					userPropertyHandlers, LecturesSearchFormController.PROPS_IDENTIFIER, true, true);
			listenTo(listCtrl);
			ctrl = listCtrl;
		} else {
			multipleUsersCtrl = new LecturesListSegmentController(ureq, getWindowControl(), statistics,
					userPropertyHandlers, LecturesSearchFormController.PROPS_IDENTIFIER);
			listenTo(multipleUsersCtrl);
			ctrl = multipleUsersCtrl;
		}

		stackPanel.popUpToRootController(ureq);
		stackPanel.pushController(translate("results"), ctrl);
	}
}
