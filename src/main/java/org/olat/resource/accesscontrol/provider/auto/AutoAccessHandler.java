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
package org.olat.resource.accesscontrol.provider.auto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.model.SystemACSecurityCallback;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.FormController;

/**
 * AccessMethodHander for the automatic booking module. This module allows to
 * save proposed assignments of users to a course by a third party service. The
 * module effectively assigns the user to a course if the course for the saved
 * key is available.
 *
 * Initial date: 11.08.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AutoAccessHandler implements AccessMethodHandler {

	private static final SystemACSecurityCallback SYSTEM_AC_SECURITY_CALLBACK = new SystemACSecurityCallback();

	@Override
	public boolean isPaymentMethod() {
		return false;
	}

	@Override
	public boolean isOverlapAllowed(AccessMethodHandler handler) {
		return true;
	}

	@Override
	public AccessMethodSecurityCallback getSecurityCallback(Identity identity, Roles roles) {
		return SYSTEM_AC_SECURITY_CALLBACK;
	}

	@Override
	public FormController createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		return null;
	}

	@Override
	public AbstractConfigurationMethodController createConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations, boolean catalogSupported) {
		return null;
	}

	@Override
	public AbstractConfigurationMethodController editConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationSupported, Collection<Organisation> offerOrganisations, boolean catalogSupported) {
		return null;
	}

	@Override
	public FormController createTransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order,
			OrderPart part, AccessMethod method, Form form) {
		return null;
	}

	@Override
	public boolean checkArgument(OfferAccess link, Object argument) {
		return true;
	}

	@Override
	public List<PSPTransaction> getPSPTransactions(List<Order> orders) {
		return Collections.emptyList();
	}

}
