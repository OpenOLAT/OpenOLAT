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

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.model.CurriculumAccountingSearchParams;
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

	public List<BookingOrder> bookingOrders(CurriculumAccountingSearchParams searchParams, List<UserPropertyHandler> userPropertyHandlers) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct cur.key, cur.displayName, cur.identifier, ");
		sb.append(" curOrg.identifier, curOrg.displayName, ");
		sb.append(" ce.key, ce.displayName, ce.identifier, ceType.identifier, ce.status, ceEduType.identifier, ce.beginDate, ce.endDate, ");
		sb.append(" o, billingAddress, billingAddressOrg.identifier, billingAddressOrg.displayName, ");
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
		sb.append(" inner join cur.organisation curOrg");
		sb.append(" inner join o.delivery orderer");
		sb.append(" inner join orderer.user user");
		sb.append(" left join billingAddress.organisation billingAddressOrg");
		if(searchParams.getIdentity() != null) {
			// Check membership of curriculum
			sb.and().append("exists (select member.group.key from bgroupmember member")
			  .append(" where member.identity.key=:identityKey and member.group.key=cur.group.key")
			  .append(")");
		}
		if(searchParams.getCurriculum() != null) {
			sb.and().append("cur.key = :curriculumKey");
		}
		if(searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			sb.and().append("cur.key in (:curriculumKeys)");
		}
		if(searchParams.getCurriculumElement() != null) {
			sb.and().append("ce.key = :curriculumElementKey");
		}
		if(searchParams.getFromDate() != null) {
			sb.and().append("o.creationDate >= :fromDate");
		}
		if(searchParams.getToDate() != null) {
			sb.and().append("o.creationDate < :toDate");
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(searchParams.getIdentity() != null) {
			query.setParameter("identityKey", searchParams.getIdentity().getKey());
		}
		if(searchParams.getCurriculum() != null) {
			query.setParameter("curriculumKey", searchParams.getCurriculum().getKey());
		}
		if(searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			List<Long> curriculumKeys = searchParams.getCurriculums().stream().map(CurriculumRef::getKey).toList();
			query.setParameter("curriculumKeys", curriculumKeys);
		}
		if(searchParams.getCurriculumElement() != null) {
			query.setParameter("curriculumElementKey", searchParams.getCurriculumElement().getKey());
		}
		if(searchParams.getFromDate() != null) {
			query.setParameter("fromDate", searchParams.getFromDate());
		}
		if(searchParams.getToDate() != null) {
			query.setParameter("toDate", searchParams.getToDate());
		}
		return query.getResultList().stream()
				.map(objects -> mapToBookingOrder(objects, userPropertyHandlers))
				.toList();
	}
	
	private BookingOrder mapToBookingOrder(Object[] objects, List<UserPropertyHandler> userPropertyHandlers) {
		BookingOrder bookingOrder = new BookingOrder();

		int srcIdx = 0;
		
		// curriculum
		if (objects[srcIdx++] instanceof Number curriculumKey) {
			bookingOrder.setCurriculumKey(curriculumKey.longValue());
		}
		if (objects[srcIdx++] instanceof String curriculumName) {
			bookingOrder.setCurriculumName(curriculumName);
		}
		if (objects[srcIdx++] instanceof String curriculumIdentifier) {
			bookingOrder.setCurriculumIdentifier(curriculumIdentifier);
		}
		
		// curriculum org
		if (objects[srcIdx++] instanceof String curriculumOrgId) {
			bookingOrder.setCurriculumOrgId(curriculumOrgId);
		}
		if (objects[srcIdx++] instanceof String curriculumOrgName) {
			bookingOrder.setCurriculumOrgName(curriculumOrgName);
		}
		
		// curriculum element (implementation)
		if (objects[srcIdx++] instanceof Number implementationKey) {
			bookingOrder.setImplementationKey(implementationKey.longValue());
		}
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
		
		if (objects[srcIdx++] instanceof String billingAddressOrgId) {
			bookingOrder.setBillingAddressOrgId(billingAddressOrgId);
		}
		
		if (objects[srcIdx++] instanceof String billingAddressOrgName) {
			bookingOrder.setBillingAddressOrgName(billingAddressOrgName);
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
