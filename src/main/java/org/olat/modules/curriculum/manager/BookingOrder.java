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
package org.olat.modules.curriculum.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.id.User;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Order;

/**
 * Initial date: 2025-02-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BookingOrder {
	User user;
	Order order;
	private List<String> identityProps;
	private String curriculumName;
	private String curriculumIdentifier;
	private String orgId;
	private String orgName;
	private String implementationName;
	private String implementationIdentifier;
	private String implementationType;
	private String implementationStatus;
	private Date beginDate;
	private Date endDate;
	private String implementationFormat;
	private String offerName;
	private String offerType;
	private String offerCostCenter;
	private String offerAccount;
	private BillingAddress billingAddress;

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

	public void setCurriculumName(String curriculumName) {
		this.curriculumName = curriculumName;
	}

	public String getCurriculumName() {
		return curriculumName;
	}

	public void setCurriculumIdentifier(String curriculumIdentifier) {
		this.curriculumIdentifier = curriculumIdentifier;
	}

	public String getCurriculumIdentifier() {
		return curriculumIdentifier;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}

	public String getImplementationName() {
		return implementationName;
	}

	public void setImplementationIdentifier(String implementationIdentifier) {
		this.implementationIdentifier = implementationIdentifier;
	}

	public String getImplementationIdentifier() {
		return implementationIdentifier;
	}

	public void setImplementationType(String implementationType) {
		this.implementationType = implementationType;
	}

	public String getImplementationType() {
		return implementationType;
	}

	public void setImplementationStatus(String implementationStatus) {
		this.implementationStatus = implementationStatus;
	}

	public String getImplementationStatus() {
		return implementationStatus;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setImplementationFormat(String implementationFormat) {
		this.implementationFormat = implementationFormat;
	}

	public String getImplementationFormat() {
		return implementationFormat;
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

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	public BillingAddress getBillingAddress() {
		return billingAddress;
	}
}
