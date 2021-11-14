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
package org.olat.modules.grading.ui;

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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingSecurityCallbackFactory;
import org.olat.modules.grading.ui.event.OpenEntryAssignmentsEvent;

/**
 * 
 * Initial date: 27 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GraderUserOverviewController extends BasicController implements Activateable2, BreadcrumbPanelAware {
	
	private final Link assignedTestsLink;
	private final Link assignedAssessmentsLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private BreadcrumbPanel stackPanel;
	
	private final Identity grader;
	
	private GradingAssignmentsListController assignmentsCtrl;
	private AssignedReferenceEntryListController assignedTestsCtrl;
	
	public GraderUserOverviewController(UserRequest ureq, WindowControl wControl, Identity grader) {
		super(ureq, wControl);
		this.grader = grader;
		
		mainVC = createVelocityContainer("overview");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		assignedTestsLink = LinkFactory.createLink("user.assigned.tests", mainVC, this);
		segmentView.addSegment(assignedTestsLink, true);
		doOpenAssignedTest(ureq);
		
		assignedAssessmentsLink = LinkFactory.createLink("user.assigned.assignments", mainVC, this);
		segmentView.addSegment(assignedAssessmentsLink, false);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		if(assignmentsCtrl != null) {
			assignmentsCtrl.setBreadcrumbPanel(stackPanel);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assignedTestsCtrl == source) {
			if(event instanceof OpenEntryAssignmentsEvent) {
				doOpenAssignedAssignments(ureq).activate((OpenEntryAssignmentsEvent)event);
				segmentView.select(assignedAssessmentsLink);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == assignedTestsLink) {
					doOpenAssignedTest(ureq);
				} else if (clickedLink == assignedAssessmentsLink) {
					doOpenAssignedAssignments(ureq);
				}
			}
		}
	}
	
	private void doOpenAssignedTest(UserRequest ureq) {
		if(assignedTestsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("AssignedTests"), null);
			assignedTestsCtrl = new AssignedReferenceEntryListController(ureq, swControl, grader);
			listenTo(assignedTestsCtrl);
		}
		addToHistory(ureq, assignedTestsCtrl);
		mainVC.put("segmentCmp", assignedTestsCtrl.getInitialComponent());
	}
	
	private GradingAssignmentsListController doOpenAssignedAssignments(UserRequest ureq) {
		if(assignmentsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Assignments"), null);
			GradingSecurityCallback secCallback = GradingSecurityCallbackFactory
					.getManagerCalllback(getIdentity(), ureq.getUserSession().getRoles());
			assignmentsCtrl = new GradingAssignmentsListController(ureq, swControl, grader, secCallback);
			listenTo(assignmentsCtrl);
			assignmentsCtrl.setBreadcrumbPanel(stackPanel);
		}
		addToHistory(ureq, assignmentsCtrl);
		mainVC.put("segmentCmp", assignmentsCtrl.getInitialComponent());
		return assignmentsCtrl;
	}

}
