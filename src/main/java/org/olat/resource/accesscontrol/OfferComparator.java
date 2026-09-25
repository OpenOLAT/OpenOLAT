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
package org.olat.resource.accesscontrol;

import java.util.Comparator;

import org.olat.core.util.StringHelper;

/**
 * Sorts offers by label (blank labels last), then by creation date.
 * 
 * Initial date: 25 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferComparator implements Comparator<Offer> {
	
	private static final Comparator<Offer> COMPARATOR = Comparator
			.comparing((Offer offer) -> StringHelper.containsNonWhitespace(offer.getLabel()) ? offer.getLabel() : null,
					Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
			.thenComparing(Offer::getCreationDate, Comparator.nullsLast(Comparator.naturalOrder()));
	
	@Override
	public int compare(Offer o1, Offer o2) {
		return COMPARATOR.compare(o1, o2);
	}

}
