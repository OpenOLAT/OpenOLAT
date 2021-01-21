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
package org.olat.course.admin;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessableCourseNodeAdminController;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.admin.EducationalTypeAdminController;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseAdminController extends BasicController implements Activateable2 {

	private static final String ORES_TYPE_SETTINGS = "Settings";
	private static final String ORES_TYPE_EDUCATIONAL = "EducationalTypes";
	
	private final Link settingsLink;
	private final Link educationalTypesLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private Controller settingsCtrl;
	private Controller educationalTypesCtrl;
	
	public CourseAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseModule.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		settingsLink = LinkFactory.createLink("admin.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);
		
		educationalTypesLink = LinkFactory.createLink("educational.types", mainVC, this);
		segmentView.addSegment(educationalTypesLink, false);

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
		if(ORES_TYPE_SETTINGS.equalsIgnoreCase(type)) {
			doOpenSettings(ureq);
			segmentView.select(settingsLink);
		} else if(ORES_TYPE_EDUCATIONAL.equalsIgnoreCase(type)) {
			doOpenEducationalTypes(ureq);
			segmentView.select(educationalTypesLink);
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
				} else if (clickedLink == educationalTypesLink) {
					doOpenEducationalTypes(ureq);
				}
			}
		}
	}
	
	private void doOpenSettings(UserRequest ureq) {
		if(settingsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(ORES_TYPE_SETTINGS, 0l), null);
			settingsCtrl = new AssessableCourseNodeAdminController(ureq, bwControl);
			listenTo(settingsCtrl);
		} else {
			addToHistory(ureq, settingsCtrl);
		}
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}
	
	private void doOpenEducationalTypes(UserRequest ureq) {
		if(educationalTypesCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(ORES_TYPE_EDUCATIONAL, 0l), null);
			educationalTypesCtrl = new EducationalTypeAdminController(ureq, bwControl);
			listenTo(educationalTypesCtrl);
		} else {
			addToHistory(ureq, educationalTypesCtrl);
		}
		mainVC.put("segmentCmp", educationalTypesCtrl.getInitialComponent());
	}
}
