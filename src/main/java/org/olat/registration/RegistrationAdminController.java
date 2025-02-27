/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.registration;

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
 * Initial date: Feb 25, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationAdminController extends BasicController implements Activateable2 {

	private static final String SEGMENT_CMP = "segmentCmp";
	private static final String ACCOUNT_RES_TYPE = "account";
	private static final String EXTERNAL_RES_TYPE = "external";

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;

	private final Link configLink;
	private final Link accountLink;
	private final Link externalLink;

	private final RegistrationConfigAdminController regConfigCtrl;
	private RegistrationAccountAdminController regAccountCtrl;
	private RegistrationExternalAdminController regExternalCtrl;

	public RegistrationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("admin");

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);

		configLink = LinkFactory.createLink("self.reg.config", mainVC, this);
		segmentView.addSegment(configLink, true);
		accountLink = LinkFactory.createLink("self.reg.account", mainVC, this);
		segmentView.addSegment(accountLink, false);
		externalLink = LinkFactory.createLink("self.reg.extern", mainVC, this);
		segmentView.addSegment(externalLink, false);

		regConfigCtrl = new RegistrationConfigAdminController(ureq, getWindowControl());
		listenTo(regConfigCtrl);
		doOpenRegConfig(ureq);

		putInitialPanel(mainVC);
	}

	private void doOpenRegConfig(UserRequest ureq) {
		addToHistory(ureq, regConfigCtrl);
		mainVC.put(SEGMENT_CMP, regConfigCtrl.getInitialComponent());
	}

	private void doOpenRegAccount(UserRequest ureq) {
		if (regAccountCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ACCOUNT_RES_TYPE), null);
			regAccountCtrl = new RegistrationAccountAdminController(ureq, swControl);
			listenTo(regAccountCtrl);
		} else {
			addToHistory(ureq, regAccountCtrl);
		}
		mainVC.put(SEGMENT_CMP, regAccountCtrl.getInitialComponent());
	}

	private void doOpenRegExternal(UserRequest ureq) {
		if (regExternalCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(EXTERNAL_RES_TYPE), null);
			regExternalCtrl = new RegistrationExternalAdminController(ureq, swControl);
			listenTo(regExternalCtrl);
		} else {
			addToHistory(ureq, regExternalCtrl);
		}
		mainVC.put(SEGMENT_CMP, regExternalCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if(clickedLink == configLink) {
					doOpenRegConfig(ureq);
				} else if(clickedLink == accountLink) {
					doOpenRegAccount(ureq);
				} else if(clickedLink == externalLink) {
					doOpenRegExternal(ureq);
				}
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doOpenRegConfig(ureq);
			segmentView.select(configLink);
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if(ACCOUNT_RES_TYPE.equalsIgnoreCase(type)) {
				doOpenRegAccount(ureq);
				segmentView.select(accountLink);
			} else if(EXTERNAL_RES_TYPE.equalsIgnoreCase(type)) {
				doOpenRegExternal(ureq);
				segmentView.select(externalLink);
			}
		}
	}
}
