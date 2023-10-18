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
package org.olat.user.ui.identity;

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
 * 
 * Initial date: 22 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAuthenticationsController extends BasicController {
	
	private final Link openOlatPasswordLink;
	private final Link externalProvidersLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private UserOpenOlatAuthenticationController openOlatCtrl;
	private UserExternalAuthenticationController externalServicesCtrl;
	
	public UserAuthenticationsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		WindowControl obwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Intern"), null);
		openOlatCtrl = new UserOpenOlatAuthenticationController(ureq, obwControl);
		listenTo(openOlatCtrl);
		
		WindowControl ebwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Extern"), null);
		externalServicesCtrl = new UserExternalAuthenticationController(ureq, ebwControl);
		listenTo(externalServicesCtrl);
		
		mainVC = createVelocityContainer("authentications_segment");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		boolean hasOpenOlatAuthentication = openOlatCtrl.hasAuthentications();
		openOlatPasswordLink = LinkFactory.createLink("authentication.intern", mainVC, this);
		openOlatPasswordLink.setVisible(hasOpenOlatAuthentication);
		segmentView.addSegment(openOlatPasswordLink, true);
		
		boolean hasExternalAuthentications = externalServicesCtrl.hasAuthentications();
		externalProvidersLink = LinkFactory.createLink("authentication.extern", mainVC, this);
		externalProvidersLink.setVisible(hasExternalAuthentications);
		segmentView.addSegment(externalProvidersLink, false);
	
		if(!hasOpenOlatAuthentication && hasExternalAuthentications) {
			doOpenExternalProviders(ureq);
			segmentView.select(externalProvidersLink);
		} else {
			doOpenOlatPassword(ureq);
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == openOlatPasswordLink) {
					doOpenOlatPassword(ureq);
				} else if (clickedLink == externalProvidersLink){
					doOpenExternalProviders(ureq);
				}
			}
		}
	}
	
	private void doOpenOlatPassword(UserRequest ureq) {
		addToHistory(ureq, openOlatCtrl);
		mainVC.put("segmentCmp", openOlatCtrl.getInitialComponent());
	}
	
	private void doOpenExternalProviders(UserRequest ureq) {
		addToHistory(ureq, externalServicesCtrl);
		mainVC.put("segmentCmp", externalServicesCtrl.getInitialComponent());
	}
}