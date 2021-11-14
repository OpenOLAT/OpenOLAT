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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.AppealListRepositoryController;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SearchAppealsEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppealsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final LecturesSecurityCallback secCallback;
	
	private final AppealListSearchController searchCtrl;
	private final AppealListRepositoryController appealsListCtrl;
	
	@Autowired
	private LectureService lectureService;
	
	public AppealsController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("appeals");
		
		searchCtrl = new AppealListSearchController(ureq, wControl);
		listenTo(searchCtrl);
		appealsListCtrl = new AppealListRepositoryController(ureq, wControl, secCallback);
		listenTo(appealsListCtrl);
		
		mainVC.put("search", searchCtrl.getInitialComponent());
		mainVC.put("appealsList", appealsListCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
		loadPendentAppeals();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchCtrl) {
			if(event instanceof SearchAppealsEvent) {
				doSearch((SearchAppealsEvent)event);
			}
		}
	}
	
	protected void reloadModels() {
		loadPendentAppeals();
	}
	
	private void loadPendentAppeals() {
		LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
		searchParams.setAppealStatus(Collections.singletonList(LectureBlockAppealStatus.pending));
		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(searchParams);
		if(rollCalls.isEmpty()) {
			mainVC.contextRemove("pendingAlert");
		} else {
			String[] args = new String[] { Integer.toString(rollCalls.size()) };
			String i18nKey = rollCalls.size() == 1 ? "alert.appeal.pending" : "alert.appeals.pending";
			mainVC.contextPut("pendingAlert", translate(i18nKey, args));
		}
	}
	
	private void doSearch(SearchAppealsEvent searchEvent) {
		LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
		searchParams.setStartDate(searchEvent.getStartDate());
		searchParams.setEndDate(searchEvent.getEndDate());
		if(searchEvent.getStatus() != null && !searchEvent.getStatus().isEmpty()) {
			searchParams.setAppealStatus(searchEvent.getStatus());
		} else {
			List<LectureBlockAppealStatus> status = Arrays
					.asList(LectureBlockAppealStatus.pending, LectureBlockAppealStatus.rejected, LectureBlockAppealStatus.approved);
			searchParams.setAppealStatus(status);
		}
		searchParams.setSearchString(searchEvent.getSearchString());
		appealsListCtrl.loadModel(searchParams);
	}
}
