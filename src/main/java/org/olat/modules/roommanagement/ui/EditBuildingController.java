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

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 1 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditBuildingController extends FormBasicController {

	private TextElement referenceEl;
	private TextElement descriptionEl;
	private ColorPickerElement colorEl;
	private FormToggle orgRestrictionToggle;
	private ObjectSelectionElement adminAccessEl;
	private TextElement addressEl;
	private TextElement infoUrlEl;
	private TextAreaElement additionalInfoEl;

	private Building building;
	private final List<Organisation> currentOrgs;

	@Autowired
	private RoomManagementService roomManagementService;
	@Autowired
	private ColorService colorService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

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
		if (building != null && StringHelper.containsNonWhitespace(building.getColorCss())) {
			colorEl.setColor(toColorId(building.getColorCss()));
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

		FormLayoutContainer addressMapCombo = FormLayoutContainer.createCustomFormLayout("addressMapCombo", 
				getTranslator(), this.velocity_root + "/address_map_combo.html");
		locationCont.add(addressMapCombo);
		
		addressEl = uifactory.addTextElement("building.address", "building.col.address", 1000,
				building != null ? building.getAddress() : null, addressMapCombo);
		addressEl.setMandatory(true);

		uifactory.addFormLink("building.find.on.map", addressMapCombo, Link.BUTTON).setIconLeftCSS("o_icon o_icon_search");

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
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateAdminAccessVisibility() {
		if (adminAccessEl != null) {
			adminAccessEl.setVisible(orgRestrictionToggle.isOn());
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
