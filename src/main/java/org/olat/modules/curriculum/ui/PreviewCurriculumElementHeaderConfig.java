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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;

/**
 * 
 * Initial date: Dec 1, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PreviewCurriculumElementHeaderConfig implements DetailsHeaderConfig {
	
	private final CurriculumElement curriculumElement;
	private RepositoryEntry repositoryEntry;
	private final ParticipantsAvailabilityNum participantsAvailability;
	private final AccessResult accessResult;
	
	public PreviewCurriculumElementHeaderConfig(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
		
		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		ACService acService = CoreSpringFactory.getImpl(ACService.class);
		
		if (curriculumElement.isSingleCourseImplementation()) {
			List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
			if (entries.size() == 1) {
				repositoryEntry = entries.get(0);
			}
		}
		
		accessResult = acService.isAccessible(curriculumElement, null, Boolean.FALSE, false, null, false);
		participantsAvailability = loadParticipantsAvailabilityNum(curriculumService, acService);
	}
	
	private ParticipantsAvailabilityNum loadParticipantsAvailabilityNum(CurriculumService curriculumService, ACService acService) {
		if (accessResult.isAccessible()) {
			// Open access
			return new ParticipantsAvailabilityNum(ParticipantsAvailability.manyLeft, Integer.MAX_VALUE);
		}
		
		Long numParticipants = curriculumService.getCurriculumElementKeyToNumParticipants(List.of(curriculumElement), true).get(curriculumElement.getKey());
		return acService.getParticipantsAvailability(curriculumElement.getMaxParticipants(), numParticipants, false);
	}

	@Override
	public Identity getBookedIdentity() {
		return null;
	}

	@Override
	public boolean isOpenAvailable() {
		return accessResult.isAccessible();
	}

	@Override
	public boolean isOpenEnabled() {
		return false;
	}

	@Override
	public boolean isBookAvailable() {
		return false;
	}

	@Override
	public boolean isBookEnabled() {
		return false;
	}

	@Override
	public boolean isOffersPreview() {
		return true;
	}
	
	@Override
	public boolean isOffersWebPublish() {
		return false;
	}
	
	@Override
	public boolean isOffersAvailable() {
		if (ParticipantsAvailability.fullyBooked == participantsAvailability.availability()) {
			return false;
		}
		return !accessResult.getAvailableMethods().isEmpty();
	}

	@Override
	public List<OfferAccess> getAvailableMethods() {
		return accessResult.getAvailableMethods();
	}
	
	@Override
	public boolean isLeaveAvailable() {
		return false;
	}

	@Override
	public boolean isLeaveWithCancellationFee() {
		return false;
	}

	@Override
	public ParticipantsAvailabilityNum getParticipantsAvailabilityNum() {
		return participantsAvailability;
	}

	@Override
	public boolean isNotPublishedYetMessage() {
		if (isNoContentYetMessage()) {
			return false;
		}
		
		if (curriculumElement.isSingleCourseImplementation() && repositoryEntry != null) {
			if (!RepositoryEntryStatusEnum.isInArray(repositoryEntry.getEntryStatus(), RepositoryEntryStatusEnum.publishedAndClosed())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isNoContentYetMessage() {
		if (curriculumElement.isSingleCourseImplementation() && repositoryEntry == null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isConfirmationPendingMessage() {
		return false;
	}

	@Override
	public boolean isAvailabilityMessage() {
		if (isOffersAvailable()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isOwnerCoachMessage() {
		return false;
	}

	@Override
	public boolean isAdministrativOpenAvailable() {
		return false;
	}

	@Override
	public boolean isAdministrativOpenEnabled() {
		return false;
	}

}
