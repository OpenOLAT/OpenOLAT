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
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.ui.list.AbstractDetailsHeaderController;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
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

	@Autowired
	private CurriculumService curriculumService;
	
	
	public CurriculumElementInfosHeaderController(UserRequest ureq, WindowControl wControl,
			DetailsHeaderConfig config, CurriculumElement element) {
		super(ureq, wControl, config);
		this.element = element;
		this.bookedIdentity = config.getBookedIdentity();
		
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
	protected String getStartLinkText() {
		return translate("open.with.type", element.getType().getDisplayName());
	}

	@Override
	protected boolean tryAutoBooking(UserRequest ureq) {
		AccessResult acResult = acService.isAccessible(element, bookedIdentity, null, false, null, false);
		return acService.tryAutoBooking(bookedIdentity, element, acResult);
	}

	@Override
	protected OLATResource getResource() {
		return element.getResource();
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
