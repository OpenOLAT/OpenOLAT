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
package org.olat.course.archiver;

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
import org.olat.core.id.Roles;
import org.olat.course.CourseModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchivesOverviewController extends BasicController {

	private final Link myArchivesLink;
	private final Link archivesManagementLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private MyArchivesController myArchivesCtrl;
	private ArchivesAdminManagementController archivesManagementCtrl;
	
	@Autowired
	private CourseModule courseModule;
	
	public ArchivesOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		Roles roles = ureq.getUserSession().getRoles();
		
		mainVC = createVelocityContainer("archives_segments");
		mainVC.contextPut("hint", translate("archives.hint", courseModule.getCourseArchiveRetention().toString()));
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		myArchivesLink = LinkFactory.createLink("archives.my", mainVC, this);
		segmentView.addSegment(myArchivesLink, true);
		archivesManagementLink = LinkFactory.createLink("archives.management", mainVC, this);
		archivesManagementLink.setVisible(roles.isAdministrator() || roles.isLearnResourceManager());
		segmentView.addSegment(archivesManagementLink, false);
		
		doOpenMyArchives(ureq);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink =  mainVC.getComponent(segmentCName);
				if(clickedLink == myArchivesLink) {
					doOpenMyArchives(ureq);
				} else if(clickedLink == archivesManagementLink) {
					doOpenArchivesManagement(ureq);
				}
			}
		}
	}
	
	private void doOpenMyArchives(UserRequest ureq) {
		if(myArchivesCtrl == null) {
			myArchivesCtrl = new MyArchivesController(ureq, getWindowControl(), false);
			listenTo(myArchivesCtrl);
		}
		mainVC.put("segmentCmp", myArchivesCtrl.getInitialComponent());
	}
	
	private void doOpenArchivesManagement(UserRequest ureq) {
		if(archivesManagementCtrl == null) {
			archivesManagementCtrl = new ArchivesAdminManagementController(ureq, getWindowControl(),
					CourseArchiveListController.COURSE_ARCHIVE_SUB_IDENT);
			listenTo(archivesManagementCtrl);
		}
		mainVC.put("segmentCmp", archivesManagementCtrl.getInitialComponent());
	}

}
