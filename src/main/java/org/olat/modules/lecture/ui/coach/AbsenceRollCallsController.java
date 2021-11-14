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

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.event.SearchLecturesBlockEvent;

/**
 * 
 * Initial date: 20 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceRollCallsController extends BasicController {
	
	private final VelocityContainer mainVC;

	private final LectureBlockRollCallSearchParameters searchParams;
	
	private final LecturesAbsenceRollCallsController rollCallsCtlr;
	private final LecturesAbsenceRollCallsSearchController searchCtrl;
	
	public AbsenceRollCallsController(UserRequest ureq, WindowControl wControl, Date date, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		searchParams = new LectureBlockRollCallSearchParameters();
		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		searchParams.setStartDate(CalendarUtils.startOfDay(date));
		searchParams.setEndDate(CalendarUtils.endOfDay(date));
		searchParams.setHasAbsence(Boolean.TRUE);

		searchCtrl = new LecturesAbsenceRollCallsSearchController(ureq, getWindowControl(), date);
		listenTo(searchCtrl);
		
		rollCallsCtlr = new LecturesAbsenceRollCallsController(ureq, getWindowControl(), searchParams, false, secCallback);
		listenTo(rollCallsCtlr);
	
		mainVC = createVelocityContainer("absences");
		mainVC.put("search", searchCtrl.getInitialComponent());
		mainVC.put("noticesList", rollCallsCtlr.getInitialComponent());
		
		putInitialPanel(mainVC);
		reloadModels();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchCtrl == source) {
			if(event instanceof SearchLecturesBlockEvent) {
				doSearch((SearchLecturesBlockEvent)event);
			}
		}
	}
	
	public void reloadModels() {
		rollCallsCtlr.loadModel();
	}
	
	private void doSearch(SearchLecturesBlockEvent event) {
		searchParams.setSearchString(event.getSearchString());
		searchParams.setStartDate(event.getStartDate());
		searchParams.setEndDate(event.getEndDate());
		rollCallsCtlr.loadModel();
	}
}
