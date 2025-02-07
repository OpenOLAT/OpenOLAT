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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.token.TokenAccessHandler;
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
		offers.sort(new OffersComparator());
		
		// typeCounters contains for every type with more than one offer a counter
		Map<String, AtomicInteger> typeCounters = offers.stream()
				.map(offer -> offer.getMethod().getType())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet()
				.stream()
				.filter(entrySet -> entrySet.getValue() > 1)
				.collect(Collectors.toMap(Entry::getKey, entrySet -> new AtomicInteger()));
		
		SelectionValues offersKV = new SelectionValues();
		for (OfferAccess offer : offers) {
			String name = getName(offer, typeCounters);
			String description = getDescription(offer);
			offersKV.add(new SelectionValue(offer.getKey().toString(), name, description, null, null, true));
		}
		offersEl = uifactory.addCardSingleSelectHorizontal("offers", "offers", null, formLayout, offersKV, true, null);
		offersEl.setElementCssClass("o_radios_invisible");
		offersEl.select(offersEl.getKey(0), true);
		offersEl.addActionListener(FormEvent.ONCLICK);
	}

	private String getName(OfferAccess offer, Map<String, AtomicInteger> typeCounters) {
		AccessMethodHandler handler = acModule.getAccessMethodHandler(offer.getMethod().getType());
		String name = handler.getMethodName(getLocale());
		
		AtomicInteger typeCounter = typeCounters.get(offer.getMethod().getType());
		if (typeCounter != null) {
			int number = typeCounter.incrementAndGet();
			name += " " + number;
		}
		
		return name;
	}

	private String getDescription(OfferAccess offer) {
		Price price = offer.getOffer().getPrice();
		if (price != null && !price.isEmpty()) {
			return PriceFormat.fullFormat(price);
		}
		return null;
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
	
	/*
	 * Sort order:
	 *	1. "No price" offers
	 *		1.1. Without booking (not possible here)
	 *		1.2. Free available
	 *		1.3. Access code
	 *	2. With price
	 *		Ascending by price
	 */
	private static final class OffersComparator implements Comparator<OfferAccess> {
		
		@Override
		public int compare(OfferAccess o1, OfferAccess o2) {
			int c = Integer.compare(getMethodSortValue(o1), getMethodSortValue(o2));
			
			if (c == 0) {
				BigDecimal priceAmount1 = o1.getOffer().getPrice() != null? o1.getOffer().getPrice().getAmount(): null;
				BigDecimal priceAmount2 = o2.getOffer().getPrice() != null? o2.getOffer().getPrice().getAmount(): null;
				if (priceAmount1 == null) {
					if (priceAmount2 == null) {
						c = 0;
					} else {
						c = -1;
					}
				} else if (priceAmount2 == null) {
					c = 1;
				} else {
					c = priceAmount1.compareTo(priceAmount2);
				}
			}
			
			return c;
		}
		
		private int getMethodSortValue(OfferAccess offer) {
			return switch (offer.getMethod().getType()) {
			case FreeAccessHandler.METHOD_TYPE -> 1;
			case TokenAccessHandler.METHOD_TYPE -> 2;
			default -> 10; // price methods
			};
			
		}
		
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
