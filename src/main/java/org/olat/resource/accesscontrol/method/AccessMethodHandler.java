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

package org.olat.resource.accesscontrol.method;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.FormController;

/**
 * 
 * Description:<br>
 * A method handler must impements this interface
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface AccessMethodHandler {
	
	public boolean isPaymentMethod();
	
	/**
	 * 
	 * @return true if it is technically possible to have 2 methods overlap
	 */
	public boolean isOverlapAllowed(AccessMethodHandler handler);
	
	public String getType();
	
	public String getMethodName(Locale locale);
	
	public AccessMethodSecurityCallback getSecurityCallback(Identity identity, Roles roles);
	
	public FormController createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link, Form form);
	
	public AbstractConfigurationMethodController createConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean catalogSupported);

	public AbstractConfigurationMethodController editConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean catalogSupported);

	public FormController createTransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order, OrderPart part, AccessMethod method, Form form);
	
	
	
	//access worklow
	public boolean checkArgument(OfferAccess link, Object argument);
	
	//control
	public List<PSPTransaction> getPSPTransactions(List<Order> orders);
	
}
