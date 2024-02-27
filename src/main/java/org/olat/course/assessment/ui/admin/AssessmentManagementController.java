/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionConfigurationListController;
import org.olat.course.assessment.ui.mode.AssessmentModeListController;
import org.olat.course.assessment.ui.mode.AssessmentModeSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentManagementController extends BasicController {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link assessmentModeLink;
	private final Link assessmentInspectionLink;
	private final TooledStackedPanel toolbarPanel;
	
	private final RepositoryEntry entry;
	private final AssessmentModeSecurityCallback secCallback;
	
	private AssessmentModeListController assessmentModeListCtrl;
	private AssessmentInspectionConfigurationListController assessmentInspectionListCtrl;
	
	@Autowired
	private AssessmentModule assessmentModule;
	
	public AssessmentManagementController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry entry, AssessmentModeSecurityCallback secCallback) {
		super(ureq, wControl);
		this.entry = entry;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		
		mainVC = createVelocityContainer("segments");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		assessmentModeLink = LinkFactory.createLink("assessment.mode", mainVC, this);
		assessmentModeLink.setVisible(assessmentModule.isAssessmentModeEnabled());		
		
		segmentView.addSegment(assessmentModeLink, true);
		assessmentInspectionLink = LinkFactory.createLink("assessment.inspection", mainVC, this);
		assessmentInspectionLink.setVisible(assessmentModule.isAssessmentInspectionEnabled());
		segmentView.addSegment(assessmentInspectionLink, false);
		
		if(assessmentModule.isAssessmentModeEnabled()) {
			doOpenAssessmentMode(ureq);
		} else if(assessmentModule.isAssessmentInspectionEnabled()) {
			doOpenAssessmentInspection(ureq);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink =  mainVC.getComponent(segmentCName);
				if(clickedLink == assessmentModeLink
						&& assessmentModule.isAssessmentModeEnabled()) {
					doOpenAssessmentMode(ureq);
				} else if(clickedLink == assessmentInspectionLink
						&& assessmentModule.isAssessmentInspectionEnabled()) {
					doOpenAssessmentInspection(ureq);
				}
			}
		}
	}
	
	private void doOpenAssessmentMode(UserRequest ureq) {
		if(assessmentModeListCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Modes"), null);
			assessmentModeListCtrl = new AssessmentModeListController(ureq, swControl, toolbarPanel, entry, secCallback);
			listenTo(assessmentModeListCtrl);
		}
		
		mainVC.put("segmentCmp", assessmentModeListCtrl.getInitialComponent());
		segmentView.select(assessmentModeLink);
	}

	private void doOpenAssessmentInspection(UserRequest ureq) {
		if(assessmentInspectionListCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Inspection"), null);
			assessmentInspectionListCtrl = new AssessmentInspectionConfigurationListController(ureq, swControl, toolbarPanel, entry);
			listenTo(assessmentInspectionListCtrl);
		}
		
		mainVC.put("segmentCmp", assessmentInspectionListCtrl.getInitialComponent());
		segmentView.select(assessmentInspectionLink);
	}
}
