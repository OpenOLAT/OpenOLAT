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
package org.olat.modules.catalog;

import java.util.List;
import java.util.function.Supplier;

import org.olat.modules.catalog.manager.CurriculumElementCallbackFactory;
import org.olat.modules.catalog.manager.MultiCurriculumElementSecurityCallbackFactory;
import org.olat.modules.catalog.manager.RepositoryEntrySecurityCallbackFactory;
import org.olat.modules.catalog.manager.SingleCurriculumElementSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: Oct 22, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogEntrySecurityCallbackFactory {
	
	public static CatalogEntrySecurityCallback createSecurityCallback(
			CatalogEntry catalogEntry,
			boolean guestAccess,
			ParticipantsAvailability participantsAvailability) {
		
		if (catalogEntry.getCurriculumElementKey() != null) {
			if (catalogEntry.isSingleCourseImplementation()) {
				return createSingleCurriculumElementSecurityCallback(
						catalogEntry.getCurriculumElementStatus(),
						catalogEntry.getSingleCourseEntryStatus(),
						catalogEntry.isMember(),
						catalogEntry.isParticipant(),
						() -> Boolean.valueOf(catalogEntry.isReservationAvailable()),
						catalogEntry.getResourceAccess(),
						participantsAvailability);
			}
			return createMultiCurriculumElementSecurityCallback(
					catalogEntry.getCurriculumElementStatus(),
					catalogEntry.isMember(),
					catalogEntry.isParticipant(),
					() -> Boolean.valueOf(catalogEntry.isReservationAvailable()),
					catalogEntry.getResourceAccess(),
					participantsAvailability);
		} else if (catalogEntry.getRepositoryEntryKey() != null) {
			return createRepositoryEntrySecurityCallback(
					catalogEntry.getRepositoryEntryStatus(),
					catalogEntry.isMember(),
					catalogEntry.isParticipant(),
					catalogEntry.isOpenAccess(),
					guestAccess,
					catalogEntry.getResourceAccess());
		}
		
		return CatalogEntrySecurityCallback.ERROR_CALLBACK;
	}
	
	public static CatalogEntrySecurityCallback createSingleCurriculumElementSecurityCallback(
			CurriculumElementStatus curriculumElementStatus, RepositoryEntryStatusEnum repositoryElementStatus,
			boolean isMember, boolean isParticipant, Supplier<Boolean> isReservation,
			List<OLATResourceAccess> resourceAccesses, ParticipantsAvailability participantsAvailability) {
		
		CurriculumElementCallbackFactory factory = new SingleCurriculumElementSecurityCallbackFactory(
				curriculumElementStatus, repositoryElementStatus, isMember, isParticipant, isReservation,
				resourceAccesses, participantsAvailability);
		return factory.getSecurityCallback();
	}

	public static CatalogEntrySecurityCallback createMultiCurriculumElementSecurityCallback(
			CurriculumElementStatus curriculumElementStatus, boolean isMember, boolean isParticipant,
			Supplier<Boolean> isReservation, List<OLATResourceAccess> resourceAccesses,
			ParticipantsAvailability participantsAvailability) {
		
		MultiCurriculumElementSecurityCallbackFactory factory = new MultiCurriculumElementSecurityCallbackFactory(
				curriculumElementStatus, isMember, isParticipant, isReservation, resourceAccesses,
				participantsAvailability);
		return factory.getSecurityCallback();
	}
	
	public static CatalogEntrySecurityCallback createRepositoryEntrySecurityCallback(
			RepositoryEntryStatusEnum repositoryElementStatus, boolean isMember, boolean isParticipant,
			boolean openAccess, boolean guestAccess, List<OLATResourceAccess> resourceAccesses) {
		RepositoryEntrySecurityCallbackFactory factory = new RepositoryEntrySecurityCallbackFactory(
				repositoryElementStatus, isMember, isParticipant, openAccess, guestAccess, resourceAccesses);
		return factory.getSecurityCallback();
	}

}
