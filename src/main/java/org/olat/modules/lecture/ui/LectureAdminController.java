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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.lecture.ui.coach.LecturesReportController;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureAdminController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private final Link reportLink;
	private final Link reasonsLink;
	private final Link settingsLink;
	private final SegmentViewComponent segmentView;
	
	private ReasonAdminController reasonsCtrl;
	private LectureSettingsAdminController settingsCtrl;
	private LecturesReportController lecturesReportCtrl;
	
	public LectureAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		settingsLink = LinkFactory.createLink("lectures.admin.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);
		reasonsLink = LinkFactory.createLink("lectures.admin.reasons", mainVC, this);
		segmentView.addSegment(reasonsLink, false);
		reportLink = LinkFactory.createLink("lectures.admin.report", mainVC, this);
		segmentView.addSegment(reportLink, false);

		doOpenSettings(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Settings".equalsIgnoreCase(type)) {
			doOpenSettings(ureq);
			segmentView.select(settingsLink);
		} else if("Reasons".equalsIgnoreCase(type)) {
			doOpenReasons(ureq);
			segmentView.select(reasonsLink);
		} else if("Report".equalsIgnoreCase(type)) {
			doOpenLectureReports(ureq);
			segmentView.select(reportLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == settingsLink) {
					doOpenSettings(ureq);
				} else if (clickedLink == reasonsLink) {
					doOpenReasons(ureq);
				} else if (clickedLink == reportLink) {
					doOpenLectureReports(ureq);
				}
			}
		}
	}
	
	private void doOpenSettings(UserRequest ureq) {
		if(settingsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Settings"), null);
			settingsCtrl = new LectureSettingsAdminController(ureq, swControl);
			listenTo(settingsCtrl);
		} else {
			addToHistory(ureq, settingsCtrl);
		}
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}
	
	private void doOpenReasons(UserRequest ureq) {
		if(reasonsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Reasons"), null);
			reasonsCtrl = new ReasonAdminController(ureq, swControl);
			listenTo(reasonsCtrl);
		} else {
			addToHistory(ureq, reasonsCtrl);
		}
		mainVC.put("segmentCmp", reasonsCtrl.getInitialComponent());
	}
	
	private void doOpenLectureReports(UserRequest ureq) {
		if(lecturesReportCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Report"), null);
			lecturesReportCtrl = new LecturesReportController(ureq, swControl);
			listenTo(lecturesReportCtrl);
		} else {
			addToHistory(ureq, lecturesReportCtrl);
		}
		mainVC.put("segmentCmp", lecturesReportCtrl.getInitialComponent());
	}
}
