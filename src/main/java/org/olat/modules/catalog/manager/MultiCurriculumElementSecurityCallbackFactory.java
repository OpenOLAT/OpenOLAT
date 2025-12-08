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
package org.olat.modules.catalog.manager;

import java.util.List;
import java.util.function.Supplier;

import org.olat.modules.catalog.CatalogEntrySecurityCallback;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: Oct 22, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MultiCurriculumElementSecurityCallbackFactory extends CurriculumElementCallbackFactory {

	public MultiCurriculumElementSecurityCallbackFactory(CurriculumElementStatus curriculumElementStatus, boolean isMember,
			boolean isParticipant, Supplier<Boolean> isReservation, List<OLATResourceAccess> resourceAccesses,
			ParticipantsAvailability participantsAvailability) {
		super(curriculumElementStatus, isMember, isParticipant, isReservation, resourceAccesses, participantsAvailability);
	}
	
	@Override
	protected CatalogEntrySecurityCallback createMemberCallback() {
		if (isAccessibleStatus()) {
			return CatalogEntrySecurityCallback.ACCESSIBLE_CALLBACK;
		}
		return CatalogEntrySecurityCallback.NOT_YET_PUPLISHED;
	}
	
	protected final boolean isAccessibleStatus() {
		return CurriculumElementStatus.isInArray(curriculumElementStatus, CurriculumElementStatus.visibleUser());
	}
}
