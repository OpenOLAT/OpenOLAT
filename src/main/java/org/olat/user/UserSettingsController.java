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
package org.olat.user;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.webdav.WebDAVModule;
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
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.ui.IMPreferenceController;
import org.olat.registration.DisclaimerController;
import org.olat.registration.RegistrationModule;

/**
 * 
 * Wrapper for some settings: preferences, webdav settings, disclaimer
 * 
 * Initial date: 27.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSettingsController extends BasicController implements Activateable2 {
	

	private final Link preferencesLink;
	private Link webdavLink, imLink, disclaimerLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private IMPreferenceController imCtrl;
	private DisclaimerController disclaimerCtrl;
	private WebDAVPasswordController webdavCtrl;
	private ChangePrefsController preferencesCtrl;

	/**
	 * @param ureq
	 * @param wControl
	 */
	public UserSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("user_settings");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		preferencesLink = LinkFactory.createLink("tab.prefs", mainVC, this);
		segmentView.addSegment(preferencesLink, true);
		if(CoreSpringFactory.getImpl(WebDAVModule.class).isEnabled()) {
			webdavLink = LinkFactory.createLink("tab.pwdav", mainVC, this);
			segmentView.addSegment(webdavLink, false);
		}
		if(CoreSpringFactory.getImpl(InstantMessagingModule.class).isEnabled()){
			imLink = LinkFactory.createLink("tab.im", mainVC, this);
			segmentView.addSegment(imLink, false);
		}
		if (CoreSpringFactory.getImpl(RegistrationModule.class).isDisclaimerEnabled()) {
			disclaimerLink = LinkFactory.createLink("tab.disclaimer", mainVC, this);
			segmentView.addSegment(disclaimerLink, false);
		}
		mainVC.put("segments", segmentView);
		doOpenPreferences(ureq);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == preferencesLink) {
					doOpenPreferences(ureq);
				} else if (clickedLink == webdavLink) {
					doOpenWebDAV(ureq);
				} else if (clickedLink == imLink) {
					doOpenIM(ureq);
				} else if (clickedLink == disclaimerLink) {
					doOpenDisclaimer(ureq);
				}
			}
		}
	}
	
	private void doOpenPreferences(UserRequest ureq) {
		if(preferencesCtrl == null) {
			preferencesCtrl = new ChangePrefsController(ureq, getWindowControl(), getIdentity());
			listenTo(preferencesCtrl);
		}
		mainVC.put("segmentCmp", preferencesCtrl.getInitialComponent());
	}
	
	private void doOpenWebDAV(UserRequest ureq) {
		if(webdavCtrl == null) {
			webdavCtrl = new WebDAVPasswordController(ureq, getWindowControl());
			listenTo(webdavCtrl);
		}
		mainVC.put("segmentCmp", webdavCtrl.getInitialComponent());
	}
	
	private void doOpenIM(UserRequest ureq) {
		if(imCtrl == null) {
			imCtrl = new IMPreferenceController(ureq, getWindowControl(), getIdentity());
			listenTo(imCtrl);
		}
		mainVC.put("segmentCmp", imCtrl.getInitialComponent());
	}
	
	private void doOpenDisclaimer(UserRequest ureq) {
		if(disclaimerCtrl == null) {
			disclaimerCtrl = new DisclaimerController(ureq, getWindowControl(), true);
			listenTo(disclaimerCtrl);
		}
		mainVC.put("segmentCmp", disclaimerCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
}