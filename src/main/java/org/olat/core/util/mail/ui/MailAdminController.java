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
package org.olat.core.util.mail.ui;

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
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailModule;

/**
 * 
 * Initial date: 12.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailAdminController extends BasicController {

	private final Link settingsLink, templateLink;
	private final SegmentViewComponent segmentView;
	
	private final VelocityContainer mainVC;
	
	private MailSettingsAdminController settingsCtrl;
	private MailTemplateAdminController templateCtrl;
	
	public MailAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MailModule.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		settingsLink = LinkFactory.createLink("mail.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);
		
		templateLink = LinkFactory.createLink("mail.template", mainVC, this);
		segmentView.addSegment(templateLink, false);
		
		mainVC.put("segments", segmentView);
		doOpenSettings(ureq);
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
				} else if (clickedLink == templateLink) {
					doOpenTemplate(ureq);
				}
			}
		}
	}

	private void doOpenSettings(UserRequest ureq) {
		if(settingsCtrl == null) {
			settingsCtrl = new MailSettingsAdminController(ureq, getWindowControl());
			listenTo(settingsCtrl);
		}
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}

	private void doOpenTemplate(UserRequest ureq) {
		if(templateCtrl == null) {
			templateCtrl = new MailTemplateAdminController(ureq, getWindowControl());
			listenTo(templateCtrl);
		}
		mainVC.put("segmentCmp", templateCtrl.getInitialComponent());
	}
}