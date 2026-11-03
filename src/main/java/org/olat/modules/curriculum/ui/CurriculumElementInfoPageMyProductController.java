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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.factsheet.FactSheet;
import org.olat.core.gui.components.factsheet.FactSheetFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 3 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfoPageMyProductController extends BasicController {
	
	private static final String CMD_CANCEL = "cancel";
	
	private Link cancelLink;
	private CloseableModalController cmc;
	private ConfirmationController cancelConfirmationCtrl;
	
	private final CurriculumElement element;
	private final Identity bookedIdentity;
	
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementInfoPageMyProductController(UserRequest ureq, WindowControl wControl,
			DetailsHeaderConfig config, CurriculumElement element) {
		super(ureq, wControl, Util.createPackageTranslator(CurriculumElementInfoPageMyProductController.class, ureq.getLocale(),
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale())));
		this.element = element;
		this.bookedIdentity = config.getBookedIdentity();
		
		if (!config.isCancelAvailable()) {
			putInitialPanel(new Panel("empty"));
			return;
		}
		
		FactSheet factSheet = FactSheetFactory.createFactSheet("factSheet", null);
		factSheet.setTitle(translate("details.my.educational.product"));
		putInitialPanel(factSheet);
		
		String cancelText = config.isCancelWithFee() ? translate("leave.cancel.fee") : translate("leave.cancel");
		cancelLink = LinkFactory.createCustomLink(CMD_CANCEL, CMD_CANCEL, cancelText, Link.BUTTON | Link.NONTRANSLATED, null, this);
		cancelLink.setElementCssClass("o_cancel btn-danger " + FactSheet.CSS_FOOTER_LINK_FULL_WIDTH);
		cancelLink.setIconLeftCSS("o_icon o_icon_sign_out");
		cancelLink.setGhost(true);
		cancelLink.setEnabled(config.isCancelEnabled());
		factSheet.setFooterLinks(List.of(cancelLink));
	}
	
	public boolean hasContent() {
		return cancelLink != null;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == cancelLink) {
			doConfirmCancel(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (cancelConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doCancel(ureq);
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
		removeAsListenerAndDispose(cancelConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		cancelConfirmationCtrl = null;
		cmc = null;
	}
	
	private void doConfirmCancel(UserRequest ureq) {
		List<Order> orders = acService.findOrders(bookedIdentity, element.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		Price cancellationFee = acService.getCancellationFee(element.getResource(), element.getBeginDate(), orders);
		
		String modalTitle;
		if (cancellationFee == null) {
			cancelConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
					translate("leave.cancel.text", StringHelper.escapeHtml(element.getDisplayName())),
					null,
					translate("leave.cancel"),
					ButtonType.regular, translate("leave.cancel.cancel"), true);
			modalTitle = translate("leave.cancel");
		} else {
			cancelConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
					translate("leave.cancel.fee.text", StringHelper.escapeHtml(element.getDisplayName()), PriceFormat.fullFormat(cancellationFee)),
					translate("leave.cancel.fee.confirmation", PriceFormat.fullFormat(cancellationFee)),
					translate("leave.cancel.fee"),
					ButtonType.regular, translate("leave.cancel.cancel"), true);
			modalTitle = translate("leave.cancel.fee");
		}
		listenTo(cancelConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				cancelConfirmationCtrl.getInitialComponent(), true, modalTitle, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCancel(UserRequest ureq) {
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
