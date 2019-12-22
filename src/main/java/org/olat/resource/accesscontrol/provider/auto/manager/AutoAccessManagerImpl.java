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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder.Status;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrderInput;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;
import org.olat.resource.accesscontrol.provider.auto.manager.AdvanceOrderDAO.IdentifierKeyValue;
import org.olat.resource.accesscontrol.provider.auto.model.AutoAccessMethod;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 *
 * Initial date: 14.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AutoAccessManagerImpl implements AutoAccessManager, UserDataDeletable {

	private static final Logger log = Tracing.createLoggerFor(AutoAccessManagerImpl.class);

	@Autowired
	private IdentifierHandler identifierHandler;
	@Autowired
	private SplitterFactory splitterFactory;
	@Autowired
	private InputValidator inputValidator;
	@Autowired
	private AdvanceOrderDAO advanceOrderDAO;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private ACService acService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public void createAdvanceOrders(AdvanceOrderInput input) {
		if (!inputValidator.isValid(input)) return;

		IdentifierValueSplitter splitter = splitterFactory.getSplitter(input.getSplitterType());
		Collection<String> values = splitter.split(input.getRawValues());
		for (IdentifierKey key : input.getKeys()) {
			for (String value : values) {
				createAndPersistAdvanceOrderIfNotExist(input.getIdentity(), key, value, input.getMethodClass());
			}
		}
	}

	private void createAndPersistAdvanceOrderIfNotExist(Identity identity, IdentifierKey key, String value, Class<? extends AutoAccessMethod> type) {
		List<AccessMethod> methods = acService.getAvailableMethodsByType(type);
		AccessMethod method = methods.get(0);
		if (doesNotExist(identity, key, value, method)) {
			AdvanceOrder advanceOrder = advanceOrderDAO.create(identity, key, value, method);
			advanceOrderDAO.save(advanceOrder);
		}
	}

	private boolean doesNotExist(Identity identity, IdentifierKey key, String value, AccessMethod method) {
		return !advanceOrderDAO.exists(identity, key, value, method);
	}

	@Override
	public Collection<AdvanceOrder> loadPendingAdvanceOrders(Identity identity) {
		return advanceOrderDAO.loadPendingAdvanceOrders(identity);
	}

	@Override
	public Collection<AdvanceOrder> loadPendingAdvanceOrders(RepositoryEntry entry) {
		if (entry == null) return new ArrayList<>();
		
		Set<IdentifierKeyValue> searchValues = new HashSet<>();
		for (IdentifierKey key: IdentifierKey.values()) {
			Set<String> values = identifierHandler.getRepositoryEntryValue(key, entry);
			for (String value : values) {
				IdentifierKeyValue identifierKeyValue = new IdentifierKeyValue(key, value);
				searchValues.add(identifierKeyValue);
			}
		}
		
		return advanceOrderDAO.loadPendingAdvanceOrders(searchValues);
	}

	@Override
	public void deleteAdvanceOrder(AdvanceOrder advanceOrder) {
		advanceOrderDAO.deleteAdvanceOrder(advanceOrder);
	}

	@Override
	public void deleteAdvanceOrders(Identity identity) {
		advanceOrderDAO.deleteAdvanceOrders(identity);
	}

	@Override
	public void grantAccessToCourse(Identity identity) {
		Collection<AdvanceOrder> pendingAdvanceOrders = loadPendingAdvanceOrders(identity);
		grantAccess(pendingAdvanceOrders);
	}

	@Override
	public void grantAccess(RepositoryEntry entry) {
		Collection<AdvanceOrder> pendingAdvanceOrders = loadPendingAdvanceOrders(entry);
		grantAccess(pendingAdvanceOrders);
	}

	@Override
	public void grantAccess(Collection<AdvanceOrder> advanceOrders) {
		if (!acModule.isAutoEnabled()) return;

		for (AdvanceOrder advanceOrder : advanceOrders) {
			try {
				tryToGrantAccess(advanceOrder);
			} catch (Exception e) {
				log.error("Advance order can not be booked.", e);
			}
		}
	}

	private void tryToGrantAccess(AdvanceOrder advanceOrder) {
		if (isAdvanceOrderAccomplished(advanceOrder))
			return;

		List<RepositoryEntry> entries = findRepositoryEntries(advanceOrder);
		if (!entries.isEmpty()) {
			for (RepositoryEntry entry: entries) {
				grantAccessIfHasNoAccess(advanceOrder, entry);
			}
			advanceOrderDAO.accomplishAndSave(advanceOrder);
		}
	}

	private void grantAccessIfHasNoAccess(AdvanceOrder advanceOrder, RepositoryEntry entry) {
		if (hasNoAccess(advanceOrder, entry)) {
			OLATResource resource = entry.getOlatResource();
			OfferAccess offerAccess = getOrCreateOfferAccess(resource, entry, advanceOrder.getMethod());
			makeOrder(offerAccess, advanceOrder);
		}
	}

	private boolean isAdvanceOrderAccomplished(AdvanceOrder advanceOrder) {
		return !Status.PENDING.equals(advanceOrder.getStatus());
	}

	private List<RepositoryEntry> findRepositoryEntries(AdvanceOrder advanceOrder) {
		IdentifierKey identifierKey = advanceOrder.getIdentifierKey();
		String identifierValue = advanceOrder.getIdentifierValue();
		return identifierHandler.findRepositoryEntries(identifierKey, identifierValue);
	}

	private boolean hasNoAccess(AdvanceOrder advanceOrder, RepositoryEntry entry) {
		Identity identity = advanceOrder.getIdentity();
		boolean hasNoAccess = true;
		if (repositoryEntryRelationDao.hasRole(identity, entry, GroupRoles.participant.name())) {
			hasNoAccess = false;
		}
		return hasNoAccess;
	}

	private OfferAccess getOrCreateOfferAccess(OLATResource resource, RepositoryEntry entry, AccessMethod method) {
		OfferAccess offerAccess;
		List<OfferAccess> offerAccesses = acService.getValidOfferAccess(resource, method);
		if (offerAccesses.isEmpty()) {
			offerAccess = createOfferAccess(resource, entry, method);
		} else {
			offerAccess = offerAccesses.get(0);
		}
		return offerAccess;
	}

	private OfferAccess createOfferAccess(OLATResource resource, RepositoryEntry entry, AccessMethod method) {
		OfferAccess offerAccess;
		String displayName = entry.getDisplayname();
		Offer offer = acService.createOffer(resource, displayName);
		offer.setAutoBooking(true);
		offerAccess = acService.createOfferAccess(offer, method);
		acService.save(offer);
		acService.saveOfferAccess(offerAccess);
		return offerAccess;
	}

	private void makeOrder(OfferAccess offerAccess, AdvanceOrder advanceOrder) {
		Identity identity = advanceOrder.getIdentity();
		acService.accessResource(identity, offerAccess, null);
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		deleteAdvanceOrders(identity);
	}

}
