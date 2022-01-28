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
package org.olat.course.assessment.ui.mode;

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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 06.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeAdminController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final Link settingsLink;
	private final Link assessmentModesLink;
	private final Link safeExamBrowserLink;
	private final SegmentViewComponent segmentView;
	
	private AssessmentModeAdminListController modeListCtrl;
	private AssessmentModeAdminSettingsController settingsCtrl;
	private SafeExamBrowserAdminController safeExamBrowserCtrl;
	
	public AssessmentModeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setReselect(true);

		settingsLink = LinkFactory.createLink("admin.assessment.mode.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);
		doOpenSettings(ureq);
		assessmentModesLink = LinkFactory.createLink("admin.assessment.mode.list", mainVC, this);
		segmentView.addSegment(assessmentModesLink, false);
		safeExamBrowserLink = LinkFactory.createLink("admin.assessment.mode.seb", mainVC, this);
		segmentView.addSegment(safeExamBrowserLink, false);

		putInitialPanel(mainVC);
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
				} else if (clickedLink == assessmentModesLink) {
					doOpenAssessmentModes(ureq);
				} else if(clickedLink == safeExamBrowserLink) {
					doOpenSafeExamBrowserConfiguration(ureq);
				}
			}
		}
	}

	private void doOpenSettings(UserRequest ureq) {
		removeControllerListener(settingsCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Settings", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		settingsCtrl = new AssessmentModeAdminSettingsController(ureq, bwControl);
		listenTo(settingsCtrl);
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}
	
	private void doOpenAssessmentModes(UserRequest ureq) {
		removeControllerListener(modeListCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("AssessmentModes", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		modeListCtrl = new AssessmentModeAdminListController(ureq, bwControl);
		listenTo(modeListCtrl);
		mainVC.put("segmentCmp", modeListCtrl.getInitialComponent());
	}
	
	private void doOpenSafeExamBrowserConfiguration(UserRequest ureq) {
		removeControllerListener(safeExamBrowserCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("SafeExamBrowser", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		safeExamBrowserCtrl = new SafeExamBrowserAdminController(ureq, bwControl);
		listenTo(safeExamBrowserCtrl);
		mainVC.put("segmentCmp", safeExamBrowserCtrl.getInitialComponent());
	}
}
