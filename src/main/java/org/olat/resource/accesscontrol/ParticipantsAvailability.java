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

import org.olat.core.gui.translator.Translator;

public enum ParticipantsAvailability { 
	fullyBooked, fewLeft, manyLeft;
	
	public static final record ParticipantsAvailabilityNum(ParticipantsAvailability availability, long numAvailable) {}
	
	public static final String getText(Translator translator, ParticipantsAvailabilityNum availabilityNum) {
		if (availabilityNum == null) return null;
		
		return switch (availabilityNum.availability) {
		case fullyBooked -> translator.translate("book.fully.booked.unfortunately");
		case fewLeft -> availabilityNum.numAvailable == 1
					? translator.translate("book.participants.left.single")
					: translator.translate("book.participants.left.multi", String.valueOf(availabilityNum.numAvailable));
		case manyLeft -> null;
		default -> null;
		};
	}
	
	public static final String getIconCss(ParticipantsAvailabilityNum availabilityNum) {
		if (availabilityNum == null) return null;
		
		return getIconCss(availabilityNum.availability);
	}
	
	public static final String getIconCss(ParticipantsAvailability availability) {
		if (availability == null) return null;
		
		return switch (availability) {
		case fullyBooked -> "o_ac_offer_fully_booked_icon";
		case fewLeft -> "o_ac_offer_almost_fully_booked_icon";
		case manyLeft -> null;
		default -> null;
		};
	}

}