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
package org.olat.repository.ui;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.RepositoryEntryInfosController;
import org.olat.resource.accesscontrol.Offer;

/**
 * 
 * Initial date: 26 Aug 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessDeniedFactory {
	
	public static Controller createRepositoryEntryDoesNotExist(UserRequest ureq, WindowControl wControl) {
		return new AccessDeniedController(ureq, wControl, "repositoryentry.not.existing", null, null);
	}

	public static Controller createRepositoryEntryDeleted(UserRequest ureq, WindowControl wControl) {
		return new AccessDeniedController(ureq, wControl, "repositoryentry.deleted", null, null);
	}

	public static Controller createRepositoryStatusClosed(UserRequest ureq, WindowControl wControl) {
		return new AccessDeniedController(ureq, wControl, "access.denied.closed", "access.denied.closed.hint", null);
	}
	
	public static AccessDeniedMessage createRepositoryEntryStatusNotPublishedMessage() {
		return new AccessDeniedMessage("access.denied.preparation", "access.denied.preparation.hint", null);
	}
	
	public static Controller createRepositoryEntryStatusNotPublished(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		// The controller (header) checks the conditions again.
		return new RepositoryEntryInfosController(ureq, wControl, entry, true);
	}

	public static Controller createBookingPending(UserRequest ureq, WindowControl wControl) {
		return new AccessDeniedController(ureq, wControl, "access.denied.pending", "access.denied.pending.hint", null);
	}

	public static Controller createNotMember(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		RepositoryModule repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		AccessDeniedController accessDeniedCtrl;

		if (repositoryModule.isRequestMembershipEnabled()) {
			accessDeniedCtrl = new AccessDeniedController(ureq, wControl, "access.denied.not.member", "access.denied.not.member.hint", null);
			accessDeniedCtrl.enableMembershipRequest(entry);
		} else {
			accessDeniedCtrl = new AccessDeniedController(ureq, wControl, "access.denied.not.member", "access.denied.not.member.hint.disabled", null);
		}

		return accessDeniedCtrl;
	}

	public static Controller createNoGuestAccess(UserRequest ureq, WindowControl wControl) {
		AccessDeniedController accessDeniedCtrl = new AccessDeniedController(ureq, wControl, "access.denied.no.guest.access", "access.denied.no.guest.access.hint", null);
		accessDeniedCtrl.enableGoToLogin();
		return accessDeniedCtrl;
	}

	public static Controller createOfferNotNow(UserRequest ureq, WindowControl wControl, List<Offer> offers) {
		Date now = new Date();
		Optional<Offer> afterNow = offers.stream()
				.filter(o -> o.getValidFrom() != null)
				.sorted((o1, o2) -> o1.getValidFrom().compareTo(o2.getValidFrom()))
				.filter(o -> o.getValidFrom().after(now))
				.findFirst();
		if (afterNow.isPresent()) {
			Offer offer = afterNow.get();
			String validFrom = Formatter.getInstance(ureq.getLocale()).formatDate(offer.getValidFrom());
			return new AccessDeniedController(ureq, wControl, "access.denied.not.now", "access.denied.not.now.future", new String[] {validFrom});
		}
		
		Optional<Offer> beforeNow = offers.stream()
				.filter(o -> o.getValidFrom() != null)
				.sorted((o1, o2) -> o2.getValidTo().compareTo(o1.getValidTo()))
				.filter(o -> o.getValidTo().before(now))
				.findFirst();
		if (beforeNow.isPresent()) {
			Offer offer = beforeNow.get();
			String validTo = Formatter.getInstance(ureq.getLocale()).formatDate(offer.getValidFrom());
			return new AccessDeniedController(ureq, wControl, "access.denied.not.now", "access.denied.not.now.past", new String[] {validTo});
		}
		
		return createNoAccess(ureq, wControl);
	}
	
	public static boolean isNotInAuthorOrganisation(RepositoryEntry entry, Roles roles) {
		if (roles.isAuthor() && (entry.getCanCopy() || entry.getCanDownload() || entry.getCanReference())) {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			Set<Long> reOrganisationKeys = repositoryService.getOrganisations(entry).stream()
					.map(Organisation::getKey)
					.collect(Collectors.toSet());
			Set<Long> authorOrganisationKeys = roles.getOrganisationsWithRole(OrganisationRoles.author).stream()
					.map(OrganisationRef::getKey)
					.collect(Collectors.toSet());
			return !reOrganisationKeys.stream().anyMatch(org -> authorOrganisationKeys.contains(org));
		}
		return false;
	}

	public static Controller createNotInAuthorOrganisation(UserRequest ureq, WindowControl wControl, Identity identity) {
		Roles roles = CoreSpringFactory.getImpl(BaseSecurity.class).getRoles(identity, false);
		String organisations = CoreSpringFactory.getImpl(OrganisationService.class)
				.getOrganisation(roles.getOrganisationsWithRole(OrganisationRoles.author))
				.stream()
				.map(Organisation::getDisplayName)
				.sorted()
				.collect(Collectors.joining(", "));
		return new AccessDeniedController(ureq, wControl, "access.denied.not.author.organisation", "access.denied.not.author.organisation.hint", new String[] {organisations});
	}

	public static Controller createNoAccess(UserRequest ureq, WindowControl wControl) {
		return new AccessDeniedController(ureq, wControl, "launch.noaccess", null, null);
	}
	
	public record AccessDeniedMessage(String messageI18nKey, String hintI18nKey, String[] hintArgs) {}

}
