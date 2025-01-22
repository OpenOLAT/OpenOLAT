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
package org.olat.modules.catalog.ui;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.course.CorruptedCourseException;
import org.olat.login.LoginProcessController;
import org.olat.login.LoginProcessEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogRepositoryEntryInfosController extends RepositoryEntryDetailsController {

	private final BreadcrumbedStackedPanel stackPanel;
	
	private LightboxController lightboxCtrl;
	private WebCatalogAuthController authCtrl;
	
	@Autowired
	protected ACService acService;

	public CatalogRepositoryEntryInfosController(UserRequest ureq, WindowControl wControl,
			BreadcrumbedStackedPanel stackPanel, RepositoryEntry entry, boolean scrollToOffers, boolean webCatalog) {
		super(ureq, wControl, entry, false, false, webCatalog);
		this.stackPanel = stackPanel;
		
		if (scrollToOffers) {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.scrollToElemId("#offers"));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.scrollToElemId("#o_navbar_wrapper"));
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (authCtrl == source) {
			lightboxCtrl.deactivate();
			cleanUp();
			if (event instanceof LoginProcessEvent) {
				LoginProcessController loginProcessEventCtrl = new LoginProcessController(ureq, getWindowControl(), stackPanel, null);
				if (event == LoginProcessEvent.REGISTER_EVENT) {
					loginProcessEventCtrl.doOpenRegistration(ureq);
				}
			}
		} else if (lightboxCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(authCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		authCtrl = null;
		lightboxCtrl = null;
	}
	
	@Override
	protected void doStart(UserRequest ureq) {
		try {
			if (getIdentity() != null) {
				String businessPath = "[RepositoryEntry:" + getEntry().getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} else {
				doShowLogin(ureq);
			}
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + getEntry().getKey() + " (" + getEntry().getOlatResource().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}

	private void doShowLogin(UserRequest ureq) {
		if (guardModalController(authCtrl)) return;
		
		String offerBusinessPath = CatalogBCFactory.get(false).getOfferBusinessPath(getEntry().getOlatResource());
		authCtrl = new WebCatalogAuthController(ureq, getWindowControl(), offerBusinessPath);
		listenTo(authCtrl);

		lightboxCtrl = new LightboxController(ureq, getWindowControl(), authCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}
}
