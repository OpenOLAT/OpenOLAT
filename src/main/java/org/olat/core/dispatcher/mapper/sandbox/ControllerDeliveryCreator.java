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
package org.olat.core.dispatcher.mapper.sandbox;

import java.util.List;

import org.olat.core.commons.fullWebApp.BaseFullWebappControllerParts;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayout;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * 
 * Initial date: 28 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ControllerDeliveryCreator implements BaseFullWebappPopupLayout {

	private final ControllerEventListener listener;
	private final ControllerCreator controllerCreator;
	
	public ControllerDeliveryCreator(ControllerCreator controllerCreator, ControllerEventListener listener) {
		this.controllerCreator = controllerCreator;
		this.listener = listener;
	}

	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		lureq.getUserSession().getSessionInfo().setContentDelivery(true);
		Controller controller = controllerCreator.createController(lureq, lwControl);
		if(listener != null) {
			controller.addControllerListener(listener);
		}
		return controller;
	}

	@Override
	public BaseFullWebappControllerParts getFullWebappParts() {
		return new BaseFullWebappControllerParts() {

			@Override
			public List<SiteInstance> getSiteInstances(UserRequest ureq, WindowControl control) {
				return null;
			}

			@Override
			public Controller getContentController(UserRequest ureq, WindowControl wControl) {
				return createController(ureq, wControl);
			}

			@Override
			public LockableController createTopNavController(UserRequest ureq, WindowControl wControl) {
				return null;
			}

			@Override
			public Controller createHeaderController(UserRequest ureq, WindowControl control) {
				return null;
			}

			@Override
			public LockableController createFooterController(UserRequest ureq, WindowControl control) {
				return null;
			}
		};
	}
}
