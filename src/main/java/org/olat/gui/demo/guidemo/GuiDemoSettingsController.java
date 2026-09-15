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
package org.olat.gui.demo.guidemo;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * Blueprint for a settings page: a module toggle that hides everything else
 * when off, followed by two {@link FormSection}s ("Configuration" and
 * "Default settings"). Follows the pattern shown in OO-9756's
 * OO-9756_CPL_Admin_ON.png / OO-9756_CPL_Admin_Off.png mockups.
 *
 * The module is a fictitious "Astronomy" module so the planet names already
 * used by other GUI demos (see {@code select.1}..{@code select.8} in this
 * package's i18n bundle) could be reused here too.
 *
 * Initial date: 2026-09-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoSettingsController extends FormBasicController {

	private static final String[] planetKeys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

	private FormToggle moduleEnabledEl;
	private FormSection configurationCont;
	private FormSection defaultSettingsCont;
	private FormLayoutContainer buttonsCont;

	public GuiDemoSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CoreSpringFactory.class, getLocale(), getTranslator()));
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("module.settings");
		setFormContextHelp("manual_user/general/");

		moduleEnabledEl = uifactory.addToggleButton("guidemo.settings.module", "guidemo.settings.module",
				translate("on"), translate("off"), formLayout);
		moduleEnabledEl.addActionListener(FormEvent.ONCHANGE);
		moduleEnabledEl.toggle(true);

		addConfigurationSection(formLayout, ureq);
		addDefaultSettingsSection(formLayout);

		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	private void addConfigurationSection(FormItemContainer formLayout, UserRequest ureq) {
		configurationCont = uifactory.addFormSection("configuration", translate("configuration"), formLayout, FormSection.Level.SUB_TITLE);

		SelectionValues planetsPK = new SelectionValues();
		for (int i = 1; i <= 8; i++) {
			planetsPK.add(SelectionValues.entry(String.valueOf(i), translate("select." + i)));
		}
		planetsPK.add(SelectionValues.entry("9", translate("select.alt.1")));
		MultipleSelectionElement enabledPlanetsEl = uifactory.addCheckboxesVertical("guidemo.settings.enabled.planets",
				"guidemo.settings.enabled.planets", configurationCont, planetsPK.keys(), planetsPK.values(), 1);
		enabledPlanetsEl.select("3", true);
		enabledPlanetsEl.select("9", true);
		enabledPlanetsEl.setExampleKey("guidemo.settings.enabled.planets.example", null);

		SingleSelection homePlanetEl = uifactory.addDropdownSingleselect("guidemo.settings.home.planet", configurationCont,
				planetKeys, translatedPlanetValues());
		homePlanetEl.select("3", true);

		TextElement telescopeNameEl = uifactory.addTextElement("guidemo.settings.telescope.name", "guidemo.settings.telescope.name",
				100, translate("guidemo.settings.telescope.name.default"), configurationCont);
		telescopeNameEl.setExampleKey("guidemo.settings.telescope.name.example", null);

		String[] distanceUnitKeys = new String[] { "lightyears", "parsecs", "furlongs" };
		String[] distanceUnitValues = new String[] {
				translate("guidemo.settings.distance.unit.lightyears"),
				translate("guidemo.settings.distance.unit.parsecs"),
				translate("guidemo.settings.distance.unit.furlongs") };
		SingleSelection distanceUnitEl = uifactory.addRadiosVertical("guidemo.settings.distance.unit", "guidemo.settings.distance.unit",
				configurationCont, distanceUnitKeys, distanceUnitValues);
		distanceUnitEl.select("lightyears", true);

		FormToggle meteorAlertsEl = uifactory.addToggleButton("guidemo.settings.meteor.alerts", "guidemo.settings.meteor.alerts",
				translate("on"), translate("off"), configurationCont);
		meteorAlertsEl.setHelpText(translate("guidemo.settings.meteor.alerts.help"));
	}

	private void addDefaultSettingsSection(FormItemContainer formLayout) {
		defaultSettingsCont = uifactory.addFormSection("defaultSettings", translate("default.settings"), formLayout, FormSection.Level.SUB_TITLE);

		String[] defaultViewKeys = new String[] { "telescope", "starmap" };
		String[] defaultViewValues = new String[] {
				translate("guidemo.settings.default.view.telescope"),
				translate("guidemo.settings.default.view.starmap") };
		SingleSelection defaultViewEl = uifactory.addRadiosVertical("guidemo.settings.default.view", "guidemo.settings.default.view",
				defaultSettingsCont, defaultViewKeys, defaultViewValues);
		defaultViewEl.select("telescope", true);

		uifactory.addSpacerElement("defaultSettingsSpacer", defaultSettingsCont, false);

		SelectionValues infoPagePK = new SelectionValues();
		infoPagePK.add(SelectionValues.entry("rings", translate("guidemo.settings.info.page.rings")));
		infoPagePK.add(SelectionValues.entry("moons", translate("guidemo.settings.info.page.moons")));
		infoPagePK.add(SelectionValues.entry("habitability", translate("guidemo.settings.info.page.habitability")));
		MultipleSelectionElement infoPageEl = uifactory.addCheckboxesVertical("guidemo.settings.info.page",
				"guidemo.settings.info.page", defaultSettingsCont, infoPagePK.keys(), infoPagePK.values(), 1);
		infoPageEl.select("rings", true);
		infoPageEl.select("moons", true);
	}

	private String[] translatedPlanetValues() {
		String[] values = new String[planetKeys.length];
		for (int i = 0; i < 8; i++) {
			values[i] = translate("select." + (i + 1));
		}
		values[8] = translate("select.alt.1");
		return values;
	}

	private void updateUI() {
		boolean enabled = moduleEnabledEl.isOn();
		configurationCont.setVisible(enabled);
		defaultSettingsCont.setVisible(enabled);
		buttonsCont.setVisible(enabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == moduleEnabledEl) {
			updateUI();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		showInfo("guidemo.settings.saved");
	}

}
