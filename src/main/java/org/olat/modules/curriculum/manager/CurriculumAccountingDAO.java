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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.model.CurriculumAccountingSearchParams;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.model.AccessMethod;
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

	private Map<String, GroupMembershipStatus> getImplementationAndIdentityToMembership(CurriculumAccountingSearchParams searchParams) {
		HashMap<String, GroupMembershipStatus> result = new HashMap<>();

		if (searchParams == null) {
			return result;
		}
		
		boolean specifyCurriculum = (searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) 
				|| searchParams.getCurriculum() != null;
		
		boolean specifyCurriculumElement = searchParams.getCurriculumElement() != null;
		
		if (!specifyCurriculum && !specifyCurriculumElement) {
			return result;
		}
		
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select ce.key, gmh.identity.key, gmh.status ");
		sb.append(" from bgroupmemberhistory gmh");
		sb.append(" inner join curriculumelement ce on ce.group.key = gmh.group.key ");
		
		if (specifyCurriculum) {
			sb.append(" inner join curriculum c on c.key = ce.curriculum.key ");
		}
		
		sb.append(" where gmh.role = 'participant' ");

		if (specifyCurriculum) {
			sb.append(" and c.key in :curriculumKeys ");
		}
		
		if (specifyCurriculumElement) {
			sb.append(" and ce.key = :curriculumElementKey ");
		}
		
		sb.append(" and (gmh.group.key, gmh.identity.key, gmh.creationDate) in (");
		sb.append("  select gmh2.group.key, gmh2.identity.key, max(gmh2.creationDate) ");
		sb.append("   from bgroupmemberhistory gmh2 ");
		sb.append("   inner join curriculumelement ce2 on ce2.group.key = gmh2.group.key ");

		if (specifyCurriculum) {
			sb.append("   inner join curriculum c2 on c2.key = ce2.curriculum.key ");
		}

		sb.append("   where gmh2.role = 'participant' ");

		if (specifyCurriculum) {
			sb.append(" and c2.key in :curriculumKeys ");
		}

		if (specifyCurriculumElement) {
			sb.append(" and ce2.key = :curriculumElementKey ");
		}

		sb.append("   group by (gmh2.group.key, gmh2.identity.key) ");
		sb.append(" ) ");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);

		if (searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			List<Long> curriculumKeys = searchParams.getCurriculums().stream().map(CurriculumRef::getKey).toList();
			query.setParameter("curriculumKeys", curriculumKeys);
		} else if (searchParams.getCurriculum() != null) {
			List<Long> curriculumKeys = Collections.singletonList(searchParams.getCurriculum().getKey());
			query.setParameter("curriculumKeys", curriculumKeys);
		}
		
		if (specifyCurriculumElement) {
			query.setParameter("curriculumElementKey", searchParams.getCurriculumElement().getKey());			
		}

		query.getResultStream().forEach(row -> addToMembershipMap(result, row));
		
		return result;
	}

	private void addToMembershipMap(HashMap<String, GroupMembershipStatus> result, Object[] row) {
		if (row.length < 3) {
			return;
		}
		if (row[0] instanceof Number curriculumElementKey && row[1] instanceof Number identityKey && 
				row[2] instanceof GroupMembershipStatus status) {
			result.put(curriculumElementKey + "_" + identityKey, status);
		}
	}
	
	public List<BookingOrder> bookingOrders(CurriculumAccountingSearchParams searchParams, List<UserPropertyHandler> userPropertyHandlers) {
		Map<String, GroupMembershipStatus> implementationAndIdentityToMembership = getImplementationAndIdentityToMembership(searchParams);
		
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct cur.key, cur.displayName, cur.identifier, ");
		sb.append(" curOrg.identifier, curOrg.displayName, ");
		sb.append(" ce.key, ce.displayName, ce.identifier, ceType.identifier, ce.status, ceEduType.identifier, ce.beginDate, ce.endDate, ce.location, ");
		sb.append(" o, billingAddress, billingAddressOrg.identifier, billingAddressOrg.displayName, ");
		sb.append(" trx.statusStr, p_trx.status, c_trx.status, m, ");
		sb.append(" offer.label, offer.resourceTypeName, offerCostCenter.name, offerCostCenter.account, ");
		sb.append(" orderer.key ");
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
		sb.append(" left join actransaction trx on trx.order.key = o.key");
		sb.append(" left join paypaltransaction p_trx on p_trx.orderId = o.key");
		sb.append(" left join paypalcheckouttransaction c_trx on c_trx.orderId = o.key");
		sb.append(" left join trx.method m");
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
		if(searchParams.isExcludeDeletedCurriculumElements()) {
			sb.and().append("ce.status <> 'deleted'");
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
				.map(objects -> mapToBookingOrder(objects, userPropertyHandlers, implementationAndIdentityToMembership))
				.toList();
	}
	
	private BookingOrder mapToBookingOrder(Object[] objects, List<UserPropertyHandler> userPropertyHandlers, 
										   Map<String, GroupMembershipStatus> implementationAndIdentityToMembership) {
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
		if (objects[srcIdx++] instanceof String implementationLocation) {
			bookingOrder.setImplementationLocation(implementationLocation);
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
		
		// transaction
		if (objects[srcIdx++] instanceof String transactionStatus) {
			bookingOrder.setTransactionStatus(transactionStatus);
		}

		if (objects[srcIdx++] instanceof String paypalTransactionStatus) {
			bookingOrder.setPaypalTransactionStatus(paypalTransactionStatus);
		}

		if (objects[srcIdx++] instanceof String checkoutTransactionStatus) {
			bookingOrder.setCheckoutTransactionStatus(checkoutTransactionStatus);
		}

		if (objects[srcIdx++] instanceof AccessMethod accessMethod) {
			bookingOrder.setAccessMethod(accessMethod);
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

		// orderer
		if (objects[srcIdx++] instanceof Number ordererKey) {
			bookingOrder.setIdentityKey(ordererKey.longValue());
			String implementationAndIdentityKey = bookingOrder.getImplementationKey() + "_" + ordererKey;
			if (implementationAndIdentityToMembership.containsKey(implementationAndIdentityKey)) {
				bookingOrder.setOrdererMembershipStatus(implementationAndIdentityToMembership.get(implementationAndIdentityKey));
			}
		}
		
		for (int dstIdx = 0; dstIdx < userPropertyHandlers.size() && srcIdx < objects.length; dstIdx++, srcIdx++) {
			if (objects[srcIdx] instanceof String sourceString) {
				bookingOrder.setIdentityProp(dstIdx, sourceString);
			}
		}

		return bookingOrder;
	}
	
	public void loadAssessmentsInfos(List<BookingOrder> bookingOrders, CurriculumAccountingSearchParams searchParams) {
		Map<BookingKey,List<BookingOrder>> ordersMap = new HashMap<>();
		for(BookingOrder order:bookingOrders) {
			BookingKey key = new BookingKey(order.getImplementationKey(), order.getIdentityKey());
			ordersMap.computeIfAbsent(key, k -> new ArrayList<>(2))
				.add(order);
		}
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select curEl.key, curEl.curriculum.key, participant.key,")
		  .append(" statement,")
		  .append(" certificate.creationDate, certificate.nextRecertificationDate,")
		  .append(" courseInfos.initialLaunch, courseInfos.recentLaunch")
		  .append(" from repositoryentry as re")
		  .append(" inner join re.groups as reToParticipantGroup")
		  .append(" inner join reToParticipantGroup.group as participantGroup")
		  .append(" inner join participantGroup.members as participantMembers on (participantMembers.role='participant')")
		  .append(" inner join participantMembers.identity as participant")
		  .append(" inner join curriculumelement as curEl on (participantGroup.key=curEl.group.key)")
		  .append(" left join effstatementlight as statement on (statement.identity.key=participant.key and statement.courseRepoKey=re.key and statement.lastStatement=true)")
		  .append(" left join usercourseinfos as courseInfos on (courseInfos.identity.key=participant.key and courseInfos.resource.key=re.olatResource.key)")
		  .append(" left join certificateentryconfig as certificateConfig on (certificateConfig.entry.key=re.key)")
		  .append(" left join certificate as certificate on (certificate.identity.key=participant.key and certificate.olatResource.key=re.olatResource.key and certificate.last=true)");
		if(searchParams.getCurriculum() != null || (searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty())) {
			sb.and().append(" curEl.curriculum.key in (:curriculumKeys)");
		}
		if(searchParams.getCurriculumElement() != null) {
			sb.and().append(" curEl.key=:curriculumElementKey");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(searchParams.getCurriculum() != null) {
			query.setParameter("curriculumKeys", List.of(searchParams.getCurriculum().getKey()));
		} else if(searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			List<Long> curriculumKeys = searchParams.getCurriculums().stream()
					.map(CurriculumRef::getKey)
					.toList();
			query.setParameter("curriculumKeys", curriculumKeys);
		}
		if(searchParams.getCurriculumElement() != null ) {
			query.setParameter("curriculumElementKey", searchParams.getCurriculumElement().getKey());
		}  
		
		List<Object[]> rawObjects = query.getResultList();
		for(Object[] objects:rawObjects) {
			Long curriculumElementKey = (Long)objects[0];
			Long identityKey = (Long)objects[2];
			UserEfficiencyStatementLight statement = (UserEfficiencyStatementLight)objects[3];
			Date certificateDate = (Date)objects[4];
			Date nextRecertificationDate = (Date)objects[5];
			Date initialLaunch = (Date)objects[6];
			Date recentLaunch = (Date)objects[7];

			BookingKey key = new BookingKey(curriculumElementKey, identityKey);
			List<BookingOrder> orders = ordersMap.get(key);
			if(orders != null) {
				for(BookingOrder order:orders) {
					if(certificateDate != null
							&& (order.getCertificateDate() == null || certificateDate.after(order.getCertificateDate()))) {
						order.setCertificateDate(certificateDate);
					}
					if(nextRecertificationDate != null
							&& (order.getNextCertificationDate() == null || nextRecertificationDate.before(order.getNextCertificationDate()))) {
						order.setNextCertificationDate(nextRecertificationDate);
					}
					
					if(statement != null) {
						if(order.getEfficiencyStatements() == null) {
							order.setEfficiencyStatements(new ArrayList<>(2));
						}
						order.getEfficiencyStatements().add(statement);
					}
					
					if(initialLaunch != null
							&& (order.getFirstVisit() == null || initialLaunch.before(order.getFirstVisit()))) {
						order.setFirstVisit(initialLaunch);
					}
					
					if(recentLaunch != null
							&& (order.getLastVisit() == null || recentLaunch.after(order.getLastVisit()))) {
						order.setLastVisit(recentLaunch);
					}
				}
			}
		}
	}
}
