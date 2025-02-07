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
package org.olat.resource.accesscontrol.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Feb 6, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferDetailsController extends BasicController {
	
	@Autowired
	private AccessControlModule acModule;

	public OfferDetailsController(UserRequest ureq, WindowControl wControl, OfferAccess offerAccess) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("offer_details");
		putInitialPanel(mainVC);
		
		Offer offer = offerAccess.getOffer();
		
		Price price = offer.getPrice();
		if (price != null && !price.isEmpty()) {
			String priceStr = "<span class=\"o_ac_offer_price_ammount\">" + PriceFormat.fullFormat(price) + "</span>";
			if(acModule.isVatEnabled()) {
				BigDecimal vat = acModule.getVat();
				String vatStr = vat == null ? "" : vat.setScale(3, RoundingMode.HALF_EVEN).toPlainString();
				priceStr = translate("access.info.price.vat", priceStr, vatStr);
			} else {
				priceStr = translate("access.info.price.noVat", priceStr);
			}
			mainVC.contextPut("price", priceStr);
		}
		
		Price cancellingFee = offer.getCancellingFee();
		if (cancellingFee != null && !cancellingFee.isEmpty()) {
			String cancellation = offer.getCancellingFeeDeadlineDays() != null
					? translate("access.info.cancelling.with.deadline", String.valueOf(offer.getCancellingFeeDeadlineDays()))
					: translate("access.info.cancelling.without.deadline");
			mainVC.contextPut("cancellation", cancellation);
		}
		
		if (offer.getValidTo() != null) {
			String availableUntil = translate("access.info.available.until", Formatter.getInstance(getLocale()).formatDate(offer.getValidTo()));
			mainVC.contextPut("end", availableUntil);
		}
		
		String description = offer.getDescription();
		if(StringHelper.containsNonWhitespace(description)) {
			if(!StringHelper.isHtml(description)) {
				description = Formatter.escWithBR(description).toString();
			}
			description = StringHelper.xssScan(description);
			mainVC.contextPut("description", description);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
