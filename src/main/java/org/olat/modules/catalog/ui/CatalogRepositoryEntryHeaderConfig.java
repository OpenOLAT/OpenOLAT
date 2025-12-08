/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.catalog.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;

/**
 * 
 * Initial date: Dec 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogRepositoryEntryHeaderConfig extends BasicDetailsHeaderConfig {

	private static final ParticipantsAvailabilityNum PARTICIPANTS_AVAILABILITY_NUM
			= new ParticipantsAvailabilityNum(ParticipantsAvailability.manyLeft, Long.MAX_VALUE);
	
	private final RepositoryEntry repositoryEntry;
	private final RepositoryEntrySecurity reSecurity;
	
	public CatalogRepositoryEntryHeaderConfig(RepositoryEntry repositoryEntry, Identity identity, Roles roles) {
		super(identity);
		this.repositoryEntry = repositoryEntry;
		participantsAvailability = PARTICIPANTS_AVAILABILITY_NUM;
		
		reSecurity = CoreSpringFactory.getImpl(RepositoryManager.class).isAllowed(identity, roles, repositoryEntry);
		
		initOpenBookOffers();
		initAdminAccess();
		initLeave();
	}

	private void initOpenBookOffers() {
		if (reSecurity.isParticipant()) {
			openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.publishedAndClosed());
		}
		if (openAvailable && openEnabled) {
			return;
		}
		
		if (reSecurity.isOwner() || reSecurity.isCoach()) {
			boolean openAvailableBefore = openAvailable;
			boolean openEnabledBefore = openEnabled;
			
			if (reSecurity.isCoach()) {
				openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.coachPublishedToClosed());
			}
			if (reSecurity.isOwner()) {
				openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.preparationToClosed());
			}
			
			ownerCoachMessage = openAvailableBefore != openAvailable || openEnabledBefore != openEnabled;
		}
		
		if (openAvailable) {
			return;
		}
		
		AccessResult acResult = CoreSpringFactory.getImpl(ACService.class).isAccessible(repositoryEntry, identity, Boolean.FALSE, false, null, false);
		if (acResult.isAccessible()) {
			openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.publishedAndClosed());
			if (openAvailable && openEnabled) {
				return;
			}
		}
		
		availableMethods = acResult.getAvailableMethods();
	}
	
	private void initAdminAccess() {
		if (openAvailable && openEnabled) {
			return;
		}
		
		if (reSecurity.canLaunch()) {
			if (reSecurity.isAdministrator() || reSecurity.isLearnResourceManager() || reSecurity.isPrincipal() || reSecurity.isCurriculumManager()) {
				administrativOpenAvailable = true;
			}
		}
	}
	
	private void initLeave() {
		if (reSecurity.isParticipant() && CoreSpringFactory.getImpl(RepositoryService.class).isParticipantAllowedToLeave(repositoryEntry)) {
			leaveAvailable = true;
		}
	}
	
	private void openWithStatusCheck(RepositoryEntry repositoryEntry, RepositoryEntryStatusEnum[] statusArray) {
		if (RepositoryEntryStatusEnum.isInArray(repositoryEntry.getEntryStatus(), statusArray)) {
			openEnabled();
			notPublishedYetMessage = false;
		} else {
			openDisabledNotPublishedYet();
		}
	}

}
