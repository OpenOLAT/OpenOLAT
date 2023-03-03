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
package org.olat.modules.project.ui;

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

/**
 * 
 * Initial date: 25 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectsAdminController extends BasicController implements Activateable2 {

	private static final String ORES_TYPE_SETTINGS = "Settings";
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link settingsLink;
	
	private ProjectsSettingsController settingsCtrl;

	public ProjectsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("admin");
		putInitialPanel(mainVC);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		settingsLink = LinkFactory.createLink("admin.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);
		
		doOpenSettings(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_SETTINGS.equalsIgnoreCase(type) && settingsLink.isVisible()) {
			doOpenSettings(ureq);
			segmentView.select(settingsLink);
		}
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == settingsLink) {
					doOpenSettings(ureq);
				}
			}
		}
	}

	private void doOpenSettings(UserRequest ureq) {
		if (settingsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_SETTINGS), null);
			settingsCtrl = new ProjectsSettingsController(ureq, swControl);
			listenTo(settingsCtrl);
		} else {
			addToHistory(ureq, settingsCtrl);
		}
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}
	
}
