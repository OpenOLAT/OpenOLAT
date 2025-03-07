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
package org.olat.resource.accesscontrol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.olat.core.id.User;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Order;

/**
 * Initial date: 2025-01-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class UserOrder {
	User user;
	Order order;
	private List<String> identityProps;
	private BillingAddress billingAddress;
	private String billingAddressOrgId;
	private String billingAddressOrgName;
	private String offerName;
	private String offerType;
	private String offerCostCenter;
	private String offerAccount;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setIdentityProp(int index, String value) {
		if (identityProps == null) {
			identityProps = new ArrayList<>();
		}
		while (identityProps.size() < index) {
			identityProps.add(null);
		}
		if (index < identityProps.size()) {
			identityProps.set(index, value);
		} else {
			identityProps.add(value);
		}
	}

	public String getIdentityProp(int index) {
		if (identityProps == null || index >= identityProps.size()) {
			return "";
		}
		return identityProps.get(index);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		UserOrder userOrder = (UserOrder) o;
		return Objects.equals(order, userOrder.order);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, order, identityProps);
	}

	public void setBillingAddressOrgId(String billingAddressOrgId) {
		this.billingAddressOrgId = billingAddressOrgId;
	}

	public String getBillingAddressOrgId() {
		return billingAddressOrgId;
	}

	public void setBillingAddressOrgName(String billingAddressOrgName) {
		this.billingAddressOrgName = billingAddressOrgName;
	}

	public String getBillingAddressOrgName() {
		return billingAddressOrgName;
	}

	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}

	public String getOfferName() {
		return offerName;
	}

	public void setOfferType(String offerType) {
		this.offerType = offerType;
	}

	public String getOfferType() {
		return offerType;
	}

	public void setOfferCostCenter(String offerCostCenter) {
		this.offerCostCenter = offerCostCenter;
	}

	public String getOfferCostCenter() {
		return offerCostCenter;
	}

	public void setOfferAccount(String offerAccount) {
		this.offerAccount = offerAccount;
	}

	public String getOfferAccount() {
		return offerAccount;
	}
}
