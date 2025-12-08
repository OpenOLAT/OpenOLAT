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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.list.AbstractDetailsHeaderController;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosHeaderController extends AbstractDetailsHeaderController {
	
	private CloseableModalController cmc;
	private ConfirmationController leaveConfirmationCtrl;
	
	private final CurriculumElement element;
	private final Identity bookedIdentity;
	private final RepositoryEntry entry;
	private final boolean isMember;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private ACReservationDAO reservationDao;
	
	
	public CurriculumElementInfosHeaderController(UserRequest ureq, WindowControl wControl,
			DetailsHeaderConfig config, CurriculumElement element) {
		super(ureq, wControl, config);
		this.element = element;
		this.bookedIdentity = config.getBookedIdentity();
		this.entry = null;
		this.isMember = false;
		
		init(ureq);
	}
	
	public CurriculumElementInfosHeaderController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			RepositoryEntry entry, Identity bookedIdentity, boolean isMember) {
		super(ureq, wControl);
		this.entry = entry;
		this.element = element;
		this.bookedIdentity = bookedIdentity;
		this.isMember = isMember;
		
		// Remove isPreview() if this constructor is deleted.
		// Get it from the secCallback.
		
		init(ureq);
	}

	@Override
	protected String getIconCssClass() {
		return "o_icon_curriculum_element";
	}
	
	@Override
	protected String getExternalRef() {
		return element.getIdentifier();
	}

	@Override
	protected String getTranslatedTechnicalType() {
		return element.getType() != null? element.getType().getDisplayName(): null;
	}

	@Override
	protected String getTitle() {
		return element.getDisplayName();
	}

	@Override
	protected String getAuthors() {
		return element.getAuthors();
	}

	@Override
	protected String getTeaser() {
		return element.getTeaser();
	}

	@Override
	protected VFSLeaf getTeaserImage() {
		return curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserImage);
	}

	@Override
	protected VFSLeaf getTeaserMovie() {
		return curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserVideo);
	}

	@Override
	protected RepositoryEntryEducationalType getEducationalType() {
		return element.getEducationalType();
	}

	@Override
	protected String getPendingMessageElementName() {
		return element.getType().getDisplayName();
	}
	
	@Override
	protected String getLeaveText(boolean withFee) {
		return withFee? translate("leave.cancel.fee"): translate("leave.cancel");
	}

	@Override
	protected boolean isPreview() {
		return config != null && config.isOffersPreview();
	}

	@Override
	protected void initAccess(UserRequest ureq) {
		if (ureq.getUserSession().getRoles() == null) {
			initOffers(ureq, Boolean.TRUE, Boolean.FALSE);
			return;
		}
		
		if (isMember) {
			startCtrl.getInitialComponent().setVisible(true);
			if (element.isSingleCourseImplementation()) {
				if (entry == null) {
					setWarning(translate("access.denied.not.instance.course"), translate("access.denied.not.instance.course.hint"));
					startCtrl.getStartLink().setEnabled(false);
				} else if (RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.publishedAndClosed())) {
					//TODO OO-8519 temporary revert
					startCtrl.getStartLink().setEnabled(true);
				} else {
					setWarning(translate("access.denied.preparation"), translate("access.denied.preparation.hint"));
					startCtrl.getStartLink().setEnabled(false);
				}
			} else if (element != null && !CurriculumElementStatus.isInArray(element.getElementStatus(), CurriculumElementStatus.visibleUser())) {
				setWarning(translate("access.denied.preparation.element", StringHelper.escapeHtml(element.getType().getDisplayName())),
						translate("access.denied.preparation.hint"));
				startCtrl.getStartLink().setEnabled(false);
			}
			initLeaveButton();
		} else {
			if (!isPreview() && (acService.isAccessToResourcePending(element.getResource(), bookedIdentity) 
					|| acService.getReservation(bookedIdentity, element.getResource()) != null)) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.getStartLink().setEnabled(false);
				showInfoMessage(translate("access.denied.not.accepted.yet"));
				initLeaveButton();
			} else {
				initOffers(ureq, null, isMember);
			}
		}
	}

	private void initOffers(UserRequest ureq, Boolean webPublish, Boolean knownMember) {
		AccessResult acResult = isPreview()
				? acService.isAccessible(element, null, Boolean.FALSE, false, null, false)
				: acService.isAccessible(element, bookedIdentity, knownMember, false, webPublish, false);
		if (acResult.isAccessible()) {
			startCtrl.getInitialComponent().setVisible(true);
		} else if (!acResult.getAvailableMethods().isEmpty()) {
			ParticipantsAvailabilityNum availabilityNum = getParticipantsAvailabilityNum();
			if (availabilityNum.availability() == ParticipantsAvailability.fullyBooked) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.getStartLink().setEnabled(false);
				startCtrl.getStartLink().setIconRightCSS(null);
				startCtrl.getStartLink().setCustomDisplayText(translate("book"));
				startCtrl.setError(getAvailabilityText(availabilityNum));
				return;
			}
			
			if (acResult.getAvailableMethods().size() == 1 && acResult.getAvailableMethods().get(0).getOffer().isAutoBooking()) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.setAutoBooking(true);
				if (element.isSingleCourseImplementation()) {
					if (entry == null || !RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.publishedAndClosed())) {
						startCtrl.getStartLink().setEnabled(false);
					}
				}
				if (availabilityNum.availability() == ParticipantsAvailability.fewLeft) {
					startCtrl.setWarning(getAvailabilityText(availabilityNum));
				}
			} else {
				showOffers(ureq, acResult.getAvailableMethods(), false, webPublish != null && webPublish, bookedIdentity);
				if (availabilityNum.availability() == ParticipantsAvailability.fewLeft) {
					if (offersCtrl != null) {
						offersCtrl.setWarning(getAvailabilityText(availabilityNum));
					}
				}
			}
		}
	}
	
	private void initLeaveButton() {
		if (!canLeave()) {
			return;
		}
		
		List<Order> orders = acService.findOrders(bookedIdentity, element.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		Price cancellationFee = acService.getCancellationFee(element.getResource(), element.getBeginDate(), orders);
		if (cancellationFee == null) {
			startCtrl.getLeaveLink().setCustomDisplayText(translate("leave.cancel"));
			startCtrl.getLeaveLink().setVisible(true);
		} else {
			startCtrl.getLeaveLink().setCustomDisplayText(translate("leave.cancel.fee"));
			startCtrl.getLeaveLink().setVisible(true);
		}
	}

	private boolean canLeave() {
		if (isPreview()) {
			return false;
		}
		
		// Leave only allowed before start
		if (element.getBeginDate() == null || DateUtils.getStartOfDay(element.getBeginDate()).before(new Date())) {
			return false;
		}
		
		// Leave only allowed if order available
		if (!isOrderAvailable()) {
			return false;
		}
		
		// Leave not allowed if not a participant
		if (!isParticipant()) {
			return false;
		}
		
		return true;
	}
	
	private boolean isOrderAvailable() {
		return !acService.findOrders(bookedIdentity, element.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED).isEmpty();
	}

	private boolean isParticipant() {
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(List.of(element), List.of(bookedIdentity));
		if (memberships != null && !memberships.isEmpty() && memberships.get(0).isParticipant()) {
			return true;
		}
		
		SearchReservationParameters searchParams = new SearchReservationParameters(List.of(element.getResource()));
		searchParams.setIdentities(List.of(bookedIdentity));
		List<ResourceReservation> reservations = reservationDao.loadReservations(searchParams);
		if (!reservations.isEmpty()) {
			return true;
		}
		
		return false;
	}
	
	private ParticipantsAvailabilityNum getParticipantsAvailabilityNum() {
		if (isMember || element.getMaxParticipants() == null) {
			return new ParticipantsAvailabilityNum(ParticipantsAvailability.manyLeft, Integer.MAX_VALUE);
		}
		
		Long numParticipants = curriculumService.getCurriculumElementKeyToNumParticipants(List.of(element), true).get(element.getKey());
		return acService.getParticipantsAvailability(element.getMaxParticipants(), numParticipants, false);
	}
	
	@Override
	protected String getStartLinkText() {
		return translate("open.with.type", element.getType().getDisplayName());
	}

	@Override
	protected boolean tryAutoBooking(UserRequest ureq) {
		if (isPreview()) {
			return false; // Paranoia. Button should not be displayed anyway.
		}
		
		AccessResult acResult = acService.isAccessible(element, bookedIdentity, null, false, null, false);
		return acService.tryAutoBooking(bookedIdentity, element, acResult);
	}

	@Override
	protected Long getResourceKey() {
		return element.getResource().getKey();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == startCtrl) {
			if (event == LEAVE_EVENT) {
				doConfirmLeave(ureq);
			}
		} else if (leaveConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doLeave(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(leaveConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		leaveConfirmationCtrl = null;
		cmc = null;
	}
	
	
	private void doConfirmLeave(UserRequest ureq) {
		if (!canLeave()) {
			return;
		}
		
		List<Order> orders = acService.findOrders(bookedIdentity, element.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		Price cancellationFee = acService.getCancellationFee(element.getResource(), element.getBeginDate(), orders);
		
		String modalTitle;
		if (cancellationFee == null) {
			leaveConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
					translate("leave.cancel.text", StringHelper.escapeHtml(element.getDisplayName())),
					null,
					translate("leave.cancel"),
					ButtonType.regular, translate("leave.cancel.cancel"), true);
			modalTitle = translate("leave.cancel");
		} else {
			leaveConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
					translate("leave.cancel.fee.text", StringHelper.escapeHtml(element.getDisplayName()), PriceFormat.fullFormat(cancellationFee)),
					translate("leave.cancel.fee.confirmation", PriceFormat.fullFormat(cancellationFee)),
					translate("leave.cancel.fee"),
					ButtonType.regular, translate("leave.cancel.cancel"), true);
			modalTitle = translate("leave.cancel.fee");
		}
		listenTo(leaveConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				leaveConfirmationCtrl.getInitialComponent(), true, modalTitle, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doLeave(UserRequest ureq) {
		if (!canLeave()) {
			return;
		}
		
		List<Order> orders = acService.findOrders(bookedIdentity, element.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		if (orders.isEmpty()) {
			curriculumService.removeMember(element, bookedIdentity, CurriculumRoles.participant, GroupMembershipStatus.removed, getIdentity(), null);
		} else {
			for (Order order : orders) {
				Map<Long, Price> cancellationFees = new HashMap<>(1);
				Price cancellationFee = acService.getCancellationFee(element.getResource(), element.getBeginDate(), List.of(order));
				if (cancellationFee != null) {
					cancellationFees.put(bookedIdentity.getKey(), cancellationFee);
				}
				MailerResult result = new MailerResult();
				MailTemplate template = CurriculumMailing.getMembershipCancelledByParticipantTemplate(
						element.getCurriculum(), element, cancellationFees, getIdentity());
				MailPackage mailing = new MailPackage(template, result, (MailContext) null, template != null);
				acService.cancelOrder(order, getIdentity(), null, mailing);
				curriculumService.removeMemberReservation(element, bookedIdentity, CurriculumRoles.participant, GroupMembershipStatus.removed, getIdentity(), null);
			}
		}
		
		fireEvent(ureq, new LeavingEvent(element));
	}
	
}
