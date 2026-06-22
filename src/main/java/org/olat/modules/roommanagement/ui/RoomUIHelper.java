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
package org.olat.modules.roommanagement.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;

/**
 * Initial date: 19 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
class RoomUIHelper {

	static String forgeRoomCard(FormItemContainer formLayout, Room room, String velocityRoot, Translator translator) {
		String cardId = "roomCard_" + room.getKey();
		FormLayoutContainer cardCont = FormLayoutContainer.createCustomFormLayout(
				cardId, translator, velocityRoot + "/room_card.html");
		formLayout.add(cardCont);

		if (StringHelper.containsNonWhitespace(room.getExternalRef())) {
			cardCont.contextPut("reference", room.getExternalRef());
		}
		if (StringHelper.containsNonWhitespace(room.getDescription())) {
			cardCont.contextPut("description", room.getDescription());
		}

		Building building = room.getBuilding();
		if (building != null) {
			if (StringHelper.containsNonWhitespace(building.getExternalRef())) {
				cardCont.contextPut("buildingRef", building.getExternalRef());
			}
			String buildingDesc = building.getDescription();
			if (StringHelper.containsNonWhitespace(buildingDesc) && !buildingDesc.equals(building.getExternalRef())) {
				cardCont.contextPut("buildingDesc", buildingDesc);
			}
			if (StringHelper.containsNonWhitespace(building.getAddress())) {
				cardCont.contextPut("address", building.getAddress());
			}
			if (StringHelper.containsNonWhitespace(building.getColorCss())) {
				cardCont.contextPut("colorCss", building.getColorCss());
			}
			if (StringHelper.containsNonWhitespace(building.getInfoUrl())) {
				cardCont.contextPut("infoUrl", building.getInfoUrl());
			}

			if (building.getGeoLatitude() != null && building.getGeoLongitude() != null) {
				String mapId = "roomCardMap_" + room.getKey();
				FormLayoutContainer mapCont = FormLayoutContainer.createCustomFormLayout(
						mapId, translator, velocityRoot + "/building_detail_map.html");
				cardCont.add(mapCont);
				cardCont.contextPut("roomCardMapId", mapId);

				mapCont.contextPut("geoLat", building.getGeoLatitude());
				mapCont.contextPut("geoLon", building.getGeoLongitude());
				if (StringHelper.containsNonWhitespace(building.getColorCss())) {
					mapCont.contextPut("colorCss", building.getColorCss());
				}
				String leafletCssUri = StaticMediaDispatcher.getStaticURI("js/leaflet/leaflet.css");
				JSAndCSSComponent leafletLoader = new JSAndCSSComponent("leafletLoader",
						new String[]{"js/leaflet/leaflet.min.js"},
						new String[]{leafletCssUri});
				mapCont.put("leafletLoader", leafletLoader);
			}
		}

		return cardId;
	}

	static String formatNextEvent(RoomBooking booking, Locale locale) {
		Date startDate = booking.getStartDate();
		Date endDate = booking.getEndDate();

		String dayOfWeek = new SimpleDateFormat("EEE", locale).format(startDate);
		String date = DateFormat.getDateInstance(DateFormat.SHORT, locale).format(startDate);
		String time = DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(startDate);

		long durationMinutes = (endDate.getTime() - startDate.getTime()) / 60000L;
		String duration = formatDuration(durationMinutes);

		return dayOfWeek + " " + date + ", " + time + " " + duration;
	}

	static String formatDuration(long minutes) {
		long hours = minutes / 60;
		long mins = minutes % 60;
		if (hours > 0 && mins > 0) {
			return hours + "h " + mins + "m";
		} else if (hours > 0) {
			return hours + "h";
		} else {
			return mins + "m";
		}
	}
}
