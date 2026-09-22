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
package org.olat.admin.restapi;

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
import org.olat.core.util.resource.OresHelper;

/**
 * The administration of the REST API: the configuration and the audit log.
 * 
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class RestapiAdminMainController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link configurationLink;
	private final Link auditLogLink;

	private ApiAuditLogAdminController logCtrl;
	private RestapiAdminController configurationCtrl;
	
	public RestapiAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("rest_main");
		putInitialPanel(mainVC);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("segment.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		auditLogLink = LinkFactory.createLink("segment.auditlog", mainVC, this);
		segmentView.addSegment(auditLogLink, false);
		
		doOpenConfiguration(ureq);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView && event instanceof SegmentViewEvent sve) {
			Component clickedLink = mainVC.getComponent(sve.getComponentName());
			if(clickedLink == configurationLink) {
				doOpenConfiguration(ureq);
			} else if(clickedLink == auditLogLink) {
				doOpenAuditLog(ureq);
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new RestapiAdminController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		addToHistory(ureq, configurationCtrl);
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}
	
	private void doOpenAuditLog(UserRequest ureq) {
		if(logCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("AuditLog"), null);
			logCtrl = new ApiAuditLogAdminController(ureq, bwControl);
			listenTo(logCtrl);
		}
		addToHistory(ureq, logCtrl);
		mainVC.put("segmentCmp", logCtrl.getInitialComponent());
	}
}
