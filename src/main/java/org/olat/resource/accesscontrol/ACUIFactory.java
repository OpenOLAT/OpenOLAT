/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.resource.accesscontrol;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.AccessListController;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.olat.resource.accesscontrol.ui.OrdersController;

/**
 * 
 * Description:<br>
 * Factory for the controllers used by access control
 * 
 * <P>
 * Initial Date: 18 avr. 2011 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACUIFactory {

	public static Controller createAccessController(UserRequest ureq, WindowControl wControl, List<OfferAccess> links) {
		// Always use multi list for layouting purpose
		AccessListController multiAccessCtrl = new AccessListController(ureq, wControl, links);
		return multiAccessCtrl;
	}

	public static AbstractConfigurationMethodController createAccessConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link) {

		AccessControlModule module = (AccessControlModule) CoreSpringFactory.getBean("acModule");
		AccessMethodHandler handler = module.getAccessMethodHandler(link.getMethod().getType());
		if (handler != null) { return handler.createConfigurationController(ureq, wControl, link); }
		return null;
	}

	public static Controller createOrdersController(UserRequest ureq, WindowControl wControl) {
		return new OrdersController(ureq, wControl);
	}

	public static Controller createOrdersAdminController(UserRequest ureq, WindowControl wControl, OLATResource resource) {
		return new OrdersAdminController(ureq, wControl, resource);
	}
}
