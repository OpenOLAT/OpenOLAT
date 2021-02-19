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
package org.olat.repository.wizard.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.repository.wizard.AccessAndProperties;
import org.olat.repository.wizard.InfoMetadata;
import org.olat.repository.wizard.RepositoryWizardProvider;
import org.olat.repository.wizard.RepositoryWizardService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryWizardServiceImpl implements RepositoryWizardService {
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private ACService acService;
	@Autowired
	private List<RepositoryWizardProvider> providers;
	
	@Override
	public RepositoryWizardProvider getProvider(String providerType) {
		return providers.stream()
				.filter(provider -> repositoryModule.getEnabledWizardTypes().contains(provider.getType()))
				.filter(provider -> providerType.equals(provider.getType()))
				.findFirst().get();
	}
	
	@Override
	public List<RepositoryWizardProvider> getProviders(String resourceType) {
		return providers.stream()
				.filter(provider -> repositoryModule.getEnabledWizardTypes().contains(provider.getType()))
				.filter(provider -> resourceType.equals(provider.getSupportedResourceType()))
				.collect(Collectors.toList());
	}
	
	@Override
	public RepositoryEntry updateRepositoryEntry(RepositoryEntryRef entryRef, InfoMetadata infoMetadata) {
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(entryRef.getKey());
		
		String displayName = infoMetadata.getDisplayName();
		if (StringHelper.containsNonWhitespace(displayName)) {
			entry.setDisplayname(displayName);
		}
		
		String externalRef = infoMetadata.getExternalRef();
		if (StringHelper.containsNonWhitespace(externalRef)) {
			entry.setExternalRef(externalRef);
		}
		
		String description = infoMetadata.getDescription();
		if (StringHelper.containsNonWhitespace(description)) {
			entry.setDescription(description);
		}
		
		String authors = infoMetadata.getAuthors();
		if (StringHelper.containsNonWhitespace(authors)) {
			entry.setAuthors(authors);
		}
		
		Collection<TaxonomyLevelRef> taxonomyLevelRefs = infoMetadata.getTaxonomyLevelRefs();
		Set<TaxonomyLevel> taxonomyLevels = taxonomyLevelRefs != null
				? new HashSet<>(taxonomyService.getTaxonomyLevelsByRefs(taxonomyLevelRefs))
				: null;
		
		RepositoryEntryEducationalType educationalType = entry.getEducationalType();
		if (infoMetadata.getEducationalTypeKey() != null) {
			RepositoryEntryEducationalType loadedEducationalType = repositoryManager
					.getEducationalType(infoMetadata.getEducationalTypeKey());
			if (loadedEducationalType != null) {
				educationalType = loadedEducationalType;
			}
		}
		
		return repositoryManager.setDescriptionAndName(entry, entry.getDisplayname(), entry.getExternalRef(),
				entry.getAuthors(), entry.getDescription(), entry.getObjectives(), entry.getRequirements(),
				entry.getCredits(), entry.getMainLanguage(), entry.getLocation(), entry.getExpenditureOfWork(),
				entry.getLifecycle(), null, taxonomyLevels, educationalType);
	}

	@Override
	public void addRepositoryMembers(Identity executor, Roles roles, RepositoryEntry entry,
			Collection<Identity> coaches, Collection<Identity> participants) {
		Map<Long, RepositoryEntryMembership> identityKeyToMembership = repositoryManager.getRepositoryEntryMembership(entry).stream()
				.collect(Collectors.toMap(RepositoryEntryMembership::getIdentityKey, Function.identity()));
		Map<Long ,RepositoryEntryPermissionChangeEvent> identityKeyToEvent = new HashMap<>();
		for (Identity coach : coaches) {
			RepositoryEntryMembership membership = identityKeyToMembership.get(coach.getKey());
			if (membership == null) {
				RepositoryEntryPermissionChangeEvent event = identityKeyToEvent.computeIfAbsent(coach.getKey(), key -> new RepositoryEntryPermissionChangeEvent(coach));
				event.setRepoTutor(Boolean.TRUE);
			} else if (!membership.isCoach()) {
				RepositoryEntryPermissionChangeEvent event = identityKeyToEvent.computeIfAbsent(coach.getKey(), key -> new RepositoryEntryPermissionChangeEvent(coach));
				event.setRepoTutor(Boolean.TRUE);
			}
		}
		for (Identity participant : participants) {
			RepositoryEntryMembership membership = identityKeyToMembership.get(participant.getKey());
			if (membership == null) {
				RepositoryEntryPermissionChangeEvent event = identityKeyToEvent.computeIfAbsent(participant.getKey(), key -> new RepositoryEntryPermissionChangeEvent(participant));
				event.setRepoParticipant(Boolean.TRUE);
			} else if (!membership.isCoach()) {
				RepositoryEntryPermissionChangeEvent event = identityKeyToEvent.computeIfAbsent(participant.getKey(), key -> new RepositoryEntryPermissionChangeEvent(participant));
				event.setRepoParticipant(Boolean.TRUE);
			}
		}
		
		List<RepositoryEntryPermissionChangeEvent> changeEvents = new ArrayList<>(identityKeyToEvent.values());
		repositoryManager.updateRepositoryEntryMemberships(executor, roles, entry, changeEvents, null);
	}
	
	@Override
	public void changeAccessAndProperties(Identity executor, AccessAndProperties accessAndProps, boolean fireEvents) {
		RepositoryEntry entry = accessAndProps.getRepositoryEntry();
		
		entry = repositoryManager.setAccess(entry,
				accessAndProps.isAllUsers(), accessAndProps.isGuests(), accessAndProps.isBookable(),
				accessAndProps.getSetting(), accessAndProps.getOrganisations());
		
		entry = repositoryManager.setAccessAndProperties(entry, accessAndProps.getStatus(),
				accessAndProps.isAllUsers(), accessAndProps.isGuests(),
				accessAndProps.isCanCopy(), accessAndProps.isCanReference(), accessAndProps.isCanDownload());
		
		List<OfferAccess> offerAccess = accessAndProps.getOfferAccess();
		List<Offer> deletedOffers = accessAndProps.getDeletedOffer();
		if(entry.isBookable()) {
			// 1: add new and update existing offerings
			for (OfferAccess newLink : offerAccess) {
				if(accessAndProps.getConfirmationEmail() != null) {
					Offer offer = newLink.getOffer();
					boolean confirmation = accessAndProps.getConfirmationEmail().booleanValue();
					if(offer.isConfirmationEmail() != confirmation) {
						offer.setConfirmationEmail(confirmation);
						if(offer.getKey() != null) {
							acService.save(offer);
						}
					}
				}
				acService.saveOfferAccess(newLink);
			}
		} else {
			for (OfferAccess deletedOffer : offerAccess) {
				acService.deleteOffer(deletedOffer.getOffer());
			}
		}
		// 2: remove offerings not available anymore
		for (Offer deletedOffer : deletedOffers) {
			acService.deleteOffer(deletedOffer);
		}
		
		if (fireEvents) {
			MultiUserEvent modifiedEvent = new EntryChangedEvent(entry, executor, Change.modifiedAtPublish, "publish");
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, entry);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		}
	}

}
