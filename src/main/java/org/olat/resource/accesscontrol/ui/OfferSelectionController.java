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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 13, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSelectionController extends FormBasicController {

	private SingleSelection offersEl;
	
	private final List<OfferAccess> offers;
	
	@Autowired
	private AccessControlModule acModule;

	public OfferSelectionController(UserRequest ureq, WindowControl wControl, List<OfferAccess> offers) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.offers = offers;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues offersKV = new SelectionValues();
		for (OfferAccess offer : offers) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(offer.getMethod().getType());
			String name = handler.getMethodName(getLocale());
			String iconCSS = "o_icon " + offer.getMethod().getMethodCssClass() + "_icon";
			String description = createDescription(offer);
			offersKV.add(new SelectionValue(offer.getKey().toString(), name, description, iconCSS, null, true));
		}
		offersEl = uifactory.addCardSingleSelectHorizontal("offers", "offers", null, formLayout, offersKV, true, null);
		offersEl.setElementCssClass("o_radios_invisible");
		offersEl.select(offersEl.getKey(0), true);
		offersEl.addActionListener(FormEvent.ONCLICK);
	}

	private String createDescription(OfferAccess offer) {
		String description = null;
		if (offer.getOffer().getValidTo() != null || (offer.getOffer().getPrice() != null && !offer.getOffer().getPrice().isEmpty())) {
			description = "<div class=\"o_radio_desc_center\">";
			if (offer.getOffer().getValidTo() != null) {
				description += "<div>" + translate("offers.selection.valid.until", Formatter.getInstance(getLocale()).formatDate(offer.getOffer().getValidTo())) + "</div>";
			}
			if (offer.getOffer().getPrice() != null && !offer.getOffer().getPrice().isEmpty()) {
				description += "<div><strong>" +  PriceFormat.fullFormat(offer.getOffer().getPrice()) + "</strong></div>";
			}
			description += "</div>";
		}
		return description;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == offersEl) {
			doSelectOffer(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSelectOffer(UserRequest ureq) {
		if (offersEl.isOneSelected()) {
			String selectedKey = offersEl.getSelectedKey();
			offers.stream()
				.filter(offer -> selectedKey.equals(offer.getKey().toString()))
				.findFirst()
				.ifPresent(offer -> fireEvent(ureq, new OfferSelectedEvent(offer)));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static final class OfferSelectedEvent extends Event {
		
		private static final long serialVersionUID = -5555438802460215826L;
		
		private final OfferAccess offer;
		
		public OfferSelectedEvent(OfferAccess offer) {
			super("offer-selected");
			this.offer = offer;
		}
		
		public OfferAccess getOffer() {
			return offer;
		}
		
	}

}
