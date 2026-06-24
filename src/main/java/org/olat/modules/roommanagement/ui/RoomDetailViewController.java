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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;

/**
 * Initial date: 24 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomDetailViewController extends FormBasicController {

	private final Room room;

	public RoomDetailViewController(UserRequest ureq, WindowControl wControl, Room room) {
		super(ureq, wControl, "room_detail_view");
		this.room = room;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Building building = room.getBuilding();

		if (building != null && StringHelper.containsNonWhitespace(building.getColorCss())) {
			formLayout.contextPut("colorCss", building.getColorCss());
		}

		String reference = room.getExternalRef();
		if (StringHelper.containsNonWhitespace(reference)) {
			formLayout.contextPut("reference", reference);
		}
		String description = room.getDescription();
		if (StringHelper.containsNonWhitespace(description) && !description.equals(reference)) {
			formLayout.contextPut("description", description);
		}

		if (building != null) {
			if (StringHelper.containsNonWhitespace(building.getExternalRef())) {
				formLayout.contextPut("buildingRef", building.getExternalRef());
			}
			String buildingDesc = building.getDescription();
			if (StringHelper.containsNonWhitespace(buildingDesc) && !buildingDesc.equals(building.getExternalRef())) {
				formLayout.contextPut("buildingDesc", buildingDesc);
			}
			if (StringHelper.containsNonWhitespace(building.getAddress())) {
				formLayout.contextPut("address", building.getAddress());
			}
			if (StringHelper.containsNonWhitespace(building.getInfoUrl())) {
				formLayout.contextPut("infoUrl", building.getInfoUrl());
				formLayout.contextPut("infoUrlLabel", translate("building.information"));
			}
		}

		if (StringHelper.containsNonWhitespace(room.getRoomInfo())) {
			formLayout.contextPut("roomInfo", StringHelper.xssScan(room.getRoomInfo()));
		}
		if (building != null && StringHelper.containsNonWhitespace(building.getInfo())) {
			formLayout.contextPut("buildingInfo", StringHelper.xssScan(building.getInfo()));
		}

		if (building != null && building.getGeoLatitude() != null && building.getGeoLongitude() != null) {
			FormLayoutContainer mapCont = FormLayoutContainer.createCustomFormLayout(
					"roomDetailMap", getTranslator(), velocity_root + "/building_detail_map.html");
			formLayout.add(mapCont);

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

			if (StringHelper.containsNonWhitespace(building.getAddress())) {
				String query = URLEncoder.encode(building.getAddress(), StandardCharsets.UTF_8);
				mapCont.contextPut("appleMapsUrl", "https://maps.apple.com/?q=" + query);
				mapCont.contextPut("googleMapsUrl", "https://maps.google.com/?q=" + query);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
