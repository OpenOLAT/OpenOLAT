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
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesToolController extends BasicController implements BreadcrumbPanelAware, Activateable2 {
	
	private BreadcrumbPanel stackPanel;
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link teacherLink, participantLink;
	
	private final TeacherToolOverviewController teacherOverviewCtrl;
	private ParticipantLecturesOverviewController participantOverviewCtrl;
	
	@Autowired
	private CurriculumModule curriculumModule;
	
	public LecturesToolController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("user_tool");

		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("coach"), null);
		teacherOverviewCtrl = new TeacherToolOverviewController(ureq, swControl);
		listenTo(teacherOverviewCtrl);
		boolean withTitle = teacherOverviewCtrl.getRowCount() == 0;
		WindowControl twControl = addToHistory(ureq, OresHelper.createOLATResourceableType("attendee"), null);
		
		boolean curriculumEnabled = curriculumModule.isEnabled();
		participantOverviewCtrl = new ParticipantLecturesOverviewController(ureq, twControl, withTitle, curriculumEnabled);
		listenTo(participantOverviewCtrl);
		
		if(teacherOverviewCtrl.getRowCount() > 0 && participantOverviewCtrl.getRowCount() > 0) {
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			teacherLink = LinkFactory.createLink("tool.teacher", mainVC, this);
			segmentView.addSegment(teacherLink, true);
			participantLink = LinkFactory.createLink("tool.participant", mainVC, this);
			segmentView.addSegment(participantLink, false);
			doOpenTeacherView(ureq);
		} else if(teacherOverviewCtrl.getRowCount() > 0) {
			doOpenTeacherView(ureq);
		} else if(participantOverviewCtrl.getRowCount() > 0) {
			doOpenParticipantView(ureq);
		}
		putInitialPanel(mainVC);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		participantOverviewCtrl.setBreadcrumbPanel(stackPanel);
		teacherOverviewCtrl.setBreadcrumbPanel(stackPanel);
		stackPanel.addListener(this);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("coach".equalsIgnoreCase(type)) {
			if(segmentView != null) {
				segmentView.select(teacherLink);
			}
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenTeacherView(ureq).activate(ureq, subEntries, entry.getTransientState());
		} else if("attendee".equalsIgnoreCase(type)) {
			if(segmentView != null) {
				segmentView.select(participantLink);
			}
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenParticipantView(ureq).activate(ureq, subEntries, entry.getTransientState());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == teacherLink) {
				doOpenTeacherView(ureq);
			} else if (clickedLink == participantLink) {
				doOpenParticipantView(ureq);
			}
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent)event;
				if(popEvent.getController() instanceof TeacherRollCallController) {
					addToHistory(ureq, teacherOverviewCtrl);
				} else if(popEvent.getController() instanceof ParticipantLectureBlocksController) {
					addToHistory(ureq, participantOverviewCtrl);
				}
			}
		}
	}
	
	private Activateable2 doOpenTeacherView(UserRequest ureq) {
		mainVC.put("segmentCmp", teacherOverviewCtrl.getInitialComponent());
		mainVC.put("teacherView", teacherOverviewCtrl.getInitialComponent());
		addToHistory(ureq, teacherOverviewCtrl);
		return teacherOverviewCtrl;
	}
	
	private Activateable2 doOpenParticipantView(UserRequest ureq) {
		mainVC.put("segmentCmp", participantOverviewCtrl.getInitialComponent());
		mainVC.put("participantView", participantOverviewCtrl.getInitialComponent());
		addToHistory(ureq, participantOverviewCtrl);
		return participantOverviewCtrl;
	}
}