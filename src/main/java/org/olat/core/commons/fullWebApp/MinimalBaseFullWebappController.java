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
package org.olat.core.commons.fullWebApp;

import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowController;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.registration.DisclaimerController;
import org.olat.registration.RegistrationManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MinimalBaseFullWebappController extends BaseFullWebappController implements PopupBrowserWindowController {

	private DTab delayedTab;
	private List<ContextEntry> delayedEntries;
	
	private boolean needAcceptDisclaimer;
	
	private CloseableModalController cmc;
	private DisclaimerController disclaimerCtrl;
	
	@Autowired
	private RegistrationManager registrationManager;
	
	/**
	 * @param ureq The user request
	 */
	public MinimalBaseFullWebappController(UserRequest ureq) {
		super(ureq, new MinimalControllerParts());
		// apply custom css if available
		if (contentCtrl instanceof MainLayoutController) {
			MainLayoutController mainLayoutCtr = (MainLayoutController) contentCtrl;
			addCurrentCustomCSSToView(mainLayoutCtr.getCustomCSS());
		}
		
		addBodyCssClass("o_body_minimal");
		
		needAcceptDisclaimer = registrationManager.needsToConfirmDisclaimer(getIdentity());
	}
	
	@Override
	public boolean delayLaunch(UserRequest ureq, BusinessControl bc) {
		delayedEntries = bc == null ? List.of() : bc.getEntries();
		if(needAcceptDisclaimer) {
			openDisclaimer(ureq);
		}
		return needAcceptDisclaimer;
	}
	
	@Override
	public void activate(UserRequest ureq, DTab dTab, List<ContextEntry> entries) {
		if(needAcceptDisclaimer) return;

		super.activate(ureq, dTab, entries);
	}

	@Override
	public void open(UserRequest ureq) {
		ureq.getDispatchResult().setResultingWindow(getWindowControl().getWindowBackOffice().getWindow());
	}

	@Override
	public WindowControl getPopupWindowControl() {
		return getWindowControl();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(disclaimerCtrl == source) {
			cmc.deactivate();
			cleanUpDisclaimer();
			if(event == Event.DONE_EVENT) {
				confirmedDisclaimer();
				openBusinessPath(ureq);
			}
		} else {
			cleanUpDisclaimer();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUpDisclaimer() {
		removeControllerListener(disclaimerCtrl);
		removeControllerListener(cmc);
		disclaimerCtrl = null;
		cmc = null;
	}
	
	private void confirmedDisclaimer() {
		registrationManager.setHasConfirmedDislaimer(getIdentity());
		this.needAcceptDisclaimer = false;
	}
	
	private void openBusinessPath(UserRequest ureq) {
		removeDTab(ureq, delayedTab);
		String businessPath = BusinessControlFactory.getInstance().getAsString(delayedEntries);
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void openDisclaimer(UserRequest ureq) {
		removeControllerListener(disclaimerCtrl);
		removeAsListenerAndDispose(cmc);
		
		disclaimerCtrl = new DisclaimerController(ureq, getWindowControl(), ureq.getIdentity(), false);
		listenTo(disclaimerCtrl);

		String title = disclaimerCtrl.getAndRemoveTitle();
		cmc = new CloseableModalController(getWindowControl(), "close", disclaimerCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	public static class MinimalControllerParts implements BaseFullWebappControllerParts {

		@Override
		public List<SiteInstance> getSiteInstances(UserRequest ureq, WindowControl wControl) {
			return Collections.emptyList();
		}

		@Override
		public Controller getContentController(UserRequest ureq, WindowControl wControl) {
			return null;
		}

		@Override
		public Controller createHeaderController(UserRequest ureq, WindowControl wControl) {
			return null;
		}

		@Override
		public LockableController createTopNavController(UserRequest ureq, WindowControl wControl) {

			return null;
		}

		@Override
		public LockableController createFooterController(UserRequest ureq, WindowControl wControl) {
			return null;
		}
	}
	
	public static class EmptyController extends BasicController {
		
		public EmptyController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			
			putInitialPanel(new Panel("empty"));
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
