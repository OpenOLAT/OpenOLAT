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

import org.olat.modules.catalog.CatalogEntrySecurityCallback;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;

/**
 * 
 * Initial date: Oct 22, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntrySecurityCallbackFactory {
	
	private final RepositoryEntryStatusEnum repositoryElementStatus;
	private final boolean isMember;
	private final boolean isParticipant;
	private final boolean openAccess;
	private final boolean guestAccess;
	private final List<OLATResourceAccess> resourceAccesses;
	private final CatalogEntrySecurityCallback secCallback;
	
	public RepositoryEntrySecurityCallbackFactory(RepositoryEntryStatusEnum repositoryElementStatus, boolean isMember,
			boolean isParticipant, boolean openAccess, boolean guestAccess, List<OLATResourceAccess> resourceAccesses) {
		this.repositoryElementStatus = repositoryElementStatus;
		this.isMember = isMember;
		this.isParticipant = isParticipant;
		this.openAccess = openAccess;
		this.guestAccess = guestAccess;
		this.resourceAccesses = resourceAccesses;
		
		secCallback = createSecCallback();
	}

	public CatalogEntrySecurityCallback getSecurityCallback() {
		return secCallback;
	}
	
	private CatalogEntrySecurityCallback createSecCallback() {
		if (openAccess || guestAccess) {
			return CatalogEntrySecurityCallback.ACCESSIBLE_CALLBACK;
		} else if (isParticipant) {
			return createMemberCallback(repositoryElementStatus);
		} else if (isAutoBooking()) {
			return CatalogEntrySecurityCallback.AUTO_BOOKING_CALLBACK;
		}
		
		return CatalogEntrySecurityCallback.BOOKING_CALLBACK;
	}

	public static CatalogEntrySecurityCallback createMemberCallback(RepositoryEntryStatusEnum repositoryElementStatus) {
		if (isAccessibleStatus(repositoryElementStatus)) {
			return CatalogEntrySecurityCallback.ACCESSIBLE_CALLBACK;
		}
		return CatalogEntrySecurityCallback.NOT_YET_PUPLISHED;
	}
	
	private static boolean isAccessibleStatus(RepositoryEntryStatusEnum repositoryElementStatus) {
		return RepositoryEntryStatusEnum.isInArray(repositoryElementStatus, RepositoryEntryStatusEnum.publishedAndClosed());
	}
	
	private boolean isAutoBooking() {
		if (isMember || openAccess || guestAccess) {
			return false;
		}
		
		List<PriceMethodBundle> bundles = resourceAccesses.stream()
			.flatMap(ra -> ra.getMethods().stream())
			.toList();
		
		if (bundles.size() == 1) {
			PriceMethodBundle bundle = bundles.get(0);
			if (FreeAccessHandler.METHOD_TYPE.equals(bundle.getMethod().getType()) && bundle.isAutoBooking()) {
				return true;
			}
		}
		
		return false;
	}

}
