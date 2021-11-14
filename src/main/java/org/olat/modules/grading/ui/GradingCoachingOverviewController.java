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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingSecurityCallbackFactory;
import org.olat.modules.grading.ui.event.OpenAssignmentsEvent;

/**
 * 
 * Initial date: 24 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingCoachingOverviewController extends BasicController implements Activateable2 {

	private final Link gradersLink;
	private final Link myAssignmentsLink;
	private final Link gradersAssignmentsLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel stackPanel;
	
	private GradersListController gradersCtrl;
	private GradingAssignmentsListController assignmentsCtrl;
	private GradingAssignmentsListController myAssignmentsCtrl;
	
	private final GradingSecurityCallback secCallback;
	
	public GradingCoachingOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, GradingSecurityCallback secCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("overview");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		gradersLink = LinkFactory.createLink("coaching.graders", mainVC, this);
		gradersLink.setVisible(secCallback.canManage());
		segmentView.addSegment(gradersLink, true);
		if(secCallback.canManage()) {
			doOpenGraders(ureq);
		}

		gradersAssignmentsLink = LinkFactory.createLink("coaching.graders.assignments", mainVC, this);
		gradersAssignmentsLink.setVisible(secCallback.canManage());
		segmentView.addSegment(gradersAssignmentsLink, false);

		myAssignmentsLink = LinkFactory.createLink("coaching.my.assignments", mainVC, this);
		myAssignmentsLink.setVisible(secCallback.canManage());
		segmentView.addSegment(myAssignmentsLink, false);
		if(secCallback.canGrade() && !secCallback.canManage()) {
			doOpenMyAssignments(ureq);
		}
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Graders".equalsIgnoreCase(type) && secCallback.canManage()) {
			doOpenGraders(ureq);
			segmentView.select(gradersLink);
		} else if("Assignments".equalsIgnoreCase(type) && secCallback.canManage()) {
			doOpenAssignments(ureq);
			segmentView.select(gradersAssignmentsLink);
		} else if("MyAssignments".equalsIgnoreCase(type) && secCallback.canGrade()) {
			doOpenMyAssignments(ureq);
			segmentView.select(myAssignmentsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == gradersLink) {
					doOpenGraders(ureq);
				} else if (clickedLink == gradersAssignmentsLink) {
					doOpenAssignments(ureq);
				} else if (clickedLink == myAssignmentsLink) {
					doOpenMyAssignments(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(gradersCtrl == source) {
			if(event instanceof OpenAssignmentsEvent) {
				doOpenAssignments(ureq).activate((OpenAssignmentsEvent)event);
				segmentView.select(gradersAssignmentsLink);
			}
		}
	}
	
	private void doOpenGraders(UserRequest ureq) {
		if(gradersCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Graders"), null);
			gradersCtrl = new GradersListController(ureq, swControl, secCallback);
			listenTo(gradersCtrl);
		} else {
			gradersCtrl.updateModel();
		}
		addToHistory(ureq, gradersCtrl);
		mainVC.put("segmentCmp", gradersCtrl.getInitialComponent());
	}
	
	private GradingAssignmentsListController doOpenAssignments(UserRequest ureq) {
		if(assignmentsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Assignments"), null);
			assignmentsCtrl = new GradingAssignmentsListController(ureq, swControl, secCallback);
			listenTo(assignmentsCtrl);
			assignmentsCtrl.setBreadcrumbPanel(stackPanel);
		}
		addToHistory(ureq, assignmentsCtrl);
		mainVC.put("segmentCmp", assignmentsCtrl.getInitialComponent());
		return assignmentsCtrl;
	}

	private void doOpenMyAssignments(UserRequest ureq) {
		if(myAssignmentsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("MyAssignments"), null);
			GradingSecurityCallback mySecCallback = GradingSecurityCallbackFactory.mySecurityCalllback(secCallback);
			myAssignmentsCtrl = new GradingAssignmentsListController(ureq, swControl, getIdentity(), mySecCallback);
			listenTo(myAssignmentsCtrl);
			myAssignmentsCtrl.setBreadcrumbPanel(stackPanel);
		}
		addToHistory(ureq, myAssignmentsCtrl);
		mainVC.put("segmentCmp", myAssignmentsCtrl.getInitialComponent());
	}
}
