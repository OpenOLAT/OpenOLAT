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
package org.olat.resource.accesscontrol.provider.auto.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder.Status;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrderSearchParams;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;
import org.olat.resource.accesscontrol.provider.auto.model.AdvanceOrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 14.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AdvanceOrderDAO {

	@Autowired
	private DB dbInstance;

	AdvanceOrder create(Identity identity, IdentifierKey key, String identifierValue, AccessMethod method) {
		AdvanceOrderImpl advanceOrder = new AdvanceOrderImpl();
		Date creationDate = new Date();
		advanceOrder.setCreationDate(creationDate);
		advanceOrder.setLastModified(creationDate);
		advanceOrder.setIdentity(identity);
		advanceOrder.setIdentifierKey(key);
		advanceOrder.setIdentifierValue(identifierValue);
		advanceOrder.setStatus(Status.PENDING);
		advanceOrder.setStatusModified(creationDate);
		advanceOrder.setMethod(method);
		return advanceOrder;
	}

	List<AdvanceOrder> loadAdvanceOrders(AdvanceOrderSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select advanceOrder from advanceOrder advanceOrder");
		if (searchParams.getIdentitfRef() != null) {
			sb.and().append("advanceOrder.identity.key = :identityKey");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("advanceOrder.status in (:status)");
		}
		if (searchParams.getIdentifierKey() != null) {
			sb.and().append("advanceOrder.identifierKey = :identifierKey");
		}
		if (searchParams.getMethod() != null) {
			sb.and().append("advanceOrder.method.key = :methodKey");
		}
		
		TypedQuery<AdvanceOrder> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AdvanceOrder.class);
		if (searchParams.getIdentitfRef() != null) {
			query.setParameter("identityKey", searchParams.getIdentitfRef().getKey());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			query.setParameter("status", searchParams.getStatus());
		}
		if (searchParams.getIdentifierKey() != null) {
			query.setParameter("identifierKey", searchParams.getIdentifierKey());
		}
		if (searchParams.getMethod() != null) {
			query.setParameter("methodKey", searchParams.getMethod().getKey());
		}
		return query.getResultList();
	}

	Collection<AdvanceOrder> loadPendingAdvanceOrders(Collection<IdentifierKeyValue> identifiers) {
		if (identifiers == null || identifiers.isEmpty()) return new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		sb.append("select advanceOrder from advanceOrder advanceOrder")
		  .append(" where advanceOrder.status=:status")
		  .append("   and ((1 = 2)");
		
		List<IdentifierKeyValue> identifierList = identifiers.stream()
				.collect(Collectors.toList());
		for (int i = 0; i < identifierList.size(); i++) {
			IdentifierKeyValue keyValue = identifierList.get(i);
			if (keyValue.getKey() != null && StringHelper.containsNonWhitespace(keyValue.getValue())) {
				sb.append(" or (advanceOrder.identifierKey=:").append("key" + i);
				sb.append("       and advanceOrder.identifierValue=:").append("value" + i);
				sb.append("    )");
			}	
		}
		sb.append(")");
		
		TypedQuery<AdvanceOrder> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AdvanceOrder.class)
				.setParameter("status", Status.PENDING);
		
		for (int i = 0; i < identifierList.size(); i++) {
			IdentifierKeyValue keyValue = identifierList.get(i);
			if (keyValue.getKey() != null && StringHelper.containsNonWhitespace(keyValue.getValue())) {
				query.setParameter("key" + i, keyValue.getKey());
				query.setParameter("value" + i, keyValue.getValue());
			}
		}
		
		return query.getResultList();
	}

	public void deleteAdvanceOrder(AdvanceOrder advanceOrder) {
		dbInstance.getCurrentEntityManager()
		.createNamedQuery("deleteByKey")
		.setParameter("key", advanceOrder.getKey())
		.executeUpdate();
	}

	void deleteAdvanceOrders(Identity identity) {
		dbInstance.getCurrentEntityManager()
				.createNamedQuery("deleteByIdentity")
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}

	public AdvanceOrder save(AdvanceOrder advanceOrder) {
		if(advanceOrder.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(advanceOrder);
		} else {
			advanceOrder.setLastModified(new Date());
			advanceOrder = dbInstance.getCurrentEntityManager().merge(advanceOrder);
		}
		return advanceOrder;
	}

	AdvanceOrder accomplishAndSave(AdvanceOrder advanceOrder, boolean multiOrder) {
		if (advanceOrder == null) return advanceOrder;

		if (!multiOrder) {
			advanceOrder.setStatus(Status.DONE);
		}
		advanceOrder = save(advanceOrder);

		return advanceOrder;
	}

	public void resetStatusPending() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update advanceOrder advanceOrder");
		sb.append("   set advanceOrder.status = :status");
		sb.append("     , advanceOrder.lastModified = :lastModified");
		sb.and().append(" advanceOrder.status not ").in(Status.PENDING, Status.CANCELED);
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("status", Status.PENDING)
				.setParameter("lastModified", new Date())
				.executeUpdate();
	}

	
	static final class IdentifierKeyValue {
		
		private final IdentifierKey key;
		private final String value;
		
		public IdentifierKeyValue(IdentifierKey key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		public IdentifierKey getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IdentifierKeyValue other = (IdentifierKeyValue) obj;
			if (key != other.key)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		
	}

}
