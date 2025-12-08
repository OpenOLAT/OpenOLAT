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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;

/**
 * 
 * Initial date: Dec 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BasicDetailsHeaderConfig implements DetailsHeaderConfig {

	protected final Identity identity;
	protected ParticipantsAvailabilityNum participantsAvailability;
	protected boolean openAvailable = false;
	protected boolean openEnabled = false;
	protected boolean bookAvailable = false;
	protected boolean bookEnabled = false;
	protected List<OfferAccess> availableMethods;
	protected boolean notPublishedYetMessage = false;
	protected boolean noContentYetMessage = false;
	protected boolean confirmationPendingMessage = false;
	protected boolean availabilityMessage = false;
	protected boolean ownerCoachMessage = false;
	protected boolean administrativOpenAvailable = false;
	protected boolean leaveAvailable = false;
	protected boolean leaveWithCancellationFee = false;

	public BasicDetailsHeaderConfig(Identity identity) {
		this.identity = identity;
	}

	protected void openEnabled() {
		openAvailable = true;
		openEnabled = true;
	}

	protected void openDisabledNotPublishedYet() {
		openAvailable = true;
		openEnabled = false;
		notPublishedYetMessage = true;
	}

	protected void openDisabledNoContentYet() {
		openAvailable = true;
		openEnabled = false;
		noContentYetMessage = true;
	}

	protected void openDisabledConfirmationPending() {
		openAvailable = true;
		openEnabled = false;
		confirmationPendingMessage = true;
	}

	protected void bookDisabledAvailability() {
		bookAvailable = true;
		bookEnabled = false;
		availabilityMessage = true;
	}

	@Override
	public Identity getBookedIdentity() {
		return identity;
	}

	@Override
	public boolean isOpenAvailable() {
		return openAvailable;
	}

	@Override
	public boolean isOpenEnabled() {
		return openEnabled;
	}

	@Override
	public boolean isBookAvailable() {
		return bookAvailable;
	}

	@Override
	public boolean isBookEnabled() {
		return bookEnabled;
	}

	@Override
	public boolean isOffersPreview() {
		return false;
	}

	@Override
	public boolean isOffersWebPublish() {
		return false;
	}

	@Override
	public boolean isOffersAvailable() {
		return availableMethods != null && !availableMethods.isEmpty();
	}

	@Override
	public List<OfferAccess> getAvailableMethods() {
		return availableMethods;
	}

	@Override
	public boolean isLeaveAvailable() {
		return leaveAvailable;
	}

	@Override
	public boolean isLeaveWithCancellationFee() {
		return leaveWithCancellationFee;
	}

	@Override
	public ParticipantsAvailabilityNum getParticipantsAvailabilityNum() {
		return participantsAvailability;
	}

	@Override
	public boolean isNotPublishedYetMessage() {
		return notPublishedYetMessage;
	}

	@Override
	public boolean isNoContentYetMessage() {
		return noContentYetMessage;
	}

	@Override
	public boolean isConfirmationPendingMessage() {
		return confirmationPendingMessage;
	}

	@Override
	public boolean isAvailabilityMessage() {
		return availabilityMessage;
	}

	@Override
	public boolean isOwnerCoachMessage() {
		return ownerCoachMessage;
	}

	@Override
	public boolean isAdministrativOpenAvailable() {
		return administrativOpenAvailable;
	}

	@Override
	public boolean isAdministrativOpenEnabled() {
		return administrativOpenAvailable;
	}

}