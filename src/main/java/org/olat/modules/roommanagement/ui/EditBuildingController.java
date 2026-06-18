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

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 1 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditBuildingController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(EditBuildingController.class);

	private TextElement referenceEl;
	private TextElement descriptionEl;
	private ColorPickerElement colorEl;
	private FormToggle orgRestrictionToggle;
	private ObjectSelectionElement adminAccessEl;
	private FormLayoutContainer addressMapCombo;
	private TextElement addressEl;
	private TextElement infoUrlEl;
	private TextAreaElement additionalInfoEl;
	private FormLink findOnMapLink;

	private Building building;
	private final List<Organisation> currentOrgs;
	private BigDecimal geoLatitude;
	private BigDecimal geoLongitude;

	@Autowired
	private RoomManagementService roomManagementService;
	@Autowired
	private ColorService colorService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private HttpClientService httpClientService;

	public EditBuildingController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		building = null;
		currentOrgs = List.of();
		initForm(ureq);
	}

	public EditBuildingController(UserRequest ureq, WindowControl wControl,
			Building building, List<Organisation> organisations) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.building = building;
		this.currentOrgs = organisations != null ? organisations : List.of();
		geoLatitude = building.getGeoLatitude();
		geoLongitude = building.getGeoLongitude();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		referenceEl = uifactory.addTextElement("building.reference", "building.col.reference", 255,
				building != null ? building.getExternalRef() : null, formLayout);
		referenceEl.setMandatory(true);

		descriptionEl = uifactory.addTextElement("building.description", "building.col.description", 1000,
				building != null ? building.getDescription() : null, formLayout);

		List<ColorPickerElement.Color> colors = ColorUIFactory.createColors(colorService.getColors(), getLocale());
		colorEl = uifactory.addColorPickerElement("building.color", "building.col.color", formLayout, colors);
		colorEl.setElementCssClass("o_building_color_picker");
		if (building != null && StringHelper.containsNonWhitespace(building.getColorCss())) {
			colorEl.setColor(toColorId(building.getColorCss()));
		} else {
			colorEl.setColor(colorService.getDefaultColor());
		}

		boolean orgRestricted = !currentOrgs.isEmpty();
		orgRestrictionToggle = uifactory.addToggleButton("building.org.restriction",
				"building.org.restriction", translate("on"), translate("off"), formLayout);
		orgRestrictionToggle.toggle(orgRestricted);
		orgRestrictionToggle.addActionListener(FormEvent.ONCHANGE);

		if (organisationModule.isEnabled()) {
			Roles roles = ureq.getUserSession().getRoles();
			OrganisationSelectionSource source = new OrganisationSelectionSource(
					currentOrgs,
					() -> organisationService.getOrganisations(getIdentity(), roles, OrganisationRoles.administrator));
			adminAccessEl = uifactory.addObjectSelectionElement("building.admin.access",
					"building.admin.access", formLayout, getWindowControl(), true, source);
			adminAccessEl.setMandatory(true);
			adminAccessEl.setVisible(orgRestricted);
		}

		FormLayoutContainer locationCont = FormLayoutContainer.createVerticalFormLayout("location", getTranslator());
		locationCont.setFormTitle(translate("building.location"));
		formLayout.add(locationCont);

		String leafletCssUri = StaticMediaDispatcher.getStaticURI("js/leaflet/leaflet.css");
		JSAndCSSComponent leafletLoader = new JSAndCSSComponent("leafletLoader",
				new String[] { "js/leaflet/leaflet.min.js" },
				new String[] { leafletCssUri });

		addressMapCombo = FormLayoutContainer.createCustomFormLayout("addressMapCombo",
				getTranslator(), velocity_root + "/address_map_combo.html");
		locationCont.add(addressMapCombo);
		addressMapCombo.put("leafletLoader", leafletLoader);

		addressEl = uifactory.addTextElement("building.address", "building.col.address", 1000,
				building != null ? building.getAddress() : null, addressMapCombo);
		addressEl.setMandatory(true);

		findOnMapLink = uifactory.addFormLink("building.find.on.map", addressMapCombo, Link.BUTTON);
		findOnMapLink.setIconLeftCSS("o_icon o_icon_search");

		updateMapContext();

		FormLayoutContainer infoCont = FormLayoutContainer.createVerticalFormLayout("information", getTranslator());
		infoCont.setFormTitle(translate("building.information"));
		formLayout.add(infoCont);

		infoUrlEl = uifactory.addTextElement("building.info.url", "building.col.info.url", 2000,
				building != null ? building.getInfoUrl() : null, infoCont);

		additionalInfoEl = uifactory.addTextAreaElement("building.additional.info",
				"building.col.additional.info", -1, 6, 60, true, false,
				building != null ? building.getInfo() : null, infoCont);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton(building == null ? "create" : "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void updateMapContext() {
		if (geoLatitude != null && geoLongitude != null) {
			addressMapCombo.contextPut("geoLat", geoLatitude);
			addressMapCombo.contextPut("geoLon", geoLongitude);
		} else {
			addressMapCombo.contextRemove("geoLat");
			addressMapCombo.contextRemove("geoLon");
		}
		ColorPickerElement.Color color = colorEl != null ? colorEl.getColor() : null;
		addressMapCombo.contextPut("colorCss", color != null ? color.cssClass() : "");
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		referenceEl.clearError();
		if (!StringHelper.containsNonWhitespace(referenceEl.getValue())) {
			referenceEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		addressEl.clearError();
		if (!StringHelper.containsNonWhitespace(addressEl.getValue())) {
			addressEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		if (adminAccessEl != null && adminAccessEl.isVisible()) {
			adminAccessEl.clearError();
			if (adminAccessEl.getSelectedKeys().isEmpty()) {
				adminAccessEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == orgRestrictionToggle) {
			updateAdminAccessVisibility();
		} else if (source == findOnMapLink) {
			doFindOnMap();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateAdminAccessVisibility() {
		if (adminAccessEl != null) {
			adminAccessEl.setVisible(orgRestrictionToggle.isOn());
		}
	}

	private void doFindOnMap() {
		String address = addressEl.getValue();
		if (!StringHelper.containsNonWhitespace(address)) {
			addressEl.setErrorKey("form.legende.mandatory");
			return;
		}
		addressEl.clearError();

		try {
			String encoded = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
			String url = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";

			HttpGet get = new HttpGet(url);
			get.setHeader("User-Agent", "OpenOlat/RoomManagement (https://www.openolat.org)");
			get.setHeader("Accept-Language", getLocale().getLanguage());

			try (CloseableHttpClient client = httpClientService.createThreadSafeHttpClient(true);
				 CloseableHttpResponse response = client.execute(get)) {

				int status = response.getStatusLine().getStatusCode();
				String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

				if (status == 200) {
					JSONArray results = new JSONArray(body);
					if (results.length() > 0) {
						JSONObject first = results.getJSONObject(0);
						geoLatitude  = new BigDecimal(first.getString("lat"));
						geoLongitude = new BigDecimal(first.getString("lon"));
						updateMapContext();
					} else {
						showWarning("building.geocoding.not.found");
					}
				} else {
					log.warn("Nominatim returned HTTP {} for address: {}", status, address);
					showWarning("building.geocoding.error");
				}
			}
		} catch (Exception e) {
			log.error("Geocoding failed for address: {}", address, e);
			showWarning("building.geocoding.error");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (building == null) {
			building = roomManagementService.createBuilding(descriptionEl.getValue(), getIdentity());
		}

		building.setExternalRef(referenceEl.getValue());
		building.setDescription(descriptionEl.getValue());
		building.setAddress(addressEl.getValue());
		building.setInfoUrl(infoUrlEl.getValue());
		building.setInfo(additionalInfoEl.getValue());
		building.setGeoLatitude(geoLatitude);
		building.setGeoLongitude(geoLongitude);

		ColorPickerElement.Color color = colorEl.getColor();
		building.setColorCss(color != null ? color.cssClass() : null);

		List<Organisation> orgs = List.of();
		if (adminAccessEl != null && adminAccessEl.isVisible() && !adminAccessEl.getSelectedKeys().isEmpty()) {
			orgs = organisationService.getOrganisation(
					OrganisationSelectionSource.toRefs(adminAccessEl.getSelectedKeys()));
		}

		roomManagementService.updateBuilding(building, orgs, getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private static String toColorId(String colorCss) {
		return colorCss != null && colorCss.startsWith("o_color_")
				? colorCss.substring("o_color_".length()) : colorCss;
	}
}
