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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Order;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2025-02-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class CurriculumAccountingDAO {

	@Autowired
	private DB dbInstance;

	public List<BookingOrder> bookingOrders(IdentityRef identity, List<UserPropertyHandler> userPropertyHandlers) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct cur.displayName, cur.identifier, ");
		sb.append(" org.identifier, org.displayName, ");
		sb.append(" ce.displayName, ce.identifier, ceType.identifier, ce.status, ceEduType.identifier, ce.beginDate, ce.endDate, ");
		sb.append(" o, billingAddress, ");
		sb.append(" offer.resourceDisplayName, offer.resourceTypeName, offerCostCenter.name, offerCostCenter.account ");
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			sb.append(", user.").append(userPropertyHandler.getName()).append(" as p_").append(userPropertyHandler.getName());
		}
		sb.append(" from acorder o");
		sb.append(" inner join fetch o.billingAddress billingAddress");
		sb.append(" inner join o.parts orderPart");
		sb.append(" inner join orderPart.lines orderLine");
		sb.append(" inner join orderLine.offer offer");
		sb.append(" left join offer.costCenter offerCostCenter");
		sb.append(" inner join curriculumelement ce on ce.resource = offer.resource");
		sb.append(" left join ce.type ceType");
		sb.append(" left join ce.educationalType ceEduType");
		sb.append(" inner join ce.curriculum cur");
		sb.append(" inner join cur.organisation org");
		sb.append(" inner join cur.group cGroup");
		sb.append(" inner join cGroup.members mgmtMember");
		sb.append(" inner join o.delivery orderer");
		sb.append(" inner join orderer.user user");
		sb.append(" where mgmtMember.identity.key = :identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList().stream()
				.map(objects -> mapToBookingOrder(objects, userPropertyHandlers))
				.toList();
	}
	
	private BookingOrder mapToBookingOrder(Object[] objects, List<UserPropertyHandler> userPropertyHandlers) {
		BookingOrder bookingOrder = new BookingOrder();

		int srcIdx = 0;
		
		// curriculum
		if (objects[srcIdx++] instanceof String curriculumName) {
			bookingOrder.setCurriculumName(curriculumName);
		}
		if (objects[srcIdx++] instanceof String curriculumIdentifier) {
			bookingOrder.setCurriculumIdentifier(curriculumIdentifier);
		}
		
		// org
		if (objects[srcIdx++] instanceof String orgId) {
			bookingOrder.setOrgId(orgId);
		}
		if (objects[srcIdx++] instanceof String orgName) {
			bookingOrder.setOrgName(orgName);
		}
		
		// curriculum element (implementation)
		if (objects[srcIdx++] instanceof String implementationName) {
			bookingOrder.setImplementationName(implementationName);
		}
		if (objects[srcIdx++] instanceof String implementationIdentifier) {
			bookingOrder.setImplementationIdentifier(implementationIdentifier);
		}
		if (objects[srcIdx++] instanceof String implementationType) {
			bookingOrder.setImplementationType(implementationType);
		}
		if (objects[srcIdx++] instanceof String implementationStatus) {
			bookingOrder.setImplementationStatus(implementationStatus);
		}
		if (objects[srcIdx++] instanceof String implementationFormat) {
			bookingOrder.setImplementationFormat(implementationFormat);
		}
		if (objects[srcIdx++] instanceof Date beginDate) {
			bookingOrder.setBeginDate(beginDate);
		}
		if (objects[srcIdx++] instanceof Date endDate) {
			bookingOrder.setEndDate(endDate);
		}
		
		// order (booking)
		if (objects[srcIdx++] instanceof Order order) {
			bookingOrder.setOrder(order);
		}
		
		// billing address
		if (objects[srcIdx++] instanceof BillingAddress billingAddress) {	
			bookingOrder.setBillingAddress(billingAddress);
		}
		
		// offer
		if (objects[srcIdx++] instanceof String offerName) {
			bookingOrder.setOfferName(offerName);
		}
		if (objects[srcIdx++] instanceof String offerType) {
			bookingOrder.setOfferType(offerType);
		}
		if (objects[srcIdx++] instanceof String offerCostCenter) {
			bookingOrder.setOfferCostCenter(offerCostCenter);
		}
		if (objects[srcIdx++] instanceof String offerAccount) {
			bookingOrder.setOfferAccount(offerAccount);
		}


		for (int dstIdx = 0; dstIdx < userPropertyHandlers.size() && srcIdx < objects.length; dstIdx++, srcIdx++) {
			if (objects[srcIdx] instanceof String sourceString) {
				bookingOrder.setIdentityProp(dstIdx, sourceString);
			}
		}

		return bookingOrder;
	}
}
