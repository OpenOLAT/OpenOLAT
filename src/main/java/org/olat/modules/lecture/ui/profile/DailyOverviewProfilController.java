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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.DailyAbsenceNoticesController;
import org.olat.modules.lecture.ui.coach.DailyLectureBlockOverviewController;
import org.olat.modules.lecture.ui.coach.DayChooserController;
import org.olat.modules.lecture.ui.event.ChangeDayEvent;

/**
 * 
 * Initial date: 2 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyOverviewProfilController extends BasicController {

	private final VelocityContainer mainVC;
	
	private final DayChooserController dayChooserCtrl;
	private final DailyAbsenceNoticesController absencesListCtrl;
	private final DailyLectureBlockOverviewController lectureBlocksCtrl;
	
	public DailyOverviewProfilController(UserRequest ureq, WindowControl wControl,
			Identity profiledIdentity, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));

		mainVC = createVelocityContainer("daily_overview");
		
		dayChooserCtrl = new DayChooserController(ureq, getWindowControl());
		listenTo(dayChooserCtrl);
		mainVC.put("day.chooser", dayChooserCtrl.getInitialComponent());
		lectureBlocksCtrl = new DailyLectureBlockOverviewController(ureq, getWindowControl(), getCurrentDate(),
				profiledIdentity, secCallback, false);
		listenTo(lectureBlocksCtrl);
		mainVC.put("lectureBlocks", lectureBlocksCtrl.getInitialComponent());
		
		absencesListCtrl = new DailyAbsenceNoticesController(ureq, getWindowControl(), getCurrentDate(), profiledIdentity, secCallback);
		listenTo(absencesListCtrl);
		mainVC.put("absences", absencesListCtrl.getInitialComponent());

		putInitialPanel(mainVC);
		updateCurrentDate();
	}
	
	public Date getCurrentDate() {
		return dayChooserCtrl.getDate();
	}
	
	private void updateCurrentDate() {
		Date currentDate = getCurrentDate();
		String dateString = Formatter.getInstance(getLocale()).formatDate(currentDate);
		String msg = translate("cockpit.date", new String[] { dateString });
		mainVC.contextPut("date", msg);
		lectureBlocksCtrl.setCurrentDate(currentDate);
		absencesListCtrl.setCurrentDate(currentDate);
	}
	
	
	public void reloadModel() {
		absencesListCtrl.reloadModel();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == dayChooserCtrl) {
			if(event instanceof ChangeDayEvent) {
				updateCurrentDate();
			}
		}
		super.event(ureq, source, event);
	}
}
