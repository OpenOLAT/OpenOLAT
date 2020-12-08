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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.TeacherToolOverviewController;
import org.olat.modules.lecture.ui.event.OpenRepositoryEntryEvent;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesCoachingController extends BasicController implements Activateable2 {
	
	private Link reportLink;
	private Link appealsLink;
	private Link absenceLink;
	private Link dispensationLink;
	private final Link cockpitLink;
	private final Link lecturesLink;
	private final Link lecturesSearchLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	private final SegmentViewComponent segmentView;
	
	private final LecturesSecurityCallback secCallback;
	
	private AppealsController appealsController;
	private AbsencesController absencesController;
	private LecturesSearchController reportController;
	private MultiSearchesController searchesController;
	private LecturesCockpitController cockpitController;
	private DispensationsController dispensationsController;
	private TeacherToolOverviewController teacherToolOverviewController;
	
	@Autowired
	private LectureModule lectureModule;
	
	public LecturesCoachingController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		mainVC = createVelocityContainer("coaching");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		cockpitLink = LinkFactory.createLink("coach.cockpit", mainVC, this);
		segmentView.addSegment(cockpitLink, true);
		lecturesLink = LinkFactory.createLink("coach.lectures", mainVC, this);
		segmentView.addSegment(lecturesLink, false);
		
		if(lectureModule.isAbsenceNoticeEnabled()) {
			absenceLink = LinkFactory.createLink("coach.absence", mainVC, this);
			segmentView.addSegment(absenceLink, false);
			dispensationLink = LinkFactory.createLink("coach.dispensation", mainVC, this);
			segmentView.addSegment(dispensationLink, false);
		}
		
		if(lectureModule.isAbsenceAppealEnabled() && secCallback.canSeeAppeals()) {
			appealsLink = LinkFactory.createLink("coach.appeals", mainVC, this);
			segmentView.addSegment(appealsLink, false);
		}
		
		lecturesSearchLink = LinkFactory.createLink("coach.lectures.search", mainVC, this);
		segmentView.addSegment(lecturesSearchLink, false);
		
		if(secCallback.viewAs() == LectureRoles.lecturemanager) {
			reportLink = LinkFactory.createLink("coach.report", mainVC, this);
			segmentView.addSegment(reportLink, false);
		}
		
		putInitialPanel(mainVC);
		doOpenCockpit(ureq);
	}
	
	public Date getCurrentDate() {
		return cockpitController.getCurrentDate();
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			//
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Cockpit".equalsIgnoreCase(type)) {
				doOpenCockpit(ureq);
				segmentView.select(cockpitLink);
			} else if("Teacher".equalsIgnoreCase(type)) {
				doOpenLectures(ureq);
				segmentView.select(lecturesLink);
			} else if("Absences".equalsIgnoreCase(type) && absenceLink != null) {
				doAbsences(ureq);
				segmentView.select(absenceLink);
			} else if("Dispenses".equalsIgnoreCase(type) && dispensationLink != null) {
				doDispenses(ureq);
				segmentView.select(dispensationLink);
			} else if("Appeals".equalsIgnoreCase(type)) {
				if(secCallback.canSeeAppeals()) {
					doAppeals(ureq);
					segmentView.select(appealsLink);
				}
			} else if("Search".equalsIgnoreCase(type)) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenLecturesSearch(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				segmentView.select(lecturesSearchLink);
			} else if("Report".equalsIgnoreCase(type)) {
				doOpenReport(ureq);
				segmentView.select(reportLink);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cockpitController) {
			if(event instanceof OpenRepositoryEntryEvent) {
				OpenRepositoryEntryEvent oree = (OpenRepositoryEntryEvent)event;
				doOpenLectures(ureq, oree.getEntry());
				segmentView.select(lecturesLink);
			} else if(event instanceof SelectLectureIdentityEvent) {
				SelectLectureIdentityEvent oree = (SelectLectureIdentityEvent)event;
				doOpenLecturesSearchForIdentity(ureq, oree.getIdentityKey());
				segmentView.select(lecturesSearchLink);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == cockpitLink) {
					doOpenCockpit(ureq);
				} else if (clickedLink == lecturesLink) {
					doOpenLectures(ureq);
				} else if(clickedLink == absenceLink) {
					doAbsences(ureq);
				} else if(clickedLink == dispensationLink) {
					doDispenses(ureq);
				} else if(clickedLink == appealsLink) {
					doAppeals(ureq);
				} else if (clickedLink == lecturesSearchLink) {
					doOpenLecturesSearch(ureq);
				} else if (clickedLink == reportLink) {
					doOpenReport(ureq);
				}
			}
		}
	}
	
	private void doOpenCockpit(UserRequest ureq) {
		if(cockpitController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Cockpit"), null);
			cockpitController = new LecturesCockpitController(ureq, swControl, secCallback);
			listenTo(cockpitController);
		} else {
			cockpitController.reloadModels();
		}
		addToHistory(ureq, cockpitController);
		mainVC.put("segmentCmp", cockpitController.getInitialComponent()); 
	}
	
	private TeacherToolOverviewController doOpenLectures(UserRequest ureq) {
		if(teacherToolOverviewController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Teacher"), null);
			teacherToolOverviewController = new TeacherToolOverviewController(ureq, swControl, secCallback);
			listenTo(teacherToolOverviewController);
			teacherToolOverviewController.setBreadcrumbPanel(stackPanel);
		}
		addToHistory(ureq, teacherToolOverviewController);
		mainVC.put("segmentCmp", teacherToolOverviewController.getInitialComponent());
		return teacherToolOverviewController;
	}
	
	private void doOpenLectures(UserRequest ureq, RepositoryEntry entry) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(entry);
		doOpenLectures(ureq).activate(ureq, entries, null);
	}
	
	private void doAbsences(UserRequest ureq) {
		if(absencesController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Absences"), null);
			absencesController = new AbsencesController(ureq, swControl, getCurrentDate(), secCallback);
			listenTo(absencesController);
		} else {
			absencesController.reloadModels();
		}
		addToHistory(ureq, absencesController);
		mainVC.put("segmentCmp", absencesController.getInitialComponent());  
	}
	
	private void doDispenses(UserRequest ureq) {
		if(dispensationsController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Dispenses"), null);
			dispensationsController = new DispensationsController(ureq, swControl, getCurrentDate(), secCallback, true, true);
			listenTo(dispensationsController);
		} else {
			dispensationsController.reloadModel();
		}
		addToHistory(ureq, dispensationsController);
		mainVC.put("segmentCmp", dispensationsController.getInitialComponent());   
	}
	
	private void doAppeals(UserRequest ureq) {
		if(appealsController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Appeals"), null);
			appealsController = new AppealsController(ureq, swControl, secCallback);
			listenTo(appealsController);
		} else {
			appealsController.reloadModels();
		}
		addToHistory(ureq, appealsController);
		mainVC.put("segmentCmp", appealsController.getInitialComponent());   
	}
	
	private MultiSearchesController doOpenLecturesSearch(UserRequest ureq) {
		if(searchesController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Search"), null);
			searchesController = new MultiSearchesController(ureq, swControl, secCallback);
			listenTo(searchesController);
		}
		addToHistory(ureq, searchesController);
		mainVC.put("segmentCmp", searchesController.getInitialComponent());
		return searchesController;
	}
	
	private void doOpenLecturesSearchForIdentity(UserRequest ureq, Long identityKey) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(OresHelper.createOLATResourceableInstance("Identity", identityKey));
		doOpenLecturesSearch(ureq).activate(ureq, entries, null);
	}
	
	private void doOpenReport(UserRequest ureq) {
		if(reportController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Report"), null);
			reportController = new LecturesSearchController(ureq, swControl, secCallback);
			listenTo(reportController);
		}
		addToHistory(ureq, reportController);
		mainVC.put("segmentCmp", reportController.getInitialComponent());
	}
}
