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
package org.olat.resource.accesscontrol.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;

/**
 * 
 * Description:<br>
 *  It's a wrapper to manage the acces to repository entries
 * 
 * <P>
 * Initial Date:  9 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryMainAccessControllerWrapper extends MainLayoutBasicController implements Activateable2 {

	private final StackedPanel contentP;
	private VelocityContainer mainVC;
	private Controller accessController;
	private final MainLayoutController resController;

	public RepositoryMainAccessControllerWrapper(UserRequest ureq, WindowControl wControl, RepositoryEntry re, MainLayoutController resController) {
		super(ureq, wControl);
		
		this.resController = resController;
		
		if(ureq.getUserSession().getRoles().isOLATAdmin()) {
			contentP = (StackedPanel)resController.getInitialComponent();
		} else {
			// guest are allowed to see resource with BARG 
			if(re.getAccess() == RepositoryEntry.ACC_USERS_GUESTS && ureq.getUserSession().getRoles().isGuestOnly()) {
				contentP = (StackedPanel)resController.getInitialComponent();
			} else {
				ACService acService = CoreSpringFactory.getImpl(ACService.class);
				AccessResult acResult = acService.isAccessible(re, getIdentity(), false);
				if(acResult.isAccessible()) {
					contentP = (StackedPanel)resController.getInitialComponent();
				} else if (re != null && acResult.getAvailableMethods().size() > 0) {
					accessController = new AccessListController(ureq, getWindowControl(), acResult.getAvailableMethods());
					listenTo(accessController);
					mainVC = createVelocityContainer("access_wrapper");
					mainVC.put("accessPanel", accessController.getInitialComponent());
					contentP = new SimpleStackedPanel("");
					contentP.setContent(mainVC);
				} else {
					mainVC = createVelocityContainer("access_refused");
					contentP = new SimpleStackedPanel("");
					contentP.setContent(mainVC);
					wControl.setWarning(translate("course.closed"));
				}
			}
		}
		putInitialPanel(contentP);
	}
	
	private void openContent() {
		contentP.setContent(resController.getInitialComponent());
	}
	
	@Override
	public CustomCSS getCustomCSS() {
		return resController == null ? null : resController.getCustomCSS();
	}

	@Override
	public void setCustomCSS(CustomCSS newCustomCSS) {
		if(resController != null) {
			resController.setCustomCSS(newCustomCSS);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(resController instanceof Activateable2) {
			((Activateable2)resController).activate(ureq, entries, state);
		}
	}

	@Override
	protected void doDispose() {
		if(resController != null && !resController.isDisposed()) {
			resController.dispose();
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == resController) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessController) {
			if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
				openContent();
				removeAsListenerAndDispose(accessController);
				accessController = null;
			} else if(event.equals(AccessEvent.ACCESS_FAILED_EVENT)) {
				String msg = ((AccessEvent)event).getMessage();
				if(StringHelper.containsNonWhitespace(msg)) {
					getWindowControl().setError(msg);
				} else {
					showError("error.accesscontrol");
				}
			}
		}
	}
}