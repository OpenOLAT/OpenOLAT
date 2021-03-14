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
package org.olat.ims.lti13.ui;

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
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13AdminController extends BasicController implements Activateable2 {
	

	private Link configurationLink;
	private Link externalToolsLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private LTI13AdminExternalToolsController toolsCtrl;
	private LTI13AdminConfigurationController configCtrl;
	
	public LTI13AdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("lti13_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("lti13.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		externalToolsLink = LinkFactory.createLink("lti13.external.tools", mainVC, this);
		segmentView.addSegment(externalToolsLink, false);

		doOpenConfiguration(ureq);
		
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
		if("Configuration".equalsIgnoreCase(type)) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
		} else if("Tools".equalsIgnoreCase(type)) {
			doOpenTools(ureq);
			segmentView.select(externalToolsLink);
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
				} else if (clickedLink == externalToolsLink) {
					doOpenTools(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		removeAsListenerAndDispose(configCtrl);

		WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Configuration", 0l), null);
		configCtrl = new LTI13AdminConfigurationController(ureq, bwControl);
		listenTo(configCtrl);

		mainVC.put("segmentCmp", configCtrl.getInitialComponent());
	}
	
	private void doOpenTools(UserRequest ureq) {
		if(toolsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Tools", 0l), null);
			toolsCtrl = new LTI13AdminExternalToolsController(ureq, bwControl);
			listenTo(toolsCtrl);
		} else {
			addToHistory(ureq, toolsCtrl);
		}
		mainVC.put("segmentCmp", toolsCtrl.getInitialComponent());
	}


}
