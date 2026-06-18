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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BuildingDetailsController extends FormBasicController {

	private FormLink editLink;
	private FormLink infoUrlLink;
	private FormLink appleMapsLink;
	private FormLink googleMapsLink;
	private final Building building;

	@Autowired
	private RoomManagementService roomManagementService;

	public BuildingDetailsController(UserRequest ureq, WindowControl wControl, Building building, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "building_details_view", rootForm);
		this.building = building;
		initForm(ureq);
	}

	public Building getBuilding() {
		return building;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (StringHelper.containsNonWhitespace(building.getColorCss())) {
			formLayout.contextPut("colorCss", building.getColorCss());
		}
		if (StringHelper.containsNonWhitespace(building.getExternalRef())) {
			formLayout.contextPut("reference", building.getExternalRef());
		}
		if (StringHelper.containsNonWhitespace(building.getDescription())) {
			formLayout.contextPut("description", building.getDescription());
		}
		formLayout.contextPut("statusName", building.getStatus().name());
		formLayout.contextPut("statusLabel", translate("building.status." + building.getStatus().name()));

		if (building.getStatus() != RoomStatus.deleted) {
			editLink = uifactory.addFormLink("building.detail.edit", formLayout, Link.BUTTON);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		}

		if (StringHelper.containsNonWhitespace(building.getAddress())) {
			formLayout.contextPut("address", building.getAddress());
		}

		if (StringHelper.containsNonWhitespace(building.getInfoUrl())) {
			infoUrlLink = uifactory.addFormLink("detail.info.url", "detail.info.url", "building.information",
					null, formLayout, Link.LINK);
			infoUrlLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
			infoUrlLink.setUrl(building.getInfoUrl());
			infoUrlLink.setNewWindow(true, true, false);
		}

		List<Organisation> orgs = roomManagementService.getOrganisations(building);
		if (!orgs.isEmpty()) {
			formLayout.contextPut("organisations", orgs.stream()
					.map(Organisation::getDisplayName)
					.collect(Collectors.joining(", ")));
		}

		SearchRoomParameters activeParams = new SearchRoomParameters();
		activeParams.setBuilding(building);
		activeParams.setStatus(List.of(RoomStatus.active));
		int activeCount = (int) roomManagementService.countRooms(activeParams);

		SearchRoomParameters inactiveParams = new SearchRoomParameters();
		inactiveParams.setBuilding(building);
		inactiveParams.setStatus(List.of(RoomStatus.inactive));
		int inactiveCount = (int) roomManagementService.countRooms(inactiveParams);

		formLayout.contextPut("hasRooms", activeCount + inactiveCount > 0);
		formLayout.contextPut("roomCount", String.valueOf(activeCount + inactiveCount));
		formLayout.contextPut("activeRoomCount", String.valueOf(activeCount));
		formLayout.contextPut("inactiveRoomCount", String.valueOf(inactiveCount));

		if (StringHelper.containsNonWhitespace(building.getInfo())) {
			formLayout.contextPut("additionalInfo", StringHelper.xssScan(building.getInfo()));
		}

		FormLayoutContainer mapCont = FormLayoutContainer.createCustomFormLayout(
				"buildingDetailMap", getTranslator(), velocity_root + "/building_detail_map.html");
		formLayout.add(mapCont);

		if (building.getGeoLatitude() != null && building.getGeoLongitude() != null) {
			if (StringHelper.containsNonWhitespace(building.getColorCss())) {
				mapCont.contextPut("colorCss", building.getColorCss());
			}
			mapCont.contextPut("geoLat", building.getGeoLatitude());
			mapCont.contextPut("geoLon", building.getGeoLongitude());
			String leafletCssUri = StaticMediaDispatcher.getStaticURI("js/leaflet/leaflet.css");
			JSAndCSSComponent leafletLoader = new JSAndCSSComponent("leafletLoader",
					new String[] { "js/leaflet/leaflet.min.js" },
					new String[] { leafletCssUri });
			mapCont.put("leafletLoader", leafletLoader);

			if (StringHelper.containsNonWhitespace(building.getAddress())) {
				String query = URLEncoder.encode(building.getAddress(), StandardCharsets.UTF_8);
				String appleMapsUrl = "https://maps.apple.com/?q=" + query;
				String googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=" + query;

				appleMapsLink = uifactory.addFormLink("detail.apple.maps", "detail.apple.maps",
						"building.apple.maps", null, mapCont, Link.BUTTON);
				appleMapsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
				appleMapsLink.setUrl(appleMapsUrl);
				appleMapsLink.setNewWindow(true, true, false);

				googleMapsLink = uifactory.addFormLink("detail.google.maps", "detail.google.maps",
						"building.google.maps", null, mapCont, Link.BUTTON);
				googleMapsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
				googleMapsLink.setUrl(googleMapsUrl);
				googleMapsLink.setNewWindow(true, true, false);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
