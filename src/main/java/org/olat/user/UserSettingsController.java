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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.ui.IMPreferenceController;
import org.olat.registration.DisclaimerController;
import org.olat.registration.RegistrationModule;
import org.olat.user.ui.data.UserDataController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Wrapper for some settings: preferences, webdav settings, disclaimer
 * 
 * Initial date: 27.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSettingsController extends BasicController implements Activateable2 {
	
	private Link webdavLink;
	private Link imLink;
	private Link disclaimerLink;
	private final Link userDataLink;
	private final Link preferencesLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private IMPreferenceController imCtrl;
	private DisclaimerController disclaimerCtrl;
	private WebDAVPasswordController webdavCtrl;
	private ChangePrefsController preferencesCtrl;
	private UserDataController userDataCtrl;
	
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private RegistrationModule registrationModule;

	/**
	 * @param ureq
	 * @param wControl
	 */
	public UserSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("user_settings");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		preferencesLink = LinkFactory.createLink("tab.prefs", mainVC, this);
		preferencesLink.setElementCssClass("o_sel_user_settings_prefs");
		segmentView.addSegment(preferencesLink, true);
		if(webDAVModule.isEnabled()) {
			webdavLink = LinkFactory.createLink("tab.pwdav", mainVC, this);
			webdavLink.setElementCssClass("o_sel_user_settings_webdav");
			segmentView.addSegment(webdavLink, false);
		}
		if(imModule.isEnabled()) {
			imLink = LinkFactory.createLink("tab.im", mainVC, this);
			imLink.setElementCssClass("o_sel_user_settings_im");
			segmentView.addSegment(imLink, false);
		}
		if (registrationModule.isDisclaimerEnabled()) {
			disclaimerLink = LinkFactory.createLink("tab.disclaimer", mainVC, this);
			disclaimerLink.setElementCssClass("o_sel_user_settings_disclaimer");
			segmentView.addSegment(disclaimerLink, false);
		}
		userDataLink = LinkFactory.createLink("tab.user.data", mainVC, this);
		userDataLink.setElementCssClass("o_sel_user_data_download");
		segmentView.addSegment(userDataLink, false);
		
		
		mainVC.put("segments", segmentView);
		doOpenPreferences(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Preferences".equalsIgnoreCase(name)) {
			doOpenPreferences(ureq);
			segmentView.select(preferencesLink);
		} else if("WebDAV".equalsIgnoreCase(name) && webdavLink != null && webDAVModule.isEnabled()) {
			doOpenWebDAV(ureq);
			segmentView.select(webdavLink);	
		} else if("Chat".equalsIgnoreCase(name) && imLink != null && imModule.isEnabled()) {
			doOpenIM(ureq);
			segmentView.select(webdavLink);
		} else if("Disclaimer".equalsIgnoreCase(name) && disclaimerLink != null && registrationModule.isDisclaimerEnabled()) {
			doOpenDisclaimer(ureq);
			segmentView.select(disclaimerLink);	
		} else if("Data".equalsIgnoreCase(name)) {
			doOpenUserData(ureq);
			segmentView.select(userDataLink);	
		}
	}
	
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
				} else if (clickedLink == userDataLink) {
					doOpenUserData(ureq);
				}
			}
		}
	}
	
	private void doOpenPreferences(UserRequest ureq) {
		if(preferencesCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Preferences", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			preferencesCtrl = new ChangePrefsController(ureq, bwControl, getIdentity());
			listenTo(preferencesCtrl);
		}
		mainVC.put("segmentCmp", preferencesCtrl.getInitialComponent());
		addToHistory(ureq, preferencesCtrl);
	}
	
	private void doOpenWebDAV(UserRequest ureq) {
		if(webdavCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("WebDAV", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			webdavCtrl = new WebDAVPasswordController(ureq, bwControl);
			listenTo(webdavCtrl);
		}
		mainVC.put("segmentCmp", webdavCtrl.getInitialComponent());
		addToHistory(ureq, webdavCtrl);
	}
	
	private void doOpenIM(UserRequest ureq) {
		if(imCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Chat", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			imCtrl = new IMPreferenceController(ureq, bwControl, getIdentity());
			listenTo(imCtrl);
		}
		mainVC.put("segmentCmp", imCtrl.getInitialComponent());
		addToHistory(ureq, imCtrl);
	}
	
	private void doOpenDisclaimer(UserRequest ureq) {
		if(disclaimerCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Disclaimer", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			disclaimerCtrl = new DisclaimerController(ureq, bwControl);
			listenTo(disclaimerCtrl);
		}
		mainVC.put("segmentCmp", disclaimerCtrl.getInitialComponent());
		addToHistory(ureq, disclaimerCtrl);
	}
	
	private void doOpenUserData(UserRequest ureq) {
		if(userDataCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Data", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			userDataCtrl = new UserDataController(ureq, bwControl, getIdentity());
			listenTo(userDataCtrl);
		}
		mainVC.put("segmentCmp", userDataCtrl.getInitialComponent());
		addToHistory(ureq, userDataCtrl);
	}	
}