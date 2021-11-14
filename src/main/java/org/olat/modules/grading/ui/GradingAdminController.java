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
 * Initial date: 7 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAdminController extends BasicController implements Activateable2 {
	
	private final Link templatesLink;
	private final Link configurationLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private final GradingAdminTemplatesController templatesCtrl;
	private final GradingAdminConfigurationController configurationCtrl;
	
	public GradingAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		WindowControl cswControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
		configurationCtrl = new GradingAdminConfigurationController(ureq, cswControl);
		listenTo(configurationCtrl);

		WindowControl tswControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Templates"), null);
		templatesCtrl = new GradingAdminTemplatesController(ureq, tswControl);
		listenTo(templatesCtrl);
		
		mainVC = createVelocityContainer("overview");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		configurationLink = LinkFactory.createLink("grading.admin.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		doOpenConfiguration(ureq);
		
		templatesLink = LinkFactory.createLink("grading.admin.templates", mainVC, this);
		segmentView.addSegment(templatesLink, false);

		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Configuration".equalsIgnoreCase(type)) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
		} else if("Templates".equalsIgnoreCase(type)) {
			doOpenTemplates(ureq);
			segmentView.select(templatesLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == templatesLink) {
					doOpenTemplates(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		addToHistory(ureq, configurationCtrl);
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}
	
	private void doOpenTemplates(UserRequest ureq) {
		addToHistory(ureq, templatesCtrl);
		mainVC.put("segmentCmp", templatesCtrl.getInitialComponent());
	}
}
