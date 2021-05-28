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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.TeacherRollCallController;
import org.olat.modules.lecture.ui.component.LectureBlockComparator;
import org.olat.modules.lecture.ui.event.ChangeDayEvent;
import org.olat.modules.lecture.ui.event.OpenRepositoryEntryEvent;
import org.olat.modules.lecture.ui.event.RollCallEvent;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesCockpitController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private final List<Link> pendingLecturesLink = new ArrayList<>();
	
	private DailyAbsenceRollCallsController absenceRollCallsCtrl;
	private DailyAbsenceNoticesController absenceNoticesListCtrl;
	private final DayChooserController dayChooserCtrl;
	private DailyLectureBlockOverviewController lectureBlocksCtrl;

	private TeacherRollCallController rollCallCtrl;
	private IdentitiesLecturesRollCallController identitiesRollCallCtrl;
	
	private int counter = 0;
	private final boolean viewAsTeacher;
	private final LecturesSecurityCallback secCallback;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public LecturesCockpitController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("cockpit");
		
		dayChooserCtrl = new DayChooserController(ureq, getWindowControl());
		listenTo(dayChooserCtrl);
		mainVC.put("day.chooser", dayChooserCtrl.getInitialComponent());
		
		viewAsTeacher = secCallback.viewAs() == LectureRoles.teacher;
		if(viewAsTeacher) {
			lectureBlocksCtrl = new DailyLectureBlockOverviewController(ureq, getWindowControl(), getCurrentDate(), null, secCallback);
			listenTo(lectureBlocksCtrl);
			mainVC.put("lectureBlocks", lectureBlocksCtrl.getInitialComponent());
		}
		
		if(lectureModule.isAbsenceNoticeEnabled()) {
			absenceNoticesListCtrl = new DailyAbsenceNoticesController(ureq, getWindowControl(), getCurrentDate(), null, secCallback);
			listenTo(absenceNoticesListCtrl);
			mainVC.put("absenceNotices", absenceNoticesListCtrl.getInitialComponent());
		}
		
		absenceRollCallsCtrl = new DailyAbsenceRollCallsController(ureq, getWindowControl(), getCurrentDate(), secCallback);
		listenTo(absenceRollCallsCtrl);
		mainVC.put("absenceRollCalls", absenceRollCallsCtrl.getInitialComponent());
		
		mainVC.contextPut("pendingLectures", pendingLecturesLink);

		putInitialPanel(mainVC);
		updateCurrentDate();
		if(viewAsTeacher) {
			loadPendingLectureBlocks();//only viewed as teacher
		}
	}
	
	public Date getCurrentDate() {
		return dayChooserCtrl.getDate();
	}
	
	private void updateCurrentDate() {
		String dateString = Formatter.getInstance(getLocale()).formatDate(getCurrentDate());
		String msg = translate("cockpit.date", new String[] { dateString });
		mainVC.contextPut("date", msg);
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == lectureBlocksCtrl) {
			if(event instanceof OpenRepositoryEntryEvent) {
				fireEvent(ureq, event);
			} else if(event instanceof RollCallEvent) {
				RollCallEvent rce = (RollCallEvent)event;
				doRollCall(ureq, rce.getLectureBlocks());
			}
		} else if(source == dayChooserCtrl) {
			if(event instanceof ChangeDayEvent) {
				ChangeDayEvent cde = (ChangeDayEvent)event;
				doChangeCurrentDate(cde.getDate());
			}
		} else if(source == absenceNoticesListCtrl) {
			if(event instanceof SelectLectureIdentityEvent) {
				fireEvent(ureq, event);
			}
		} else if(source == rollCallCtrl) {
			if(event == Event.DONE_EVENT || event == Event.BACK_EVENT || event == Event.CANCELLED_EVENT) {
				backToDaily();
				reloadModels();
			}
		} else if(event == Event.BACK_EVENT) {
			backToDaily();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("pending-day".equals(link.getCommand())) {
				doChangeCurrentDate((Date)link.getUserObject());
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(identitiesRollCallCtrl);
		removeAsListenerAndDispose(rollCallCtrl);
		identitiesRollCallCtrl = null;
		rollCallCtrl = null;
	}
	
	private void backToDaily() {
		mainVC.remove("rollcall");
		cleanUp();
	}
	
	protected void reloadModels() {
		if(lectureBlocksCtrl != null) {
			lectureBlocksCtrl.loadModel();
		}
		if(absenceNoticesListCtrl != null) {
			absenceNoticesListCtrl.reloadModel();
		}
		loadPendingLectureBlocks();
	}
	
	private void doRollCall(UserRequest ureq, List<LectureBlock> lectureBlocks) {
		if(lectureBlocks.isEmpty()) {
			// msg
		} else if(lectureBlocks.size() == 1) {
			LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlocks.get(0));
			List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
			rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), reloadedBlock, participants,
					getRollCallSecurityCallback(reloadedBlock), true);
			listenTo(rollCallCtrl);
			mainVC.put("rollcall", rollCallCtrl.getInitialComponent());
		} else {
			Map<LectureBlock, List<Identity>> startedLectureBlocks = new HashMap<>();
			Set<Identity> participantsSet = new HashSet<>();
			for(LectureBlock lectureBlock:lectureBlocks) {
				LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
				List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
				startedLectureBlocks.put(reloadedBlock, participants);
				participantsSet.addAll(participants);
			}

			List<Identity> participantsList = new ArrayList<>(participantsSet);
			identitiesRollCallCtrl = new IdentitiesLecturesRollCallController(ureq, getWindowControl(),
					participantsList, startedLectureBlocks, secCallback);
			listenTo(identitiesRollCallCtrl);
			mainVC.put("rollcall", identitiesRollCallCtrl.getInitialComponent());
		}
	}
	
	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block) {
		boolean teacher = secCallback.viewAs() == LectureRoles.teacher;
		boolean masterCoach = secCallback.viewAs() == LectureRoles.mastercoach;
		return new RollCallSecurityCallbackImpl(false, masterCoach, teacher, block, lectureModule);
	}
	
	private void loadPendingLectureBlocks() {
		if(!viewAsTeacher) return;
		
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setTeacher(getIdentity());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date yesterday = CalendarUtils.endOfDay(cal.getTime());
		searchParams.setEndDate(yesterday);
		searchParams.addLectureBlockStatus(LectureBlockStatus.active, LectureBlockStatus.done);
		searchParams.addRollCallStatus(LectureRollCallStatus.open);

		Formatter formatter = Formatter.getInstance(getLocale());
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams);
		Collections.sort(lectureBlocks, new LectureBlockComparator());
		
		List<Link> pendingLinks = new ArrayList<>();
		Set<String> deduplicate = new HashSet<>();
		for(LectureBlock lectureBlock:lectureBlocks) {
			int lectures = lectureBlock.getCalculatedLecturesNumber();

			Date startdDate = lectureBlock.getStartDate();
			String day = formatter.dayOfWeek(startdDate);
			String date = formatter.formatDate(startdDate);
			String[] args = new String[] { day, date, Integer.toString(lectures) };
			String linkName;
			if(lectures == 1) {
				linkName = translate("cockpit.pending.day", args);
			} else {
				linkName = translate("cockpit.pending.day.plural", args);
			}
			if(!deduplicate.contains(linkName)) {
				Link pendingLink = LinkFactory.createLink("pending-" + (++counter), linkName, "pending-day", linkName, getTranslator(), mainVC, this, Link.NONTRANSLATED);
				pendingLink.setUserObject(startdDate);
				pendingLinks.add(pendingLink);
				deduplicate.add(linkName);
			}
		}
		mainVC.contextPut("pendingDays", pendingLinks);
	}
	
	private void doChangeCurrentDate(Date date) {
		dayChooserCtrl.setDate(date);
		if(lectureBlocksCtrl != null) {
			lectureBlocksCtrl.setCurrentDate(date);
		}
		if(absenceNoticesListCtrl != null) {
			absenceNoticesListCtrl.setCurrentDate(date);
		}
		absenceRollCallsCtrl.setCurrentDate(date);

		updateCurrentDate();
	}
}
