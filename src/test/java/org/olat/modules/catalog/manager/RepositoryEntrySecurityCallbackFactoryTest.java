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

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.modules.catalog.CatalogEntrySecurityCallback;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: Dec 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntrySecurityCallbackFactoryTest {

	@Test
	public void shouldCreateCallback_participant_access() {
		CatalogEntrySecurityCallback secCallback = secCallback(RepositoryEntryStatusEnum.published, true, true, false, false, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
	}
	
	@Test
	public void shouldCreateCallback_participant_noaccess_status() {
		CatalogEntrySecurityCallback secCallback = secCallback(RepositoryEntryStatusEnum.preparation, true, true, false, false, oneAccess());
		assertCallback(secCallback, true, false, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.review, true, true, false, false, oneAccess());
		assertCallback(secCallback, true, false, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.coachpublished, true, true, false, false, oneAccess());
		assertCallback(secCallback, true, false, false, false, false);
	}
	
	@Test
	public void shouldCreateCallback_guest_access() {
		CatalogEntrySecurityCallback secCallback = secCallback(RepositoryEntryStatusEnum.published, false, false, true, false, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.preparation, false, false, true, false, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.review, false, false, true, false, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.coachpublished, false, false, true, false, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
	}
	
	@Test
	public void shouldCreateCallback_open_access() {
		CatalogEntrySecurityCallback secCallback = secCallback(RepositoryEntryStatusEnum.published, false, false, false, true, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.preparation, false, false, false, true, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.review, false, false, false, true, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.coachpublished, false, false, false, true, oneAccess());
		assertCallback(secCallback, true, true, false, false, false);
	}
	
	@Test
	public void shouldCreateCallback_offer() {
		CatalogEntrySecurityCallback secCallback = secCallback(RepositoryEntryStatusEnum.published, false, false, false, false, oneAccess());
		assertCallback(secCallback, false, false, true, true, false);
	}
	
	@Test
	public void shouldCreateCallback_offers() {
		CatalogEntrySecurityCallback secCallback = secCallback(RepositoryEntryStatusEnum.published, false, false, false, false, toAccess(List.of(false, false)));
		assertCallback(secCallback, false, false, true, true, false);
	}
	
	@Test
	public void shouldCreateCallback_autoBooking() {
		CatalogEntrySecurityCallback secCallback = secCallback(RepositoryEntryStatusEnum.published, false, false, false, false, toAccess(List.of(true)));
		assertCallback(secCallback, false, false, true, true, true);
		secCallback = secCallback(RepositoryEntryStatusEnum.published, false, false, false, false, toAccess(List.of(true, true)));
		assertCallback(secCallback, false, false, true, true, false);
		secCallback = secCallback(RepositoryEntryStatusEnum.published, false, false, false, false, toAccess(List.of(false, true)));
		assertCallback(secCallback, false, false, true, true, false);
	}
	

	private CatalogEntrySecurityCallback secCallback(RepositoryEntryStatusEnum status, boolean isMember,
			boolean isParticipant, boolean openAccess, boolean guestAccess, List<OLATResourceAccess> accesses) {
		return new RepositoryEntrySecurityCallbackFactory(status, isMember, isParticipant, openAccess, guestAccess,
				accesses).getSecurityCallback();
	}
	
	private void assertCallback(CatalogEntrySecurityCallback secCallback, boolean openAvailable, boolean openEnabled,
			boolean bookAvailable, boolean bookEnabled, boolean autoBooking) {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(secCallback.isOpenAvailable()).isEqualTo(openAvailable);
		softly.assertThat(secCallback.isOpenEnabled()).isEqualTo(openEnabled);
		softly.assertThat(secCallback.isBookAvailable()).isEqualTo(bookAvailable);
		softly.assertThat(secCallback.isBookEnabled()).isEqualTo(bookEnabled);
		softly.assertThat(secCallback.isAutoBooking()).isEqualTo(autoBooking);
		softly.assertAll();
	}

	private List<OLATResourceAccess> oneAccess() {
		return toAccess(List.of(false));
	}
	
	
	private List<OLATResourceAccess> toAccess(List<Boolean> autoBookings) {
		if (autoBookings == null || autoBookings.isEmpty()) {
			return List.of();
		}
		
		return autoBookings.stream()
				.map(autoBooking -> new OLATResourceAccess(null, null, new FreeAccessMethod(), autoBooking))
				.toList();
	}

}
