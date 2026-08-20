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
package org.olat.repository.ui.list;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The booking/start/leave half of what {@code AbstractDetailsHeaderController}
 * does today - display concerns (title, teaser, ...) stay in
 * {@link InfoPageHeaderController} / {@link InfoPageData}.
 *
 * Initial date: 19 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class AbstractInfoPageGetStartedController extends BasicController {

	public static final Event START_EVENT = new Event("details.start");
	public static final Event START_ADMIN_EVENT = new Event("details.start.admin");
	public static final Event LEAVE_EVENT = new Event("details.leave");
	public static final Event RESERVATION_CONFIRMATION_EVENT = new Event("reservation.confirmation");

	private final VelocityContainer mainVC;

	protected final HeaderStartController startCtrl;
	private OffersController offersCtrl;
	private AcceptPendingReservationController acceptPendingReservationCtrl;

	private final DetailsHeaderConfig config;
	private boolean hasContent;

	@Autowired
	protected ACService acService;

	protected AbstractInfoPageGetStartedController(UserRequest ureq, WindowControl wControl, DetailsHeaderConfig config) {
		super(ureq, wControl);
		this.config = config;

		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setVelocityRoot(Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class));
		mainVC = createVelocityContainer("get_started");
		putInitialPanel(mainVC);

		startCtrl = new HeaderStartController(ureq, wControl);
		listenTo(startCtrl);
		startCtrl.getInitialComponent().setVisible(false);
		mainVC.put("start", startCtrl.getInitialComponent());
	}

	protected void init(UserRequest ureq) {
		startCtrl.getStartLink().setCustomDisplayText(getStartLinkText());
		initByConfig(ureq);
	}

	protected abstract String getStartLinkText();
	protected abstract String getLeaveText(boolean withFee);
	protected abstract OLATResource getResource();

	public boolean hasContent() {
		return hasContent;
	}

	private void initByConfig(UserRequest ureq) {
		// Guest start
		if (org.olat.core.util.StringHelper.containsNonWhitespace(config.getGuestStartUrl())) {
			startCtrl.getInitialComponent().setVisible(true);
			startCtrl.getStartLink().setVisible(false);
			startCtrl.getGuestStartLink().setVisible(true);
			startCtrl.getGuestStartLink().setUrl(config.getGuestStartUrl());
			hasContent = true;
			return;
		}

		if (config.isParticipantConfirmationPending()) {
			acceptPendingReservationCtrl = new AcceptPendingReservationController(ureq, getWindowControl());
			listenTo(acceptPendingReservationCtrl);
			mainVC.put("acceptPendingReservation", acceptPendingReservationCtrl.getInitialComponent());
			hasContent = true;
		}

		// Start
		startCtrl.getStartLink().setVisible(config.isOpenAvailable());
		startCtrl.getStartLink().setEnabled(config.isOpenEnabled());

		if (config.isNoContentYetMessage() || config.isNotPublishedYetMessage()) {
			// Same message if one of this two reasons
			setWarning(translate("access.denied.not.yet.available"), translate("access.denied.not.yet.available.hint"));
		} else if (config.isAdminConfirmationPendingMessage()) {
			setWarning(translate("access.denied.reservation.confirmation.pending"), translate("access.denied.reservation.confirmation.pending.hint"));
		} else if (config.isFinishedNoAccessMessage()) {
			String typeName = translate(getResource().getResourceableTypeName());
			setWarning(translate("access.denied.finished", typeName), translate("access.denied.finished.hint"));
		}
		if (config.isOwnerCoachMessage()) {
			setWarning2(translate("access.available.roles"), translate("access.available.roles.hint"));
		}

		// Book
		if (!startCtrl.getStartLink().isVisible() && config.isBookAvailable()) {
			startCtrl.getStartLink().setVisible(config.isBookAvailable());
			startCtrl.getStartLink().setEnabled(config.isBookEnabled());
			startCtrl.getStartLink().setIconRightCSS(null);
			startCtrl.getStartLink().setCustomDisplayText(translate("book"));
		}

		if (config.isOffersAvailable()) {
			showOffers(ureq, config.getAvailableMethods(), config.isOffersWebPublish(), config.getBookedIdentity());
		}

		if (config.isAvailabilityMessage()) {
			ParticipantsAvailabilityNum availabilityNum = config.getParticipantsAvailabilityNum();
			if (availabilityNum.availability() == ParticipantsAvailability.fullyBooked) {
				startCtrl.setError(getAvailabilityText(availabilityNum));
			} else if (availabilityNum.availability() == ParticipantsAvailability.fewLeft) {
				if (offersCtrl != null) {
					offersCtrl.setWarning(getAvailabilityText(availabilityNum));
				}
			}
		}

		// Leave
		if (config.isLeaveAvailable()) {
			startCtrl.getLeaveLink().setVisible(true);
			startCtrl.getLeaveLink().setEnabled(config.isLeaveEnabled());
			startCtrl.getLeaveLink().setCustomDisplayText(getLeaveText(config.isLeaveWithCancellationFee()));
		}

		// Administrative access
		if (config.isAdministrativOpenAvailable()) {
			startCtrl.getStartAdminLink().setVisible(config.isAdministrativOpenAvailable());
			startCtrl.getStartAdminLink().setEnabled(config.isAdministrativOpenEnabled());

			if (offersCtrl != null) {
				offersCtrl.getStartAdminLink().setVisible(config.isAdministrativOpenAvailable());
				offersCtrl.getStartAdminLink().setEnabled(config.isAdministrativOpenEnabled());
				offersCtrl.getStartAdminLink().setCustomDisplayText(translate("start.admin"));
			}
		}

		boolean startCtrlVisible = startCtrl.getStartLink().isVisible()
				|| startCtrl.getLeaveLink().isVisible();
		startCtrl.getInitialComponent().setVisible(startCtrlVisible);
		hasContent = hasContent || startCtrlVisible;
	}

	protected String getAvailabilityText(ParticipantsAvailabilityNum participantsAvailabilityNum) {
		return "<i class=\"o_icon " + ParticipantsAvailability.getIconCss(participantsAvailabilityNum) + "\"> </i> "
				+ ParticipantsAvailability.getText(getTranslator(), participantsAvailabilityNum);
	}

	private void showOffers(UserRequest ureq, List<OfferAccess> offers, Boolean webPublish, Identity bookedIdentity) {
		offersCtrl = new OffersController(ureq, getWindowControl(), bookedIdentity, offers, webPublish, false, config.isOffersPreview());
		listenTo(offersCtrl);
		mainVC.put("offers", offersCtrl.getInitialComponent());
		hasContent = true;
	}

	protected void setWarning(String warning, String warningHint) {
		mainVC.contextPut("warning", warning);
		mainVC.contextPut("warningHint", warningHint);
		hasContent = true;
	}

	protected void setWarning2(String warning, String warningHint) {
		mainVC.contextPut("warning2", warning);
		mainVC.contextPut("warning2Hint", warningHint);
		hasContent = true;
	}

	protected void showInfoMessage(String info) {
		mainVC.contextPut("info", info);
		hasContent = true;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == acceptPendingReservationCtrl) {
			if (event == AcceptPendingReservationController.ACCEPT_EVENT) {
				doAcceptReservation();
			} else if (event == AcceptPendingReservationController.DECLINE_EVENT) {
				doDeclineReservation();
			}
			fireEvent(ureq, RESERVATION_CONFIRMATION_EVENT);
		} else if (source == startCtrl) {
			if (event == START_EVENT) {
				fireEvent(ureq, START_EVENT);
			} else if (event == START_ADMIN_EVENT) {
				fireEvent(ureq, START_EVENT);
			}
		} else if (source == offersCtrl) {
			if (event == OffersController.START_ADMIN_EVENT) {
				fireEvent(ureq, START_EVENT);
			} else {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void doAcceptReservation() {
		ResourceReservation reservation = acService.getReservation(getIdentity(), getResource());
		if (reservation != null) {
			acService.acceptReservationToResource(getIdentity(), reservation);
		}
	}

	private void doDeclineReservation() {
		ResourceReservation reservation = acService.getReservation(getIdentity(), getResource());
		if (reservation != null) {
			acService.removeReservation(getIdentity(), getIdentity(), reservation, null);
		}
	}

}
