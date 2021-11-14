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

import java.util.List;

import org.olat.core.CoreSpringFactory;
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
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailModule;

/**
 * 
 * A controller with 2 segments for the inbox and the outbox
 * 
 * Initial date: 27.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailBoxOverviewController extends BasicController implements Activateable2 {
	
	private final Link inboxLink, outboxLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	private MailListController inboxCtrl, outboxCtrl;
	
	private final MailContextResolver resolver;
	
	public MailBoxOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MailModule.class, ureq.getLocale()));
		
		resolver = (MailContextResolver)CoreSpringFactory.getBean("mailBoxExtension");
		
		mainVC = createVelocityContainer("mailbox_overview");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		inboxLink = LinkFactory.createLink("mail.inbox", mainVC, this);
		segmentView.addSegment(inboxLink, true);
		outboxLink = LinkFactory.createLink("mail.outbox", mainVC, this);
		segmentView.addSegment(outboxLink, false);
		
		mainVC.put("segments", segmentView);
		doOpenInbox(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == inboxLink) {
					doOpenInbox(ureq);
				} else if (clickedLink == outboxLink) {
					doOpenOutbox(ureq);
				}
			}
		}
	}

	private void doOpenInbox(UserRequest ureq) {
		if(inboxCtrl == null) {
			inboxCtrl = new MailListController(ureq, getWindowControl(), false, resolver);
			listenTo(inboxCtrl);
		} else {
			inboxCtrl.activate(ureq, null, null);
		}
		mainVC.put("segmentCmp", inboxCtrl.getInitialComponent());
	}

	private void doOpenOutbox(UserRequest ureq) {
		if(outboxCtrl == null) {
			outboxCtrl = new MailListController(ureq, getWindowControl(), true, resolver);
			listenTo(outboxCtrl);
		} else {
			outboxCtrl.activate(ureq, null, null);
		}
		mainVC.put("segmentCmp", outboxCtrl.getInitialComponent());
	}
}
