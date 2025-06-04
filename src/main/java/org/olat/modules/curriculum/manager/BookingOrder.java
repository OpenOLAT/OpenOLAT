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

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.id.User;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.model.AccessMethod;

/**
 * Initial date: 2025-02-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BookingOrder {
	User user;
	Order order;
	private Long identityKey;
	private List<String> identityProps;
	private Long curriculumKey;
	private String curriculumName;
	private String curriculumIdentifier;
	private String curriculumOrgId;
	private String curriculumOrgName;
	private Long implementationKey;
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
	private String billingAddressOrgId;
	private String billingAddressOrgName;
	private AccessMethod accessMethod;
	private String transactionStatus;
	private String paypalTransactionStatus;
	private String checkoutTransactionStatus;
	private String implementationLocation;
	private GroupMembershipStatus ordererMembershipStatus;
	
	private Date nextCertificationDate;
	private List<Long> certificateKeys;
	private List<UserEfficiencyStatementLight> efficiencyStatements;
	
	private Date firstVisit;
	private Date lastVisit;
	
	private LectureBlockIdentityStatistics lectureBlockStatistics;

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
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
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

	public Long getCurriculumKey() {
		return curriculumKey;
	}

	public void setCurriculumKey(Long curriculumKey) {
		this.curriculumKey = curriculumKey;
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

	public void setCurriculumOrgId(String curriculumOrgId) {
		this.curriculumOrgId = curriculumOrgId;
	}

	public String getCurriculumOrgId() {
		return curriculumOrgId;
	}

	public void setCurriculumOrgName(String curriculumOrgName) {
		this.curriculumOrgName = curriculumOrgName;
	}

	public String getCurriculumOrgName() {
		return curriculumOrgName;
	}

	public Long getImplementationKey() {
		return implementationKey;
	}

	public void setImplementationKey(Long implementationKey) {
		this.implementationKey = implementationKey;
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
	
	public String getImplementationLocation() {
		return implementationLocation;
	}

	public void setImplementationLocation(String implementationLocation) {
		this.implementationLocation = implementationLocation;
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

	public String getBillingAddressOrgId() {
		return billingAddressOrgId;
	}

	public void setBillingAddressOrgId(String billingAddressOrgId) {
		this.billingAddressOrgId = billingAddressOrgId;
	}

	public String getBillingAddressOrgName() {
		return billingAddressOrgName;
	}

	public void setBillingAddressOrgName(String billingAddressOrgName) {
		this.billingAddressOrgName = billingAddressOrgName;
	}

	public void setAccessMethod(AccessMethod accessMethod) {
		this.accessMethod = accessMethod;
	}

	public AccessMethod getAccessMethod() {
		return accessMethod;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setPaypalTransactionStatus(String paypalTransactionStatus) {
		this.paypalTransactionStatus = paypalTransactionStatus;
	}

	public String getPaypalTransactionStatus() {
		return paypalTransactionStatus;
	}

	public void setCheckoutTransactionStatus(String checkoutTransactionStatus) {
		this.checkoutTransactionStatus = checkoutTransactionStatus;
	}

	public String getCheckoutTransactionStatus() {
		return checkoutTransactionStatus;
	}

	public void setOrdererMembershipStatus(GroupMembershipStatus ordererMembershipStatus) {
		this.ordererMembershipStatus = ordererMembershipStatus;
	}

	public GroupMembershipStatus getOrdererMembershipStatus() {
		return ordererMembershipStatus;
	}

	public List<Long> getCertificateKeys() {
		return certificateKeys;
	}

	public void addCertificateKey(Long certificateKey) {
		if(certificateKeys == null) {
			certificateKeys = new ArrayList<>(3);
		}
		certificateKeys.add(certificateKey);
	}

	public Date getNextCertificationDate() {
		return nextCertificationDate;
	}

	public void setNextCertificationDate(Date nextCertificationDate) {
		this.nextCertificationDate = nextCertificationDate;
	}

	public Date getFirstVisit() {
		return firstVisit;
	}

	public void setFirstVisit(Date firstVisit) {
		this.firstVisit = firstVisit;
	}

	public Date getLastVisit() {
		return lastVisit;
	}

	public void setLastVisit(Date lastVisit) {
		this.lastVisit = lastVisit;
	}

	public List<UserEfficiencyStatementLight> getEfficiencyStatements() {
		return efficiencyStatements;
	}

	public void setEfficiencyStatements(List<UserEfficiencyStatementLight> efficiencyStatements) {
		this.efficiencyStatements = efficiencyStatements;
	}

	public LectureBlockIdentityStatistics getLectureBlockStatistics() {
		return lectureBlockStatistics;
	}

	public void setLectureBlockStatistics(LectureBlockIdentityStatistics lectureBlockStatistics) {
		this.lectureBlockStatistics = lectureBlockStatistics;
	}
}
